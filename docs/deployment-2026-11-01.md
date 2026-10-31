# 2026-11-01 每日挑战与海报后端发布记录

发布于北京时间 01:53，域名 `https://challenge.haihui.site`，目标容器 `challenge-api`。

## 已发布边界

- 源分支：`agent/restore-challenge`，已非强制推送 GitHub。
- 应用源 SHA：`47af125b8ff05e196e465d36c500e5b754c61203`。
- 镜像标签：`registry.cn-beijing.aliyuncs.com/roselle-1/challenge:daily-20261101-47af125`。
- 运行镜像固定仓库 digest：`registry.cn-beijing.aliyuncs.com/roselle-1/challenge@sha256:7730b49556ecec823630061c2dec42d29c757895888a8117a213c2e3455f0148`。
- image ID：`sha256:2f0038bb3bdfe941ee876b7176139697b96891745747a7f7e631b6fb86d7544b`。
- Java8 基础镜像固定为 `docker.m.daocloud.io/library/eclipse-temurin@sha256:377fe8e36a4b1530d9d52020069367ef44313fa891a82ae102b1016a6a3a72f5`。
- 容器内 jar SHA256：`d31d70b93f20434b48ce42ca690c5c68ba7657164e09a0eb83475c25f978ef82`。
- 镜像归档 SHA256：`a6ac7e7be4644d987e9a456d3607407669b40609e3b6bf495ff0e1155a007f83`。

镜像由该提交的干净 `git archive` 独立测试、打包并通过本地 Lima Docker 构建，OCI revision 与容器内 jar 哈希逐一核对；传入 roselle 后重新校验源归档、镜像归档、迁移 SQL、image ID、revision 和 jar。使用 roselle 已有 registry 登录态推送。未发布默认分支或执行合并；仓库没有 CI 文件，依照既有手工发布流程执行，不声称存在成功 CI。此记录的后续文档提交不会改变应用镜像的上述源 SHA。

## 数据库与运行配置

发布目录：`/home/roselle/challenge/releases/daily-20261101-47af125`。

DDL 前使用 `mysqldump --single-transaction --quick --routines --triggers` 备份现有 challenge 数据库，备份为 `backup/challenge-before.sql.gz`，目录权限 700、备份权限 600，gzip 与 SHA 校验通过。备份 SHA256 为 `f3195badbe5cd4d1b2eddea98f1d3d652858f515ff4ad490a866a35866314d2a`。

应用 `sql/daily-challenge.sql`，重复执行验证幂等，确认新建四表：`daily_challenge`、`daily_reminder_preference`、`daily_reminder_credit`、`daily_reminder_delivery`。该迁移没有生成订阅次数或修改原业务数据。迁移前后及服务滚动后，现有汇总一致：用户 1、轮次 3、轮次分数合计 415、答题明细 18、金币记录 7、金币变动合计 12082、题目 18281。

继续复用 `deploy` profile、host 网络、`127.0.0.1:8180`、Redis DB 13 和原 `/home/roselle/challenge/shared/app.env`（仍为 600）。环境文件未输出、未进入镜像或仓库。

## 部署与验证

使用与上一发布完全相同的 `run-container.sh`，传入固定 digest。新实例通过启动健康检查后保留旧容器为停止状态的 `challenge-api-previous`。

- 干净提交 Maven 完整打包：37 项 Java 测试全部通过；现有数据库恢复 Python 测试 19 项通过；`git diff --check` 通过。
- 公网 HTTPS 分类、语录、周榜和总榜：HTTP 200、业务码 10000。
- 未登录访问 `/api/daily-challenge/today` 与 `/api/daily-challenge/reminder`：HTTP 401，认证保护生效。
- 未登录 POST `/api/common/share-code`、`env_version=release`：HTTP 200、业务码 10000，返回首页真实微信小程序码，JPEG 92926 字节；连续两次返回相同图片。图片 SHA256：`ca780626a0fc58cd579a1d0fc0d3361012b895b3c588cb3ba0baea58b97d6c11`。
- 未对真实用户发送订阅提醒，未构造登录凭据或授权次数。提醒真实投递和小程序真机体验仍按实际用户授权与 08:00 调度验收。

发布目录保留 `release.json`、`verification.json`、`source.tar`、`image.tar.gz`、迁移 SQL、原回滚脚本、registry push 日志、备份及汇总比较记录、真实首页码 JPEG。CI/PR 不适用本次既有手工发布路径。测试依赖原有 SLF4J 多绑定提示仍存在，不影响测试和启动。

## 回滚

上一版本：`registry.cn-beijing.aliyuncs.com/roselle-1/challenge:restore-20261031-b3287c2`，源 SHA `b3287c2d4579d438c1e11b73d685337fe17b1b66`，仓库 digest `sha256:0bc2305a2ad7473f818ddb9241c48a0a2de7cd4537e195862edb3668818e7148`。

可在 roselle 调用本发布保留的脚本恢复旧镜像：

```bash
bash /home/roselle/challenge/releases/daily-20261101-47af125/run-container.sh registry.cn-beijing.aliyuncs.com/roselle-1/challenge@sha256:0bc2305a2ad7473f818ddb9241c48a0a2de7cd4537e195862edb3668818e7148
```

应用回滚不删除新增表、不自动恢复数据库；原用户数据未被迁移改写。只有确有数据恢复需求时才单独评估受保护备份。
