# start command.
nohup java -server ${vmOptions} -jar ../${jarName} ${programArgs} > ../logs/nohup_${name}.out 2>&1 &
