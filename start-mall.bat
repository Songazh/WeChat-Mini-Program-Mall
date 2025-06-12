@echo off 
cd /d C:\Users\SH\Desktop\cshi11 
docker-compose up -d 
echo Starting mall system... 
timeout /t 30 
docker-compose ps 
pause 
