# Java Chess Game

A feature-rich chess game implemented in Java using Swing, with both player vs player and player vs AI modes.

## Features

- Player vs Player mode
- Player vs AI mode with adjustable difficulty (1-10)
- Legal move highlighting
- Check and checkmate detection
- Pawn promotion
- Castling
- En passant
- Move history
- Optional timed games
- AI powered by Google's Gemini API

## Dependencies

- Java 8 or higher
- org.json library for JSON parsing
- Python 3.x (for downloading chess piece images)

## Setup

1. Make sure you have Java 8 or higher installed
2. Clone this repository
3. Set up your Gemini API key:
   ```bash
   # On Windows
   set GEMINI_API_KEY=your_api_key_here
   
   # On Linux/Mac
   export GEMINI_API_KEY=your_api_key_here
   ```
4. Download chess piece images:
   ```bash
   # Install required Python packages
   pip install requests Pillow
   
   # Run the image download script
   python download_images.py
   ```
5. Compile the game:
   ```bash
   javac -cp ".:org.json.jar" *.java
   ```
6. Run the game:
   ```bash
   java -cp ".:org.json.jar" ChessGame
   ```

## Game Controls

- Click on a piece to select it
- Legal moves will be highlighted
- Click on a highlighted square to make a move
- In AI mode, the AI will automatically make its move after yours

## AI Difficulty Levels

1. Beginner - Makes basic moves
2-4. Intermediate - Shows some strategy
5-7. Advanced - Strong tactical play
8-10. Expert - Strategic and tactical master

## Special Moves

- Castling: Move the king two squares towards a rook, and the rook jumps over
- En Passant: Capture a pawn that has just moved two squares
- Pawn Promotion: Automatically promotes to a queen when reaching the opposite end

## Contributing

Feel free to submit issues and enhancement requests! 