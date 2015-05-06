#!/bin/bash
ConfigXml="/data/java/jar/cfg/gs.cfg"

MultiItems=`grep dbConnUrl ${ConfigXml} | sed 's/:/\//g' | awk -F\/ '{print $5,$6,$7}'` 
MysqlIP=`echo $MultiItems| awk '{print $1}'`
MysqlPort=`echo $MultiItems| awk '{print $2}'`
MysqlDB=`echo $MultiItems| awk '{print $3}'`

MysqlUser=`grep dbUserName ${ConfigXml} | awk '{print $3}'`
MysqlPass=`grep dbPassWord ${ConfigXml} | awk '{print $3}'`

echo ${MysqlIP}
echo ${MysqlPort}
echo ${MysqlUser}
echo ${MysqlPass}
echo ${MysqlDB}

mysql -h${MysqlIP} -P${MysqlPort} -u${MysqlUser} --password=${MysqlPass} --default-character-set=utf8 -D${MysqlDB}
