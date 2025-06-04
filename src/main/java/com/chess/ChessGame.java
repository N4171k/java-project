package com.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.plaf.synth.SynthLookAndFeel;
import java.net.URL; // Import URL for resource loading

public class ChessGame extends JFrame {
    private static final int BOARD_SIZE = 8;
    private ChessBoard board;
    private ArrayList<String> movesHistory;
    private JPanel controlPanel;
    private JTextArea moveHistoryArea;
    private Point selectedSquare;

    public ChessGame() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout(10, 10)); // Add gaps between regions
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding to the content pane
        
        movesHistory = new ArrayList<>();
        setupMenuBar();
        initializeControlPanel();
        initializeBoard();
        initializeMoveHistory();
        
        // Add Start Game button
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> resetGame());
        controlPanel.add(startButton);
        
        // Set a preferred size for the main frame
        setPreferredSize(new Dimension(900, 600));

        // Set application icon
        try {
            URL iconURL = getClass().getResource("/chess_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            } else {
                System.err.println("Application icon not found: /chess_icon.png");
            }
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
            e.printStackTrace();
        }
        
        pack();
        setLocationRelativeTo(null);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> resetGame());
        gameMenu.add(newGameItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void initializeControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Use FlowLayout for better button arrangement
        controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Controls")); // Add a nice border
        
        // Add some empty space around the control panel to separate it from the board
        JPanel controlPanelWrapper = new JPanel(new BorderLayout());
        controlPanelWrapper.add(controlPanel, BorderLayout.NORTH);
        controlPanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Padding on the left
        
        add(controlPanelWrapper, BorderLayout.EAST);
    }

    private void initializeMoveHistory() {
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5)); // Add gaps
        historyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Move History"));
        
        moveHistoryArea = new JTextArea(20, 15); // Adjust size for better fit
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        moveHistoryArea.setBackground(new Color(240, 240, 240)); // Light background for readability
        moveHistoryArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding inside text area
        
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // Add some empty space around the history panel
        JPanel historyPanelWrapper = new JPanel(new BorderLayout());
        historyPanelWrapper.add(historyPanel, BorderLayout.NORTH);
        historyPanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Padding on the right
        
        add(historyPanelWrapper, BorderLayout.WEST);
    }

    private void initializeBoard() {
        JPanel boardPanel = new JPanel(new BorderLayout());
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around the board

        board = new ChessBoard(this);
        boardPanel.add(board, BorderLayout.CENTER);
        
        // Add algebraic notation labels
        JPanel fileLabels = new JPanel(new GridLayout(1, 8));
        JPanel rankLabelsLeft = new JPanel(new GridLayout(8, 1));
        JPanel rankLabelsRight = new JPanel(new GridLayout(8, 1));

        // File labels (A-H)
        for (char c = 'a'; c <= 'h'; c++) {
            JLabel label = new JLabel(String.valueOf(c), SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            fileLabels.add(label);
        }

        // Rank labels (8-1)
        for (int i = 8; i >= 1; i--) {
            JLabel leftLabel = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            leftLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            rankLabelsLeft.add(leftLabel);

            JLabel rightLabel = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            rightLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            rankLabelsRight.add(rightLabel);
        }

        boardPanel.add(fileLabels, BorderLayout.SOUTH);
        boardPanel.add(rankLabelsLeft, BorderLayout.WEST);
        boardPanel.add(rankLabelsRight, BorderLayout.EAST);

        add(boardPanel, BorderLayout.CENTER);
    }

    private void handleGameModeChange(String mode) {
        System.out.println("Game mode changed, but only Player vs Player is supported.");
    }

    private void initializeTimers() {
        // Timers are no longer needed as the game is not timed.
    }

    private void updateTimer(JLabel label, Timer timer) {
        // This method is no longer needed as the game is not timed.
    }

    private void checkForCheck() {
        ChessPiece[][] boardState = board.getBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = boardState[i][j];
                if (piece instanceof King) {
                    King king = (King) piece;
                    if (king.isInCheck(boardState, i, j)) {
                        // Highlight the king's square in red
                        board.highlightSquare(i, j, Color.RED);
                    } else {
                        board.highlightSquare(i, j, null);
                    }
                }
            }
        }
    }

    public void addMoveToHistory(String move) {
        movesHistory.add(move);
        updateMoveHistoryDisplay();
    }

    private void updateMoveHistoryDisplay() {
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < movesHistory.size(); i += 2) {
            history.append(String.format("%d. %s", (i/2 + 1), movesHistory.get(i)));
            if (i + 1 < movesHistory.size()) {
                history.append(String.format(" %s\n", movesHistory.get(i + 1)));
            } else {
                history.append("\n");
            }
        }
        moveHistoryArea.setText(history.toString());
        moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());
    }

    // Reset the board and move history for a new game
    private void resetGame() {
        System.out.println("Resetting game..."); // Debug log
        remove(board);
        board = new ChessBoard(this); // create a new board and assign it
        add(board, BorderLayout.CENTER); // add the new board to the UI
        movesHistory.clear();
        updateMoveHistoryDisplay();
        
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame().setVisible(true);
        });
    }

    private void handleSquareClick(int row, int col) {
        // Player vs Player logic
        if (selectedSquare == null) {
            ChessPiece piece = board.getPieceAt(row, col);
            if (piece != null && piece.isWhite() == board.isWhiteTurn()) {
                selectedSquare = new Point(col, row);
                updateBoard();
            }
        } else {
            ChessPiece piece = board.getPieceAt(selectedSquare.y, selectedSquare.x);
            if (piece != null && piece.isValidMove(selectedSquare.y, selectedSquare.x, row, col, board)) {
                board.makeMove(selectedSquare, new Point(col, row));
                selectedSquare = null;
                updateBoard();
            } else {
                selectedSquare = null;
                updateBoard();
            }
        }
    }

    private void updateBoard() {
        // Implement the updateBoard method to refresh the game board display
        // This can be left empty for now if not needed
    }
}

enum GameMode {
    PVP
} 