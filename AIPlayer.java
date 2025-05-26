import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.awt.Point;

public class AIPlayer {
    private int difficultyLevel;
    private boolean isWhite;
    private Random random;
    private ChessBoard board;
    private static final int MAX_DEPTH = 3;

    public AIPlayer(int difficultyLevel, boolean isWhite) {
        this.difficultyLevel = difficultyLevel;
        this.isWhite = isWhite;
        this.random = new Random();
        this.board = new ChessBoard(null); // Create a temporary board for move validation
    }

    public String getNextMove(List<String> movesHistory) {
        try {
            // Update board with current position
            updateBoard(movesHistory);
            
            // Get all possible moves
            List<Move> possibleMoves = getAllPossibleMoves();
            if (possibleMoves.isEmpty()) {
                System.err.println("No legal moves available");
                return null;
            }

            // Select move based on difficulty level
            Move selectedMove;
            if (difficultyLevel <= 3) {
                // Random move for lower difficulties
                selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            } else {
                // Use minimax for higher difficulties
                selectedMove = getBestMove(possibleMoves);
            }

            // Convert to our notation
            return convertToNotation(selectedMove);
        } catch (Exception e) {
            System.err.println("Error getting AI move: " + e.getMessage());
            e.printStackTrace();
            return getDefaultMove();
        }
    }

    private void updateBoard(List<String> movesHistory) {
        // Reset board
        board = new ChessBoard(null);
        
        // Apply all moves from history
        for (String move : movesHistory) {
            try {
                Point from = parseSquare(move.substring(0, 2));
                Point to = parseSquare(move.substring(2, 4));
                board.makeMove(from.y, from.x, to.y, to.x);
            } catch (Exception e) {
                System.err.println("Error applying move to board: " + move);
            }
        }
    }

    private List<Move> getAllPossibleMoves() {
        List<Move> moves = new ArrayList<>();
        ChessPiece[][] boardState = board.getBoard();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = boardState[row][col];
                if (piece != null && piece.isWhite() == isWhite) {
                    Set<Point> legalMoves = piece.getLegalMoves(boardState, row, col);
                    for (Point move : legalMoves) {
                        moves.add(new Move(new Point(col, row), move, piece));
                    }
                }
            }
        }
        
        return moves;
    }

    private Move getBestMove(List<Move> moves) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Move move : moves) {
            // Make the move
            Point from = move.from;
            Point to = move.to;
            board.makeMove(from.y, from.x, to.y, to.x);
            
            // Evaluate position
            int score = -minimax(MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            
            // Undo the move
            board.undoMove();
            
            // Update best move if this is better
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove != null ? bestMove : moves.get(random.nextInt(moves.size()));
    }

    private int minimax(int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0) {
            return evaluatePosition();
        }

        List<Move> moves = getAllPossibleMoves();
        if (moves.isEmpty()) {
            return evaluatePosition();
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Point from = move.from;
                Point to = move.to;
                board.makeMove(from.y, from.x, to.y, to.x);
                int eval = minimax(depth - 1, alpha, beta, false);
                board.undoMove();
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Point from = move.from;
                Point to = move.to;
                board.makeMove(from.y, from.x, to.y, to.x);
                int eval = minimax(depth - 1, alpha, beta, true);
                board.undoMove();
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private int evaluatePosition() {
        int score = 0;
        ChessPiece[][] boardState = board.getBoard();
        
        // Material evaluation
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = boardState[row][col];
                if (piece != null) {
                    int value = getPieceValue(piece);
                    score += piece.isWhite() == isWhite ? value : -value;
                }
            }
        }
        
        // Position evaluation
        score += evaluatePosition(boardState);
        
        return score;
    }

    private int getPieceValue(ChessPiece piece) {
        if (piece instanceof Pawn) return 100;
        if (piece instanceof Knight) return 320;
        if (piece instanceof Bishop) return 330;
        if (piece instanceof Rook) return 500;
        if (piece instanceof Queen) return 900;
        if (piece instanceof King) return 20000;
        return 0;
    }

    private int evaluatePosition(ChessPiece[][] boardState) {
        int score = 0;
        
        // Center control bonus
        for (int row = 3; row <= 4; row++) {
            for (int col = 3; col <= 4; col++) {
                ChessPiece piece = boardState[row][col];
                if (piece != null) {
                    score += piece.isWhite() == isWhite ? 10 : -10;
                }
            }
        }
        
        // Development bonus
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = boardState[isWhite ? 1 : 6][col];
            if (piece == null) {
                score += isWhite ? 5 : -5;
            }
        }
        
        return score;
    }

    private Point parseSquare(String square) {
        int col = square.charAt(0) - 'a';
        int row = 8 - (square.charAt(1) - '0');
        return new Point(col, row);
    }

    private String convertToNotation(Move move) {
        Point from = move.from;
        Point to = move.to;
        return String.format("%c%d%c%d", 
            'a' + from.x, 8 - from.y,
            'a' + to.x, 8 - to.y);
    }

    private String getDefaultMove() {
        return isWhite ? "e2e4" : "e7e5";
    }

    public void setDifficultyLevel(int level) {
        if (level < 1 || level > 10) {
            throw new IllegalArgumentException("Difficulty level must be between 1 and 10");
        }
        this.difficultyLevel = level;
    }

    private static class Move {
        Point from;
        Point to;
        ChessPiece piece;

        Move(Point from, Point to, ChessPiece piece) {
            this.from = from;
            this.to = to;
            this.piece = piece;
        }
    }
}
