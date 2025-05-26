import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ChessBoard extends JPanel {
    private static final int SQUARE_SIZE = 60;
    private static final int BOARD_SIZE = 8;
    private ChessPiece[][] board;
    private ChessPiece selectedPiece;
    private Point selectedSquare;
    private boolean isWhiteTurn;
    private boolean isAITurn;
    private Set<Point> legalMoves;
    private ChessGame game;
    private JTextArea moveHistoryArea;
    private int moveNumber;
    private Color[][] squareColors;

    public ChessBoard(ChessGame game) {
        this.game = game;
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        squareColors = new Color[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        addMouseListener(new ChessMouseListener());
        moveNumber = 1;
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
        
        // Draw board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isLight = (row + col) % 2 == 0;
                g2d.setColor(isLight ? new Color(240, 217, 181) : new Color(181, 136, 99));
                g2d.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }

        // Highlight squares with custom colors (like check)
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (squareColors[row][col] != null) {
                    g2d.setColor(squareColors[row][col]);
                    g2d.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, 
                                SQUARE_SIZE, SQUARE_SIZE);
                }
            }
        }

        // Highlight selected square
        if (selectedSquare != null) {
            g2d.setColor(new Color(130, 151, 105, 150));
            g2d.fillRect(selectedSquare.x * SQUARE_SIZE, selectedSquare.y * SQUARE_SIZE, 
                        SQUARE_SIZE, SQUARE_SIZE);
        }

        // Highlight legal moves with light blue squares
        g2d.setColor(new Color(173, 216, 230, 150)); // Light blue with transparency
        for (Point move : legalMoves) {
            g2d.fillRect(move.x * SQUARE_SIZE, move.y * SQUARE_SIZE,
                        SQUARE_SIZE, SQUARE_SIZE);
        }

        // Draw pieces last so they appear on top
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    piece.draw(g2d, col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE);
                }
            }
        }
    }

    private class ChessMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int col = e.getX() / SQUARE_SIZE;
            int row = e.getY() / SQUARE_SIZE;
            
            if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                handleSquareClick(row, col);
            }
        }
    }

    private void handleSquareClick(int row, int col) {
        // Don't allow moves during AI's turn
        if (isAITurn) {
            return;
        }

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
                isWhiteTurn = !isWhiteTurn;
                isAITurn = !isAITurn; // Toggle AI turn
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

    private void makeMove(Point from, Point to) {
        String moveNotation = convertToAlgebraicNotation(from, to);
        game.addMoveToHistory(moveNotation);
        
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
                    King king = (King) piece;
                    if (king.isInCheck(board, row, col)) {
                        highlightSquare(row, col, Color.RED);
                    } else {
                        highlightSquare(row, col, null);
                    }
                }
            }
        }
    }

    private String convertToAlgebraicNotation(Point from, Point to) {
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
        
        // Add check or checkmate symbol
        if (isCheck()) {
            notation.append("+");
        } else if (isCheckmate()) {
            notation.append("#");
        }
        
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

    public boolean isAITurn() {
        return isAITurn;
    }

    public void makeMove(String move) {
        // Parse algebraic notation and make the move
        if (move.equals("O-O")) {
            // Kingside castling
            int row = isWhiteTurn ? 7 : 0;
            Point kingFrom = new Point(4, row);
            Point kingTo = new Point(6, row);
            makeMove(kingFrom, kingTo);
        } else if (move.equals("O-O-O")) {
            // Queenside castling
            int row = isWhiteTurn ? 7 : 0;
            Point kingFrom = new Point(4, row);
            Point kingTo = new Point(2, row);
            makeMove(kingFrom, kingTo);
        } else {
            // Regular move
            int toCol = move.charAt(move.length() - 2) - 'a';
            int toRow = 8 - (move.charAt(move.length() - 1) - '0');
            
            // Find the piece that can make this move
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    ChessPiece piece = board[row][col];
                    if (piece != null && piece.isWhite() == isWhiteTurn) {
                        Set<Point> moves = piece.getLegalMoves(board, row, col);
                        if (moves.contains(new Point(toCol, toRow))) {
                            makeMove(new Point(col, row), new Point(toCol, toRow));
                            isWhiteTurn = !isWhiteTurn;
                            isAITurn = !isAITurn; // Toggle AI turn
                            return;
                        }
                    }
                }
            }
        }
    }

    public void highlightSquare(int row, int col, Color color) {
        squareColors[row][col] = color;
        repaint();
    }

    public ChessPiece[][] getBoard() {
        return board;
    }

    public void setAITurn(boolean isAITurn) {
        this.isAITurn = isAITurn;
    }
} 