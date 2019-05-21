# Pull Java 8 image.
FROM openjdk:8-jdk-alpine

MAINTAINER jpack

#设置时区
RUN echo http://mirrors.ustc.edu.cn/alpine/v3.9/main > /etc/apk/repositories \
&& echo http://mirrors.ustc.edu.cn/alpine/v3.9/community >> /etc/apk/repositories \
&& apk add tzdata && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

VOLUME /tmp

# 添加 jar.
ADD ${jarName} /${jarName}

ENV JVM_OPTS="${vmOptions}" PROGRAM_ARGS="${programArgs}"

# 定义运行的命令.
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /${jarName} $PROGRAM_ARGS"]
