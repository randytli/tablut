package tablut;

  .//
import java.util.ArrayList;


import static java.lang.Math.*;


import static tablut.Board.*;
import static tablut.Piece.*;
import static tablut.Square.ROOK_SQUARES;
import static tablut.Square.sq;

/**
 * A Player that automatically generates moves.
 *
 * @author Randy Li
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * An accelerator for Depth.
     */
    private static final int DIVID = 40;
    /** A magnitude for static score. */
    private static final int FIRST = 500;
    /** A magnitude for static score. */
    private static final int SECOND = 50;
    /** A magnitude for static score. */
    private static final int THIRD = 200;
    /** A magnitude for static score. */
    private static final int FOURTH = 150;
    /** A magnitude for static score. */
    private static final int FIFTH = 100;
    /** A magnitude for static score. */
    private static final int SIXTH = 25;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        if (move == null) {
            return "null";
        }
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Piece turnNow = board().turn();
        Board copy = new Board(board());
        int senseval;
        if (turnNow == BLACK) {
            senseval = -1;
        } else {
            senseval = 1;
        }
        int score = findMove(
                copy, maxDepth(copy), true, senseval, -INFTY, INFTY);
//        System.out.println(board());
        return _lastFoundMove;
    }


    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        int bestSoFar = -sense * INFTY; Piece side = null;
        if (board.winner() != null || depth == 0) {
            return staticScore(board);
        }
        if (sense == 1) {
            side = WHITE;
        } else {
            side = BLACK;
        }
        ArrayList<Move> allmoves = (ArrayList<Move>) board.legalMoves(side);
        Move returnMove = allmoves.get(0);
        if (sense == 1) {
            for (Move m : allmoves) {
                Board myCopy = new Board();
                myCopy.copy(board);
                myCopy.makeMove(m);
                int nextValue = findMove(myCopy, depth - 1,
                        false, -1, alpha, beta);
                if (nextValue > bestSoFar) {
                    returnMove = m;
                    bestSoFar = nextValue;
                }
                alpha = max(alpha, bestSoFar);
                if (beta <= alpha) {
                    break;
                }
            }
        } else if (sense == -1) {
            for (Move m : allmoves) {
                Board myCopy = new Board();
                myCopy.copy(board);
                myCopy.makeMove(m);
                int nextValue = findMove(myCopy, depth - 1,
                        false, 1, alpha, beta);
                if (nextValue < bestSoFar) {
                    returnMove = m;
                    bestSoFar = nextValue;
                }
                beta = min(beta, bestSoFar);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        if (sense == 1) {
            if (bestSoFar == -WINNING_VALUE) {
                bestSoFar = -WILL_WIN_VALUE;
            }
        }
        if (sense == -1) {
            if (bestSoFar == WINNING_VALUE) {
                bestSoFar = WILL_WIN_VALUE;
            }
        }
        if (saveMove) {
            _lastFoundMove = returnMove;
        }
        return bestSoFar;
    }


    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        int n = board.moveCount();
        if (n / DIVID + 1 > 4) {
            return 4;
        }
        return n / DIVID + 1;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        if (board.winner() == BLACK) {
            return -WINNING_VALUE;
        } else if (board.winner() == WHITE) {
            return WINNING_VALUE;
        }
        int evaluation = 0;
        Square kingpos = board.kingPosition();
        evaluation += ((double) (board.pieceLocations(WHITE).size()
                / board.pieceLocations(BLACK).size())
                - (double) (9 / 16)) * FIRST;
        evaluation += min(min(kingpos.col(), 8 - kingpos.col()),
                min(kingpos.row(), 8 - kingpos.row())) * SECOND;
        for (int i = 0; i < 4; i++) {
            Square.SqList temp = ROOK_SQUARES[kingpos.index()][i];
            if (board.get(temp.get(0)) == BLACK) {
                evaluation += -THIRD;
            } else if (board.get(temp.get(0)) == EMPTY) {
                for (Square sq : temp.subList(1, temp.size())) {
                    if (board.get(sq) == BLACK) {
                        evaluation += -FOURTH;
                        break;
                    }
                }
            } else if (board.get(temp.get(0)) == WHITE) {
                for (Square sq : temp.subList(1, temp.size())) {
                    boolean status = true;
                    if (board.get(sq) == BLACK) {
                        evaluation += -FIFTH;
                        status = false;
                    }
                    if (status && board.get(sq) == WHITE) {
                        evaluation += SIXTH;
                    }
                    if (status && board.get(sq) == EMPTY) {
                        evaluation += SECOND;
                    }
                }
            }
        }
        evaluation += (board.legalMoves(WHITE).size()
                - board.legalMoves(BLACK).size()) * SECOND;
        return evaluation;
    }

    /** Check if the given BOARD and TURN is a will-win position.
     @return */
    private boolean willWin(Board board, Piece turn) {
        if (board.turn() != turn) {
            return false;
        }
        Square kingpos = board.kingPosition();
        if (board.turn() == BLACK) {
            if (kingpos == THRONE) {
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    if (board.get(kingpos.rookMove(i, 1)) == BLACK) {
                        count++;
                    }
                }
                if (count == 3) {
                    return true;
                }
            } else if (checkThrone(board, kingpos)) {
                return true;
            } else {
                for (int i = 0; i < 4; i++) {
                    Square adj = kingpos.rookMove(i, 1);
                    if (board.get(adj) == BLACK) {
                        Square possible
                                = adj.rookMove(adj.direction(kingpos), 2);
                        for (int j = 0; j < 4; j++) {
                            if (i != possible.direction(kingpos)) {
                                Square.SqList orthogonal
                                        = ROOK_SQUARES[possible.index()][j];
                                for (Square sq: orthogonal) {
                                    if (board.get(sq) == WHITE) {
                                        return false;
                                    } else if (board.get(sq) == BLACK) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        } else if (board.turn() == WHITE) {
            for (int i = 0; i < 4; i++) {
                for (Square sq : ROOK_SQUARES[kingpos.index()][i]) {
                    if (sq.isEdge() && board.isUnblockedMove(kingpos, sq)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**Use BOARD and KINGPOS to check throne.
     @return */
    private boolean checkThrone(Board board, Square kingpos) {
        if (kingpos.equals(WTHRONE)) {
            int count = 0;
            if (board.get(sq(2, 4)) == BLACK) {
                count++;
            }
            if (board.get(sq(3, 3)) == BLACK) {
                count++;
            }
            if (board.get(sq(3, 5)) == BLACK) {
                count++;
            }
            return count == 2;
        } else if (kingpos.equals(NTHRONE)) {
            int count = 0;
            if (board.get(sq(4, 6)) == BLACK) {
                count++;
            }
            if (board.get(sq(5, 5)) == BLACK) {
                count++;
            }
            if (board.get(sq(3, 5)) == BLACK) {
                count++;
            }
            return count == 2;
        } else if (kingpos.equals(STHRONE)) {
            int count = 0;
            if (board.get(sq(4, 2)) == BLACK) {
                count++;
            }
            if (board.get(sq(5, 3)) == BLACK) {
                count++;
            }
            if (board.get(sq(3, 3)) == BLACK) {
                count++;
            }
            return count == 2;
        } else if (kingpos.equals(ETHRONE)) {
            int count = 0;
            if (board.get(sq(6, 4)) == BLACK) {
                count++;
            }
            if (board.get(sq(5, 5)) == BLACK) {
                count++;
            }
            if (board.get(sq(5, 3)) == BLACK) {
                count++;
            }
            return count == 2;
        }
        return false;
    }
}
