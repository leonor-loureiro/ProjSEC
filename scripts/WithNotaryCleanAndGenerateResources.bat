set "client=%cd%\Client"
set "filesystem=%cd%\Server"
set "res =%cd%\ResourcesLoader"

rem -- delete resources
start cmd /c "title cleaningresources & cd .. & cd resources & del *.jceks & del *.ser & del *.log & del nonces*"

rem -- delete resourcesServer
start cmd /c "title resourcesServerClean & cd .. & cd resourcesServer & del *.ser"

timeout /t 15

rem -- Start ResourcesLoader
start cmd /c "title ResourcesLoader & cd .. & cd ResourcesLoader & mvn clean install exec:java -Dnotary=1"
