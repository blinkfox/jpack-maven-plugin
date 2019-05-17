# jpack-maven-plugin

[![HitCount](http://hits.dwyl.io/blinkfox/jpack-maven-plugin.svg)](https://github.com/blinkfox/jpack-maven-plugin) [![Build Status](https://secure.travis-ci.org/blinkfox/jpack-maven-plugin.svg)](https://travis-ci.org/blinkfox/jpack-maven-plugin) [![GitHub license](https://img.shields.io/github/license/blinkfox/jpack-maven-plugin.svg)](https://github.com/blinkfox/jpack-maven-plugin/blob/master/LICENSE) [![codecov](https://codecov.io/gh/blinkfox/jpack-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/blinkfox/jpack-maven-plugin) ![Java Version](https://img.shields.io/badge/Java-%3E%3D%208-blue.svg) [![Maven Central](https://img.shields.io/maven-central/v/com.blinkfox/jpack-maven-plugin.svg)](https://search.maven.org/artifact/com.blinkfox/jpack-maven-plugin/1.0.0/maven-plugin) [![Javadocs](https://img.shields.io/badge/javadoc-1.0.0-brightgreen.svg)](https://www.javadoc.io/doc/com.blinkfox/jpack-maven-plugin/1.0.0)

> 这是一个用于对 SpringBoot 服务打包为 Windows、Linux 和 Docker 下可发布部署包的 Maven 插件。

## 特性

- 简单易用
- 支持打包为 `Windows`、 `Linux` 和 `Docker` 下的发布部署包，也可单独选择打某些平台下的部署包
- `Windows`部署包可以安装为服务，从 `Windows` 的服务界面中来启动和停止应用服务，且默认为开机自启动
- 支持 `Docker` 的镜像构建、导出镜像 `tar` 包、推送到远程镜像等功能
- jpack 内部默认提供了一个简单通用的 `Dockerfile` 来构建 SpringBoot 服务的镜像，也支持自定义 `Dockerfile` 来构建镜像
- 可自定义复制文件资源到部署包中，例如通常发布时需要的：数据库脚本、文档说明等
- 可自定义排除不需要的文件资源被打包到部署包中，例如默认生成的文件目录资源你可以选择性排除掉

## 集成本插件

我将以一个新建的 SpringBoot 2.1.5.RELEASE 的 web 项目 `web-demo`，版本 `1.0.0` 作为示例，来做介绍。

### 最简示例

首先，在项目的 Maven `pom.xml` 的 `build -> plugins` 节点中加入 `jpack-maven-plugin` 的插件配置：

```xml
<build>
    <plugins>
        <!-- 引入 jpack-maven-plugin 插件 -->
        <plugin>
            <groupId>com.blinkfox</groupId>
            <artifactId>jpack-maven-plugin</artifactId>
            <version>1.1.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

然后，执行如下 jpack 命令：

```bash
mvn clean package jpack:build
```

> **注**：jpack 须要有 Maven 打包的 jar 包，才能来生成用于发布的各平台的部署包，所以至少须要执行 package 阶段的命令。

然后，执行成功之后，你将在 Maven 控制台看到如下输出：

```bash
[INFO] --- jpack-maven-plugin:1.1.0-SNAPSHOT:build (default-cli) @ web-demo ---
[INFO] -------------------------- jpack start packing... -------------------------
                             __                          __
                            |__|______  _____     ____  |  | __
                            |  |\____ \ \__  \  _/ ___\ |  |/ /
                            |  ||  |_> > / __ \_\  \___ |    <
                        /\__|  ||   __/ (____  / \___  >|__|_ \
                        \______||__|         \/      \/      \/

[INFO] 制作 windows 下的部署压缩包完成.
[INFO] 制作 linux 下的部署压缩包完成.
[INFO] 将使用 jpack 默认提供的 Dockerfile 文件来构建 Docker 镜像.
[INFO] 正在构建 com.blinkfox/web-demo:1.0.0 镜像...
[INFO] 构建 com.blinkfox/web-demo:1.0.0 镜像完毕，镜像ID: c8f91718f286
[INFO] ------------- jpack has been packaged to end. [costs: 8.49 s] -------------
```

成功之后就可以在 `target` 目录中看到 `jpack` 的文件夹，`jpack` 文件夹中就包含了对应各个平台下的可部署服务包, Docker 下的包除外，因为 Docker 构建不做额外配置的话，默认只是构建镜像，想要可导入的离线镜像部署包，可以参看下面的介绍。

- `web-demo-1.0.0.zip`: Windows 下的服务部署包
- `web-demo-1.0.0.tar.gz`: Linux 下的服务部署包
- `web-demo-1.0.0-docker.tar.gz`: Docker 下的可离线导入的部署包（此时应该没有这个包）

> **注**：不做额外配置的话，Docker 平台下默认只是构建 docker 镜像，不会导出和生成压缩包。且如果你的执行环境中没有安装 Docker、Docker 没有开启或者 Docker 无根执行权限，将默认跳过 Docker 的构建环节。

构建成功之后，可以在 docker 中键入 `docker images` 命令，将看到构建的 docker 镜像：

```bash
REPOSITORY                     TAG                 IMAGE ID            CREATED             SIZE
com.blinkfox/web-demo          1.0.0               c8f91718f286        12 minutes ago      122MB
openjdk                        8-jdk-alpine        a3562aa0b991        6 days ago          105MB
...
```

### 另一种简单配置

你也可以在 jpack 的插件中配置执行的阶段，就不用再手动输入 `jpack:build` 的命令了，配置如下：

```xml
<build>
    <plugins>
        <!-- 引入 jpack-maven-plugin 插件 -->
        <plugin>
            <groupId>com.blinkfox</groupId>
            <artifactId>jpack-maven-plugin</artifactId>
            <version>1.1.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>build</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

然后只需输入 `mvn package` 命令，jpack 就可以构建执行了。

## 各平台包结构介绍

待续...

压缩包中的结构大体如下：

- `bin`: 可执行的文件目录
  - `install.bat`: 仅 `.zip` 包中有此安装为 `windows` 服务的可执行文件
  - `uninstall.bat`: 仅 `.zip` 包中有此卸载 `windows` 服务的可执行文件
  - `start.sh (.bat)`: 启动服务的可执行文件
  - `stop.sh (.bat)`: 停止服务的脚本
  - `restart.sh (.bat)`: 重启服务的脚本
- `docs`: 文档目录, 默认是空的, 可自己填充内容
- `logs`: 日志文件目录, 默认是空的, 建议应用服务的日志生成到此目录中
- `xxx-yyy.jar`: SpringBoot 打的 `jar` 包
- `README.md`: 说明文件, 内嵌了部分默认的内容, 可覆盖

### 最全示例及说明

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <!-- 构建的目标是 build, 默认是在打包阶段执行. -->
        <execution>
            <goals>
                <goal>build</goal>
            </goals>
            <phase>package</phase>
        </execution>
    </executions>
    <configuration>
        <!-- JVM 运行所需的参数选项. -->
        <vmOptions>-Xms512m -Xmx1024m</vmOptions>
        <!-- 所集成的 Java 服务程序运行所需的参数. -->
        <programArgs>--server.port=9090</programArgs>
        <!-- 打包哪些平台的包，不填写则代表所有平台. 目前支持 Windows 和 Linux 两种(大小写均可). -->
        <platforms>
            <param>Windows</param>
            <param>Linux</param>
        </platforms>
        <!-- 需要copy 哪些资源(可以是目录或者具体的相对、绝对或网络资源路径)到部署包中的某个目录;
            to 的值只能是目录，为空或者 '.' 或者 '/' 符号则表示复制到各平台包的根目录中，否则就复制到具体的目录下 -->
        <copyResources>
            <!-- 复制本项目根目录的 README.md 到各平台包的根目录中. -->
            <copyResource>
                <from>README.md</from>
                <to>.</to>
            </copyResource>
            <!-- 复制本项目 docs 文件夾到各平台包的 docs 目录中. -->
            <copyResource>
                <from>docs</from>
                <to>docs</to>
            </copyResource>
            <!-- 复制本项目上层目录的 hello.pdf 文件到各平台包的 abc/def 目录中. -->
            <copyResource>
                <from>../test-dir/hello.pdf</from>
                <to>abc/def</to>
            </copyResource>
            <!-- 复制网络资源 http://xxx.com/image.png 文件到各平台包的根目录中. -->
            <copyResource>
                <from>http://xxx.com/image.png</from>
                <to>/</to>
            </copyResource>
        </copyResources>
        <!-- 排除哪些文件或目录不需要打包进压缩包中，输入文件的相对路径即可. -->
        <excludeFiles>
            <param>logs</param>
            <param>README.md</param>
            <param>xxxxx.abc</param>
        </excludeFiles>
    </configuration>
</plugin>
```

## 许可证

本 [jpack-maven-plugin](https://github.com/blinkfox/jpack-maven-plugin) Maven 插件 遵守 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) 许可证。