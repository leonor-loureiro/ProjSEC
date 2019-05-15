set "client=%cd%\Client"
set "filesystem=%cd%\Server"
set "res =%cd%\ResourcesLoader"

rem -- Start Server 0
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8080"

rem -- Start Server 1
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8081"

rem -- Start Server 2
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8082"

rem -- Start Server 3
start cmd /c "title Server & cd .. & cd Server & mvn exec:java -Dport=8083"

rem -- Start Client
start cmd /c "title ClientByzantine & cd .. & cd Client & mvn exec:java -Ddouble=true"

rem -- Start Client
start cmd /c "title Client & cd .. & cd Client & mvn exec:java"