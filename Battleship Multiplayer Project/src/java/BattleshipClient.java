import java.io.*;
import java.net.*;
import java.util.Scanner;

public class BattleshipClient {
    // Server address and port to connect to
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    // Socket and streams for communication with the server
    private Socket socket;
    private ObjectOutputStream objectOut;           // Output stream for sending objects to the server
    private ObjectInputStream objectIn;            // Input stream for receiving objects from the server
    private Scanner scanner;                       // Scanner for user input

    // Entry point for the Battleship client
    public static void main(String[] args) {
        new BattleshipClient().startGame();        // Start the game by creating an instance and calling startGame()
    }

    // Main method to handle the game flow
    public void startGame() {
        try {
            // Connect to the server and initialize input/output streams
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());

            // Initialize scanner for user input
            scanner = new Scanner(System.in);

            // Receive and display the player's role from the server
            String role = (String) objectIn.readObject(); // "You are Player 1." or "You are Player 2."
            System.out.println(role);

            // Handle the sequence of game phases
            welcomeAndPlaceShips();  // Ship placement phase
            waitForGameStart();      // Wait for game start signal
            playTurns();             // Main gameplay loop

        } catch (IOException | ClassNotFoundException e) {
            // Handle connection errors or invalid responses
            System.err.println("Error: Unable to connect to the server.");
            e.printStackTrace();
        } finally {
            // Ensure connections are properly closed
            closeConnections();
            if (scanner != null) scanner.close(); // Close scanner resource
        }
    }

    // Handles the welcome message and ship placement phase
    private void welcomeAndPlaceShips() throws IOException, ClassNotFoundException {
        System.out.println((String) objectIn.readObject()); // Welcome message from the server

        int shipsPlaced = 0; // Counter for successfully placed ships

        // Loop until all 5 ships are placed
        while (shipsPlaced < 5) {
            System.out.println("Enter ship type, startX, startY, and orientation (0: horizontal, 1: vertical):");
            String type = scanner.next();             // Read ship type (e.g., "Battleship")
            int startX = scanner.nextInt();           // Read starting X-coordinate
            int startY = scanner.nextInt();           // Read starting Y-coordinate
            int orientation = scanner.nextInt();      // Read orientation (0 for horizontal, 1 for vertical)

            // Send placement command to the server
            objectOut.writeObject(type + " " + startX + " " + startY + " " + orientation);
            objectOut.flush(); // Ensure the data is sent immediately

            // Receive server's response and print it
            String response = (String) objectIn.readObject();
            System.out.println(response);

            // Check if the placement was successful
            if (response.contains("Ship placed successfully. Your updated self grid:\n")) {
                shipsPlaced++; // Increment the counter for each successful placement
            } else if (response.equals("Invalid placement. Try again.")) {
                System.out.println("Placement failed. Retrying...");
            } else {
                System.out.println("Unexpected server response: " + response); // Handle unexpected messages
            }
        }
    }

    // Wait for the game start signal from the server
    private void waitForGameStart() throws IOException, ClassNotFoundException {
        String message = (String) objectIn.readObject(); // Receive "Game is starting!" message
        System.out.println(message);
    }

    // Main gameplay loop where turns are taken
    private void playTurns() throws IOException, ClassNotFoundException {
        String message; // Variable to hold messages from the server
        scanner.nextLine(); // Clear any leftover input from the scanner

        // Loop until the server signals the end of the game
        while ((message = (String) objectIn.readObject()) != null) {
            System.out.println("Server: " + message); // Display server's message

            // If it's the player's turn, process their attack input
            if (message.contains("Your turn")) {
                int row = -1, col = -1;
                boolean validInput = false;

                // Loop until valid coordinates are provided
                while (!validInput) {
                    String input = scanner.nextLine().trim(); // Read and trim input

                    // Validate input format (two integers separated by space)
                    if (input.matches("\\d+ \\d+")) {
                        String[] parts = input.split(" ");   // Split input into row and column
                        row = Integer.parseInt(parts[0]);
                        col = Integer.parseInt(parts[1]);
                        validInput = true; // Mark input as valid
                    } else {
                        System.out.println("Invalid format. Please enter coordinates in the format row column.");
                    }
                }

                // Send attack coordinates to the server
                objectOut.writeObject(row + " " + col);
                objectOut.flush(); // Ensure the data is sent immediately

                // Receive and display the result of the attack
                String attackResult = (String) objectIn.readObject();
                System.out.println("Server: " + attackResult);
            }

            // If the server signals game over, display the final message and exit loop
            if (message.contains("Game Over")) {
                System.out.println((String) objectIn.readObject()); // Read and display the winner message
                break;
            }
        }
    }

    // Closes all connections and resources
    private void closeConnections() {
        try {
            if (objectOut != null) objectOut.close(); // Close output stream
            if (objectIn != null) objectIn.close();   // Close input stream
            if (socket != null) socket.close();       // Close socket connection
        } catch (IOException e) {
            System.err.println("Error closing connections.");
            e.printStackTrace(); // Print stack trace for debugging
        }
    }
}

