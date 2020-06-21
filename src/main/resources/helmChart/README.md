# 使用说明

> **注**：此压缩包中的文件是用于 Helm Chart 中导入的离线发布包，也包含了 Chart 中所需的离线镜像包。

## 包结构说明

本压缩包的文件结构说明：

- `xxx`
  - `images.tgz`: 可导入 Docker 的离线镜像包
  - `xxx-chart.tgz`: 可导入的离线 Chart 包 
- `config`: 存放 `application.yml` 等配置文件的目录，以供参考
- `docs`: 存放文档的目录
- `README.md`: 主入口说明文件
