import java.util.Random;

public class GameEngine {
    // Flags to track if each player has placed their ships and is ready
    private boolean player1Ready = false;  // Flag for Player 1 readiness
    private boolean player2Ready = false;  // Flag for Player 2 readiness
    private boolean gameStarted = false;  // Flag to indicate if the game has started
    private int currentPlayer = 1;  // Variable to track the current player's turn (1 or 2)

    // Object used as a lock for synchronizing readiness-related operations
    private final Object readinessLock = new Object();

    // BattleshipJNI objects to represent Player 1 and Player 2
    private BattleshipJNI player1;
    private BattleshipJNI player2;

    // Constructor to initialize the game engine with two players
    public GameEngine(BattleshipJNI player1, BattleshipJNI player2) {
        this.player1 = player1;  // Assign Player 1's BattleshipJNI object
        this.player2 = player2;  // Assign Player 2's BattleshipJNI object
        this.currentPlayer = 1;  // Initialize with Player 1's turn
    }

    // Method to start the game; waits for both players to be ready
    public void startGame() {
        synchronized (readinessLock) {
            try {
                // Wait until both players have placed their ships
                while (!player1Ready || !player2Ready) {
                    readinessLock.wait();  // Release the lock and wait for a notification
                }
                gameStarted = true;  // Mark the game as started
                System.out.println("Game has started!");

                // Randomly decide which player attacks first
                chooseFirstAttacker();
            } catch (InterruptedException e) {
                e.printStackTrace();  // Handle interruption while waiting
            }
        }
    }

    // Randomly chooses which player will attack first
    public void chooseFirstAttacker() {
        Random random = new Random();
        currentPlayer = random.nextInt(2) + 1;  // Generate either 1 (Player 1) or 2 (Player 2)
        System.out.println("Player " + currentPlayer + " will attack first.");
    }

    // Marks a player as ready and notifies waiting threads
    public void playerReady(int playerNumber) {
        synchronized (readinessLock) {
            if (playerNumber == 1) {
                player1Ready = true;  // Set Player 1 as ready
                System.out.println("Player 1 is ready.");
            } else if (playerNumber == 2) {
                player2Ready = true;  // Set Player 2 as ready
                System.out.println("Player 2 is ready.");
            }
            readinessLock.notifyAll();  // Notify all threads waiting on readinessLock
        }
    }

    // Checks if the game has started
    public boolean hasGameStarted() {
        synchronized (readinessLock) {
            return gameStarted;  // Return the gameStarted flag
        }
    }

    // Moves to the next turn, switching the current player
    public void nextTurn() {
        synchronized (readinessLock) {
            do {
                // Toggle currentPlayer between 1 and 2
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            } while (getCurrentPlayerObject() == null || hasPlayerLost(getCurrentPlayerObject()));
            // Skip turns for players who have lost, ensuring only active players take turns
            readinessLock.notifyAll();  // Notify all threads waiting on readinessLock
        }
    }

    // Returns the current player's number (1 or 2)
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    // Checks if a specific player has lost the game
    public boolean hasPlayerLost(BattleshipJNI player) {
        return player.HasLost();  // Calls the HasLost() method from BattleshipJNI
    }

    // Returns the BattleshipJNI object for the current player
    private BattleshipJNI getCurrentPlayerObject() {
        return (currentPlayer == 1) ? player1 : player2;  // Return player1 or player2 based on currentPlayer
    }

    // Checks if the game has ended by verifying if either player has lost
    public boolean checkGameEnd() {
        if (player1.HasLost()) {
            // If Player 1 has lost, Player 2 wins
            System.out.println("Player 2 wins!");
            return true;
        }
        if (player2.HasLost()) {
            // If Player 2 has lost, Player 1 wins
            System.out.println("Player 1 wins!");
            return true;
        }
        return false;  // Return false if neither player has lost
    }
}

