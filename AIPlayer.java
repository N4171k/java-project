import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.awt.Point;
import java.io.IOException;

public class AIPlayer {
    private ChessBoard board;
    private boolean isWhite;
    private StockfishEngine engine;
    private Random random;
    private int difficulty;

    public AIPlayer(ChessBoard board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
        this.engine = new StockfishEngine();
        this.random = new Random();
        this.difficulty = 3; // Default difficulty level
    }

    public String getNextMove() {
        try {
            // Convert current board state to FEN
            String fen = convertToFEN();
            
            // Get best move from engine
            String bestMove = engine.getBestMove(fen, getMoveTime());
            
            if (bestMove != null) {
                return bestMove;
            }
            
            // Fallback to random move if engine fails
            return getRandomMove();
        } catch (Exception e) {
            System.err.println("Error getting AI move: " + e.getMessage());
            return getRandomMove();
        }
    }

    private String convertToFEN() {
        StringBuilder fen = new StringBuilder();
        
        // Board position
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board.getPieceAt(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    char pieceChar = piece.getSymbol();
                    fen.append(pieceChar);
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 7) {
                fen.append('/');
            }
        }
        
        // Active color
        fen.append(' ').append(board.isWhiteTurn() ? 'w' : 'b');
        
        // Castling availability
        fen.append(" KQkq");
        
        // En passant target square
        fen.append(" -");
        
        // Halfmove clock and fullmove number
        fen.append(" 0 1");
        
        return fen.toString();
    }

    private String getRandomMove() {
        // Get all legal moves
        java.util.List<Point> legalMoves = new java.util.ArrayList<>();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                ChessPiece piece = board.getPieceAt(fromRow, fromCol);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (piece.isValidMove(fromRow, fromCol, toRow, toCol, board)) {
                                legalMoves.add(new Point(fromCol, fromRow));
                                legalMoves.add(new Point(toCol, toRow));
                            }
                        }
                    }
                }
            }
        }
        
        if (legalMoves.isEmpty()) {
            return null;
        }
        
        // Select random move
        int moveIndex = random.nextInt(legalMoves.size() / 2) * 2;
        Point from = legalMoves.get(moveIndex);
        Point to = legalMoves.get(moveIndex + 1);
        
        return convertToAlgebraicNotation(from, to);
    }

    private String convertToAlgebraicNotation(Point from, Point to) {
        char fromFile = (char)('a' + from.x);
        char fromRank = (char)('8' - from.y);
        char toFile = (char)('a' + to.x);
        char toRank = (char)('8' - to.y);
        return "" + fromFile + fromRank + toFile + toRank;
    }

    private int getMoveTime() {
        // Adjust thinking time based on difficulty
        switch (difficulty) {
            case 1: return 500;  // Easy
            case 2: return 1000; // Medium
            case 3: return 2000; // Hard
            default: return 1000;
        }
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(3, difficulty));
    }
}
