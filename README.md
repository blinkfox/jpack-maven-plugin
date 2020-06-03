# jpack-maven-plugin

[![HitCount](http://hits.dwyl.io/blinkfox/jpack-maven-plugin.svg)](https://github.com/blinkfox/jpack-maven-plugin) [![Build Status](https://secure.travis-ci.org/blinkfox/jpack-maven-plugin.svg)](https://travis-ci.org/blinkfox/jpack-maven-plugin) [![GitHub license](https://img.shields.io/github/license/blinkfox/jpack-maven-plugin.svg)](https://github.com/blinkfox/jpack-maven-plugin/blob/master/LICENSE) [![codecov](https://codecov.io/gh/blinkfox/jpack-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/blinkfox/jpack-maven-plugin) ![Java Version](https://img.shields.io/badge/Java-%3E%3D%208-blue.svg) [![Maven Central](https://img.shields.io/maven-central/v/com.blinkfox/jpack-maven-plugin.svg)](https://search.maven.org/artifact/com.blinkfox/jpack-maven-plugin/1.4.0/maven-plugin) [![Javadocs](https://img.shields.io/badge/javadoc-1.4.0-brightgreen.svg)](https://www.javadoc.io/doc/com.blinkfox/jpack-maven-plugin/1.4.0)

> 这是一个用于对 SpringBoot 服务打包为 Windows、Linux 和 Docker 下可发布部署包的 Maven 插件。

## 特性

- 简单易用，基于**约定优于配置**的思想来构建部署包
- 支持打包为 `Windows`、 `Linux` 和 `Docker` 下的发布部署包，也可单独选择打某些平台下的部署包
- `Windows`部署包可以安装为服务，从 `Windows` 的服务界面中来启动和停止应用服务，且默认为开机自启动
- 支持 `Docker` 的镜像构建、导出镜像 `tar` 包和推送镜像到远程仓库或远程私有仓库等功能
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
            <version>1.4.0</version>
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
[INFO] --- jpack-maven-plugin:1.4.0:build (default-cli) @ web-demo ---
[INFO] ---------------------------------- jpack start packing... ---------------------------------
                                     __                          __
                                    |__|______  _____     ____  |  | __
                                    |  |\____ \ \__  \  _/ ___\ |  |/ /
                                    |  ||  |_> > / __ \_\  \___ |    <
                                /\__|  ||   __/ (____  / \___  >|__|_ \
                                \______||__|         \/      \/      \/ v1.4.0

[INFO] 【构建镜像 -> 默认】将使用 jpack 默认提供的 Dockerfile 文件来构建 Docker 镜像.
[INFO] 【构建镜像 -> 进行】正在构建 com.blinkfox/web-demo:1.0.0 镜像...
[INFO] 【构建打包 -> 成功】制作 windows 下的部署压缩包完成.
[INFO] 【构建打包 -> 成功】制作 linux 下的部署压缩包完成.
[INFO] 【构建镜像 -> 成功】构建 com.blinkfox/web-demo:1.0.0 镜像完毕，镜像ID: c8f91718f286
[INFO] --------------------- jpack has been packaged to end. [costs: 2.45 s] ---------------------
```

成功之后就可以在 `target` 目录中看到 `jpack` 的文件夹，`jpack` 文件夹中就包含了对应各个平台下的可部署服务包, Docker 下的包除外，因为 Docker 构建不做额外配置的话，默认只是构建镜像，想要可导入的离线镜像部署包，可以参看下面的介绍。

- `web-demo-1.0.0.zip`: Windows 下的服务部署包
- `web-demo-1.0.0.tar.gz`: Linux 下的服务部署包
- `web-demo-1.0.0-docker.tar.gz`: Docker 下的可离线导入的部署包（此时应该没有这个包）

> **注**：不做额外配置的话，Docker 平台下默认只是构建 docker 镜像，不会导出和生成压缩包。而且如果你的执行环境中没有安装 Docker、没有开启 Docker 或者 Docker 无根执行权限，将默认跳过 Docker 的构建环节。

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
            <version>1.4.0</version>
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

### Windows

生成的 Windows 下的 `web-demo-1.0.0.zip` 包，解压之后的文件结构说明：

- `bin`: 存放可执行脚本的目录
  - `install.bat`: 安装为 `windows` 服务的可执行脚本
  - `uninstall.bat`: 卸载本 `windows` 服务的可执行脚本
  - `start.bat`: 启动服务的可执行脚本
  - `stop.bat`: 停止服务的可执行脚本
  - `restart.bat`: 重启服务的可执行脚本
  - `status.bat`: 查看服务运行状态的可执行脚本
  - `web-demo-1.0.0.exe`: 可执行的二进制文件，可不用管
  - `web-demo-1.0.0.exe.config`: 也可不用管
  - `web-demo-1.0.0.xml`: 服务执行相关的配置文件，一般情况下不需要修改
- `config`: 存放 `application.yml` 等配置文件的目录（可自定义复制配置文件到此目录，方便部署时按需修改，SpringBoot 启动时会自动读取）
- `docs`: 存放文档的目录（可自定义复制文档到此目录，方便部署时查阅文档）
- `logs`: 存放日志的目录（建议 SpringBoot 的日志存放到 jar 包同级的 logs 目录中）
- `web-demo-1.0.0.jar`: 可执行的 jar 文件
- `README.md`: 主入口说明文件

**注意事项**：

- 6 个 `.bat` 可执行脚本，请以管理员的身份运行；
- 请先执行 `install.bat` 来安装为 `windows` 服务，安装服务只需要执行一次即可，以后就可以通过 `Windows` 服务界面来启动了，且默认是开机自启动；
- `bin` 目录下的文件不要移动，各文件的文件名无特殊情况也不要修改；
- 命令运行时，可能会提示安装 `.NET`, 安装完成就可运行命令了，不过现在大部分的 Windows 服务器或者个人电脑都会默认安装了 `.NET`, 没有的话启用一下就好了;

### Linux

生成的 Linux 下的 `web-demo-1.0.0.tar.gz` 包，解压之后的文件结构说明：

- `bin`: 存放可执行脚本的目录
  - `start.sh`: 启动服务的 shell 脚本
  - `stop.sh`: 停止服务的 shell 脚本
  - `restart.sh`: 重启服务的 shell 脚本
  - `status.sh`: 查看服务运行状态的 shell 脚本
- `config`: 存放 `application.yml` 等配置文件的目录（可自定义复制配置文件到此目录，方便部署时按需修改，SpringBoot 启动时会自动读取）
- `docs`: 存放文档的目录（可自定义复制文档到此目录，方便部署时查阅文档）
- `logs`: 存放日志的目录（建议 SpringBoot 的日志存放到 jar 包同级的 logs 目录中）
- `web-demo-1.0.0.jar`: 可执行的 jar 文件
- `README.md`: 主入口说明文件

**注意事项**：

- 各个可执行脚本请以 `bash` 命令来执行，如：`bash start.sh`，或者对 `bin` 目录添加可执行权限（`chmod -R 755 bin`），然后执行 `./start.sh` 即可。

### Docker

如果不做任何配置的话，默认只是构建最新的 Docker 镜像，而不会生成部署需要的 `.tar` 包和相关资源文件。你需要做下 docker 的额外导出 (`save`) 配置即可在执行 `mvn package` 之后生成 docker 的 `.tar.gz` 包，该包中包含了可导入Docker 的离线镜像 `.tar` 包和其他你配置的资源。

```xml
<build>
    <plugins>
        <!-- 引入 jpack-maven-plugin 插件 -->
        <plugin>
            <groupId>com.blinkfox</groupId>
            <artifactId>jpack-maven-plugin</artifactId>
            <version>1.4.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>build</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <docker>
                    <extraGoals>
                        <!-- 构建 docker 镜像之外的额外目标，可以填写 save 值.
                            save 表示导出镜像的离线包. -->
                        <param>save</param>
                    </extraGoals>
                </docker>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Maven 构建成功之后，就可以在 `jpack` 目录中看到一个 `web-demo-1.0.0-docker.tar.gz` 的压缩包，即为 docker 的离线导入相关的资源包。该包解压之后的文件结构说明：

- `config`: 存放 `application.yml` 等配置文件的目录（可自定义复制配置文件到此目录，生成该目录与 Windows 或 Linux 下的目的不同，主要是为了方便部署时查阅配置信息，了解系统环境和参数配置，辅助运维人员配置 Docker 的环境变量等，让配置有据可查。）
- `docs`: 存放文档的目录（可自定义复制文档到此目录，方便部署时查阅文档）
- `web-demo-1.0.0.tar`: 可导入 Docker 的离线镜像包，主要使用此包来导入到 Docker 中
- `Dockerfile`: 用于构建镜像的源 `Dockerfile` 文件（可以再自定义修改，用于二次构建）
- `web-demo-1.0.0.jar`: 用于构建镜像的源 `jar` 包（可以用于二次构建）
- `README.md`: 主入口说明文件

**注意事项**：

- 使用 `docker load < web-demo-1.0.0.tar` 命令来将本离线镜像包导入到 Docker 中；
- `Dockerfile` 和 `web-demo-1.0.0.jar` 让开发者或部署人员，可以根据自己的需要再做自定义修改，构建出符合自己需要的新镜像；

**命令参考**：

使用以下命令是用来启动镜像的，仅供参考：

**简单的方式**：

```bash
docker run -d -p 8080:8080 com.blinkfox/web-demo:1.0.0
```

稍等一会儿，访问 <http://127.0.0.1:8080> 即可访问服务。

**带参数的方式**：

```bash
docker run -d -p 7070:7070 -e JVM_OPTS="-Xms1024m -Xmx2048m" -e PROGRAM_ARGS="--server.port=7070" com.blinkfox/web-demo:1.0.0
```

稍等一会儿，访问 <http://127.0.0.1:7070> 即可访问服务。

## jpack 配置详解

## configuration 配置详解

jpack 的所有配置参数都非必填或者有默认值。以下是关于 jpack Maven 插件 `configuration` 下的配置信息介绍，供你参考。

### platforms

用于配置打包时可以打包哪些平台的包。默认为 Windows、Linux 和 Dokcer 三个平台，如果打包的环境没有或没开启 Docker，则会自动忽略 Docker 下的构建及后续的打包等操作。这三个平台的值大小写均可。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
        ...
    <configuration>
        <!-- 打包哪些平台的包，不填写则代表所有平台. 目前支持 Windows、Linux 和 Dokcer 三个平台（大小写均可）.
            这里作为示例，注释 Docker 的方式，就表明只打包 Windows 和 Linux 平台下的包。 -->
        <platforms>
            <param>Windows</param>
            <param>Linux</param>
            <!-- <param>Docker</param> -->
        </platforms>
    </configuration>
</plugin>
```

### vmOptions

用于配置 JVM 运行所需的参数选项，作用于各个平台下。默认为空，如果配置了对应的值，生成的部署包命令行中将会追加这些 JVM 选项参数。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
        ...
    <configuration>
        <!-- JVM 运行所需的选项参数. -->
        <vmOptions>-Xms1024m -Xmx2048m</vmOptions>
    </configuration>
</plugin>
```

### programArgs

用于配置 SpringBoot 运行所需的参数，作用于各个平台下。默认为空，如果配置了对应的值，生成的部署包命令行中将会追加这些程序运行参数。

> **注**：不太建议使用此配置项来使参数生效，这是命令行级别的生效，不便于运维人员修改，容易出错，建议使用 `configFiles` 配置文件的方式。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 所集成的 SpringBoot 服务程序运行所需的参数. -->
        <programArgs>--server.port=7070</programArgs>
    </configuration>
</plugin>
```

### configFiles

用来复制诸如 `application.yml` 等类似的配置文件到部署包的 `config` 目录中。针对所有平台生效。默认为空，如果配置了对应的值，生成的部署包的 `config` 目录中将会有你复制的配置文件。该值只能配置一个，可以是相对于 `pom.xml` 的相对路径或绝对路径的目录或文件，也可以是网络资源。

> **注**：强烈建议配置该值用来替代 `programArgs`，以便于在 Windows 或 Linux 下，直接让运维人员来修改配置文件的方式来使配置生效，而不是通过命令行参数的方式来修改配置。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 需要复制到部署包中 config 目录下的 yml 或者 .properties 文件的配置文件路径，
            它的值可以配置多个，可以是相对路径、绝对路径具体的目录或文件，也可以是网络资源. -->
        <configFiles>
            <param>src/test/resources/application.yml</param>
            <param>src/test/resources/application-local.yml</param>
        </configFiles>
    </configuration>
</plugin>
```

### skipError

遇到错误时是否跳过错误。默认是不填写或者 `default`，程序会自动处理，不需要你额外关注；`true` 的话，会忽略所有异常；`false` 的话，遇到错误就直接报错。

> **注**：目前仅 Docker 下有用到此配置。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 遇到错误时是否跳过错误，目前仅Docker 下有用到此配置。默认是不填写或者 default，程序会自动处理，不需要你额外关注；
            true的话，会忽略所有异常；false的话，遇到错误就直接报错。 -->
        <skipError>default</skipError>
    </configuration>
</plugin>
```

### cleanPackDir

表示执行打包命令是否清除之前生成的发布包数据，默认清除（`true`），可配置为 `false`。

### copyResources

系统发布时，发布包中必然会有很多除了 `xxx.jar` 之外的其他文件，如：数据库脚本、配置文件、文档手册等等，这些也需要集成到发布包中。为了统一管理和维护，所以通过 `copyResources` 配置项，可以复制你项目中几乎所有你想要的目录、文件或者网络资源等到发布包及发布包里的自定义的目录中。

`copyResources` 可以包含多个 `copyResource` 子节点配置项，`copyResource` 中有 `from` 和 `to` 两个参数。用于表示从哪里复制到哪里。

- `from`: 复制的源。可以是相对于 `pom.xml` 的相对路径或绝对路径的本地资源，也可以是网络资源文件路径。`from`如果是本地路径时可以是目录也可以是文件。
- `to`: 需要复制到哪里。不填写或者填写`.`、`/` 时表示的是复制到各平台包的根目录中。如果要填写就只能是目录，可以嵌套多级。如：`abc/def`，表示复制到`abc`目录下的 `def` 目录中，目录会自动创建。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 需要copy 哪些资源(from 的值可以是目录或者具体的相对、绝对或网络资源路径)到部署包中的某个目录;
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
            <!-- 复制本项目上层目录的 hello.pdf 文件到各平台包的 abc/def 目录中（会自动创建此目录）. -->
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
    </configuration>
</plugin>
```

### excludeFiles

也需生成的可部署包中，某些文件资源对你而已，显得有些多余，你想排除它们。此时你可以使用 `excludeFiles` 来配置需要排除的文件或目录不会打包的部署包中，可以配置多个。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 排除哪些文件或目录不需要打包进压缩包中，输入文件的相对路径即可. -->
        <excludeFiles>
            <param>logs</param>
            <param>README.md</param>
            <param>xxxxx.abc</param>
        </excludeFiles>
    </configuration>
</plugin>
```

### windows

这是用于专门对 Windows 平台生效的配置项，主要包括前面提到的如下几个子配置项，用法同前面类似，不再赘述：

- `vmOptions`: 针对 Windows 平台的 JVM 选项参数配置。如果你配置了这个值，那么在 Windows 下将会覆盖通用的 `vmOptions` 的值。
- `programArgs`: 针对 Windows 平台的程序参数配置。如果你配置了这个值，那么在 Windows 下将会覆盖通用的 `programArgs` 的值。
- `configFiles`: 针对 Windows 平台的配置文件配置。如果你配置了这个值，那么在 Windows 下将会覆盖通用的 `configFiles` 的值。
- `copyResources`: 针对 Windows 平台的资源复制。如果你配置了这个值，那么会额外复制这些资源到 Windows 平台的包中。
- `excludeFiles`: 针对 Windows 平台的资源排除。如果你配置了这个值，那么会额外从 Windows 平台的包中排除这些资源。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 以下是仅对 Windows 平台下生效的配置项。 -->
        <windows>
            <vmOptions>-Xms1024m -Xmx2048m</vmOptions>
            <programArgs>--server.port=7070</programArgs>
            <configFiles>
                <param>src/test/resources/application.yml</param>
                <param>src/test/resources/application-dev.yml</param>
            </configFiles>
            <copyResources>
                <copyResource>
                    <from>README.pdf</from>
                    <to>.</to>
                </copyResource>
            </copyResources>
            <excludeFiles>
                <param>logs</param>
            </excludeFiles>
        </windows>
    </configuration>
</plugin>
```

### linux

这是用于专门对 Linux 平台生效的配置项，主要包括前面提到的如下几个子配置项，用法同前面类似，不再赘述：

- `vmOptions`: 针对 Linux 平台的 JVM 选项参数配置。如果你配置了这个值，那么在 Linux 下将会覆盖通用的 `vmOptions` 的值。
- `programArgs`: 针对 Linux 平台的程序参数配置。如果你配置了这个值，那么在 Linux 下将会覆盖通用的 `programArgs` 的值。
- `configFiles`: 针对 Linux 平台的配置文件配置。如果你配置了这个值，那么在 Linux 下将会覆盖通用的 `configFiles` 的值。
- `copyResources`: 针对 Linux 平台的资源复制。如果你配置了这个值，那么会额外复制这些资源到 Linux 平台的包中。
- `excludeFiles`: 针对 Linux 平台的资源排除。如果你配置了这个值，那么会额外从 Linux 平台的包中排除这些资源。

示例同 Windows 下的类似。

### docker

这是用于专门对 Docker 平台生效的配置项，主要包括前面提到的如下几个子配置项及Docker 平台下特有的一些配置项，用法同前面类似，不再赘述：

- `dockerfile`: 构建 Docker 镜像的 `Dockerfile` 文件的相对路径，没有此配置项或者不填写，则使用 jpack 默认的 `Dockerfile` 文件.
- `registry`: 远程仓库地址，不填写则默认为 Dockerhub。
- `repo`: 构建镜像的仓库名，类似于 `groupId`，不填写则默认使用 `groupId`。
- `name`: 构建镜像的名称，类似于 `artifactId`，不填写则默认使用 `artifactId`。
- `tag`: 构建镜像的版本标签，不填写则默认使用 `version`。
- `fromImage`: Dockerfile 文件中 FROM 引用的基础镜像，如果没有配置该值，将默认使用 `openjdk:8-jdk-alpine`。
- `expose`: 对外暴露的端口，不配置该值，将不会在 Dockerfile 中生成 `EXPOSE` 指令。
- `volumes`: 挂载的数据卷，不填写则默认挂载 "/tmp" 和 "/logs" 目录。
- `customCommands`: 自定义的 Dockerfile 指令选项，可以填写多个值，每个值在 Dockerfile 中占一行，充当Dockerfile 中的一条指令。
- `extraGoals`: 额外的构建目标，默认只是构建镜像，如果你需要配置导出镜像包可以再填写 `save`，推送到 Dockerhub 可以再填写 `push`。
- `vmOptions`: 针对 Docker 平台的 JVM 选项参数配置。如果你配置了这个值，那么在 Docker 下将会覆盖通用的 `vmOptions` 的值。
- `programArgs`: 针对 Docker 平台的程序参数配置。如果你配置了这个值，那么在 Docker 下将会覆盖通用的 `programArgs` 的值。
- `configFiles`: 针对 Docker 平台的配置文件配置。如果你配置了这个值，那么在 Docker 下将会覆盖通用的 `configFiles` 的值。
- `copyResources`: 针对 Docker 平台的资源复制。如果你配置了这个值，那么会额外复制这些资源到 Docker 平台的包中。
- `excludeFiles`: 针对 Docker 平台的资源排除。如果你配置了这个值，那么会额外从 Docker 平台的包中排除这些资源。
- `registryUser`：`v1.4.0` 版本新增，当镜像推送到私有权限限制的镜像仓库中时，可以填写如下用户权限认证信息。通常，至少需要填写用户名和密码。
  - `username`：registry 仓库的用户名。可以对用户名进行加密，只要将用户名以 `ENCRYPT#` 开头就表示是加过密的用户名，否则说明直接使用原文。
  - `password`：registry 仓库的密码。可以对密码进行加密，只要将文本内容以 `ENCRYPT#` 开头就表示是加过密的密码，否则说明直接使用原文密码。
  - `email`：用户的邮箱信息，非必填。
  - `serverAddress`：registry 仓库的服务地址，如果不填写就默认使用上面的 `registry` 地址，通常你不需要填写。 
  - `identityToken`：Token 标识，通常你可以不用填写。

> 注：关于 `registryUser` 中的用户名或密码加密，你可以使用 jpack 中的 `AesKit.encrypt("your-username");` 方法进行加密。该种加密并不安全，意在防君子，防不了小人。

示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
       ...
    <configuration>
        <!-- 以下是仅对 Docker 平台下生效的配置项。 -->
        <docker>
            <!-- 构建 Docker 镜像的 Dockerfile 文件的相对路径，没有此配置项或者不填写，则使用 jpack 默认的 Dockerfile 文件. -->
            <dockerfile>Dockerfile</dockerfile>
            <!-- 构建镜像的几个基础参数, registry远程仓库地址，不填写默认视为 Dockerhub 的地址;
                repo不填写则默认为 groupId，name 不填写则默认为 artifactId，tag不填写则默认为 version.-->
            <registry></registry>
            <repo>blinkfox</repo>
            <name>jpack-test</name>
            <tag>1.0.0-SNAPSHOT</tag>
            <!-- FROM 的镜像，如果没有配置该值，将默认是 openjdk:8-jdk-alpine. -->
            <fromImage>openjdk:8-jdk-alpine</fromImage>
            <!-- 对外暴露的端口，不配置该值，将不会在 Dockerfile 中生成 EXPOSE 指令. -->
            <expose>8080</expose>
            <!-- 挂载的数据卷，不填写则默认挂载 "/tmp" 和 "/logs" 目录. -->
            <volumes>
                <param>/tmp</param>
                <param>/logs</param>
            </volumes>
            <!-- 自定义的 Dockerfile 指令选项，可以填写多个值，每个值在 Dockerfile 中占一行，充当Dockerfile中的一条指令，例如：下面设置时区的一个指令. -->
            <customCommands>
                <param>RUN echo 'Asia/Shanghai' >/etc/timezone</param>
            </customCommands>
            <!-- jpack 的 Docker 构建的默认目标是构建镜像，如果你需要其他目标的话，可以在此配置）.
                目前这里只支持导出镜像为 .tar 包(save). -->
            <!-- jpack 的 Docker 构建的默认目标是构建镜像，如果你需要其他目标的话，可以再此配置（可配多个）.
                目前这里支持导出镜像为 .tar 包(save)和 推送镜像到远程仓库(push) 两种. -->
            <extraGoals>
                <param>save</param>
                <param>push</param>
            </extraGoals>
            <!-- jpack 推送到远程私有镜像仓库的用户权限认证信息，通常至少需要填写用户名和密码两项信息，
                serverAddress 不填写就默认使用 registry. -->
            <registryUser>
                <username>blinkfox</username>
                <password>123456</password>
                <!-- 可以对用户名或密码进行加密，可同时加密，也可只加密用户名或密码，只要将用户名或密码以 'ENCRYPT#' 开头就表示是加过密的信息. -->
                <!-- <password>ENCRYPT#xrrHHzgJsGUQrfkEasF6WahbGnMXTg==</password>-->
                <email>your-emial-name@gmail.com</email>
                <serverAddress></serverAddress> <!-- registry 服务地址，不填写则使用上面 registry 的值. -->
                <identityToken></identityToken> <!-- 该 token 值通常不需要填写. -->
            </registryUser>
            <vmOptions>-Xms1024m -Xmx2048m</vmOptions>
            <programArgs>--server.port=7070</programArgs>
            <configFiles>
                <param>src/test/resources/application.yml</param>
                <param>src/test/resources/application-dev.yml</param>
            </configFiles>
            <copyResources>
                <copyResource>
                    <from>README.pdf</from>
                    <to>.</to>
                </copyResource>
            </copyResources>
            <excludeFiles>
                <param>logs</param>
            </excludeFiles>
        </docker>
    </configuration>
</plugin>
```

## 更全的配置示例及说明

jpack 的所有配置参数都非必填或者有默认值。下面是 jpack Maven 插件的所有配置项配置示例如下：

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
    <version>1.4.0</version>
    <executions>
        <execution>
            <goals>
                <!-- 只能是 build. -->
                <goal>build</goal>
            </goals>
            <!-- 默认执行阶段是 package，你可以根据情况写为 install 或 deploy 等. -->
            <phase>package</phase>
        </execution>
    </executions>
    <configuration>
        <!-- 打包哪些平台的包，不填写则代表所有平台. 目前支持 Windows、Linux 和 Dokcer 三种（大小写均可）. -->
        <platforms>
            <param>Windows</param>
            <param>Linux</param>
            <param>Docker</param>
        </platforms>
        <!-- JVM 运行所需的参数选项. -->
        <vmOptions>-Xms1024m -Xmx2048m</vmOptions>
        <!-- 所集成的 SpringBoot 服务程序运行所需的参数. -->
        <programArgs>--server.port=7070</programArgs>
        <!-- 遇到错误时是否跳过错误，目前仅Docker 下有用到此配置。默认是不填写或者 default，程序会自动处理，不需要你额外关注；
            true的话，会忽略所有异常；false的话，遇到错误就直接报错。 -->
        <skipError>default</skipError>
        <cleanPackDir>true</cleanPackDir>
        <windows>
            ...
        </windows>
        <linux>
            ...
        </linux>
        <docker>
            <!-- 构建 Docker 镜像的 Dockerfile 文件的相对路径，没有此配置项或者不填写则使用 jpack 默认的 Dockerfile 文件. -->
            <dockerfile>Dockerfile</dockerfile>
            <!-- 构建镜像的几个基础参数, registry远程仓库地址，不填写默认视为 Dockerhub 的地址;
                repo 不填写则默认为 groupId，name 不填写则默认为 artifactId，tag不填写则默认为 version.-->
            <registry></registry>
            <repo>blinkfox</repo>
            <name>web-demo</name>
            <tag>1.0.0</tag>
            <!-- FROM 的镜像，如果没有配置该值，将默认是 openjdk:8-jdk-alpine. -->
            <fromImage>openjdk:8-jdk-alpine</fromImage>
            <!-- 对外暴露的端口，不配置该值，将不会在 Dockerfile 中生成 EXPOSE 指令. -->
            <expose>8080</expose>
            <!-- 挂载的数据卷，不填写则默认挂载 "/tmp" 和 "/logs" 目录. -->
            <volumes>
                <param>/tmp</param>
                <param>/logs</param>
            </volumes>
            <!-- 自定义的 Dockerfile 指令选项，可以填写多个值，每个值在 Dockerfile 中占一行，充当Dockerfile中的一条指令，例如：下面设置时区的一个指令. -->
            <customCommands>
                <param>RUN echo 'Asia/Shanghai' >/etc/timezone</param>
            </customCommands>
            <!-- jpack 的 Docker 构建的默认目标是构建镜像，如果你需要其他目标的话，可以在此配置）.
                目前这里只支持导出镜像为 .tar 包(save). -->
            <!-- jpack 的 Docker 构建的默认目标是构建镜像，如果你需要其他目标的话，可以再此配置（可配多个）.
                目前这里支持导出镜像为 .tar 包(save)和 推送镜像到远程仓库(push) 两种. -->
            <extraGoals>
                <param>save</param>
                <param>push</param>
            </extraGoals>
            <!-- jpack 推送到远程私有镜像仓库的用户权限认证信息，通常至少需要填写用户名和密码两项信息，
                serverAddress 不填写就默认使用 registry. -->
            <registryUser>
                <username>blinkfox</username>
                <password>123456</password>
                <!-- 可以对用户名或密码进行加密，可同时加密，也可只加密用户名或密码，只要将用户名或密码以 'ENCRYPT#' 开头就表示是加过密的信息. -->
                <!-- <password>ENCRYPT#xrrHHzgJsGUQrfkEasF6WahbGnMXTg==</password>-->
                <email>your-emial-name@gmail.com</email>
                <serverAddress></serverAddress> <!-- registry 服务地址，不填写则使用上面 registry 的值. -->
                <identityToken></identityToken> <!-- 该 token 值通常不需要填写. -->
            </registryUser>
            ...
        </docker>
        <!-- 需要copy 哪些资源(from 的值可以是目录或者具体的相对、绝对或网络资源路径)到部署包中的某个目录;
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
            <!-- 复制本项目上层目录的 hello.pdf 文件到各平台包的 abc/def 目录中（会自动创建此目录）. -->
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

## 版本记录

- v1.4.0 (2020-06-03)
  - 新增了推送到远程私有镜像仓库时，可以配置 registry 的用户权限认证信息；
  - 修改了日志格式的一些显示格式；
- v1.3.4 (2019-11-18)
  - 新增了是否清除之前的打包目录的配置项；
  - 新增了在 `start.sh` 脚本中对是否已经启动了本服务的判断；
- v1.3.3 (2019-10-12)
  - 升级了相关包的依赖；
- v1.3.2 (2019-09-08)
  - 新增了 Windows 和 Linux 下查看服务运行状态（`status`）的脚本；
  - 修改了 Linux 发布包脚本中的 `shell` 声明为 `bash`；
- v1.3.1 (2019-08-21)
  - 修改了默认构建的 `Dockerfile` 从 `target` 目录下读取 jar 包；
- v1.3.0 (2019-06-04)
  - 修改了 jpack 默认的 Dockerfile 的一些指令为从配置文件读取；
  - 新增了 `fromImage`, `expose`, `volumes`, `customCommands` 4项 jpack 默认提供的 Dockerfile 的指令配置；
- v1.2.1 (2019-06-01)
  - 修改了 `configFile` 改为了 `configFiles`，支持配置多个配置文件地址；
  - 新增了推送到自定义仓库时的打标签功能；
- v1.2.0 (2019-05-22)
  - 修复了 Windows 下的 SpringBoot 配置文件不生效的问题；
  - 新增了 `configFile` 的配置支持；
  - 新增了自定义 Windows、Linux、Docker 下的一些通用配置为平台专属配置；
  - 修复了默认 `Dockerfile` 构建的镜像容器时间不对的问题；
- v1.1.0 (2019-05-18)
  - 新增了 Docker 平台下的构建、部署包导出、推送镜像等；
  - 新增了一些配置参数；
  - 完善文档说明；
- v1.0.0 (2019-05-07)
  - 基础功能完成；
  - 支持打包为 Windows 和 Linux 下的部署包；
