public class BattleshipJNI {
    // Load the shared library
    static {
        try {
            // Get the absolute path of the DLL file in the bin folder
            String libraryPath = System.getProperty("user.dir") + "/bin/BattleshipGame.dll";
            System.load(libraryPath); // Load the DLL directly
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load BattleshipGame.dll");
            e.printStackTrace();
        } catch (SecurityException e) {
            System.err.println("Permission denied while loading BattleshipGame.dll");
            e.printStackTrace();
        }
    }     

    // Declare native methods for Player functionality
    public native int PlaceShip(String type, int[] coords, int orientation);
    public native String Attack(int[] coord, BattleshipJNI other);
    public native boolean HasLost();

    // Declare native methods for GRID functionality
    public native String printSelfGrid();
    public native String printTargetGrid();
    public native String printGridsSideBySide();

    // Native method to initialize the player
    private native long initializePlayer();

    // A field that stores the native Player object (this will hold a pointer to the C++ Player object)
    private long player;

    // Constructor to initialize the player on the C++ side
    public BattleshipJNI() {
        this.player = initializePlayer();
    }

    // Getter for the native Player object
    public long getPlayer() {
        return this.player;
    }
}








