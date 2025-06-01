@echo off
echo Building Chess Game...

if not exist "src\main\resources\stockfish\stockfish-windows-x86-64.exe" (
    echo Error: Stockfish binary not found!
    echo Please download stockfish-windows-x86-64.exe from:
    echo https://github.com/official-stockfish/Stockfish/releases/tag/sf_16
    echo and place it in src\main\resources\stockfish\
    exit /b 1
)

mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b 1
)

echo Build successful!
echo Running game...
java -jar target\chess-game-1.0-SNAPSHOT-jar-with-dependencies.jar 