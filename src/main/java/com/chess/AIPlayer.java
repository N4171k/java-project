package com.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.awt.Point;
import java.io.IOException;

public class AIPlayer {
    private ChessBoard board;
    private boolean isWhite;
    private KomodoDragonEngine engine;
    private int difficulty; // 1-10

    public AIPlayer(ChessBoard board, boolean isWhite) {
        this(board, isWhite, 5); // Default difficulty
    }

    public AIPlayer(ChessBoard board, boolean isWhite, int difficulty) {
        this.board = board;
        this.isWhite = isWhite;
        this.difficulty = Math.max(1, Math.min(10, difficulty));
        this.engine = new KomodoDragonEngine();
    }

    public String getNextMove() {
        try {
            String fen = board.convertToFEN();
            System.out.println("Getting AI move for FEN: " + fen);
            int moveTime = getMoveTime();
            System.out.println("Requesting move with time: " + moveTime + "ms");
            String bestMove = engine.getBestMove(fen, moveTime);
            
            if (bestMove != null && !bestMove.isEmpty()) {
                System.out.println("Engine returned move: " + bestMove);
                return bestMove;
            } else {
                System.err.println("Komodo Dragon returned null or empty move. FEN: " + fen);
            }
        } catch (IOException e) {
            System.err.println("Error getting AI move: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback to a random move if Komodo Dragon fails
        System.err.println("Falling back to random move due to Komodo Dragon failure");
        board.makeRandomMove();
        return null;
    }

    private int getMoveTime() {
        // Scale move time based on difficulty (500ms to 5000ms)
        return 500 + (difficulty * 500);
    }

    public void stop() {
        if (engine != null) {
            engine.stop();
        }
    }
}
