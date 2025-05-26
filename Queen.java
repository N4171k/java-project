import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class Queen extends ChessPiece {
    
    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        try {
            pieceImage = new ImageIcon(getClass().getResource("/svg/" + color + "q.png")).getImage();
        } catch (Exception e) {
            pieceImage = null;
        }
    }

    @Override
    public Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col) {
        Set<Point> legalMoves = new HashSet<>();
        
        // Queen moves like a rook and bishop combined
        // Horizontal and vertical moves (like a rook)
        int[] rookDirections = {0, 1, 0, -1, 1, 0, -1, 0};
        // Diagonal moves (like a bishop)
        int[] bishopDirections = {1, 1, 1, -1, -1, 1, -1, -1};
        
        // Check rook-like moves
        for (int i = 0; i < 8; i += 2) {
            int rowDir = rookDirections[i];
            int colDir = rookDirections[i + 1];
            checkDirection(board, row, col, rowDir, colDir, legalMoves);
        }
        
        // Check bishop-like moves
        for (int i = 0; i < 8; i += 2) {
            int rowDir = bishopDirections[i];
            int colDir = bishopDirections[i + 1];
            checkDirection(board, row, col, rowDir, colDir, legalMoves);
        }
        
        return legalMoves;
    }

    private void checkDirection(ChessPiece[][] board, int row, int col, int rowDir, int colDir, Set<Point> moves) {
        int newRow = row + rowDir;
        int newCol = col + colDir;
        
        while (isValidPosition(newRow, newCol)) {
            ChessPiece targetPiece = board[newRow][newCol];
            if (targetPiece == null) {
                moves.add(new Point(newCol, newRow));
            } else if (isOpponentPiece(targetPiece)) {
                moves.add(new Point(newCol, newRow));
                break;
            } else {
                break;
            }
            newRow += rowDir;
            newCol += colDir;
        }
    }
} 