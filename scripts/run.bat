set "client=%cd%\Client"
set "filesystem=%cd%\Server"
set "res =%cd%\ResourcesLoader"

rem  -- installs project

start cmd /c "title INSTALLING & cd .. & mvn clean install -DskipTests"
timeout /t 15
rem -- Start Auth Server
start cmd /c "title Server & cd .. & cd Server & mvn exec:java"

rem -- Start FileSystem
start cmd /c "title Client & cd .. & cd Client & mvn exec:java"

rem -- Start FileSystem
start cmd /c "title Client & cd .. & cd Client & mvn exec:java"

rem -- Start FileSystem
start cmd /c "title Client & cd .. & cd Client & mvn exec:java"