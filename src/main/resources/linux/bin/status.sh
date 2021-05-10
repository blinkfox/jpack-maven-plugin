#! /bin/bash
#=====================================================================
# 查看应用服务是否启动或停止状态的 shell 脚本
# 通过项目名称查找到 PID
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
    exit 0;
fi

echo "\${APPLICATION} 服务正在运行中 (PID: \${pid}). [is running ...]"
exit 1
