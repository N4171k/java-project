import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class Bishop extends ChessPiece {
    
    public Bishop(boolean isWhite) {
        super(isWhite);
        this.symbol = 'b';
        loadImage();
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        try {
            pieceImage = new ImageIcon(getClass().getResource("/svg/" + color + "b.png")).getImage();
        } catch (Exception e) {
            pieceImage = null;
        }
    }

    @Override
    public Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col) {
        Set<Point> legalMoves = new HashSet<>();
        
        // Bishop moves diagonally
        int[] directions = {1, 1, 1, -1, -1, 1, -1, -1};
        
        for (int i = 0; i < 8; i += 2) {
            int rowDir = directions[i];
            int colDir = directions[i + 1];
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