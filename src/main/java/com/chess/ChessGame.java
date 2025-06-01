package com.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ChessGame extends JFrame {
    private static final int BOARD_SIZE = 8;
    private ChessBoard board;
    GameMode gameMode;
    private AIPlayer aiPlayer;
    private boolean isGameTimed;
    private int timeLimit; // in minutes
    private ArrayList<String> movesHistory;
    private JPanel controlPanel;
    private JSlider aiLevelSlider;
    private JComboBox<String> colorChoice;
    private JCheckBox timedGameCheckbox;
    private JSpinner timeLimitSpinner;
    private JTextArea moveHistoryArea;
    private static final int TIME_LIMIT = 10 * 60 * 1000; // 10 minutes in milliseconds
    private Timer whiteTimer, blackTimer;
    private JLabel whiteTimeLabel, blackTimeLabel;
    private JLabel aiThinkingLabel;
    private boolean isAITurn;
    private static final int DEFAULT_AI_DIFFICULTY = 5;
    private int aiDifficulty = DEFAULT_AI_DIFFICULTY;
    private Point selectedSquare;

    public ChessGame() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        movesHistory = new ArrayList<>();
        initializeControlPanel();
        initializeBoard();
        initializeMoveHistory();
        
        // Add Start Game button
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> resetGame());
        controlPanel.add(startButton);
        
        // Initialize timers
        initializeTimers();
        
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // Game mode selection
        String[] modes = {"Player vs Player", "Player vs AI"};
        JComboBox<String> modeSelector = new JComboBox<>(modes);
        modeSelector.addActionListener(e -> handleGameModeChange((String)modeSelector.getSelectedItem()));
        
        // AI Level slider
        aiLevelSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        aiLevelSlider.setMajorTickSpacing(1);
        aiLevelSlider.setPaintTicks(true);
        aiLevelSlider.setPaintLabels(true);
        
        // Color choice
        String[] colors = {"White", "Black"};
        colorChoice = new JComboBox<>(colors);
        
        // Timed game options
        timedGameCheckbox = new JCheckBox("Timed Game");
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 60, 1);
        timeLimitSpinner = new JSpinner(spinnerModel);
        
        controlPanel.add(new JLabel("Game Mode:"));
        controlPanel.add(modeSelector);
        controlPanel.add(new JLabel("AI Level:"));
        controlPanel.add(aiLevelSlider);
        controlPanel.add(new JLabel("Your Color:"));
        controlPanel.add(colorChoice);
        controlPanel.add(timedGameCheckbox);
        controlPanel.add(new JLabel("Time Limit (minutes):"));
        controlPanel.add(timeLimitSpinner);
        
        add(controlPanel, BorderLayout.EAST);
    }

    private void initializeMoveHistory() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Move History"));
        
        moveHistoryArea = new JTextArea(20, 20);
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(historyPanel, BorderLayout.WEST);
    }

    private void initializeBoard() {
        board = new ChessBoard(this);
        add(board, BorderLayout.CENTER);
    }

    private void handleGameModeChange(String mode) {
        System.out.println("Changing game mode to: " + mode); // Debug log
        if (mode.equals("Player vs Player")) {
            gameMode = GameMode.PVP;
            aiLevelSlider.setEnabled(false);
            colorChoice.setEnabled(false);
            aiPlayer = null;
            board.setAITurn(false);
        } else {
            gameMode = GameMode.PVAI;
            aiLevelSlider.setEnabled(true);
            colorChoice.setEnabled(true);
            boolean isWhiteAI = colorChoice.getSelectedItem().equals("Black");
            System.out.println("AI is playing as: " + (isWhiteAI ? "White" : "Black")); // Debug log
            aiPlayer = new AIPlayer(board, isWhiteAI, aiDifficulty);
            board.setAITurn(isWhiteAI);
        }
    }

    private void initializeTimers() {
        whiteTimeLabel = new JLabel("White: 10:00");
        blackTimeLabel = new JLabel("Black: 10:00");
        
        // Initialize AI thinking label with proper styling
        aiThinkingLabel = new JLabel("AI is thinking...");
        aiThinkingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        aiThinkingLabel.setForeground(Color.RED);
        aiThinkingLabel.setVisible(false);
        
        // Add labels to a panel for better organization
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.add(whiteTimeLabel);
        timerPanel.add(blackTimeLabel);
        timerPanel.add(aiThinkingLabel);
        
        controlPanel.add(timerPanel);
        
        whiteTimer = new Timer(1000, e -> updateTimer(whiteTimeLabel, whiteTimer));
        blackTimer = new Timer(1000, e -> updateTimer(blackTimeLabel, blackTimer));
    }

    private void updateTimer(JLabel label, Timer timer) {
        String text = label.getText();
        String[] parts = text.split(":");
        if (parts.length < 2) return;
        
        String player = parts[0].trim();
        String[] timeParts = parts[1].trim().split(":");
        if (timeParts.length < 2) return;
        
        int minutes = Integer.parseInt(timeParts[0].trim());
        int seconds = Integer.parseInt(timeParts[1].trim());
        
        if (seconds == 0) {
            if (minutes == 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, player + " time's up!");
                return;
            }
            minutes--;
            seconds = 59;
        } else {
            seconds--;
        }
        
        label.setText(String.format("%s: %02d:%02d", player, minutes, seconds));
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
        
        // Check if it's AI's turn
        if (gameMode == GameMode.PVAI && board.isAITurn()) {
            // Start AI move in a separate thread to avoid freezing the UI
            new Thread(() -> {
                makeAIMove();
            }).start();
        }
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

    private void makeAIMove() {
        System.out.println("makeAIMove called. isAITurn: " + isAITurn + ", aiPlayer: " + (aiPlayer != null));
        if (aiPlayer != null && isAITurn) {
            // Show loading indicator on EDT
            SwingUtilities.invokeLater(() -> {
                aiThinkingLabel.setVisible(true);
                aiThinkingLabel.setText("AI is thinking...");
                controlPanel.revalidate();
                controlPanel.repaint();
            });
            
            // Start AI move in a separate thread
            new Thread(() -> {
                try {
                    String aiMove = aiPlayer.getNextMove();
                    System.out.println("AI move received: " + aiMove);
                    
                    // Execute move on the EDT
                    SwingUtilities.invokeLater(() -> {
                        if (aiMove != null) {
                            board.executeAIMove(aiMove);
                            isAITurn = false;
                            board.setAITurn(false);
                            addMoveToHistory(aiMove);
                        }
                        // Hide loading indicator
                        aiThinkingLabel.setVisible(false);
                        controlPanel.revalidate();
                        controlPanel.repaint();
                    });
                } catch (Exception e) {
                    System.err.println("Error during AI move: " + e.getMessage());
                    e.printStackTrace();
                    // Hide loading indicator on error
                    SwingUtilities.invokeLater(() -> {
                        aiThinkingLabel.setVisible(false);
                        controlPanel.revalidate();
                        controlPanel.repaint();
                    });
                }
            }).start();
        }
    }

    private void initializeAI() {
        if (aiPlayer != null) {
            aiPlayer = null;
        }
        aiPlayer = new AIPlayer(board, isAITurn, aiDifficulty);
    }

    // Reset the board and move history for a new game
    private void resetGame() {
        System.out.println("Resetting game..."); // Debug log
        remove(board);
        board = new ChessBoard(this); // create a new board and assign it
        add(board, BorderLayout.CENTER); // add the new board to the UI
        movesHistory.clear();
        updateMoveHistoryDisplay();
        
        if (gameMode == GameMode.PVAI) {
            boolean isWhiteAI = colorChoice.getSelectedItem().equals("Black");
            System.out.println("Reinitializing AI as: " + (isWhiteAI ? "White" : "Black")); // Debug log
            aiPlayer = new AIPlayer(board, isWhiteAI, aiDifficulty);
            board.setAITurn(isWhiteAI);
        }
        
        whiteTimer.restart();
        blackTimer.restart();
        revalidate();
        repaint();
    }

    public void setAIDifficulty(int difficulty) {
        this.aiDifficulty = Math.max(1, Math.min(10, difficulty));
        if (aiPlayer != null) {
            aiPlayer = new AIPlayer(board, !isAITurn, aiDifficulty);
        }
    }

    public int getAIDifficulty() {
        return aiDifficulty;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame().setVisible(true);
        });
    }

    private void handleSquareClick(int row, int col) {
        if (gameMode == GameMode.PVP) {
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
        } else if (gameMode == GameMode.PVAI) {
            // Player vs AI logic
            if (isAITurn) {
                System.out.println("AI's turn, ignoring player click");
                return;
            }
            
            // Player's turn
            if (selectedSquare == null) {
                ChessPiece piece = board.getPieceAt(row, col);
                if (piece != null && piece.isWhite() == board.isWhiteTurn()) {
                    selectedSquare = new Point(col, row);
                    updateBoard();
                }
            } else {
                ChessPiece piece = board.getPieceAt(selectedSquare.y, selectedSquare.x);
                if (piece != null && piece.isValidMove(selectedSquare.y, selectedSquare.x, row, col, board)) {
                    String moveNotation = board.convertToAlgebraicNotation(selectedSquare, new Point(col, row));
                    board.makeMove(selectedSquare, new Point(col, row));
                    selectedSquare = null;
                    updateBoard();
                    
                    // Set AI turn and trigger AI move
                    isAITurn = true;
                    board.setAITurn(true);
                    System.out.println("Player move complete: " + moveNotation + ", triggering AI move");
                    addMoveToHistory(moveNotation);
                } else {
                    selectedSquare = null;
                    updateBoard();
                }
            }
        }
    }

    private void updateBoard() {
        // Implement the updateBoard method to refresh the game board display
        // This can be left empty for now if not needed
    }
}

enum GameMode {
    PVP,
    PVAI
} 