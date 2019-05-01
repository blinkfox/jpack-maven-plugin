# start command.
nohup java -server ${vmOptions} -jar ../${jarName} ${programArgs} > nohup_${name}.out 2>&1 &
