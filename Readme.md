# Simple network library


## Description

A simple library for a simple communication between a computer and an android phone.  
This library has been designed for use in private wi-fi network and for non-sensitive messages, so it is not secure. 
 
 
## How to use

To use this library, just override one abstract class in receiver package for the device receiving the connection and  
do the same with a class in sender package for the device which asks for the connection.

## Behavior

The receiver wait for a connection.  
The sender sends a connection message using broadcasting to search the receiver.  
When the connection is up, the receiver stops listening for connection to start the communication.

## installation with gradle

```gradle
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

    dependencies {
        ...
        implementation 'com.github.StephaneDionisio:simple-network-lib:2.+'
    }
```
