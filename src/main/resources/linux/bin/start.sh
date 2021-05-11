#! /bin/bash
#====================================================
# 描述: 用于启动 ${name} 服务的 shell 脚本.
# 文件:
#     1. logs 目录: 项目运行的日志目录;
#     2. logs/nohup-xxx.out: 记录后台启动日志;
#====================================================

# 项目名称
APPLICATION="${name}"

# 项目启动jar包名称
APPLICATION_JAR="${jarName}"

# 判断该服务是否已经启动，如果已经启动了，就不再重复运行了.
if [[ -z "$1" ]]
then
    pid=$(ps ax |grep -i '${jarName}' |grep java | grep -v grep |  awk '{print $1}')
else
    pid=$(ps ax |grep -i '${jarName}' |grep java | grep -i 'server.port='''${1}''''| grep -v grep |  awk '{print $1}')
fi

if [[ "$pid" ]] ; then
    echo "监测到 \${APPLICATION} 服务正在运行中，将不再重复启动... [running...]"
    exit 1;
fi

# bin目录绝对路径
BIN_PATH=$(cd `dirname $0`; pwd)
# 进入bin目录
cd `dirname $0`
# 返回到上一级项目根目录路径
cd ..
# 打印项目根目录绝对路径
# `pwd` 执行系统命令并获得结果
BASE_PATH=$(pwd)

# 项目日志输出绝对路径
LOG_DIR=\${BASE_PATH}"/logs"

# 项目启动日志输出绝对路径
LOG_STARTUP_PATH="\${LOG_DIR}/nohup-${name}.out"

# 如果logs文件夹不存在,则创建文件夹
if [[ ! -d "\${LOG_DIR}" ]]; then
  mkdir "\${LOG_DIR}"
fi

# 当前时间
NOW_PRETTY=$(date +'%Y-%m-%m %H:%M:%S')

# 启动日志
STARTUP_LOG="================================= \${NOW_PRETTY} =================================\n"

#==========================================================================================
# JVM Configuration
# -Xmx1024m:设置JVM最大可用内存为256m,根据项目实际情况而定，建议最小和最大设置成一样。
# -Xms1024m:设置JVM初始内存。此值可以设置与-Xmx相同,以避免每次垃圾回收完成后JVM重新分配内存
# -Xmn512m:设置年轻代大小为512m。整个JVM内存大小=年轻代大小 + 年老代大小 + 持久代大小。
#          持久代一般固定大小为64m,所以增大年轻代,将会减小年老代大小。此值对系统性能影响较大,Sun官方推荐配置为整个堆的3/8
# -XX:MetaspaceSize=64m:存储class的内存大小,该值越大触发Metaspace GC的时机就越晚
# -XX:MaxMetaspaceSize=320m:限制Metaspace增长的上限，防止因为某些情况导致Metaspace无限的使用本地内存，影响到其他程序
# -XX:-OmitStackTraceInFastThrow:解决重复异常不打印堆栈信息问题
#==========================================================================================
JAVA_OPT="${vmOptions}"
JAVA_OPT="\${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"
PROGRAM_ARGS="${programArgs}"

#=======================================================
# 将命令启动相关日志追加到日志文件
#=======================================================

# 输出项目名称
STARTUP_LOG="\${STARTUP_LOG}应用服务名称: \${APPLICATION}\n"
# 输出jar包名称
STARTUP_LOG="\${STARTUP_LOG}应用服务 jar 包名称: \${APPLICATION_JAR}\n"
# 输出项目根目录
STARTUP_LOG="\${STARTUP_LOG}应用服务根目录: \${BASE_PATH}\n"
# 输出项目bin路径
STARTUP_LOG="\${STARTUP_LOG}应用服务 bin 目录: \${BIN_PATH}\n"
# 打印日志路径
STARTUP_LOG="\${STARTUP_LOG}应用服务 log 目录: \${LOG_STARTUP_PATH}\n"
# 打印JVM配置
STARTUP_LOG="\${STARTUP_LOG}应用服务 JVM 配置: \${JAVA_OPT}\n"
# 打印程序参数配置
STARTUP_LOG="\${STARTUP_LOG}应用服务 参数配置: ${programArgs}\n"

# 打印启动命令
STARTUP_LOG="\${STARTUP_LOG}应用服务的启动命令: nohup java \${JAVA_OPT} -jar \${BASE_PATH}/\${APPLICATION_JAR} \${PROGRAM_ARGS} > \${LOG_STARTUP_PATH} 2>&1 &\n"

#======================================================================
# 执行启动命令：后台启动项目,并将日志输出到项目根目录下的logs文件夹下
#======================================================================
nohup java \${JAVA_OPT} -jar \${BASE_PATH}/\${APPLICATION_JAR} \${PROGRAM_ARGS} > \${LOG_STARTUP_PATH} 2>&1 &

# 进程ID
PID=$(ps -ef | grep "\${APPLICATION_JAR}" | grep -v grep | awk '{ print $2 }')

STARTUP_LOG="\${STARTUP_LOG}应用服务(进程ID: \${PID})正在后台启动中，请稍后一段时间访问本服务.\n"
STARTUP_LOG="\${STARTUP_LOG}=======================================================================================\n"

# 打印启动日志
echo -e \${STARTUP_LOG}
exit 0
