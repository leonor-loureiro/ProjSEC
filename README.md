# ProjSEC

## Requirements:
+ Maven https://maven.apache.org/install.html
+ Java 1.8
+ Portugal's CC authentication program https://www.autenticacao.gov.pt/cc-aplicacao
+ a Citizen Card and a Card Reader

## Setup:
Go to the project's root directory and execute the following commands
+ mvn clean install -DskipTests
+ cd ResourcesLoader/
+ mvn exec:java

## Executing
The following instructions are to start a 2 clients and 1 server

Travel back to root directory

#### Script (Windows only)
execute the script ProjSEC/run.bat

#### Non script version:
Open 3 terminals at the project's root
1st terminal:
+ cd Server/
+ mvn exec:java
+ input citizen card authentication pin if requested

2nd and 3rd terminal:
+ cd Client/
+ mvn exec:java


