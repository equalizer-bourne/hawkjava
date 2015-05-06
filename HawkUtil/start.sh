>./console.log
nohup java -jar -XX:PermSize=64m -XX:MaxPermSize=256m -XX:-OmitStackTraceInFastThrow -XX:CompileCommand=exclude,com/hawk/game/util/QuickPhotoUtil,attachEquipInfo -Xms2048m -Xmx4096m game.jar >>./console.log &
tail -f console.log
