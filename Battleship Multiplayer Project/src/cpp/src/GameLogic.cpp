#include <bits/stdc++.h>
using namespace std;

// Define the size of the grid and number of ships
constexpr int GRID_SIZE = 10;
constexpr int NUM_SHIPS = 5;

// Enum to represent the state of each grid cell
enum State {
    unoccupied = 0,  // Cell is empty
    occupied = 1,    // Cell is occupied by a ship
    missed = 2,      // Cell was attacked but no ship was hit
    hit = 3          // Cell was attacked, and a ship was hit
};

// Enum to represent different types of ships
enum Type {
    carrier,
    battleship,
    cruiser,
    submarine,
    destroyer
};

// Function to convert a string to the corresponding ship Type enum
Type stringToEnum(const std::string& typeStr) {
    static std::unordered_map<std::string, Type> strToTypeMap = {
        {"carrier", carrier},
        {"battleship", battleship},
        {"cruiser", cruiser},
        {"submarine", submarine},
        {"destroyer", destroyer}
    };

    auto it = strToTypeMap.find(typeStr);
    if (it != strToTypeMap.end()) {
        return it->second; // Return corresponding enum value
    }

    cerr << "Error: Invalid type string '" << typeStr << "'" << endl;
    exit(EXIT_FAILURE); // Exit if the string doesn't match any type
}

// Map to associate ship types with their lengths
unordered_map<Type, int> ship_sizes = {
    {carrier, 5}, {battleship, 4}, {cruiser, 3}, {submarine, 3}, {destroyer, 2}
};

// Structure to represent a ship
struct Ship {
    int orientation; // 0 for horizontal, 1 for vertical
    pair<int, int> start; // Starting position of the ship
    Type type; // Type of the ship

    // Comparison operator to check if two ships are the same
    bool operator==(const Ship& other) const {
        return orientation == other.orientation && start == other.start && type == other.type;
    }
};

// Class to represent a player's grid
class GRID {
public:
    vector<vector<int>> self_grid;    // Grid for player's own ships
    vector<vector<int>> target_grid;  // Grid to track attacks on the opponent

    // Constructor initializes both grids with unoccupied cells
    GRID() {
        self_grid = vector<vector<int>>(10, vector<int>(10, unoccupied));
        target_grid = vector<vector<int>>(10, vector<int>(10, unoccupied));
    }

    // Method to print the player's self grid
    string printSelfGrid() const {
        ostringstream buffer;                 
        buffer << "Self Grid:" << endl;
        buffer << printGrid(self_grid);                 
        return buffer.str();                 
    }

    // Method to print the target grid
    string printTargetGrid() const {
        ostringstream buffer;
        buffer << "Target Grid:" << endl;
        buffer << printGrid(target_grid);
        return buffer.str();
    }

    // Utility method to print a grid (self or target)
    string printGrid(const vector<vector<int>>& grid) const {
        ostringstream buffer;
        for (int row = 0; row < 10; ++row) {
            for (int col = 0; col < 10; ++col) {
                buffer << setw(2) << grid[row][col] << " ";
            }
            buffer << endl;
        }
        return buffer.str();
    }

    // Method to print self and target grids side by side
    string printGridsSideBySide() const {
        ostringstream buffer;
        buffer << "   Self Grid" << setw(39) << "Target Grid" << endl;

        for (int row = 0; row < 10; ++row) {
            // Print a row of the self grid
            for (int col = 0; col < 10; ++col) {
                buffer << setw(2) << self_grid[row][col] << " ";
            }

            // Separator between grids
            buffer << setw(10) << "   ";

            // Print the corresponding row of the target grid
            for (int col = 0; col < 10; ++col) {
                buffer << setw(2) << target_grid[row][col] << " ";
            }

            buffer << endl;
        }
        return buffer.str();
    }
};

// Class to represent a player in the game
class Player {
public:
    vector<Ship> ships;  // List of ships placed by the player
    int num_ships = NUM_SHIPS; // Number of ships remaining
    GRID PlayerGrid; // The player's grid

    Player() = default;

    // Method to place a ship on the player's grid
    int PlaceShip(const string& type, pair<int, int> start, int orientation) {
        // Validate starting position
        if (start.first < 0 || start.first >= GRID_SIZE || start.second < 0 || start.second >= GRID_SIZE) {
            cout << "Cannot place ship: start position out of bounds" << endl;
            return -1;
        }
        // Validate orientation
        if (orientation != 0 && orientation != 1) {
            cout << "Cannot place ship: invalid orientation" << endl;
            return -1;
        }

        Type t = stringToEnum(type); // Convert string to Type enum
        int length = ship_sizes[t]; // Get ship length

        // Check if the ship can be placed without overlapping or going out of bounds
        for (int i = 0; i < length; i++) {
            int row = start.first + (orientation == 1 ? i : 0);
            int col = start.second + (orientation == 0 ? i : 0);

            if (row >= GRID_SIZE || col >= GRID_SIZE || PlayerGrid.self_grid[row][col] == occupied) {
                cout << "Cannot place ship: out of bounds or overlapping" << endl;
                return -1;
            }
        }

        // Place the ship on the grid
        for (int i = 0; i < length; i++) {
            int row = start.first + (orientation == 1 ? i : 0);
            int col = start.second + (orientation == 0 ? i : 0);
            PlayerGrid.self_grid[row][col] = occupied;
        }

        ships.push_back({orientation, start, t}); // Add ship to player's list
        return 1;
    }

    // Method to check if a specific ship is sunk
    bool ShipSunk(Ship& ship) {
        int len = ship_sizes[ship.type];
        for (int i = 0; i < len; ++i) {
            int row = ship.start.first + (ship.orientation == 1 ? i : 0);
            int col = ship.start.second + (ship.orientation == 0 ? i : 0);
            if (PlayerGrid.self_grid[row][col] != hit) {
                return false;
            }
        }

        ships.erase(remove(ships.begin(), ships.end(), ship), ships.end()); // Remove the sunk ship
        return true;
    }

    // Method to attack a cell on the opponent's grid
    string Attack(vector<int> coord, Player& other) {
        // cout << "Attacking coordinates: (" << coord[0] << ", " << coord[1] << ")" << endl;

        // Validate the attack coordinates
        if (coord[0] < 0 || coord[0] >= GRID_SIZE || coord[1] < 0 || coord[1] >= GRID_SIZE) {
            return "Error: Coordinates out of bounds.";
        }
        if (PlayerGrid.target_grid[coord[0]][coord[1]] != unoccupied) {
            return "Error: Cannot attack this cell <Already attacked>";
        }

        // Perform the attack
        if (other.PlayerGrid.self_grid[coord[0]][coord[1]] == unoccupied) {
            PlayerGrid.target_grid[coord[0]][coord[1]] = missed;
            return "Miss!";
        } else if (other.PlayerGrid.self_grid[coord[0]][coord[1]] == occupied) {
            PlayerGrid.target_grid[coord[0]][coord[1]] = hit;
            other.PlayerGrid.self_grid[coord[0]][coord[1]] = hit;

            for (auto& ship : other.ships) {
                if (other.ShipSunk(ship)) {
                    other.num_ships--;
                    return "Enemy ship has been taken down!";
                }
            }
            return "Hit!";
        }
        return "Empty";
    }

    // Method to check if the player has lost all ships
    bool HasLost() const {
        return num_ships == 0;
    }
};

// Uncomment this section to test directly
#ifdef INCLUDE_MAIN
int main() {
    // Initialize two players for testing
    Player player1;
    Player player2;

    // Place some ships for testing
    player1.PlaceShip("carrier", {4, 4}, 0);  // Horizontal placement at (4, 4)
    player2.PlaceShip("battleship", {1, 1}, 1);  // Vertical placement at (1, 1)

    // Print initial grids
    cout << "Player 1 grid:" << endl;
    player1.PlayerGrid.printSelfGrid();
    cout << "Player 2 grid:" << endl;
    player2.PlayerGrid.printSelfGrid();

    // Perform an attack
    player1.Attack({4, 4}, player2); // Attack Player 2 at (4,4)
    player2.PlayerGrid.printTargetGrid();
    player2.PlayerGrid.printGridsSideBySide();

    // Check if player1 has lost
    if (player1.HasLost()) {
        cout << "Player 1 has lost!" << endl;
    } else {
        cout << "Player 1 is still in the game!" << endl;
    }

    return 0;
}
#endif
