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

# 启动服务.
echo 正在启动 \${APPLICATION} 服务...
bash \${BIN_PATH}/start.sh
