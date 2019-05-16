# Pull Java 8 image.
FROM openjdk:8-jdk-alpine

MAINTAINER jpack

VOLUME /tmp

# 添加 jar.
ADD ${jarName} /${jarName}
# RUN bash -c 'touch /${jarName}'

ENV JVM_OPTS="${vmOptions}" PROGRAM_ARGS="${programArgs}"

# 定义运行的命令.
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /${jarName} $PROGRAM_ARGS"]

# Centos6 设置时区.
# RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# Centos7 设置时区.
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone