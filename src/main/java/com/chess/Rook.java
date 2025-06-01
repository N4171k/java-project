package com.chess;

import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class Rook extends ChessPiece {
    private boolean hasMoved;

    public Rook(boolean isWhite) {
        super(isWhite);
        this.symbol = 'r';
        hasMoved = false;
        loadImage();
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        try {
            pieceImage = new ImageIcon(getClass().getResource("/svg/" + color + "r.png")).getImage();
        } catch (Exception e) {
            pieceImage = null;
        }
    }

    @Override
    public Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col) {
        Set<Point> legalMoves = new HashSet<>();
        
        // Rook moves horizontally and vertically
        int[] directions = {0, 1, 0, -1, 1, 0, -1, 0};
        
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

    public boolean hasMoved() {
        return this.hasMoved;
    }

    public void setHasMoved() {
        hasMoved = true;
    }

    public void resetHasMoved() {
        hasMoved = false;
    }
} 