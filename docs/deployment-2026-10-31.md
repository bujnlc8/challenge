# 2026-10-31 roselle 部署记录

- 域名：`https://challenge.haihui.site`。
- 容器：`challenge-api`，单副本，自动重启策略 `unless-stopped`。
- 镜像：`registry.cn-beijing.aliyuncs.com/roselle-1/challenge:restore-20261031-b3287c2`。
- 仓库 digest：`sha256:0bc2305a2ad7473f818ddb9241c48a0a2de7cd4537e195862edb3668818e7148`。
- 应用源提交：`b3287c2d4579d438c1e11b73d685337fe17b1b66`。
- 镜像在本地由干净提交构建，校验归档后，使用 roselle 已有仓库登录态上传阿里云。
- 运行配置：`deploy` profile，host 网络，只监听 `127.0.0.1:8180`。
- MySQL：复用现有 `mysql` 容器及 gsc 数据库账号，新建 `challenge` 数据库。
- Redis：复用 `draw_redis_1`，使用独立 DB 13。
- 环境文件：`/home/roselle/challenge/shared/app.env`，权限 600，不进入镜像或仓库。
- 发布目录：`/home/roselle/challenge/releases/restore-20261031-b3287c2`。
- Nginx：`/etc/nginx/conf.d/challenge.haihui.site.conf`，复用既有通配符证书。

数据库共 8 张表、18,281 道启用题目。1,543 个 COS 图片或视频地址已逐一验证可读，继续复用原地址。
39 条无法完整解析的原始记录、12 组答案冲突保存在题库报告中，没有猜测答案入库。
46 条异常题经过模型语义整理，并通过原文哈希绑定来源。
用户、历史成绩、金币、反馈和语录没有备份来源，未伪造这些数据。

验证完成：19 项 Python 恢复测试；干净源码 Maven 打包；数据库逐行哈希回读一致；重复导入不新增；
公网 HTTPS 的分类、语录和周榜、总榜接口返回 HTTP 200、业务码 10000；HTTP 跳转 HTTPS。
微信登录和小程序真机答题仍需真实登录 code 验收。

发布目录保留 `release.json`、`database-validation.json`、`deployment.json`、题库 SQL、JSONL 和审计报告。
原工作区中已有的 `pom.xml`、`WebSocketConfig.java` 和 `.gitignore` 暂存修改保留，未混入发布镜像。

后续发布可执行 `deploy/run-container.sh IMAGE`；脚本先拉取镜像，停止旧容器后才启动新副本，
旧容器保留为停止状态的 `challenge-api-previous`。新容器未通过启动检查时自动恢复上一容器。
回滚容器不回滚数据库，数据库操作应单独备份和核对。
