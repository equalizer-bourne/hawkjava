netstat -ntlp | grep 9595 | grep -v grep | awk '{print $7}' | awk -F'/' '{print $1}'
netstat -ntlp | grep 9595 | grep -v grep | awk '{print $7}' | awk -F'/' '{print $1}' | xargs kill
echo 'kill success ...'

#if test `ps aux | grep game | grep -v grep | wc -l` -gt 1
#then
#     echo 'waiting dataland ...'
#     sleep 1
#fi

while [ `netstat -nltp | grep 9595 | grep -v grep | wc -l` -gt 1 ]
do
     echo 'waiting dataland ...'
     sleep 1
done

echo 'cope files ...'
cp -rf cfg/db.xml /data/java/jar/cfg/
cp -rf game.jar /data/java/jar/
cp -rf log4j.properties /data/java/jar/
cp -rf script /data/java/jar/
cp -rf xml /data/java/jar/
cp -rf lib /data/java/jar/

echo 'running ...'
cd /data/java/jar/
>./console.log
nohup java -jar -Xms1024m -Xmx4096m game.jar >>./console.log &

sleep 10

cat console.log
