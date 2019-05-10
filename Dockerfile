# Pull base image
FROM java:8

MAINTAINER blinkfox "chenjiayin1990@163.com"

VOLUME /tmp

# 添加.
ADD web-demo-0.0.1-SNAPSHOT.jar web-demo-0.0.1-SNAPSHOT.jar
RUN bash -c 'touch /web-demo-0.0.1-SNAPSHOT.jar'

# Define default command.
ENTRYPOINT ["java", "-jar", "/web-demo-0.0.1-SNAPSHOT.jar"]

#设置时区.
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone