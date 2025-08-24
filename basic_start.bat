@echo off
cd backend
start cmd /k "mvn spring-boot:run"
ping 127.0.0.1 -n 6 > nul
cd ..\frontend
start cmd /k "npm run dev"
cd ..
echo Services started
echo Backend: http://localhost:8088
echo Frontend: http://localhost:3000