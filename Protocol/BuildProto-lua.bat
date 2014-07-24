@echo protocol file generator...
@echo off
for /r %%i in (*.proto) do (
	@echo general %%~ni.proto
	protoc.exe --lua_out=./Protobuf/Lua/ --plugin=protoc-gen-lua=".\plugin\protoc-gen-lua.bat" %%~ni.proto
)
@echo done!