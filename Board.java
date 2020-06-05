package tablut;
//waduwaduwadu

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/**
 * The state of a Tablut Game.
 *
 * @author Randy Li
 */
class Board {

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 9;

    /**
     * The throne (or castle) square and its four surrounding squares..
     */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /**
     * Initial positions of attackers.
     */
    static final Square[] INITIAL_ATTACKERS = {
            sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
            sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
            sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
            sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /**
     * Initial positions of defenders of the king.
     */
    static final Square[] INITIAL_DEFENDERS
            = {NTHRONE, ETHRONE, STHRONE, WTHRONE,
            sq(4, 6), sq(4, 2),
            sq(2, 4), sq(6, 4)};

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        init();
    }

    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }

    /**
     * Copies MODEL into me.
     */
    @SuppressWarnings("unchecked")
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                _board.put(sq(i, j), model.get(i, j));
            }
        }
        _turn = model._turn;
        _repeated = model._repeated;
        _moveCount = model._moveCount;
        _winner = model._winner;
        _moveLimit = model._moveLimit;
        _memory = (HashSet<String>) model._memory.clone();
        _boxOfrevputs = (Stack<Object[]>) model._boxOfrevputs.clone();

        for (int i = 0; i < NUM_SQUARES; i++) {
            put(model.get(sq(i)), sq(i));
        }
    }

    /**
     * Clears the board to the initial position.
     */
    void init() {
        _turn = BLACK;
        _winner = null;
        _moveCount = 0;
        _repeated = false;
        _boxOfrevputs = new Stack<>();
        _myMoves = new Stack<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                _board.put(sq(i, j), EMPTY);
            }
        }
        for (Square b : INITIAL_ATTACKERS) {
            _board.put(b, BLACK);
        }
        for (Square w : INITIAL_DEFENDERS) {
            _board.put(w, WHITE);
        }
        _board.put(sq(4, 4), KING);
        _moveLimit = 0;
    }

    /**
     * Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     *
     * @param n is a input of move limit.
     */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException("Limit is ");
        } else {
            _moveLimit = n;
        }
    }

    /**
     * Return a Piece representing whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the winner in the current position, or null if there is no winner
     * yet.
     */
    Piece winner() {
        return _winner;
    }

    /**
     * Returns true iff this is a win due to a repeated position.
     */
    boolean repeatedPosition() {
        return _repeated;
    }

    /**
     * Record current position and set winner() next mover if the current
     * position is a repeat.
     */
    private void checkRepeated() {
        boolean isnotExisted = _memory.add(encodedBoard());
        if (!isnotExisted) {
            _winner = _turn.opponent();
            _repeated = true;
        }
    }

    /**
     * Return the number of moves since the initial position that have not been
     * undone.
     */
    int moveCount() {
        return _moveCount;
    }

    /**
     * Return location of the king.
     */
    Square kingPosition() {
        for (Square key : _board.keySet()) {
            if (_board.get(key).equals(KING)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     */
    private Piece get(int col, int row) {
        Square sq = sq(col, row);
        return _board.get(sq);
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     */
    final void put(Piece p, Square s) {
        _board.replace(s, p);
    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /**
     * Return true iff FROM - TO is an unblocked rook move on the current
     * board.  For this to be true, FROM-TO must be a rook move and the
     * squares along it, other than FROM, must be empty.
     */
    boolean isUnblockedMove(Square from, Square to) {
        if (!from.isRookMove(to)) {
            return false;
        }
        int dir = from.direction(to);
        for (Square sq : ROOK_SQUARES[from.index()][dir]) {
            if (!get(sq).equals(EMPTY)) {
                return false;
            } else if (sq == to) {
                return true;
            }
        }
        throw new Error();
    }

    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    private boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /**
     * Return true iff FROM-TO is a valid move.
     */
    private boolean isLegal(Square from, Square to) {
        if (isUnblockedMove(from, to) && isLegal(from) && from.isRookMove(to)) {
            return true;
        }
        return false;
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        if (!hasMove(_turn.side())) {
            _winner = _turn.opponent();
        }
        _moveCount += 1;
        if (_moveCount == _moveLimit) {
            _winner = _turn.opponent();
        }
        Piece oldPiece = get(from);
        revPut(EMPTY, from);
        revPut(oldPiece, to);
        captureHelper(to);
        for (Square s : captureHelper(to)) {
            capture(to, s);
        }
        Move m = Move.mv(from, to);
        _myMoves.push(m);
        checkRepeated();
        if (kingPosition() == null) {
            _winner = BLACK;
        } else if (kingPosition().isEdge()) {
            _winner = WHITE;
        }

        _turn = get(to).opponent();
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /**
     * Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     * SQ0 and the necessary conditions are satisfied.
     */
    private void capture(Square sq0, Square sq2) {
        if (canCapture(sq0, sq2)) {
            put(EMPTY, sq0.between(sq2));
        }
        if (kingCapture(sq0, sq2)) {
            put(EMPTY, kingPosition());
            _winner = BLACK;
        }
    }

    /**
     * Return a list of all possible sq2.
     *
     * @param sq0 is a "to" square for move.
     */
    private List<Square> captureHelper(Square sq0) {
        List<Square> possibleSq2 = new ArrayList<>();
        if (!get(sq0).equals(EMPTY)) {
            for (int d = 0; d < 4; d++) {
                if (sq0.rookMove(d, 2) != null
                        && isHostile(sq0, sq0.rookMove(d, 2))) {
                    possibleSq2.add(sq0.rookMove(d, 2));
                }
            }
        }
        return possibleSq2;
    }

    /**
     * Whether sq0 and sq2 is hostile to mid square sq1.
     *
     * @param sq0 is the "to" square.
     * @param sq2 is another square for capture the mid sq.
     * @return whether sq0 and sq2 is hostile to mid square sq1.
     */
    private boolean isHostile(Square sq0, Square sq2) {
        if (get(sq0).side().equals(get(sq2).side())) {
            return true;
        }

        if (sq2 == THRONE && get(sq2) == EMPTY) {
            return true;
        }
        if (sq2 == THRONE && get(sq2) == KING) {
            if (get(sq0.between(sq2).diag1(sq2)) == BLACK
                    && get(sq0.between(sq2).diag1(sq2)) == BLACK
                    && get(sq0.between(sq2).rookMove(sq0.direction(sq2), 2)
            ) == BLACK) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether sq0 and sq2 could capture the mid square sq1.
     *
     * @param sq0 is the to square.
     * @param sq2 is the another square for capture.
     * @return whether could capture.
     */
    private boolean canCapture(Square sq0, Square sq2) {
        if (get(sq0) != EMPTY) {
            if (isHostile(sq0, sq2)
                    && get(sq0.between(sq2)) == get(sq0).opponent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Similar to method above, determine whether
     * sq0 and sq2 could capture king.
     *
     * @param sq0 is the to square.
     * @param sq2 is another square.
     * @return whether capture king.
     */
    private boolean kingCapture(Square sq0, Square sq2) {
        if (get(sq0) != EMPTY && get(sq0.between(sq2)) == KING) {
            if (guardedKing(kingPosition())) {
                if (checkKneighbors(sq0, sq2)) {
                    return true;
                }
            }
            if (!guardedKing(kingPosition())) {
                if (isHostile(sq0, sq2) && get(sq0).equals(BLACK)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check the neighbor squares near the KING.
     *
     * @param sq0 is to sq.
     * @param sq2 is another sq.
     * @return whether the neighbors would cap king.
     */
    private boolean checkKneighbors(Square sq0, Square sq2) {
        int count = 0;
        Square sq1 = sq0.between(sq2);
        if (get(sq1) == KING) {
            if (sq1.adjacent(THRONE)) {
                for (int dir = 0; dir < 4; dir++) {
                    if (get(sq1.rookMove(dir, 1)) == BLACK) {
                        count++;
                    }
                }
                if (count == 3) {
                    return true;
                } else {
                    count = 0;
                }
            } else if (sq1.equals(THRONE)) {
                for (int dir = 0; dir < 4; dir++) {
                    if (get(sq1.rookMove(dir, 1)) == BLACK) {
                        count++;
                    }
                    if (count == 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine whether king is on the five thrones.
     *
     * @param kposn is the king position
     * @return whether king is on thrones.
     */
    private boolean guardedKing(Square kposn) {
        if (kposn == THRONE || kposn.adjacent(THRONE)) {
            return true;
        }
        return false;
    }

    /**
     * Set square S to P and record for undoing.
     */
    private void revPut(Piece p, Square s) {
        Object[] originPut = new Object[2];
        originPut[0] = get(s);
        originPut[1] = s;
        _boxOfrevputs.push(originPut);
        put(p, s);
    }


    /**
     * Undo one move.  Has no effect on the initial board.
     */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            _turn = _turn.opponent();
            boolean findTosq = _boxOfrevputs.peek()[0].equals(EMPTY);
            while (!findTosq) {
                Object[] targetInfo = _boxOfrevputs.pop();
                Piece originPiece = (Piece) targetInfo[0];
                Square originSquare = (Square) targetInfo[1];
                put(originPiece, originSquare);
            }
            for (int i = 0; i < 2; i++) {
                Object[] targetInfo = _boxOfrevputs.pop();
                Piece originPiece = (Piece) targetInfo[0];
                Square originSquare = (Square) targetInfo[1];
                put(originPiece, originSquare);
            }
            _moveCount -= 1;
        }
    }

    /**
     * Remove record of current position in the set of positions encountered,
     * unless it is a repeated position or we are at the first move.
     */
    private void undoPosition() {
        if (!_repeated) {
            _memory.remove(encodedBoard());
        }
        _repeated = false;
    }

    /**
     * Clear the undo stack and board-position counts. Does not modify the
     * current position or win status.
     */
    void clearUndo() {
        _memory.clear();
        _boxOfrevputs.clear();
        _myMoves.clear();
    }

    /**
     * Return a new mutable list of all legal moves on the current board for
     * SIDE (ignoring whose turn it is at the moment).
     */
    List<Move> legalMoves(Piece side) {
        List<Move> temp = new ArrayList<>();
        if (side != EMPTY) {
            for (Square s : pieceLocations(side)) {
                for (int i = 0; i < SIZE; i++) {
                    if (i != s.col() && get(i, s.row()).equals(EMPTY)
                            && isUnblockedMove(s, sq(i, s.row()))) {
                        temp.add(mv(s, sq(i, s.row())));
                    }
                }
                for (int j = 0; j < SIZE; j++) {
                    if (j != s.row() && get(s.col(), j).equals(EMPTY)
                            && isUnblockedMove(s, sq(s.col(), j))) {
                        temp.add(mv(s, sq(s.col(), j)));
                    }
                }
            }
        }
        return temp;
    }


    /**
     * Return true iff SIDE has a legal move.
     */
    private boolean hasMove(Piece side) {
        if (legalMoves(side).size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Return a text representation of this Board.  If COORDINATES, then row
     * and column designations are included along the left and bottom sides.
     */
    private String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /**
     * Return the locations of all pieces on SIDE.
     */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> store = new HashSet<>();
        for (Square key : SQUARE_LIST) {
            Piece p = get(key);
            if (!p.equals(EMPTY)) {
                if (p.side().equals(side)) {
                    store.add(key);
                }
            }
        }
        return store;
    }

    /**
     * Return the contents of _board in the order of SQUARE_LIST as a sequence
     * of characters: the toString values of the current turn and Pieces.
     */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /**
     * Piece whose turn it is (WHITE or BLACK).
     */
    private Piece _turn;
    /**
     * Cached value of winner on this board, or EMPTY if it has not been
     * computed.
     */
    private Piece _winner;
    /**
     * Number of (still undone) moves since initial position.
     */
    private int _moveCount;
    /**
     * True when current board is a repeated position (ending the game).
     */
    private boolean _repeated;
    /**
     * Initialize my own board.
     */
    private HashMap<Square, Piece> _board = new HashMap<>();
    /**
     * My stack saves all the records of puts.
     */
    private Stack<Object[]> _boxOfrevputs = new Stack<>();
    /**
     * Store the result of encoded board.
     */
    private HashSet<String> _memory = new HashSet<>();
    /**
     * My stack saves all the move actions for AI.
     */
    private Stack<Move> _myMoves;
    /**
     * Move limit of the player, beyond which would cause failure.
     */
    private int _moveLimit;

}
