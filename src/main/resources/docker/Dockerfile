# Pull Java 8 image.
FROM java:8

# MAINTAINER jpack "jpack@xxx.com"

VOLUME /tmp

# 添加 jar.
ADD ${jarName} ${jarName}
RUN bash -c 'touch /${jarName}'

# 定义运行的命令.
ENTRYPOINT ["java", "-jar", "/${jarName}"]

# 设置时区.
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone