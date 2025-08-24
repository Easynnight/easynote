@echo off

chcp 65001 > nul

:: 启动后端服务
echo 正在启动后端服务...
start cmd /k "call start_backend.bat"

:: 等待1秒，确保后端服务有时间启动
ping 127.0.0.1 -n 2 > nul

:: 启动前端服务
echo 正在启动前端服务...
start cmd /k "call start_frontend.bat"

:: 提示用户服务已启动
echo 前后端服务已启动，请在各自的命令窗口中查看启动状态。
echo 后端服务通常运行在 http://localhost:8085
echo 前端服务通常运行在 http://localhost:3000