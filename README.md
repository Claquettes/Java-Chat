# Java Chat Server and Client

This project is a simple implementation of a chat server and client in Java using the UDP protocol. It was developed by Arthur Rigonnet and Ponton Mathieu as part of the Networks course in the 3rd year of FISA at Polytech Lyon, under the supervision of Mr. Lucas De Meyer.

## Project Structure

The project is divided into two main parts: the server and the client, both implemented in Java.

### Server

The server, implemented in [`serveurUDP.java`](src/serveurUDP.java), is a class that implements `Runnable`. It uses a `DatagramSocket` to listen for incoming messages from clients. The server maintains a `Map` where the key is the chat room number and the value is a list of client information in that room. The server also has a method `sendMessagesToClient` which sends messages to a specific client.

### Client

The client, implemented in [`clientUDP.java`](src/clientUDP.java), is a class that initializes a `DatagramSocket` to communicate with the server. The user is prompted to enter the chat room number and the client sends a message to the server to join that room. Once in the chat room, the user can send messages which will be received by all other users in the same room.

## Usage

To use this project, you need to first start the server by running the `serveurUDP.java` file. Then, you can start as many clients as you want by running the `clientUDP.java` file. When prompted, enter the chat room number to join a chat room.

## Conclusion

This project is a simple demonstration of how to use UDP sockets for client-server communication in Java. We have reworked the code, especially to ensure that clients do not need to fetch messages from the server, but the server sends them messages directly. We have also added features such as the ability to choose your nickname and timeouts, which close the sockets after a certain period of client inactivity.
