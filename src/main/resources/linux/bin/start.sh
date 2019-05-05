#!/bin/bash
DIR1="`dirname $BASH_SOURCE`"
CURRENT_PATH=`readlink -f "$DIR1"`

echo "尝试获取本机 ip 地址"
IP_ADDR='127.0.0.1'
IP_ADDR=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}')
echo "成功获取本机 ip 地址: $IP_ADDR"

echo "JavaVM 运行参数为：${vmOptions}"
echo "应用程序运行参数为：${programArgs}"

cd \${CURRENT_PATH}
cd ..
echo $(pwd)

nohup java ${vmOptions} -jar ${jarName} ${programArgs} > logs/nohup_${name}.out 2>&1 &
