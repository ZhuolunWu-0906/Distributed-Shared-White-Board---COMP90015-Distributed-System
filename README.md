# Distributed Shared White Board - COMP90015 Distributed System
Distributed Shared White Board, Assignment 2 of COMP90015 Distributed System (University of Melbourne Master's degree subject)



## System break down



### Server

The server creates a thread for each client's connection, broadcast any received shapes and arguments to all connected clients to make editing the white board concurrently possible.

Server stores three array lists:

* Name of each client
* Socket (thread) of each client
* Shape drawn on the white board

All of above array lists are called and manipulated with synchronized methods and code blocks to solve the concurrency issue.



### Client

The first client connected to the server becomes the white board manager, who have access to all admin functionalities. All manipulations done on any client would be sent to the server and let it broadcast to other users.

Client can also use the chat function, view all connected users, etc.



### Server-Client Messaging

Server and clients use JSON string for communication, via Java TCP socket.



## System build up



### Server

``` java -jar server.jar <port number>```



### Client

``` java -jar client.jar```

Note: Client needs to enter server's IP address and port number in the GUI after running the above command line argument.
