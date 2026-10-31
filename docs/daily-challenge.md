# 每日挑战与题目海报

## 数据库与运行

现有数据库先运行 `sql/daily-challenge.sql`，再部署新后端。新建数据库可直接使用已包含这些表的 `sql/schema.sql`。脚本仅新增四张 InnoDB 表，不修改现有积分、复活、奖励或答题数据。

- `daily_challenge`：以中国日期为唯一键，保存当天公共题号。首次选择取上架题目按 ID 排序，以日期序号取模；写入后固定，新增题目不会改变当天题号。返回中没有正确答案或解析。当已固定题目后来被下架或删除，当天题号仍保留，`today` 返回不可用业务错误，调度器跳过发送且不预留订阅机会，不会另选一道题。
- `daily_reminder_preference`：持久化用户是否启用提醒；启用本身不产生订阅次数。
- `daily_reminder_credit`：每次客户端真正收到模板 `accept` 后报告的一个待使用机会。`uid + request_id` 唯一，重试相同请求不重复计数。
- `daily_reminder_delivery`：`uid + challenge_date` 主键，跨实例只允许一天一个发送尝试。

后端沿用现有微信 AppId、AppSecret 和 Redis access token 缓存。客户端没有密钥。新增可选 Spring 配置 `daily-challenge.env-version`，默认 `release`，允许 `release`、`trial`、`develop`；提醒跳转分别映射微信要求的 `formal`、`trial`、`developer`。

调度器在 `Asia/Shanghai` 的每天 `08:00:00` 执行，不依赖宿主机时区。需保持后端在该时刻运行；当前不会在停机恢复后补发，以免在不确定发送结果后重复提醒。应用和新表准备好后，未来调度才生效；本次实现没有对真实微信用户发送消息。

## HTTP 接口

服务已有 `/api` context path。除首页通用小程序码接口外，以下接口需要现有 `Authorization: Bearer ...` 登录凭据，POST 使用表单参数，统一返回现有 `{code,message,data}` 包装。

| 方法与地址 | 参数 | data |
| --- | --- | --- |
| GET `/api/daily-challenge/today` | 无 | `{date,question_id,title,start_time,page}` |
| GET `/api/daily-challenge/reminder` | 无 | `{enabled,credits,template_id,next_send_time}` |
| POST `/api/daily-challenge/reminder` | `enabled=true` 或 `false` | 同提醒状态 |
| POST `/api/daily-challenge/reminder/accept` | `request_id`、`template_id`、`result=accept` | 同提醒状态 |
| POST `/api/common/share-code` | `env_version=release|trial|develop`（默认 release），无需登录 | `{page:"pages/index/index",env_version,mime_type,image_base64}` |
| POST `/api/question/share-code` | `question_id`，`env_version=release|trial|develop`（默认 release） | `{question_id,env_version,mime_type,image_base64}` |

提醒、订阅回执、微信小程序码接口仅支持微信小程序账号。`today` 的 `page` 与消息跳转均为 `pages/answer/answer?question_id=ID`，进入题目后继续调用原有 `question/start-challenge` 及校验答案流程。不存在可用题目时返回业务错误，不能静默挑选下架题目。原 `question/start-challenge` 对显式正数 `question_id` 会先直接核查数据库；若题目被删除或下架，返回“题目不存在或已下架”，不会沿用旧缓存或退回随机题目。`question_id=0` 的随机挑战、积分和复活规则保持原流程。

`request_id` 使用每次 `wx.requestSubscribeMessage` 调用前新生成的 UUID；同一次微信授权的 HTTP 重试必须复用该值，长度 8–64，仅字母、数字、下划线或短横线。仅模板返回值严格等于 `accept` 时调用回执接口，不能从历史启用开关、`getSetting` 或勾选“总是保持以上选择”推断出无限次数。新回执会启用提醒；旧回执重放不会覆盖用户后来关闭提醒的选择。

`credits` 是本地尚未预留的客户端接受回执数，微信并不提供该客户端回执的服务端签名；服务端无法独立证明客户端声明属实。真实一次订阅额度以微信发送 API 为准，微信拒绝未订阅或已用完额度的发送。此接口不承诺永久或每天都能收到消息。

## 发送结果与去重

领取时在数据库事务中锁定用户提醒行、选择一个 AVAILABLE 机会、插入当天唯一发送记录，再将机会标记为 RESERVED。事务提交后才调用微信，不跨外部 HTTP 请求持有数据库锁。

- `errcode=0`：发送记录为 SENT，机会为 CONSUMED。
- `errcode=43101`：该次订阅关系/额度不可用，记录为 FAILED，机会为 INVALID。
- 微信明确返回其他非零错误码：记录为 FAILED，机会恢复 AVAILABLE，可留到以后日期；同日不重试。
- 获取 token 失败、尚未发起发送请求：记录为 FAILED，机会恢复 AVAILABLE，同日不重试。
- 发送请求的网络超时、空返回、没有错误码：结果可能已经送达，记录为 UNKNOWN，机会保持 RESERVED；同日和后续调度都不会重试该记录。
- 进程在发送前后退出或数据库回写失败：SENDING 和预留机会保持不动，不自动重发。需要人工核查时不能仅依据超时就把机会恢复为可用。

模板固定为 `faTHcOm8TebcnbsK92I5DyJ-bJxo7XQfR5wiwJdh24A`，`thing1` 为“每日挑战”，`time7` 为当天 `yyyy-MM-dd 08:00:00`。提醒仅选启用、有待使用机会、有效微信 openid 的用户。

## 海报码

使用微信 `getwxacodeunlimit` 获取真实图片，scene 是十进制题号，page 固定 `pages/answer/answer`。前端在 `onLoad` 解析数字 scene。正式版检查已发布页面，体验/开发版允许尚未发布页面。每次请求先校验环境枚举及上架题目，微信 JSON 错误体不会被伪装成图片；PNG/JPEG 图片以 base64 返回供本地画布合成。

首页通用码仅固定 scene=`home`、page=`pages/index/index`，不接受用户、题目或任意路径参数，也不返回任何密钥。关于页未登录用户可使用。服务内存只保存三个允许环境的槽位；成功图片缓存 24 小时，失败缓存 60 秒，生成过程串行合并并发请求，避免匿名请求持续击穿微信接口。重启后清空缓存，每实例最多三个槽位。该接口不会发送订阅消息。

## 验证

```bash
mvn -s settings.xml test
python3 -m unittest discover -s scripts -p 'test_*.py'
git diff --check
```

测试用 H2 的 MySQL 模式执行新增表结构及真实 SQL，覆盖固定题目、时区和 08:00 门槛、订阅开关/回执幂等、并发领取、一次额度消费、失败恢复、未知结果不重发以及微信码请求参数。微信 HTTP 由 MockRestServiceServer/网关 mock 拦截，不接入真实账号。H2 不能代替部署环境真实 MySQL 和微信开发者工具/真机验收。
