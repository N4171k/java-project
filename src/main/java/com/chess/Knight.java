package com.chess;

import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class Knight extends ChessPiece {
    
    public Knight(boolean isWhite) {
        super(isWhite);
        this.symbol = 'n';
        loadImage();
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        String imagePath = "/svg/" + color + "n.png";
        System.out.println("Attempting to load image: " + imagePath);
        try {
            pieceImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
            if (pieceImage == null) {
                System.err.println("Failed to load image for " + (isWhite ? "white" : "black") + " knight: " + imagePath + " (getResource returned null)");
            } else {
                System.out.println("Successfully loaded image for " + (isWhite ? "white" : "black") + " knight: " + imagePath);
                int width = pieceImage.getWidth(null);
                int height = pieceImage.getHeight(null);
                System.out.println("Image dimensions for " + (isWhite ? "white" : "black") + " knight: " + width + "x" + height);
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + (isWhite ? "white" : "black") + " knight: " + imagePath + " - " + e.getMessage());
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