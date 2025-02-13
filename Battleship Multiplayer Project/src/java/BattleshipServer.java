import java.io.*;
import java.net.*;

public class BattleshipServer {
    private static final int SERVER_PORT = 12345;  // Port number for the server
    private ServerSocket serverSocket;  // ServerSocket to listen for connections
    private Socket player1Socket, player2Socket;  // Sockets for Player 1 and Player 2
    private ObjectOutputStream player1Out, player2Out;  // Output streams for sending data to players
    private ObjectInputStream player1In, player2In;  // Input streams for receiving data from players

    private BattleshipJNI player1, player2;  // BattleshipJNI objects representing the game state for each player
    private GameEngine gameEngine;  // Game engine to manage game logic

    public static void main(String[] args) {
        // Start the server by creating an instance and calling startServer()
        new BattleshipServer().startServer();
    }

    public void startServer() {
        try {
            // Initialize the server socket
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started. Waiting for players...");

            // Accept connections from players
            acceptPlayers();

            // Handle the ship placement phase
            handleShipPlacement();

            // Start the gameplay phase
            startGame();

        } catch (IOException | ClassNotFoundException e) {
            // Handle exceptions during server operations
            e.printStackTrace();
        } finally {
            // Ensure all resources are properly closed
            closeConnections();
        }
    }

    private void acceptPlayers() throws IOException {
        // Accept Player 1 connection
        player1Socket = serverSocket.accept();
        player1Out = new ObjectOutputStream(player1Socket.getOutputStream());
        player1In = new ObjectInputStream(player1Socket.getInputStream());
        player1Out.writeObject("You are Player 1.");  // Inform Player 1 of their role
        player1Out.flush();
        System.out.println("Player 1 connected.");

        // Accept Player 2 connection
        player2Socket = serverSocket.accept();
        player2Out = new ObjectOutputStream(player2Socket.getOutputStream());
        player2In = new ObjectInputStream(player2Socket.getInputStream());
        player2Out.writeObject("You are Player 2.");  // Inform Player 2 of their role
        player2Out.flush();
        System.out.println("Player 2 connected.");

        // Initialize game state for both players and the game engine
        player1 = new BattleshipJNI();
        player2 = new BattleshipJNI();
        gameEngine = new GameEngine(player1, player2);
    }

    private void handleShipPlacement() throws IOException {
        // Notify both players to begin ship placement
        String welcomeMessage = """
============================================================
   ____        _   _   _           _     _       
  |  _ \\     | | | | | |         | |   (_)      
  | |_) | __ _| |_| |_| | ___  ___| |__  _ _ __  
  |  _ < / _` | __| __| |/ _ \\/ __| '_ \\| | '_ \\ 
  | |_) | (_| | |_| |_| |  __/\\__ \\ | | | | |_) |
  |____/ \\__,_|\\__|\\__|_|\\___||___/_| |_|_| .__/ 
                                          | |    
  B A T T L E S H I P   M U L T I P L A Y E R   |___|    
============================================================

            Welcome to Battleship Multiplayer!
Engage in a thrilling game of strategy and naval combat! 
Prepare to outwit your opponent and sink their fleet.

------------------------------------------------------------
                       INSTRUCTIONS
------------------------------------------------------------
1) **Each Player Has 2 Grids**:
   - **Self Grid**: Displays your ships and their positions.
   - **Target Grid**: Tracks your attacks on the opponent's ships.

2) **Ships**:
   - Players place 5 ships on the self grid.
   - Ship Types:
     - `carrier`
     - `battleship`
     - `cruiser`
     - `submarine`
     - `destroyer`

3) **Grid Cell States**:
   Each cell in the grid represents the following:
   - `unoccupied = 0`: Empty cell.
   - `occupied = 1`: Cell contains part of a ship.
   - `missed = 2`: Attack missed (no ship in cell).
   - `hit = 3`: Attack hit a ship.

4) **Gameplay**:
   - Players take turns attacking cells on the target grid.
   - If all cells of a ship are hit, it sinks.

5) **Win Condition**:
   - The first player to lose all ships **loses** the game.

6) **Good Luck**:
   - Strategize, attack wisely, and may the best captain win!

============================================================
""";

        sendToBoth(welcomeMessage + "\n");

        // Threads for simultaneous ship placement by both players
        Thread player1PlacementThread = new Thread(() -> {
            try {
                placeShips(player1In, player1Out, player1, 5, 1);  // Player 1 places 5 ships
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error during Player 1 ship placement: " + e.getMessage());
            }
        });

        Thread player2PlacementThread = new Thread(() -> {
            try {
                placeShips(player2In, player2Out, player2, 5, 2);  // Player 2 places 5 ships
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error during Player 2 ship placement: " + e.getMessage());
            }
        });

        // Start ship placement threads
        player1PlacementThread.start();
        player2PlacementThread.start();

        // Wait for both players to finish placing ships
        try {
            player1PlacementThread.join();
            player2PlacementThread.join();
        } catch (InterruptedException e) {
            System.err.println("Error waiting for ship placement threads to finish: " + e.getMessage());
        }

        // Notify both players that ship placement is complete
        sendToBoth("Ships placed successfully. Game starts now!");
    }

    private void placeShips(ObjectInputStream in, ObjectOutputStream out, BattleshipJNI player, int shipCount, int playerNumber)
            throws IOException, ClassNotFoundException {
        int shipsPlaced = 0;  // Counter for placed ships

        // Loop until the required number of ships is placed
        while (shipsPlaced < shipCount) {
            String command = (String) in.readObject();  // Read the command from the player

            if (command == null || command.trim().isEmpty()) {
                out.writeObject("Invalid input. Try again.");  // Handle invalid input
                out.flush();
                continue;
            }

            String[] parts = command.split(" ");  // Parse the command
            if (parts.length != 4) {
                out.writeObject("Invalid format. Provide: type startX startY orientation.");  // Enforce input format
                out.flush();
                continue;
            }

            try {
                // Extract ship details from input
                String type = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int orientation = Integer.parseInt(parts[3]);
                String strOrientation = (orientation == 0) ? "Horizontal" : "Vertical";

                // Attempt to place the ship using the BattleshipJNI API
                int result = player.PlaceShip(type, new int[]{x, y}, orientation);

                if (result == -1) {
                    out.writeObject("Invalid placement. Try again.");  // Invalid placement
                } else {
                    shipsPlaced++;  // Increment the counter for successful placement
                    System.out.println("Player " + playerNumber + " Placed Ship: " + type + ", Starting at: " + "[" + x + "," + y + "]" + ", With Orientation: " + strOrientation);
                    out.writeObject("Ship placed successfully. Your updated self grid:\n" + player.printSelfGrid());  // Show the updated grid
                }
            } catch (NumberFormatException e) {
                out.writeObject("Invalid coordinates or orientation. Please try again.");  // Handle invalid numerical input
            }
            out.flush();
        }
    }

    private void startGame() throws IOException, ClassNotFoundException {
        // Randomly choose the first attacker
        gameEngine.chooseFirstAttacker();
        int currentPlayer = gameEngine.getCurrentPlayer();  // Determine the starting player

        // Main game loop
        while (!player1.HasLost() && !player2.HasLost()) {
            if (currentPlayer == 1) {
                handlePlayerTurn(currentPlayer, player1, player2);  // Handle Player 1's turn
            } else {
                handlePlayerTurn(currentPlayer, player2, player1);  // Handle Player 2's turn
            }
            gameEngine.nextTurn();  // Switch to the next player's turn
            currentPlayer = gameEngine.getCurrentPlayer();  // Update the current player
        }

        // Notify both players that the game is over
        if(player1.HasLost()){
            player2Out.writeObject("Game Over!\nYou have taken down all opponent ships! You Win!");
            player1Out.writeObject("Game Over!\nYou have lost all ships! You Lose...");
            player2Out.flush();
            player1Out.flush();
        }
        else{
            player1Out.writeObject("Game over!\nYou have taken down all opponent ships! You Win!");
            player2Out.writeObject("Game Over!\nYou have lost all ships! You Lose...");
            player1Out.flush();
            player2Out.flush();
        }
    }

    private boolean handlePlayerTurn(int playerNumber, BattleshipJNI currentPlayer, BattleshipJNI opponentPlayer)
            throws IOException, ClassNotFoundException {
        // Determine the input/output streams for the current player and opponent
        ObjectOutputStream playerOut = (playerNumber == 1) ? player1Out : player2Out;
        ObjectInputStream playerIn = (playerNumber == 1) ? player1In : player2In;
        ObjectOutputStream opponentOut = (playerNumber == 1) ? player2Out : player1Out;

        // Prompt the current player to attack
        playerOut.writeObject("Your turn to attack! Enter attack coordinates (row column):");
        playerOut.flush();

        // Notify the opponent that they are waiting
        opponentOut.writeObject("Waiting for Player " + playerNumber + " to attack...");
        opponentOut.flush();

        String attackMessage = (String) playerIn.readObject();  // Read the attack command

        if (attackMessage == null || !attackMessage.matches("\\d+ \\d+")) {
            playerOut.writeObject("Invalid input. Skipping your turn.");  // Handle invalid input
            playerOut.flush();
            return true;  // Skip the turn and continue
        }

        // Parse attack coordinates
        String[] coords = attackMessage.split(" ");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);

        System.out.println("Player " + playerNumber + " attacking (" + x + ", " + y + ")");

        // Perform the attack and process the result
        String result = currentPlayer.Attack(new int[]{x, y}, opponentPlayer);

        if (result.equals("Error: Coordinates out of bounds.")) {
            playerOut.writeObject("Error: Coordinates out of bounds.");
        }
        else if (result.equals("Error: Cannot attack this cell <Already attacked>")) {
            playerOut.writeObject("Error: Cannot attack this cell <Already attacked>");
        }
        else {
            sendToBoth("Attack performed at (" + x + ", " + y + ").");
            opponentOut.writeObject("Opponent attacked your grid at (" + x + ", " + y + ")." + opponentPlayer.printSelfGrid());

            if (result.equals("Miss!")) {
                opponentOut.writeObject("Opponent missed! Your self grid: \n" + opponentPlayer.printSelfGrid());
                playerOut.writeObject("You missed! Your target grid: \n" + currentPlayer.printTargetGrid());

            } else if (result.equals("Hit!")) {
                opponentOut.writeObject("Opponent has hit your ship! Your self grid: \n" + opponentPlayer.printSelfGrid());
                playerOut.writeObject("You have hit a ship! Your target grid: \n" + currentPlayer.printTargetGrid());

            } else if (result.equals("Enemy ship has been taken down!")) {
                opponentOut.writeObject("Your ship has been taken down!");
                playerOut.writeObject("You have taken down an enemy ship!");
            }
        }
        playerOut.flush();
        opponentOut.flush();
        return true;
    }

    private void sendToBoth(String message) throws IOException {
        // Send a message to both players
        player1Out.writeObject(message);
        player2Out.writeObject(message);
        player1Out.flush();
        player2Out.flush();
    }

    private void closeConnections() {
        // Close all connections and resources
        try {
            if (player1Out != null) player1Out.close();
            if (player2Out != null) player2Out.close();
            if (player1In != null) player1In.close();
            if (player2In != null) player2In.close();
            if (player1Socket != null) player1Socket.close();
            if (player2Socket != null) player2Socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

