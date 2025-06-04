package com.chess;

import java.awt.*;
import java.util.*;

public abstract class ChessPiece {
    protected boolean isWhite;
    protected Image pieceImage;
    protected char symbol;

    public ChessPiece(boolean isWhite) {
        this.isWhite = isWhite;
        loadImage();
    }

    public boolean isWhite() {
        return isWhite;
    }

    protected abstract void loadImage();
    
    public void draw(Graphics2D g2d, int x, int y, int size) {
        if (pieceImage != null) {
            g2d.drawImage(pieceImage, x, y, size, size, null);
            System.out.println("Drawing piece: " + symbol + " at (" + x + "," + y + ")");
        } else {
            System.err.println("Cannot draw piece: " + symbol + " - pieceImage is null");
        }
    }

    public abstract Set<Point> getLegalMoves(ChessPiece[][] board, int row, int col);

    protected boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    protected boolean isOpponentPiece(ChessPiece piece) {
        return piece != null && piece.isWhite() != this.isWhite;
    }

    protected boolean isSameColorPiece(ChessPiece piece) {
        return piece != null && piece.isWhite() == this.isWhite;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessBoard board) {
        Set<Point> legalMoves = getLegalMoves(board.getBoard(), fromRow, fromCol);
        return legalMoves.contains(new Point(toCol, toRow));
    }
} 