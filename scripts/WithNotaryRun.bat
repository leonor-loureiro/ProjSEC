set "client=%cd%\Client"
set "filesystem=%cd%\Server"
set "res =%cd%\ResourcesLoader"

rem  -- installs project

start cmd /c "title INSTALLING & cd .. & mvn clean install -DskipTests"
timeout /t 15

rem -- Start Server 0
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8080"

rem -- Start Server 1
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8081"

rem -- Start Server 2
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8082"

rem -- Start Server 3
start cmd /c "title ServerWithCC & cd .. & cd Server & mvn exec:java -Dport=8088 -Dnotary=1"

rem -- Start Client
start cmd /k "title Client & cd .. & cd Client & mvn exec:java"

rem -- Start Client
start cmd /k "title Client & cd .. & cd Client & mvn exec:java"