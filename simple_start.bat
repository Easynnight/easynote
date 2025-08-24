@echo off

:: 启动后端服务
cd backend
start mvn spring-boot:run -Ddebug

:: 等待5秒
ping 127.0.0.1 -n 6 > nul

:: 启动前端服务
cd ..\frontend
start npm run dev

:: 提示
cd ..
echo 后端服务默认端口: 8088
echo 前端服务默认端口: 3000
echo 请在浏览器中访问 http://localhost:3000