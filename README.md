# Chess Game

## Introduction
This project is a Chess Game implemented in Java using Swing for the graphical user interface. The game allows two players to play against each other on a standard 8x8 chessboard.

## Technologies Used
* Java Development Kit (JDK)
* Swing (for GUI)
* `javax.sound.sampled`: For playing sound effects during gameplay.

## Project Structure
* `src/main/java/com/chess/`: Contains all the Java source code for the chess game.
  * `ChessGame.java`: The main entry point of the application. It sets up the JFrame, integrates the `ChessBoard` and move history display, and handles overall game flow, including menu bar functionalities, game reset, and updating move history.
  * `ChessBoard.java`: This is the core class responsible for managing the chessboard's state, drawing the board and pieces, handling user input (mouse clicks), and implementing complex chess logic. It manages piece selection, legal move generation, move execution, and special chess rules like castling, en passant, pawn promotion, check, and checkmate detection. It also includes functionality for undoing moves and converting board states to Algebraic Notation and FEN strings.
  * `ChessPiece.java`: An abstract base class for all chess pieces. It defines common properties like color and provides an abstract method `getLegalMoves` that concrete piece classes must implement. It also handles drawing the piece images.
  * Individual piece classes (e.g., `Rook.java`, `Queen.java`, `Pawn.java`, `Knight.java`, `King.java`, `Bishop.java`): These classes extend `ChessPiece.java` and implement the specific movement rules and behaviors for each respective chess piece, including special moves like pawn's first move double step, king's castling, and pawn promotion.
* `org.json-1.6-20240205.jar`, `org.json.jar`: JAR files likely used for JSON processing. While present in the directory, their direct utilization within the current game logic (`ChessGame.java`, `ChessBoard.java`) for game state saving/loading or other functionalities is not explicitly observed in the provided code snippets.
* `src/main/resources/sounds/`: Contains `.wav` sound files used for various game events (e.g., piece moves, captures, castling, check).
* `src/main/resources/chess_icon.png`: The application icon.

## Key Features
* **Player vs. Player (PVP) game mode**: Supports two human players taking turns to move pieces.
* **Interactive Graphical User Interface (GUI)**: Built with Swing, featuring a visually appealing chessboard with alternating square colors and draggable/clickable pieces.
* **Piece Movement and Validation**: Implements the legal moves for all standard chess pieces, considering blocking pieces, and validating moves to ensure they do not leave the king in check.
* **Move History Display**: A scrollable text area on the GUI that logs all moves in standard Algebraic Notation.
* **Special Chess Rules**:
    * **Castling**: Correctly handles both kingside and queenside castling, including validating the path and king's safety.
    * **En Passant**: Implements the en passant capture rule for pawns.
    * **Pawn Promotion**: Automatically promotes pawns to Queens upon reaching the last rank.
* **Check and Checkmate Detection**: Identifies when a king is in check and highlights the king's square. It also determines if a checkmate has occurred, indicating the end of the game.
* **Undo Move Functionality**: Allows players to undo the last move, restoring the board to its previous state. This feature uses a stack to store `MoveState` objects, enabling a robust undo mechanism.
* **Algebraic Notation Conversion**: Converts moves into standard Algebraic Notation for display in the move history.
* **FEN String Generation**: Capable of generating a FEN (Forsyth-Edwards Notation) string representing the current board state, including active color, castling availability, en passant target square, halfmove clock, and fullmove number.
* **Sound Effects**: Plays distinct sound effects for piece movements, captures, castling, and checks, enhancing the user experience.
* **Game Reset**: Provides a "New Game" option to reset the board to its initial setup.

## Game Logic Highlights (from `ChessBoard.java`)
* **`initializeBoard()`**: Sets up the initial positions of all chess pieces on the board.
* **`paintComponent(Graphics g)`**: Overrides the JPanel's paint method to draw the chessboard squares, highlight selected squares and legal moves, and render the chess pieces.
* **`ChessMouseListener`**: An inner class that handles mouse click events on the chessboard, translating clicks into row and column selections.
* **`handleSquareClick(int row, int col)`**: Manages piece selection and initiates move validation. It highlights the selected piece and its legal moves.
* **`getValidMoves(ChessPiece piece, int row, int col)`**: Filters all possible moves for a given piece to ensure they are legal (i.e., do not result in the king being in check).
* **`isMoveValid(int fromRow, int fromCol, int toRow, int toCol)`**: Performs a temporary move to check if the current player's king would be in check after the move.
* **`makeMove(Point from, Point to)`**: Executes a valid move on the board, updates the board state, handles special moves (castling, en passant, promotion), updates halfmove and fullmove clocks, plays sound effects, and adds the move to the history. It also checks for check and checkmate after each move.
* **`isCheck()` and `isCheckmate()`**: Methods to determine if the current player's king is in check or checkmate, respectively.
* **`undoMove()`**: Reverts the last move by popping the `MoveState` from the history stack and restoring the previous board configuration and game state (turn, pawn's last double move).
* **`convertToAlgebraicNotation(Point from, Point to)`**: Generates the Algebraic Notation string for a given move, including special handling for captures, castling, and check/checkmate symbols.
* **`convertToFEN()`**: Constructs the FEN string, a standard notation for describing a particular board position.
* **`canCastleKingside(boolean isWhite)` and `canCastleQueenside(boolean isWhite)`**: Logic to determine if castling is possible for a given side, checking for king and rook movement status and clear paths.

## How to Run
This is a Java Swing application. To run the game, compile the Java source files and execute the `ChessGame` class. A `main` method is provided in `ChessGame.java` to start the application. Ensure all necessary JAR files (like `org.json.jar`) are included in the classpath and that the `resources` directory (containing sounds and the application icon) is accessible. 