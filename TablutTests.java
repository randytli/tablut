package tablut;

import org.junit.Test;

import static org.junit.Assert.*;

import ucb.junit.textui;

import java.util.List;

/**
 * Junit tests for our Tablut Board class.
 *
 * @author Vivant Sakore AND Randy Li
 */
public class TablutTests {

    /**
     * Run the JUnit tests in this package.
     */
    public static void main(String[] ignored) {
        textui.runClasses(TablutTests.class);
    }

    /**
     * Tests legalMoves for white pieces to make sure it returns
     * all legal Moves.
     * This method needs to be finished and may need to be changed
     * based on your implementation.
     */
    @Test
    public void testLegalWhiteMoves() {
        Board b = new Board();
        List<Move> movesList = b.legalMoves(Piece.WHITE);

        assertEquals(56, movesList.size());

        assertFalse(movesList.contains(Move.mv("e7-8")));
        assertFalse(movesList.contains(Move.mv("e8-f")));

        assertTrue(movesList.contains(Move.mv("e6-f")));
        assertTrue(movesList.contains(Move.mv("f5-8")));

    }

    /**
     * Tests legalMoves for black pieces to make sure it
     * returns all legal Moves.
     * This method needs to be finished and may need to be changed
     * based on your implementation.
     */
    @Test
    public void testLegalBlackMoves() {
        Board b = new Board();

        List<Move> movesList = b.legalMoves(Piece.BLACK);

        assertEquals(80, movesList.size());

        assertFalse(movesList.contains(Move.mv("e8-7")));
        assertFalse(movesList.contains(Move.mv("e7-8")));

        assertTrue(movesList.contains(Move.mv("f9-i")));
        assertTrue(movesList.contains(Move.mv("h5-1")));

    }


    private void buildBoard(Board b, Piece[][] target) {
        for (int col = 0; col < Board.SIZE; col++) {
            for (int row = Board.SIZE - 1; row >= 0; row--) {
                Piece piece = target[Board.SIZE - 1 - row][col];
                b.put(piece, Square.sq(col, row));
            }
        }
        System.out.println(b);
    }

    @Test
    public void testUndo() {
        Board b = new Board();

        Piece[][] initialBoardState;
        initialBoardState = new Piece[][]{
                {E, E, E, B, B, B, E, E, E},
                {E, E, E, E, B, E, E, E, E},
                {E, E, E, E, W, E, E, E, E},
                {B, E, E, E, W, E, E, E, B},
                {B, B, W, W, K, W, W, B, B},
                {B, E, E, E, W, E, E, E, B},
                {E, E, E, E, W, E, E, E, E},
                {E, E, E, E, B, E, E, E, E},
                {E, E, E, B, B, B, E, E, E},
        };

        buildBoard(b, initialBoardState);

        b.makeMove(Square.sq(5, 8), Square.sq(5, 5));
        System.out.println(b);
        b.makeMove(Square.sq(4, 6), Square.sq(5, 6));
        System.out.println(b);
        b.makeMove(Square.sq(8, 5), Square.sq(5, 5));
        System.out.println(b);
        b.makeMove(Square.sq(6, 4), Square.sq(6, 5));
        System.out.println(b);
        b.undo();
        System.out.println(b);
        b.undo();
        System.out.println(b);
        b.undo();
        System.out.println(b);
        b.undo();
        System.out.println(b);

    }

    static final Piece E = Piece.EMPTY;
    static final Piece W = Piece.WHITE;
    static final Piece B = Piece.BLACK;
    static final Piece K = Piece.KING;
}

