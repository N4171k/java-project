package com.chess;

import java.awt.*;
import java.util.*;
import javax.swing.ImageIcon;

public class King extends ChessPiece {
    private boolean hasMoved;
    private boolean canCastleKingside;
    private boolean canCastleQueenside;

    public King(boolean isWhite) {
        super(isWhite);
        this.symbol = 'k';
        hasMoved = false;
        canCastleKingside = true;
        canCastleQueenside = true;
        loadImage();
    }

    @Override
    protected void loadImage() {
        String color = isWhite ? "w" : "b";
        String imagePath = "/svg/" + color + "k.png";
        System.out.println("Attempting to load image: " + imagePath);
        try {
            pieceImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
            if (pieceImage == null) {
                System.err.println("Failed to load image for " + (isWhite ? "white" : "black") + " king: " + imagePath + " (getResource returned null)");
            } else {
                System.out.println("Successfully loaded image for " + (isWhite ? "white" : "black") + " king: " + imagePath);
                int width = pieceImage.getWidth(null);
                int height = pieceImage.getHeight(null);
                System.out.println("Image dimensions for " + (isWhite ? "white" : "black") + " king: " + width + "x" + height);
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + (isWhite ? "white" : "black") + " king: " + imagePath + " - " + e.getMessage());
            pieceImage = null;
        }
    }

    @Override
    public Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col) {
        Set<Point> legalMoves = new HashSet<>();
        
        // Regular king moves (one square in any direction)
        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < 8; i++) {
            int newRow = row + rowOffsets[i];
            int newCol = col + colOffsets[i];
            
            if (isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = board[newRow][newCol];
                if (targetPiece == null || isOpponentPiece(targetPiece)) {
                    // Check if move puts king in check
                    if (!wouldBeInCheck(board, row, col, newRow, newCol)) {
                        legalMoves.add(new Point(newCol, newRow));
                    }
                }
            }
        }
        
        // Castling
        if (!hasMoved && !isInCheck(board, row, col)) {
            // Kingside castling
            if (canCastleKingside && canCastleKingside(board, row, col)) {
                legalMoves.add(new Point(col + 2, row));
            }
            
            // Queenside castling
            if (canCastleQueenside && canCastleQueenside(board, row, col)) {
                legalMoves.add(new Point(col - 2, row));
            }
        }
        
        return legalMoves;
    }

    private boolean canCastleKingside(ChessPiece[][] board, int row, int col) {
        // Check if path is clear
        for (int i = col + 1; i < 7; i++) {
            if (board[row][i] != null) {
                return false;
            }
        }
        
        // Check if rook is present and hasn't moved
        ChessPiece rook = board[row][7];
        if (!(rook instanceof Rook) || ((Rook)rook).hasMoved()) {
            return false;
        }
        
        // Check if squares are not under attack
        for (int i = col; i <= col + 2; i++) {
            if (isSquareUnderAttack(board, row, i)) {
                return false;
            }
        }
        
        return true;
    }

    private boolean canCastleQueenside(ChessPiece[][] board, int row, int col) {
        // Check if path is clear
        for (int i = col - 1; i > 0; i--) {
            if (board[row][i] != null) {
                return false;
            }
        }
        
        // Check if rook is present and hasn't moved
        ChessPiece rook = board[row][0];
        if (!(rook instanceof Rook) || ((Rook)rook).hasMoved()) {
            return false;
        }
        
        // Check if squares are not under attack
        for (int i = col; i >= col - 2; i--) {
            if (isSquareUnderAttack(board, row, i)) {
                return false;
            }
        }
        
        return true;
    }

    private boolean wouldBeInCheck(ChessPiece[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece temp = board[toRow][toCol];
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;
        
        boolean inCheck = isInCheck(board, toRow, toCol);
        
        board[fromRow][fromCol] = board[toRow][toCol];
        board[toRow][toCol] = temp;
        
        return inCheck;
    }

    public boolean isInCheck(ChessPiece[][] board, int row, int col) {
        return isSquareUnderAttack(board, row, col);
    }

    private boolean isSquareUnderAttack(ChessPiece[][] board, int row, int col) {
        // Check all opponent pieces for attacks
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece piece = board[r][c];
                if (piece != null && isOpponentPiece(piece)) {
                    // Directly check if the opponent piece at (r, c) attacks (row, col)
                    if (piece instanceof Pawn) {
                        // Pawn attack (diagonal forward)
                        int pawnDirection = piece.isWhite() ? 1 : -1;
                        if ((r + pawnDirection == row) && (c == col - 1 || c == col + 1)) {
                            return true;
                        }
                    } else if (piece instanceof Knight) {
                        // Knight attack (L-shape)
                        int[] rowOffsets = {-2, -2, -1, -1, 1, 1, 2, 2};
                        int[] colOffsets = {-1, 1, -2, 2, -2, 2, -1, 1};
                        for (int i = 0; i < 8; i++) {
                            if (r + rowOffsets[i] == row && c + colOffsets[i] == col) {
                                return true;
                            }
                        }
                    } else if (piece instanceof Bishop || piece instanceof Queen) {
                        // Bishop/Queen diagonal attack
                        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                        for (int[] d : directions) {
                            int dr = d[0];
                            int dc = d[1];
                            for (int i = 1; i < 8; i++) {
                                int currRow = r + dr * i;
                                int currCol = c + dc * i;
                                if (!isValidPosition(currRow, currCol)) break;
                                if (currRow == row && currCol == col) return true;
                                if (board[currRow][currCol] != null) break; // Blocked
                            }
                        }
                    } else if (piece instanceof Rook || piece instanceof Queen) {
                        // Rook/Queen straight attack
                        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                        for (int[] d : directions) {
                            int dr = d[0];
                            int dc = d[1];
                            for (int i = 1; i < 8; i++) {
                                int currRow = r + dr * i;
                                int currCol = c + dc * i;
                                if (!isValidPosition(currRow, currCol)) break;
                                if (currRow == row && currCol == col) return true;
                                if (board[currRow][currCol] != null) break; // Blocked
                            }
                        }
                    } else if (piece instanceof King) {
                        // King attack (adjacent)
                        if (Math.abs(r - row) <= 1 && Math.abs(c - col) <= 1 && (r != row || c != col)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void setHasMoved() {
        hasMoved = true;
        canCastleKingside = false;
        canCastleQueenside = false;
    }

    public void resetHasMoved() {
        hasMoved = false;
    }

    public boolean hasMoved() {
        return this.hasMoved;
    }
} 