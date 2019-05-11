# Pull base image
FROM java:8

MAINTAINER blinkfox "chenjiayin1990@163.com"

VOLUME /tmp

# 添加.
ADD jpack-test-1.0.0-SNAPSHOT.jar jpack-test-1.0.0-SNAPSHOT.jar
RUN bash -c 'touch /jpack-test-1.0.0-SNAPSHOT.jar'

# Define default command.
ENTRYPOINT ["java", "-jar", "/jpack-test-1.0.0-SNAPSHOT.jar"]

#设置时区.
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone