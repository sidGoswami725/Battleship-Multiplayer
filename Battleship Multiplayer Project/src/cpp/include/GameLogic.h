#ifndef GAMELOGIC2_H
#define GAMELOGIC2_H

#include <iostream>
#include <iomanip>
#include <vector>
#include <unordered_map>
#include <string>
#include <algorithm>

using namespace std;

constexpr int GRID_SIZE = 10;
constexpr int NUM_SHIPS = 5;

enum State {
    unoccupied = 0,
    occupied = 1,
    missed = 2,
    hit = 3
};

enum Type {
    carrier,
    battleship,
    cruiser,
    submarine,
    destroyer
};

// Utility function to map ship type string to enum
Type stringToEnum(const std::string& typeStr);

extern std::unordered_map<Type, int> ship_sizes;

class GRID {
public:
    std::vector<std::vector<int>> self_grid;
    std::vector<std::vector<int>> target_grid;

    // Constructor to initialize grids
    GRID();

    // Methods to print grids
    string printSelfGrid() const;
    string printTargetGrid() const;
    string printGridsSideBySide() const;

    // Utility method to print a single grid
    string printGrid(const std::vector<std::vector<int>>& grid) const;
};

struct Ship {
    int orientation; // 0 for horizontal, 1 for vertical
    std::pair<int, int> start;
    Type type;

    bool operator==(const Ship& other) const;
};

class Player {
public:
    std::vector<Ship> ships;
    int num_ships;
    GRID PlayerGrid;

    // Constructor
    Player();

    // Method to place a ship on the player's grid
    int PlaceShip(const std::string& type, std::pair<int, int> start, int orientation);

    // Method to check if a ship is sunk
    bool ShipSunk(Ship& ship);

    // Method to perform an attack on another player
    string Attack(std::vector<int> coord, Player& other);

    // Method to check if the player has lost
    bool HasLost() const;
};

#endif // GAMELOGIC2_H


