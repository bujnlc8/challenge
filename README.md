# 百科知识大挑战

- Deploy

```
    make deploy tag=0.0.1
```

每日挑战上线前须先执行新增表结构：

```bash
mysql --default-character-set=utf8mb4 -h HOST -P PORT -u USER -p challenge < sql/daily-challenge.sql
```

然后部署并重启后端。该脚本可重复执行，不会为现有用户自动增加订阅次数。
接口、一次订阅语义、08:00 调度与验证说明见 [每日挑战与题目海报](docs/daily-challenge.md)。
