# Pull Java 8 image.
FROM ${jdkImage}

MAINTAINER jpack

${valume}
${customCommands}
# 添加 jar.
ADD target/${jarName} /${jarName}

ENV JVM_OPTS="${vmOptions}" PROGRAM_ARGS="${programArgs}"
${expose}
# 定义运行的命令.
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /${jarName} $PROGRAM_ARGS"]
