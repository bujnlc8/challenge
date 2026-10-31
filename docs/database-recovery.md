# 数据库恢复与 Docker 部署

`sql/schema.sql` 根据 Java 实体、Mapper 和服务写入逻辑重建 8 张表：
`user`、`question`、`round`、`round_detail`、`wrong_question_book`、`coin_record`、`feedback`、`quotes`。
旧 `sql/question.sql` 缺少 `analysis` 字段；新结构还补全了轮次计分等实际依赖的默认值。
历史用户、成绩、金币、反馈和语录没有原始数据，保持为空。数据库结构是兼容重建，并非原库逐字备份。

## 从题库重新生成

```bash
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 scripts/restore_database.py
```

生成到 `target/database-recovery/`：

- `schema.sql`：建表文件，不删除已有表或数据。
- `questions.sql`：题库 SQL，临时表批量导入，重复执行跳过相同题目。
- `questions.jsonl`：结构化数据，选项为数组，答案从 1 开始，保留题目来源。
- `report.json`：各来源、分类、重复、答案冲突、模型语义修复和拒绝记录。
- `assets/`：仓库保留的国旗、地图素材备份。

2026-10-31 的恢复结果：18,281 道，16,738 道启用，1,543 道图片题暂存为停用；
39 条原始记录无法安全解析，12 组相同题目存在答案冲突。异常原文保留在报告中。
并未逐题核实题库本身的知识正确性、法律时效或拼写错误。

`scripts/recovered_questions.json` 记录了模型逐条整理的 46 条异常题和 46 条素材名称映射。
每条题目修复与原文 SHA-256 绑定，修复换行、粘连选项、材料表格和明确答案文本对应的截断内容。
不以模型推测填补缺失答案。HTML 表格转成分行文本，解析中的图片保留 URL 引用。
小程序仅支持最多 4 个单选选项，超过这一限制或丢失必要图片的题留在异常报告中。

分类依据 `CommonController` 当前接口；诗词为 5、交通规则为 8、基金为 9。
混合常识来源中的财经、交通和世界杯题按明确关键词分流，这是恢复时的分类规则，不代表原库历史分类。
首都题根据原脚本常量生成，使用固定随机种子；不会在线生成新题。

## 导入 MySQL

Python 导入需要已安装 PyMySQL。无需安装依赖即可解析并导出文件。
密码通过 `MYSQL_PASSWORD` 环境变量传入，不写到脚本或命令参数中。

```bash
export MYSQL_HOST=127.0.0.1 MYSQL_PORT=33060 MYSQL_USER=root MYSQL_DATABASE=challenge
# 在当前 shell 安全设置 MYSQL_PASSWORD
python3 scripts/restore_database.py --apply
```

Python 导入会创建缺失的库和表，按题干及选项去重，已有题目的答案冲突时终止，事务回滚题目写入。
SQL 导入跳过内容、选项顺序和分类完全一致的记录，不修改已有记录；应串行执行。
`CREATE TABLE IF NOT EXISTS` 不会升级旧表；若导入对象是旧结构而非空库，应先备份并核对列。

```bash
mysql --default-character-set=utf8mb4 -h HOST -P PORT -u USER -p challenge < target/database-recovery/schema.sql
mysql --default-character-set=utf8mb4 -h HOST -P PORT -u USER -p challenge < target/database-recovery/questions.sql
```

图片保留原 COS 路径及小程序使用的 `###` 标记。确认图片可读后，可使用
`--enable-remote-images` 在空目标库生成启用图片的数据。该参数不更新已有记录的状态。
如迁移本地素材，可用 `--image-base-url https://HOST/PREFIX`，默认仍复用原 COS。

## 本地和 Docker 运行

`local` profile 使用环境中的 MySQL、Redis 连接信息，默认 MySQL 端口 33060。
为兼容旧排行榜 Mapper，仅在该连接池移除 `ONLY_FULL_GROUP_BY`，不修改 MySQL 全局配置。
`.env` 不会由 Spring 自动读取，需要在启动前将值导入环境；支付宝的旧变量名也兼容。

```bash
mvn --settings settings.xml -Dmaven.repo.local=target/maven-repository package
docker build -f Dockerfile.recovery --build-arg SOURCE_SHA=COMMIT -t registry.cn-beijing.aliyuncs.com/roselle-1/challenge:TAG .
docker push registry.cn-beijing.aliyuncs.com/roselle-1/challenge:TAG
```

发布镜像应从精确提交导出的干净源码构建，避免混入本地其他暂存修改。
`JAVA_IMAGE` 可传入已验证的 Java 8 镜像 digest。构建上下文仅包含 Dockerfile 和 JAR，不上传密钥或题库。

roselle 复用现有 MySQL 容器及 gsc 的数据库账号，在新库 `challenge` 内导入数据；
Redis 复用 `draw_redis_1`，选择独立空闲 DB（部署配置默认 13），不清理其他 DB。
连接密码只放在远端权限 600 的环境文件中。

```bash
docker pull registry.cn-beijing.aliyuncs.com/roselle-1/challenge:TAG
docker run -d --name challenge-api --restart unless-stopped --network host \
  --env-file /home/roselle/challenge/shared/app.env \
  registry.cn-beijing.aliyuncs.com/roselle-1/challenge:TAG
```

环境文件设置 `SPRING_PROFILES_ACTIVE=deploy`、MySQL/Redis 连接信息和小程序配置。
只运行一个 API 容器，绑定 `127.0.0.1:8180`；统一 Nginx 使用 `deploy/challenge.haihui.site.conf`
和现有通配符证书代理 HTTPS。修改 Nginx 后先 `nginx -t`，成功才 reload。

验证入口：`/api/common/category`、`/api/quotes/one`、`/api/question/ranking-list`。
微信登录和答题仍需要真实小程序登录 code；HTTP 冒烟检查不代表真机验收。
