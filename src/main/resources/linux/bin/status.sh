#! /bin/bash
#=====================================================================
# 描述: 用于查看服务是否启动或停止状态的 shell 脚本.
# 步骤: 
#     1. 通过服务名称查找到 pid;
#     2. 如果 pid 为空就说明服务已经不存在了，否则说明服务正在运行; 
#=====================================================================

# 项目名称
APPLICATION="${name}"

if [[ -z "$1" ]]
then
    pid=$(ps ax |grep -i '${jarName}' |grep java | grep -v grep |  awk '{print $1}')
else
    pid=$(ps ax |grep -i '${jarName}' |grep java | grep -i 'server.port='''${1}''''| grep -v grep |  awk '{print $1}')
fi

if [[ -z "$pid" ]] ; then
    echo "未监测到 \${APPLICATION} 服务. [not running!]"
    exit 1;
fi

echo "\${APPLICATION} 服务正在运行中 (PID: \${pid}). [is running ...]"
exit 0

