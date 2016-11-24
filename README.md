# fcm-client
fcm-client is a Java Swing client that can be used to connect to an FCM XMPP 
server.

![fcm-client](doc/screenshot-1.jpg?raw=true "fcm-client")

## Pre-requisites
1. [Apache Maven](http://maven.apache.org/)
2. [Java](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) or [OpenJDK](http://openjdk.java.net/install/)

## Run with Maven
    mvn compile exec:java

## Build & Run
    mvn package
    java -jar ./target/fcm-client.jar