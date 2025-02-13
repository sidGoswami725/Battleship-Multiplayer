#include <jni.h>
#include "../include/BattleshipJNI.h" // Header file for the JNI class
#include "../include/GameLogic.h"     // Header file containing game logic and Player class
#include <iostream>
#include <vector>

using namespace std;

// Global reference to the Java field ID for accessing the native Player object
jfieldID fieldID;

// Helper function to retrieve the Player object from a Java object
Player* getPlayer(JNIEnv* env, jobject playerObj) {
    if (playerObj == nullptr) {
        cerr << "Error: Player object is null." << endl;
        return nullptr;
    }
    // Get the long value from the player field and cast it to a Player pointer
    return reinterpret_cast<Player*>(env->GetLongField(playerObj, fieldID));
}

// JNI method to initialize the Player object and associate it with a Java instance
JNIEXPORT jlong JNICALL Java_BattleshipJNI_initializePlayer(JNIEnv* env, jobject obj) {
    // Allocate a new Player object
    Player* newPlayer = new Player();
    if (newPlayer == nullptr) {
        cerr << "Error: Failed to allocate memory for Player object." << endl;
        return 0;
    }
    // Store the pointer in the Java object's player field
    env->SetLongField(obj, fieldID, reinterpret_cast<jlong>(newPlayer));
    return reinterpret_cast<jlong>(newPlayer);
}

// JNI method to place a ship on the player's grid
JNIEXPORT jint JNICALL Java_BattleshipJNI_PlaceShip(JNIEnv* env, jobject obj, jstring shipType, jintArray coords, jint orientation) {
    Player* player = getPlayer(env, obj); // Retrieve the native Player object
    if (player == nullptr) return -1;

    // Validate the coordinates array length
    jsize len = env->GetArrayLength(coords);
    if (len != 2) {
        cerr << "Error: Invalid coordinates length for ship placement." << endl;
        return -1;
    }

    // Retrieve the coordinates array elements
    jint* coordsArray = env->GetIntArrayElements(coords, nullptr);
    if (coordsArray == nullptr) {
        cerr << "Error: Failed to retrieve coordinates array elements." << endl;
        return -1;
    }
    pair<int, int> start = {coordsArray[0], coordsArray[1]}; // Convert to a pair
    env->ReleaseIntArrayElements(coords, coordsArray, JNI_ABORT); // Release array memory

    // Convert ship type from Java string to C++ string
    const char* typeChars = env->GetStringUTFChars(shipType, nullptr);
    if (typeChars == nullptr) {
        cerr << "Error: Failed to retrieve ship type string." << endl;
        return -1;
    }
    string type(typeChars);
    env->ReleaseStringUTFChars(shipType, typeChars); // Release memory for string

    // Call the C++ PlaceShip method and handle the result
    int result = player->PlaceShip(type, start, orientation);
    if (result == -1) {
        cerr << "Error: Ship placement failed for type " << type << " at (" 
             << start.first << ", " << start.second << ") with orientation " << orientation << endl;
    }
    return result;
}

// JNI method to perform an attack on the opponent's grid
JNIEXPORT jstring JNICALL Java_BattleshipJNI_Attack(JNIEnv* env, jobject obj, jintArray coord, jobject opponent) {
    // Validate the opponent object
    if (opponent == nullptr) {
        cerr << "Error: Opponent object is null." << endl;
        return env->NewStringUTF("Error: Opponent object is null.");
    }

    // Retrieve the opponent's Player object using the field ID
    jclass opponentClass = env->GetObjectClass(opponent);
    jfieldID playerField = env->GetFieldID(opponentClass, "player", "J");
    if (playerField == nullptr) {
        cerr << "Error: Could not find 'player' field in BattleshipJNI class." << endl;
        return env->NewStringUTF("Error: Opponent's player field not found.");
    }
    jlong playerPointer = env->GetLongField(opponent, playerField);
    Player* opponentPlayer = reinterpret_cast<Player*>(playerPointer);
    if (opponentPlayer == nullptr) {
        cerr << "Error: Opponent's player is null." << endl;
        return env->NewStringUTF("Error: Opponent's player is null.");
    }

    // Retrieve the attack coordinates array
    jint* coordArray = env->GetIntArrayElements(coord, nullptr);
    if (coordArray == nullptr) {
        cerr << "Error: Failed to get attack coordinates." << endl;
        return env->NewStringUTF("Error: Failed to get attack coordinates.");
    }
    vector<int> coordinates = {coordArray[0], coordArray[1]}; // Convert to vector
    env->ReleaseIntArrayElements(coord, coordArray, JNI_ABORT); // Release array memory

    // Validate the attack coordinates
    if (coordinates[0] < 0 || coordinates[0] >= GRID_SIZE || coordinates[1] < 0 || coordinates[1] >= GRID_SIZE) {
        cerr << "Error: Attack coordinates out of bounds." << endl;
        return env->NewStringUTF("Error: Attack coordinates out of bounds.");
    }

    // Get the Player pointer for the current object
    Player* player = getPlayer(env, obj);
    if (player == nullptr) {
        cerr << "Error: Current player is null." << endl;
        return env->NewStringUTF("Error: Current player is null.");
    }

    // Perform the attack and capture the result
    try {
        std::string result = player->Attack(coordinates, *opponentPlayer);
        cerr << "Attack executed at coordinates: (" << coordinates[0] << ", " << coordinates[1] << ") with result: " << result << endl;
        return env->NewStringUTF(result.c_str()); // Return result as a Java string
    } catch (const exception& e) {
        cerr << "Error during attack: " << e.what() << endl;
        return env->NewStringUTF(("Error during attack: " + std::string(e.what())).c_str());
    }
}

// JNI method to check if the player has lost
JNIEXPORT jboolean JNICALL Java_BattleshipJNI_HasLost(JNIEnv* env, jobject obj) {
    Player* player = getPlayer(env, obj);
    if (player == nullptr) return JNI_FALSE;
    return player->HasLost() ? JNI_TRUE : JNI_FALSE;
}

// --- GRID Methods ---
// These methods allow grids to be printed via JNI

// JNI method to print the player's self grid
JNIEXPORT jstring JNICALL Java_BattleshipJNI_printSelfGrid(JNIEnv* env, jobject obj) {
    Player* player = getPlayer(env, obj);
    if (player != nullptr) {
        string result = player->PlayerGrid.printSelfGrid();
        return env->NewStringUTF(result.c_str());
    } else {
        cerr << "Error: Current player is null." << endl;
        return env->NewStringUTF("Error: Current player is null.");
    }
}

// JNI method to print the player's target grid
JNIEXPORT jstring JNICALL Java_BattleshipJNI_printTargetGrid(JNIEnv* env, jobject obj) {
    Player* player = getPlayer(env, obj);
    if (player != nullptr) {
        string result = player->PlayerGrid.printTargetGrid();
        return env->NewStringUTF(result.c_str());
    } else {
        cerr << "Error: Current player is null." << endl;
        return env->NewStringUTF("Error: Current player is null.");
    }
}

// JNI method to print both grids side by side
JNIEXPORT jstring JNICALL Java_BattleshipJNI_printGridsSideBySide(JNIEnv* env, jobject obj) {
    Player* player = getPlayer(env, obj);
    if (player != nullptr) {
        string result = player->PlayerGrid.printGridsSideBySide();
        return env->NewStringUTF(result.c_str());
    } else {
        cerr << "Error: Current player is null." << endl;
        return env->NewStringUTF("Error: Current player is null.");
    }
}

// Cleanup function to delete the Player object
JNIEXPORT void JNICALL Java_BattleshipJNI_cleanupPlayer(JNIEnv* env, jobject obj) {
    Player* player = getPlayer(env, obj);
    if (player != nullptr) {
        delete player; // Properly delete the player object to avoid memory leaks
    }
}

// JNI initialization function (called when the JNI library is loaded)
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // Find the BattleshipJNI class and the 'player' field
    jclass clazz = env->FindClass("BattleshipJNI");
    if (clazz == nullptr) return JNI_ERR;

    fieldID = env->GetFieldID(clazz, "player", "J");
    if (fieldID == nullptr) return JNI_ERR;

    return JNI_VERSION_1_6; // Return JNI version on successful initialization
}


