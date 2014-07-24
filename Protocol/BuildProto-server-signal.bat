@echo Proto file generator...
@echo off
set /p filename=input the filename(without suffix):
@echo general %filename%.proto
protoc.exe --java_out=./Protobuf/Java/src %filename%.proto
@pause
@echo done!