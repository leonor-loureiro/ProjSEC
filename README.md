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

The following instructions will start a two clients and 4 servers. 

Travel back to root directory

### Option 1: Script (Windows only) without CC
Execute the scripts in order:
- ProjSEC/scripts/install.bat
- ProjSEC/scripts/cleanAndGenerateResources.bat
- ProjSEC/scripts/run.bat

### Option 2: Script (Windows only) with CC
Connect your computer to a smartcard reader and insert the test citizen card "Ana Revogado".

Execute the scripts in order:
- ProjSEC/scripts/install.bat
- ProjSEC/scripts/WithNotaryCleanAndGenerateResources.bat
- ProjSEC/scripts/WithNotaryRun.bat

At the start of the server, a window will open asking you to insert the authentication PIN ( = 1111) twice. 


## System's State
The system has the following users:
+ user0 (password: user0user0)
+ user1 (password: user1user1)
+ user2 (password: user2user2)

To see the list of goods in the system, run the command (l) list goods in the client application.


## Tests

### Byzantine Server
ProjSEC/scripts/run_byzantine_server.bat

When a message is sent from one of the clients, one of the Servers does not update correctly and gives different results,
the system is still able to proceed since a majority of the servers received a "correct" request and every server
even the ones with wrong initial message will deliver the same message as the others.


### Byzantine client different message broadcast
ProjSEC/scripts/run_byzantine_client.bat

If you send a message through the console marked as byzantine client the message sent to each server isn't the same, however since the majority of the servers received it, the request
is processed thanks to the byzantine reliable broadcast.


### Byzantine client Double Write Broadcast
ProjSEC/scripts/run_byzantine_clientDoubleBroadcast.bat

When a write operation is sent from the byzantine client cmd, the client will attempt to send 2 consecutive writes 
to the same good, to generate a inconsistent state across different servers, however since the server drops the 2nd 
write until the first has finished, the client will be stuck waiting for the dropped requests of the second write 
that will never come.





## Project Guide
The project is divided in 6 Modules: Client, CommonTypes, Communication, Crypto, ResourcesLoader and Server


### Client
Responsible for the client side of the application logic.


### CommonTypes
Data classes or util functions that are common to other modules in this project, such as Goods, Users and the AtomicFileManager


### Communication
Abstracts part of the communication and sockets, provides a easy way to start a server that receives requests.

Also contains the Byzantine Atomic Register implementation (Communication.registers)

AuthenticatedPerfectLinks implementation (Communication.AuthenticatedPerfectLinks) 
- complemented by some necessary code in client's manager and server's manager

Byzantine Reliable Broadcast implementation (Communication.ProcessMessageWithEchoRunnable)


### Crypto
Wrapper for the cryptographic functions and keystore handling


### ResourcesLoader
Wrapper for loading the necessary resources for clients and servers
Executing it, will generate the necessary resources for the project to start, such as uselists, keys, goodslist...


### Server
The base logic of server side of the application



