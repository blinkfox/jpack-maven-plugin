#!/bin/bash
# add comment
if [[ -z "$1" ]]
then
    pid=`ps ax |grep -i '${jarName}' |grep java | grep -v grep |  awk '{print $1}'`
else
    pid=`ps ax |grep -i '${jarName}' |grep java | grep -i 'server.port='''${1}''''| grep -v grep |  awk '{print $1}'`
fi

if [[ -z "$pid" ]] ; then
        echo "${name} is not running."
        exit 0;
fi
echo "${name} (\${pid}) is running."
echo "Send shutdown request to ${name}(\${pid})....."
kill -9 \${pid}
echo "Shutdown ${name}(\${pid}) success."
