# 使用说明

> **注**：此压缩包中的文件是用于 Linux 下部署的发布包。

## 包结构说明

本压缩包的文件结构说明：

- `bin`: 存放可执行脚本的目录
  - `start.sh`: 启动服务的 shell 脚本；
  - `stop.sh`: 停止服务的 shell 脚本；
  - `restart.sh`: 重启服务的 shell 脚本；
- `docs`: 存放文档的目录；
- `logs`: 存放日志的目录；
- `xxx-yyy.jar`: 可执行的 jar 文件；
- `README.md`: 主入口说明文件；

## 注意事项

- 各个可执行脚本请以 `sh` 命令来执行，如：`sh start.sh`。