import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ChessGame extends JFrame {
    private static final int BOARD_SIZE = 8;
    private ChessBoard board;
    private GameMode gameMode;
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
        if (mode.equals("Player vs Player")) {
            gameMode = GameMode.PVP;
            aiLevelSlider.setEnabled(false);
            colorChoice.setEnabled(false);
            aiPlayer = null;
        } else {
            gameMode = GameMode.PVAI;
            aiLevelSlider.setEnabled(true);
            colorChoice.setEnabled(true);
            // Initialize AI player when switching to Player vs AI mode
            boolean isWhiteAI = colorChoice.getSelectedItem().equals("Black");
            aiPlayer = new AIPlayer(aiLevelSlider.getValue(), isWhiteAI);
            board.setAITurn(isWhiteAI); // Set initial AI turn
        }
    }

    private void initializeTimers() {
        whiteTimeLabel = new JLabel("White: 10:00");
        blackTimeLabel = new JLabel("Black: 10:00");
        aiThinkingLabel = new JLabel("AI is thinking...");
        aiThinkingLabel.setVisible(false);
        
        controlPanel.add(whiteTimeLabel);
        controlPanel.add(blackTimeLabel);
        controlPanel.add(aiThinkingLabel);
        
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
        aiThinkingLabel.setVisible(true);
        try {
            String aiMove = getAIMove();
            if (aiMove != null) {
                SwingUtilities.invokeLater(() -> {
                    board.makeMove(aiMove);
                    aiThinkingLabel.setVisible(false);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            aiThinkingLabel.setVisible(false);
        }
    }

    private String getAIMove() {
        if (aiPlayer == null) {
            boolean isWhiteAI = colorChoice.getSelectedItem().equals("Black");
            aiPlayer = new AIPlayer(aiLevelSlider.getValue(), isWhiteAI);
        }
        return aiPlayer.getNextMove(movesHistory);
    }

    // Reset the board and move history for a new game
    private void resetGame() {
        remove(board);
        initializeBoard();
        movesHistory.clear();
        updateMoveHistoryDisplay();
        
        // Reset AI player if in AI mode
        if (gameMode == GameMode.PVAI) {
            boolean isWhiteAI = colorChoice.getSelectedItem().equals("Black");
            aiPlayer = new AIPlayer(aiLevelSlider.getValue(), isWhiteAI);
            board.setAITurn(isWhiteAI);
        }
        
        whiteTimer.restart();
        blackTimer.restart();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame().setVisible(true);
        });
    }
}

enum GameMode {
    PVP,
    PVAI
} 