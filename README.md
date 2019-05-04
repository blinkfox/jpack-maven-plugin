# jpack-maven-plugin (开发中...)

[![HitCount](http://hits.dwyl.io/blinkfox/jpack-maven-plugin.svg)](https://github.com/blinkfox/jpack-maven-plugin) [![Build Status](https://secure.travis-ci.org/blinkfox/jpack-maven-plugin.svg)](https://travis-ci.org/blinkfox/jpack-maven-plugin) [![GitHub license](https://img.shields.io/github/license/blinkfox/jpack-maven-plugin.svg)](https://github.com/blinkfox/jpack-maven-plugin/blob/master/LICENSE) [![codecov](https://codecov.io/gh/blinkfox/jpack-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/blinkfox/jpack-maven-plugin) ![Java Version](https://img.shields.io/badge/Java-%3E%3D%208-blue.svg)

> 这是一个用于对 Java 服务打包为 Windows、Linux 下可部署包的 Maven 插件. 开发中...

## 特性

- 简单易用
- 支持打包为 `Windows` 和 `Linux` 下的部署包
- 可自定义复制资源到部署包中.

## 集成本插件

> **注**：本插件还未发布，以下仅是集成使用的参考示例。

### 最简示例

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>build</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 最全示例及说明

```xml
<plugin>
    <groupId>com.blinkfox</groupId>
    <artifactId>jpack-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <!-- 构建的目标是 build, 在打包阶段执行. -->
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
    </configuration>
</plugin>
```

## 许可证

本 [jpack-maven-plugin](https://github.com/blinkfox/jpack-maven-plugin) Maven 插件 遵守 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) 许可证。