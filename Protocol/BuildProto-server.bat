@echo protocol file generator...
@echo off
for /r %%i in (*.proto) do (
	@echo general %%~ni.proto
	protoc.exe --java_out=./Protobuf/Java/src %%~ni.proto
)
@echo done!