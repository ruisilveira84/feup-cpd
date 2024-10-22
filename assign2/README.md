# CPD Project 2

CPD Project 2 of group T13G16.

Group members:

1. Joao Fernandes (up202108044@up.pt)
2. Bruno Leal (up202080047@up.pt)
3. Rui Silveira (up202108878@up.pt)

## Description

This project is an implementation of a Server that hosts Tic-Tac-Toe games with User Registration and Login, Concurrency and Fault Tolerance.


## Compile and Run Instructions

To compile and run the server, run the following command (at this current folder):

```
bash ./run_server.sh
```
This command will compile and run the server at the port 8000

To compile and run the client, run the following command (at this current folder):

```
bash ./run_client.sh
```
This command will compile and run the client, who will connect at the server at the localhost in port 8000.

## Files and Functions

- GameServer -> Handles the Server:
  - matchmaking_basic() -> Called as a separated thread to handle the matchmaking basic.
  - matchmaking_rank() -> Called as a separated thread to handle the matchmaking ranked.
  - handleConnection() -> Handles each connection of a socket.
  - handleClient() -> Handles each user authentication.
  - handleMatchmaking() -> Handles each user's matchmaking choice (simple or ranked).
  - fillClients() and WriteToUserLogsJson() -> Reads and writes from UserLogs.json respectivally.
  - start() -> Stays the server running and handles each new user's connection.
- TicTacToe -> A simple implementation of a tictactoe game.
- User -> Stores the user's custom class.
- GameClient -> Used to access server by a client, inserting a port and ip address.
