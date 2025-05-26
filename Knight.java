import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class Knight extends ChessPiece {
    
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        try {
            pieceImage = new ImageIcon(getClass().getResource("/svg/" + color + "n.png")).getImage();
        } catch (Exception e) {
            pieceImage = null;
        }
    }

    @Override
    public Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col) {
        Set<Point> legalMoves = new HashSet<>();
        
        // Knight moves in L-shape: 2 squares in one direction and 1 square perpendicular
        int[] rowOffsets = {-2, -2, -1, -1, 1, 1, 2, 2};
        int[] colOffsets = {-1, 1, -2, 2, -2, 2, -1, 1};
        
        for (int i = 0; i < 8; i++) {
            int newRow = row + rowOffsets[i];
            int newCol = col + colOffsets[i];
            
            if (isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = board[newRow][newCol];
                if (targetPiece == null || isOpponentPiece(targetPiece)) {
                    legalMoves.add(new Point(newCol, newRow));
                }
            }
        }
        
        return legalMoves;
    }
} 