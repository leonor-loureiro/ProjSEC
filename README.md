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




## Install and run the project:

The following instructions will start a two clients and one server. 

Travel back to root directory

### Option 1: Script (Windows only)
Execute the script ProjSEC/scripts/run.bat


### Option 2: Non script version
Connect your computer to a smartcard reader and insert the test citizen card "Ana Revogado".

Open 3 terminals at the project's root
1st terminal:
+ cd Server/
+ mvn exec:java
+ input citizen card authentication pin if requested

2nd and 3rd terminal:
+ cd Client/
+ mvn exec:java

For every request sent to the server, a window will open asking you to insert the authentication PIN ( = 1111). 


## System's State
The system has the following users:
+ user0 (password: user0user0)
+ user1 (password: user1user1)
+ user2 (password: user2user2)

To see the list of goods in the system, run the command (l) list goods in the client application.


## Automatic Tests
The project contains a set of automatic tests for the application.
Run tests:
+ Connect your computer to a smartcard reader and insert the test citizen card "Ana Revogado".
+ Open a terminal at the project's root folder
+ mvn test

Some tests will fail if the smartcard reader is not connected to the computer or the correct citizen card is not inserted.
The authentication pin will be requested for the execution of multiple tests.









