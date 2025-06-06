package com.chess;

import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class Pawn extends ChessPiece {
    private boolean hasMoved;
    private static Point lastDoubleMovePawn = null; // Track the last pawn that moved two squares

    public Pawn(boolean isWhite) {
        super(isWhite);
        this.symbol = 'p';
        hasMoved = false;
        loadImage();
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        String imagePath = "/svg/" + color + "p.png";
        System.out.println("Attempting to load image: " + imagePath);
        try {
            pieceImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
            if (pieceImage == null) {
                System.err.println("Failed to load image for " + (isWhite ? "white" : "black") + " pawn: " + imagePath + " (getResource returned null)");
            } else {
                System.out.println("Successfully loaded image for " + (isWhite ? "white" : "black") + " pawn: " + imagePath);
                int width = pieceImage.getWidth(null);
                int height = pieceImage.getHeight(null);
                System.out.println("Image dimensions for " + (isWhite ? "white" : "black") + " pawn: " + width + "x" + height);
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + (isWhite ? "white" : "black") + " pawn: " + imagePath + " - " + e.getMessage());
            pieceImage = null;
        }
    }

    @Override
    public Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col) {
        Set<Point> legalMoves = new HashSet<>();
        int direction = isWhite ? -1 : 1;
        
        // Forward move
        if (isValidPosition(row + direction, col) && board[row + direction][col] == null) {
            legalMoves.add(new Point(col, row + direction));
            
            // Double move from starting position
            if (!hasMoved && isValidPosition(row + 2 * direction, col) 
                && board[row + 2 * direction][col] == null) {
                legalMoves.add(new Point(col, row + 2 * direction));
            }
        }
        
        // Capture moves
        for (int captureCol : new int[]{col - 1, col + 1}) {
            if (isValidPosition(row + direction, captureCol)) {
                ChessPiece targetPiece = board[row + direction][captureCol];
                if (isOpponentPiece(targetPiece)) {
                    legalMoves.add(new Point(captureCol, row + direction));
                }
            }
        }
        
        // En passant
        if (lastDoubleMovePawn != null) {
            int lastRow = lastDoubleMovePawn.y;
            int lastCol = lastDoubleMovePawn.x;
            
            // Check if the last double move pawn is adjacent and on the correct rank
            if (Math.abs(lastCol - col) == 1 && lastRow == row) {
                // Check if the pawn is of opposite color
                ChessPiece lastPawn = board[lastRow][lastCol];
                if (lastPawn instanceof Pawn && isOpponentPiece(lastPawn)) {
                    legalMoves.add(new Point(lastCol, row + direction));
                }
            }
        }
        
        return legalMoves;
    }

    public void setHasMoved() {
        hasMoved = true;
    }

    public static void setLastDoubleMovePawn(Point position) {
        lastDoubleMovePawn = position;
    }

    public static void clearLastDoubleMovePawn() {
        lastDoubleMovePawn = null;
    }

    public void resetHasMoved() {
        hasMoved = false;
    }

    public static Point getLastDoubleMovePawn() {
        return lastDoubleMovePawn;
    }
} 