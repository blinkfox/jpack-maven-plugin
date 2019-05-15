# Pull base image
FROM java:8

# MAINTAINER blinkfox "chenjiayin1990@163.com"

VOLUME /tmp

# 添加.
ADD ${jarName} ${jarName}
RUN bash -c 'touch /${jarName}'

# Define default command.
ENTRYPOINT ["java", "-jar", "/${jarName}"]

#设置时区.
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone