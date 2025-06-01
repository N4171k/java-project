#!/bin/bash

echo "Building Chess Game..."

# Determine the appropriate Stockfish binary name
if [[ "$OSTYPE" == "darwin"* ]]; then
    BINARY="stockfish-mac-x86-64"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    BINARY="stockfish-linux-x86-64"
else
    echo "Unsupported operating system: $OSTYPE"
    exit 1
fi

if [ ! -f "src/main/resources/stockfish/$BINARY" ]; then
    echo "Error: Stockfish binary not found!"
    echo "Please download $BINARY from:"
    echo "https://github.com/official-stockfish/Stockfish/releases/tag/sf_16"
    echo "and place it in src/main/resources/stockfish/"
    exit 1
fi

# Make the Stockfish binary executable
chmod +x "src/main/resources/stockfish/$BINARY"

mvn clean package

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"
echo "Running game..."
java -jar target/chess-game-1.0-SNAPSHOT-jar-with-dependencies.jar 