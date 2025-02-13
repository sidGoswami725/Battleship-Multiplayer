Battleship Multiplayer Project
______________________________
______________________________




Project Overview:
_________________

This project is a multiplayer Battleship game implemented using C++ for the game logic and Java for the client-server communication and user interface. The game consists of two main components:

1) C++ Game Logic and JNI (Java Native Interface): Backend game logic and Java-C++ 
   communication via JNI.

2) Java Client and Server: Frontend and network communication
   enabling two players to connect and play.




Directory Structure:
____________________

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



Note: Make sure you execute all the necessary commands from within the folder "Battleship Multiplayer Project"



Prerequisites:
______________


C++ Development Environment:

	1) A C++ compiler (e.g., g++ or MSVC).
	2) Java Development Kit (JDK) version 21 or higher.
	3) JNI headers (jni.h and jni_md.h) from the JDK.

Java Development Environment:

	1) JDK version 21 or higher for compiling and running Java code.

Operating System:

	1) Windows (the project uses .dll files for JNI).


Note: If all the above prerequisites are fulfilled and all directory files are available,
      User just has to run the Client and Server codes on terminal in the 
      working directory, else, go through the Build instructions given below:

      Run the Server:
       _______________________________
      |			              |
      | java -cp bin BattleshipServer |
      |_______________________________|

      Run the Client (2 tabs required for 2 players):
       _______________________________
      |			              |
      | java -cp bin BattleshipClient |
      |_______________________________|

      


Build Instructions:
___________________


a) Build the C++ JNI Library (BattleshipGame.dll):

   The C++ source code files (GameLogic.cpp and BattleshipJNI.cpp) need to be compiled 
   into a shared library (BattleshipGame.dll).

   Steps:

   	1) Open a terminal and navigate to the BATTLESHIP MULTIPLAYER PROJECT folder.
   	2) Run the following commands to compile the C++ source files:
	   
	   (terminal):
           ___________

	   # Compile GameLogic.cpp and BattleshipJNI.cpp into object files

	   g++ -Ilib -Ilib -c src/cpp/src/GameLogic.cpp -o bin/GameLogic.o
           g++ -Ilib -Ilib -c src/cpp/src/BattleshipJNI.cpp -o bin/BattleshipJNI.o


	   # Link the object files into a shared library
	   
	   g++ -I./lib -I./lib -shared -DINCLUDE_MAIN ./src/cpp/src/GameLogic.cpp ./src/cpp/src/BattleshipJNI.cpp -o ./bin/BattleshipGame.dll


	This will generate the BattleshipGame.dll file in the bin/ folder.


b) Compile the Java Files:

   The Java source code files are located in the src/java/ directory.
   They need to be compiled into .class files and placed in the bin/ directory.

   Steps:

   	1) Run the following command to compile the Java files:

	   (terminal):
	   ___________

	   javac -d bin -sourcepath src/java src/java/*.java
	   
	This will compile all Java source files and place the .class files in the bin/ directory.



Running the Project:
____________________


Note: First and foremost, all 3 systems should be connected with a common mobile hotspot network to avoid any network firewall issues. 
      Before running the respective files, make sure you enter the Server System's IPv4 address in
      BattleshipClient.java. By default, the code is configured to run two player tabs and system tab
      on the same system.

      The user can edit this line in BattleshipClient.java:

      ___________________________________________________________________

      private static final String SERVER_ADDRESS = "SERVER_IP_GOES_HERE";
      ___________________________________________________________________

      Also, designate a port (Ex: 12345) on the server system to establish connections.
      


a) Run the Battleship Server:

   Start the server, which coordinates the game logic and manages player connections.

   Steps:

   	1) Open a terminal and navigate to the project folder.
   	2) Run the following command to start the server:

	   (terminal):
	   _______________________________
	  |				  |
	  | java -cp bin BattleshipServer |
	  |_______________________________|



b) Run the Battleship Client:

   Start the client, which connects to the server and allows the player to interact with the game.

   Steps:

   	1) Open another terminal and navigate to the project folder.
	2) Run the following command to start the client:

	   (terminal):
	   _______________________________
	  |				  |
	  | java -cp bin BattleshipClient |
	  |_______________________________|

	Both players should run the client on separate terminals and connect to the same server.




Troubleshooting:
________________


a) JNI Errors:

	1) Ensure BattleshipGame.dll is located in the bin/ directory.
	2) Verify that the System.loadLibrary("BattleshipGame") call in BattleshipJNI.java points to the correct library name.


b) ClassNotFoundException:

	1) Ensure the bin/ directory is included in the classpath when running Java files.
	2) Confirm that all .class files are present in the bin/ directory.




THANK YOU!
__________
__________

