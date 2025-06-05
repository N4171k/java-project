package com.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class ChessBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private ChessPiece[][] board;
    private ChessPiece selectedPiece;
    private Point selectedSquare;
    private boolean isWhiteTurn;
    private Set<Point> legalMoves;
    private ChessGame game;
    private JTextArea moveHistoryArea;
    private int moveNumber;
    private Color[][] squareColors;
    private Stack<MoveState> moveHistory;
    private int halfmoveClock;
    private int fullmoveNumber;
    private int squareSize;

    public ChessBoard(ChessGame game) {
        this.game = game;
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        squareColors = new Color[BOARD_SIZE][BOARD_SIZE];
        moveHistory = new Stack<>();
        halfmoveClock = 0;
        fullmoveNumber = 1;
        initializeBoard();
        addMouseListener(new ChessMouseListener());
        moveNumber = 1;
        setOpaque(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Calculate new square size based on the smaller dimension to maintain square aspect ratio
                squareSize = Math.min(getWidth(), getHeight()) / BOARD_SIZE;
                repaint();
            }
        });
    }

    private void initializeBoard() {
        isWhiteTurn = true;
        legalMoves = new HashSet<>();
        
        // Initialize pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(false);
            board[6][i] = new Pawn(true);
        }

        // Initialize other pieces
        board[0][0] = new Rook(false);
        board[0][7] = new Rook(false);
        board[7][0] = new Rook(true);
        board[7][7] = new Rook(true);

        board[0][1] = new Knight(false);
        board[0][6] = new Knight(false);
        board[7][1] = new Knight(true);
        board[7][6] = new Knight(true);

        board[0][2] = new Bishop(false);
        board[0][5] = new Bishop(false);
        board[7][2] = new Bishop(true);
        board[7][5] = new Bishop(true);

        board[0][3] = new Queen(false);
        board[0][4] = new King(false);
        board[7][3] = new Queen(true);
        board[7][4] = new King(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Calculate current square size based on panel dimensions
        squareSize = Math.min(getWidth(), getHeight()) / BOARD_SIZE;

        g2d.setColor(Color.LIGHT_GRAY); // Add a background color to the board panel
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isLight = (row + col) % 2 == 0;
                g2d.setColor(isLight ? new Color(240, 217, 181) : new Color(181, 136, 99));
                g2d.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
            }
        }

        // Highlight squares with custom colors (like check)
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (squareColors[row][col] != null) {
                    g2d.setColor(squareColors[row][col]);
                    g2d.fillRect(col * squareSize, row * squareSize, 
                                squareSize, squareSize);
                }
            }
        }

        // Highlight selected square
        if (selectedSquare != null) {
            g2d.setColor(new Color(130, 151, 105, 150));
            g2d.fillRect(selectedSquare.x * squareSize, selectedSquare.y * squareSize, 
                        squareSize, squareSize);
        }

        // Highlight legal moves with light blue squares
        g2d.setColor(new Color(173, 216, 230, 150)); // Light blue with transparency
        for (Point move : legalMoves) {
            g2d.fillRect(move.x * squareSize, move.y * squareSize,
                        squareSize, squareSize);
        }

        // Draw pieces last so they appear on top
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    piece.draw(g2d, col * squareSize, row * squareSize, squareSize);
                }
            }
        }
    }

    private class ChessMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int col = e.getX() / squareSize;
            int row = e.getY() / squareSize;
            
            if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                handleSquareClick(row, col);
            }
        }
    }

    private void handleSquareClick(int row, int col) {
        ChessPiece clickedPiece = board[row][col];
        
        if (selectedPiece == null) {
            if (clickedPiece != null && clickedPiece.isWhite() == isWhiteTurn) {
                selectedPiece = clickedPiece;
                selectedSquare = new Point(col, row);
                legalMoves = getValidMoves(selectedPiece, row, col);
                repaint();
            }
        } else {
            Point move = new Point(col, row);
            if (legalMoves.contains(move)) {
                makeMove(selectedSquare, move);
                selectedPiece = null;
                selectedSquare = null;
                legalMoves.clear();
                repaint();
            } else if (clickedPiece != null && clickedPiece.isWhite() == isWhiteTurn) {
                selectedPiece = clickedPiece;
                selectedSquare = new Point(col, row);
                legalMoves = getValidMoves(selectedPiece, row, col);
                repaint();
            } else {
                selectedPiece = null;
                selectedSquare = null;
                legalMoves.clear();
                repaint();
            }
        }
    }

    private Set<Point> getValidMoves(ChessPiece piece, int row, int col) {
        Set<Point> allMoves = piece.getLegalMoves(board, row, col);
        Set<Point> validMoves = new HashSet<>();
        
        for (Point move : allMoves) {
            if (isMoveValid(row, col, move.y, move.x)) {
                validMoves.add(move);
            }
        }
        
        return validMoves;
    }

    private boolean isMoveValid(int fromRow, int fromCol, int toRow, int toCol) {
        // Make a temporary move
        ChessPiece tempPiece = board[toRow][toCol];
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;
        
        // Check if the king is in check after the move
        boolean isInCheck = false;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece instanceof King && piece.isWhite() == isWhiteTurn) {
                    isInCheck = ((King)piece).isInCheck(board, row, col);
                    break;
                }
            }
        }
        
        // Undo the temporary move
        board[fromRow][fromCol] = board[toRow][toCol];
        board[toRow][toCol] = tempPiece;
        
        return !isInCheck;
    }

    public void makeMove(Point from, Point to) {
        // Save the current state for undo
        MoveState state = new MoveState(
            board[from.y][from.x],
            board[to.y][to.x],
            from,
            to,
            isWhiteTurn,
            Pawn.getLastDoubleMovePawn()
        );
        moveHistory.push(state);

        String moveNotation = convertToAlgebraicNotation(from, to);
        if (game != null) {
            game.addMoveToHistory(moveNotation);
        }
        
        // Update halfmove clock
        if (board[from.y][from.x] instanceof Pawn || board[to.y][to.x] != null) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }
        
        // Update fullmove number
        if (!isWhiteTurn) {
            fullmoveNumber++;
        }
        
        // Handle en passant
        if (board[from.y][from.x] instanceof Pawn && Math.abs(to.x - from.x) == 1 && board[to.y][to.x] == null) {
            // This is an en passant capture
            board[from.y][to.x] = null; // Remove the captured pawn
        }
        
        // Handle castling
        if (board[from.y][from.x] instanceof King && Math.abs(to.x - from.x) == 2) {
            // This is a castling move
            int rookCol = to.x > from.x ? 7 : 0;
            int newRookCol = to.x > from.x ? to.x - 1 : to.x + 1;
            board[to.y][newRookCol] = board[to.y][rookCol];
            board[to.y][rookCol] = null;
            if (board[to.y][newRookCol] instanceof Rook) {
                ((Rook)board[to.y][newRookCol]).setHasMoved();
            }
        }
        
        // Move the piece
        board[to.y][to.x] = board[from.y][from.x];
        board[from.y][from.x] = null;
        
        // Handle pawn promotion
        if (board[to.y][to.x] instanceof Pawn && (to.y == 0 || to.y == 7)) {
            board[to.y][to.x] = new Queen(board[to.y][to.x].isWhite());
        }
        
        // Update piece state
        if (board[to.y][to.x] instanceof Pawn) {
            ((Pawn)board[to.y][to.x]).setHasMoved();
            if (Math.abs(to.y - from.y) == 2) {
                Pawn.setLastDoubleMovePawn(to);
            } else {
                Pawn.clearLastDoubleMovePawn();
            }
        } else if (board[to.y][to.x] instanceof King) {
            ((King)board[to.y][to.x]).setHasMoved();
        } else if (board[to.y][to.x] instanceof Rook) {
            ((Rook)board[to.y][to.x]).setHasMoved();
        }

        // Check for check after the move
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece instanceof King) {
                    boolean isInCheck = ((King)piece).isInCheck(board, row, col);
                    if (isInCheck) {
                        highlightSquare(row, col, new Color(255, 0, 0, 100)); // Red highlight for check
                    } else {
                        highlightSquare(row, col, null); // Clear highlight
                    }
                }
            }
        }

        isWhiteTurn = !isWhiteTurn;
        repaint();
    }

    public String convertToAlgebraicNotation(Point from, Point to) {
        StringBuilder notation = new StringBuilder();
        ChessPiece piece = board[from.y][from.x];
        
        // Handle castling
        if (piece instanceof King && Math.abs(to.x - from.x) == 2) {
            return to.x > from.x ? "O-O" : "O-O-O";
        }
        
        // Add piece letter (except for pawns)
        if (!(piece instanceof Pawn)) {
            if (piece instanceof Knight) notation.append("N");
            else if (piece instanceof Bishop) notation.append("B");
            else if (piece instanceof Rook) notation.append("R");
            else if (piece instanceof Queen) notation.append("Q");
            else if (piece instanceof King) notation.append("K");
        }
        
        // Add capture symbol
        if (board[to.y][to.x] != null || (piece instanceof Pawn && from.x != to.x)) {
            if (piece instanceof Pawn) {
                notation.append((char)('a' + from.x));
            }
            notation.append("x");
        }
        
        // Add destination square
        notation.append((char)('a' + to.x));
        notation.append(8 - to.y);
        
        return notation.toString();
    }

    private boolean isCheck() {
        // Find the king of the current player
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece instanceof King && piece.isWhite() == isWhiteTurn) {
                    return ((King)piece).isInCheck(board, row, col);
                }
            }
        }
        return false;
    }

    private boolean isCheckmate() {
        if (!isCheck()) return false;
        
        // Try all possible moves for all pieces
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.isWhite() == isWhiteTurn) {
                    Set<Point> moves = piece.getLegalMoves(board, row, col);
                    if (!moves.isEmpty()) {
                        return false; // Found a legal move
                    }
                }
            }
        }
        return true; // No legal moves found
    }

    public void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Point from = new Point(fromCol, fromRow);
        Point to = new Point(toCol, toRow);
        makeMove(from, to);
    }

    public void makeMove(String move) {
        if (move.length() != 4) {
            return;
        }
        int fromCol = move.charAt(0) - 'a';
        int fromRow = 8 - (move.charAt(1) - '0');
        int toCol = move.charAt(2) - 'a';
        int toRow = 8 - (move.charAt(3) - '0');
        makeMove(fromRow, fromCol, toRow, toCol);
    }

    public void highlightSquare(int row, int col, Color color) {
        squareColors[row][col] = color;
        repaint();
    }

    public ChessPiece[][] getBoard() {
        return board;
    }

    public void undoMove() {
        if (moveHistory.isEmpty()) {
            return;
        }

        MoveState state = moveHistory.pop();
        
        // Restore the board state
        board[state.from.y][state.from.x] = state.movedPiece;
        board[state.to.y][state.to.x] = state.capturedPiece;
        
        // Restore piece states
        if (state.movedPiece instanceof Pawn) {
            ((Pawn)state.movedPiece).resetHasMoved();
        } else if (state.movedPiece instanceof King) {
            ((King)state.movedPiece).resetHasMoved();
        } else if (state.movedPiece instanceof Rook) {
            ((Rook)state.movedPiece).resetHasMoved();
        }
        
        // Restore en passant state
        Pawn.setLastDoubleMovePawn(state.lastDoubleMovePawn);
        
        // Restore turn
        isWhiteTurn = state.isWhiteTurn;
        
        repaint();
    }

    private static class MoveState {
        ChessPiece movedPiece;
        ChessPiece capturedPiece;
        Point from;
        Point to;
        boolean isWhiteTurn;
        Point lastDoubleMovePawn;

        MoveState(ChessPiece movedPiece, ChessPiece capturedPiece, Point from, Point to, 
                 boolean isWhiteTurn, Point lastDoubleMovePawn) {
            this.movedPiece = movedPiece;
            this.capturedPiece = capturedPiece;
            this.from = from;
            this.to = to;
            this.isWhiteTurn = isWhiteTurn;
            this.lastDoubleMovePawn = lastDoubleMovePawn;
        }
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

    public ChessPiece getPieceAt(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        }
        return null;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public String convertToFEN() {
        StringBuilder fen = new StringBuilder();
        
        // Board position
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = getPieceAt(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    char pieceChar = piece.getSymbol();
                    fen.append(pieceChar);
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 7) {
                fen.append('/');
            }
        }
        
        // Active color
        fen.append(' ').append(isWhiteTurn ? 'w' : 'b');
        
        // Castling availability
        StringBuilder castling = new StringBuilder();
        if (canCastleKingside(true)) castling.append('K');
        if (canCastleQueenside(true)) castling.append('Q');
        if (canCastleKingside(false)) castling.append('k');
        if (canCastleQueenside(false)) castling.append('q');
        fen.append(' ').append(castling.length() > 0 ? castling : '-');
        
        // En passant target square
        fen.append(' ').append(getEnPassantTarget() != null ? 
            convertToAlgebraicNotation(getEnPassantTarget()) : '-');
        
        // Halfmove clock and fullmove number
        fen.append(' ').append(halfmoveClock).append(' ').append(fullmoveNumber);
        
        return fen.toString();
    }

    private String convertToAlgebraicNotation(Point point) {
        char file = (char)('a' + point.x);
        char rank = (char)('8' - point.y);
        return "" + file + rank;
    }

    private Point getEnPassantTarget() {
        // Find the last double-moved pawn
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = getPieceAt(row, col);
                if (piece instanceof Pawn) {
                    Pawn pawn = (Pawn) piece;
                    Point lastDoubleMove = pawn.getLastDoubleMovePawn();
                    if (lastDoubleMove != null && lastDoubleMove.x == col && lastDoubleMove.y == row) {
                        return lastDoubleMove;
                    }
                }
            }
        }
        return null;
    }

    private boolean canCastleKingside(boolean isWhite) {
        int row = isWhite ? 7 : 0;
        ChessPiece king = board[row][4];
        ChessPiece rook = board[row][7];
        return king instanceof King && rook instanceof Rook &&
               !((King)king).hasMoved() && !((Rook)rook).hasMoved() &&
               board[row][5] == null && board[row][6] == null;
    }

    private boolean canCastleQueenside(boolean isWhite) {
        int row = isWhite ? 7 : 0;
        ChessPiece king = board[row][4];
        ChessPiece rook = board[row][0];
        return king instanceof King && rook instanceof Rook &&
               !((King)king).hasMoved() && !((Rook)rook).hasMoved() &&
               board[row][1] == null && board[row][2] == null && board[row][3] == null;
    }

    public java.util.List<Move> getAllLegalMoves(boolean forWhite) {
        java.util.List<Move> moves = new java.util.ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.isWhite() == forWhite) {
                    Set<Point> legalMoves = piece.getLegalMoves(board, row, col);
                    for (Point move : legalMoves) {
                        moves.add(new Move(row, col, move.y, move.x));
                    }
                }
            }
        }
        return moves;
    }
} 