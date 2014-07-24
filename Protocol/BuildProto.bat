@echo protocol file generator...
@echo off
for /r %%i in (*.proto) do (
	@echo general %%~ni.proto
	protoc.exe --cpp_out=./Protobuf/C++/ --java_out=./Protobuf/Java/src %%~ni.proto
	protoc.exe --lua_out=./Protobuf/Lua/ --plugin=protoc-gen-lua=".\Plugin\protoc-gen-lua.bat" %%~ni.proto
)
@pause
@echo done!