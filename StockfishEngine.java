import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StockfishEngine {
    private static final int[] PIECE_VALUES = {
        100,  // Pawn
        320,  // Knight
        330,  // Bishop
        500,  // Rook
        900,  // Queen
        20000 // King
    };

    private static final int[][] POSITION_VALUES = {
        // Pawn position values
        {0,  0,  0,  0,  0,  0,  0,  0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5,  5, 10, 25, 25, 10,  5,  5},
        {0,  0,  0, 20, 20,  0,  0,  0},
        {5, -5,-10,  0,  0,-10, -5,  5},
        {5, 10, 10,-20,-20, 10, 10,  5},
        {0,  0,  0,  0,  0,  0,  0,  0}
    };

    private Random random;
    private int maxDepth;

    public StockfishEngine() {
        this.random = new Random();
        this.maxDepth = 3;
    }

    public String getBestMove(String fen, int moveTime) {
        // Parse FEN and create board representation
        String[] parts = fen.split(" ");
        String boardState = parts[0];
        boolean isWhiteTurn = parts[1].equals("w");

        // Get all legal moves
        List<Move> legalMoves = getLegalMoves(boardState, isWhiteTurn);
        if (legalMoves.isEmpty()) {
            return null;
        }

        // Evaluate each move
        Move bestMove = null;
        int bestScore = isWhiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            int score = evaluateMove(move, boardState, isWhiteTurn, maxDepth);
            if (isWhiteTurn && score > bestScore) {
                bestScore = score;
                bestMove = move;
            } else if (!isWhiteTurn && score < bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        // If no best move found, return a random legal move
        if (bestMove == null) {
            bestMove = legalMoves.get(random.nextInt(legalMoves.size()));
        }

        return convertToAlgebraicNotation(bestMove);
    }

    private List<Move> getLegalMoves(String boardState, boolean isWhiteTurn) {
        List<Move> moves = new ArrayList<>();
        String[] rows = boardState.split("/");
        
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                char piece = rows[fromRow].charAt(fromCol);
                if ((isWhiteTurn && Character.isUpperCase(piece)) || 
                    (!isWhiteTurn && Character.isLowerCase(piece))) {
                    // Add simple moves (this is a simplified version)
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol, piece)) {
                                moves.add(new Move(fromRow, fromCol, toRow, toCol));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, char piece) {
        // Simplified move validation
        if (fromRow == toRow && fromCol == toCol) return false;
        
        switch (Character.toLowerCase(piece)) {
            case 'p': // Pawn
                return isValidPawnMove(fromRow, fromCol, toRow, toCol, Character.isUpperCase(piece));
            case 'n': // Knight
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case 'b': // Bishop
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case 'r': // Rook
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case 'q': // Queen
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case 'k': // King
                return isValidKingMove(fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, boolean isWhite) {
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;
        
        // Forward move
        if (fromCol == toCol && toRow == fromRow + direction) {
            return true;
        }
        
        // Initial two-square move
        if (fromRow == startRow && fromCol == toCol && toRow == fromRow + 2 * direction) {
            return true;
        }
        
        // Capture moves
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            return true;
        }
        
        return false;
    }

    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) == Math.abs(toCol - fromCol);
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        return fromRow == toRow || fromCol == toCol;
    }

    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return isValidBishopMove(fromRow, fromCol, toRow, toCol) || 
               isValidRookMove(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return rowDiff <= 1 && colDiff <= 1;
    }

    private int evaluateMove(Move move, String boardState, boolean isWhiteTurn, int depth) {
        if (depth == 0) {
            return evaluatePosition(boardState, isWhiteTurn);
        }

        // Make move
        String newBoardState = makeMove(boardState, move);
        
        // Evaluate resulting position
        int score = evaluatePosition(newBoardState, isWhiteTurn);
        
        // Recursive evaluation
        if (depth > 1) {
            List<Move> nextMoves = getLegalMoves(newBoardState, !isWhiteTurn);
            for (Move nextMove : nextMoves) {
                int nextScore = evaluateMove(nextMove, newBoardState, !isWhiteTurn, depth - 1);
                if (isWhiteTurn) {
                    score = Math.max(score, nextScore);
                } else {
                    score = Math.min(score, nextScore);
                }
            }
        }
        
        return score;
    }

    private String makeMove(String boardState, Move move) {
        // Simplified move execution
        String[] rows = boardState.split("/");
        char[] fromRow = rows[move.fromRow].toCharArray();
        char[] toRow = rows[move.toRow].toCharArray();
        
        char piece = fromRow[move.fromCol];
        fromRow[move.fromCol] = '1';
        toRow[move.toCol] = piece;
        
        rows[move.fromRow] = new String(fromRow);
        rows[move.toRow] = new String(toRow);
        
        return String.join("/", rows);
    }

    private int evaluatePosition(String boardState, boolean isWhiteTurn) {
        int score = 0;
        String[] rows = boardState.split("/");
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = rows[row].charAt(col);
                if (piece != '1') {
                    int value = getPieceValue(piece);
                    int positionValue = getPositionValue(piece, row, col);
                    if (Character.isUpperCase(piece) == isWhiteTurn) {
                        score += value + positionValue;
                    } else {
                        score -= value + positionValue;
                    }
                }
            }
        }
        
        return score;
    }

    private int getPieceValue(char piece) {
        switch (Character.toLowerCase(piece)) {
            case 'p': return PIECE_VALUES[0];
            case 'n': return PIECE_VALUES[1];
            case 'b': return PIECE_VALUES[2];
            case 'r': return PIECE_VALUES[3];
            case 'q': return PIECE_VALUES[4];
            case 'k': return PIECE_VALUES[5];
            default: return 0;
        }
    }

    private int getPositionValue(char piece, int row, int col) {
        if (Character.toLowerCase(piece) == 'p') {
            return POSITION_VALUES[row][col];
        }
        return 0;
    }

    private String convertToAlgebraicNotation(Move move) {
        char fromFile = (char)('a' + move.fromCol);
        char fromRank = (char)('8' - move.fromRow);
        char toFile = (char)('a' + move.toCol);
        char toRank = (char)('8' - move.toRow);
        return "" + fromFile + fromRank + toFile + toRank;
    }

    private static class Move {
        int fromRow, fromCol, toRow, toCol;
        
        Move(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
        }
    }
} 