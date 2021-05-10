#! /bin/bash
#=====================================================================
# 项目重启的 shell 脚本;
# 先调用 stop.sh 停服;
# 然后调用 start.sh 启动服务.
#=====================================================================

# 项目名称.
APPLICATION="${name}"

# bin目录绝对路径.
BIN_PATH=$(cd `dirname $0`; pwd)

# 关闭服务.
echo 正在关闭 \${APPLICATION} 服务...
bash \${BIN_PATH}/stop.sh

# 如果是优雅停机，就要循环判断服务是否关闭完成.
bash \${BIN_PATH}/status.sh
while [ $? -eq 1 ]
do
    echo "\${APPLICATION} 服务还未停止"
    sleep 1s
    bash \${BIN_PATH}/status.sh
done

# 启动服务.
echo 正在启动 \${APPLICATION} 服务...
bash \${BIN_PATH}/start.sh
exit 0
