# Battleship-Multiplayer

## Project Overview
This project is a multiplayer Battleship game implemented using C++ for the game logic and Java for the client-server communication and user interface. The game consists of two main components:

1. **C++ Game Logic and JNI (Java Native Interface):** Backend game logic and Java-C++ communication via JNI.
2. **Java Client and Server:** Frontend and network communication enabling two players to connect and play.

---

## Directory Structure
```
BATTLESHIP MULTIPLAYER PROJECT/
│
├── .vscode/            
├── bin/                
│   ├── BattleshipClient.class
│   ├── BattleshipServer.class
│   ├── GameEngine.class
│   ├── BattleshipJNI.class
│   ├── BattleshipJNI.o
│   ├── GameLogic.o
│   ├── BattleshipGame.dll
├── docs/               
│   └── README.txt
├── lib/                
│   ├── jni.h
│   └── jni_md.h
├── src/                
│   ├── cpp/
│   │   ├── include/
│   │   │   ├── BattleshipJNI.h
│   │   │   └── GameLogic.h
│   │   ├── src/
│   │       ├── BattleshipJNI.cpp
│   │       └── GameLogic.cpp
│   ├── java/
│       ├── BattleshipClient.java
│       ├── BattleshipServer.java
│       ├── GameEngine.java
│       └── BattleshipJNI.java
```

**Note:** Execute all commands from within the `Battleship Multiplayer Project` directory.

---

## Prerequisites

### C++ Development Environment
- A C++ compiler (e.g., g++ or MSVC)
- Java Development Kit (JDK) version 21 or higher
- JNI headers (`jni.h` and `jni_md.h`) from the JDK

### Java Development Environment
- JDK version 21 or higher for compiling and running Java code

### Operating System
- **Windows** (the project uses `.dll` files for JNI)

**Note:** Ensure all prerequisites are met before running the game.

---

## Running the Project

### Run the Server
```sh
java -cp bin BattleshipServer
```

### Run the Client (Open two terminals for two players)
```sh
java -cp bin BattleshipClient
```

---

## Build Instructions

### 1. Build the C++ JNI Library (`BattleshipGame.dll`)
The C++ source files (`GameLogic.cpp` and `BattleshipJNI.cpp`) need to be compiled into a shared library.

#### Steps:
```sh
# Compile C++ source files

g++ -Ilib -c src/cpp/src/GameLogic.cpp -o bin/GameLogic.o
g++ -Ilib -c src/cpp/src/BattleshipJNI.cpp -o bin/BattleshipJNI.o

# Link the object files into a shared library
g++ -Ilib -shared -DINCLUDE_MAIN ./src/cpp/src/GameLogic.cpp ./src/cpp/src/BattleshipJNI.cpp -o ./bin/BattleshipGame.dll
```
This will generate `BattleshipGame.dll` in the `bin/` directory.

### 2. Compile the Java Files
Compile the Java source files and place the `.class` files in the `bin/` directory.

#### Steps:
```sh
javac -d bin -sourcepath src/java src/java/*.java
```

---

## Network Configuration

### Update Server IP
Modify `BattleshipClient.java` with the server system's IPv4 address:
```java
private static final String SERVER_ADDRESS = "SERVER_IP_GOES_HERE";
```
Designate a port (e.g., `12345`) for the server.

---

## Troubleshooting

### JNI Errors
- Ensure `BattleshipGame.dll` is located in the `bin/` directory.
- Verify that `System.loadLibrary("BattleshipGame")` in `BattleshipJNI.java` points to the correct library name.

### ClassNotFoundException
- Ensure the `bin/` directory is included in the classpath.
- Confirm all `.class` files are present in `bin/`.

---

## Thank You!
