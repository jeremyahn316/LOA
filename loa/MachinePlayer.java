/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.Random;

import static loa.Piece.*;

/** An automated Player.
 *  @author Jeremy Ahn
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.gameOver()) {
            return score(board);
        }
        int bestscore = 0;
        Move best = board.legalMoves().get(0);
        for (Move move: board.legalMoves()) {
            Board c = new Board(board);
            c.makeMove(move);
            int score
                    = findMove(c, depth - 1, false, sense * -1, alpha, beta);
            if (score > bestscore) {
                bestscore = score;
                best = move;
            }
            if (sense == 1) {
                alpha = Math.max(alpha, score);
            } else {
                beta = Math.min(score, beta);
                if (score < bestscore) {
                    best = move;
                    bestscore = score;
                }
            }
            if (alpha >= beta) {
                break;
            }
        }
        if (saveMove) {
            _foundMove = best;
        }
        return bestscore;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        int moves = getBoard().movesMade();
        if (moves < 20) {
            return 1;
        } else if (moves < 30) {
            return 2;
        } else if (moves < 40) {
            return 3;
        }
        return 4;
    }

    /** @param board takes in the current state of the board
     * Heuristic to return score of a move. */
    private int score(Board board) {
        Random ra = new Random();
        int pos = ra.nextInt() + 1;
        int neg = -1 * (ra.nextInt() + 1);
        if (board.piecesContiguous(WP)) {
            return WINNING_VALUE;
        } else if (board.piecesContiguous(BP)) {
            return -1 * WINNING_VALUE;
        }
        if (board.whitePiece() - board.getRegionSizes(WP).get(0)
                < board.blackPiece() - board.getRegionSizes(BP).get(0)) {
            return pos;
        } else if (board.whitePiece() - board.getRegionSizes(WP).get(0)
                > board.blackPiece() - board.getRegionSizes(BP).get(0)) {
            return neg;
        } else {
            return 0;
        }

    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
