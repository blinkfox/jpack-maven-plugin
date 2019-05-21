# 使用说明

> **注**：此压缩包中的文件是用于 Docker 中导入的离线发布包。

## 包结构说明

本压缩包的文件结构说明：

- `config`: 存放 `application.yml` 等配置文件的目录
- `docs`: 存放文档的目录
- `xxx-yyy.tar`: 可导入 Docker 的离线镜像包
- `Dockerfile`: 用于构建镜像的源 `Dockerfile` 文件（可以再自定义修改，用于二次构建）
- `xxx-yyy.jar`: 用于构建镜像的源 `jar` 包（可以用于二次构建）
- `README.md`: 主入口说明文件

## 注意事项

- 使用 `docker load < xxx-yyy.tar` 命令来将本离线镜像包导入到 Docker 中；
- `Dockerfile` 和 `xxx-yyy.jar` 让开发者或部署人员，可以根据自己的需要再做自定义修改，构建出符合自己需要的新镜像；

## 命令参考

以下命令是用来启动镜像的，仅供参考：

### 简单的方式 

```bash
docker run -d -p 8080:8080 xxx:yyy
```

### 带参数的方式

```bash
docker run -d -p 8080:8080 -e JVM_OPTS="-Xms1024m -Xmx2048m" -e PROGRAM_ARGS="--server.port=7070" xxx:yyy
```
