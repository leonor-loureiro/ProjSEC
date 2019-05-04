set "client=%cd%\Client"
set "filesystem=%cd%\Server"
set "res =%cd%\ResourcesLoader"

rem  -- installs project

start cmd /c "title INSTALLING & cd .. & mvn clean install -DskipTests"
timeout /t 15

rem -- Start Server 0
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8080 -Dmode=true"

rem -- Start Server 1
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8081 -Dmode=true"

rem -- Start Server 2
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8082 -Dmode=true"

rem -- Start Server 3
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8083 -Dmode=true"

rem -- Start Client
start cmd /c "title Client & cd .. & cd Client & mvn exec:java"

rem -- Start Client
start cmd /c "title Client & cd .. & cd Client & mvn exec:java"