#! /bin/bash
#=====================================================================
# 描述: 用于重启服务的 shell 脚本.
# 步骤: 
#     1. 先调用 stop.sh 停服;
#     2. 由于服务可能是“优雅停机”，再循环判断服务是否真的已停止;
#     3. 停止之后，再调用 start.sh 启动服务.
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
while [ $? -eq 0 ]
do
    echo "\${APPLICATION} 服务仍在运行中，1s 后将继续检查服务状态."
    sleep 1s
    bash \${BIN_PATH}/status.sh
done

# 启动服务.
echo 正在启动 \${APPLICATION} 服务 ...
bash \${BIN_PATH}/start.sh
exit 0
