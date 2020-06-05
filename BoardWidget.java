package tablut;
//wryyyyyyyyyyy
import ucb.gui2.Pad;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import java.awt.event.MouseEvent;

import static tablut.Piece.*;
import static tablut.Square.sq;
/** A widget that displays a Tablut game.
 *  @author Randy Li
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /** Squares on each side of the board. */
    static final int SIZE = Board.SIZE;

    /** Colors of empty squares, pieces, grid lines, and boundaries. */
    static final Color
            SQUARE_COLOR = new Color(238, 207, 161),
            THRONE_COLOR = new Color(180, 255, 180),
            ADJACENT_THRONE_COLOR = new Color(200, 220, 200),
            CLICKED_SQUARE_COLOR = new Color(255, 255, 100),
            GRID_LINE_COLOR = Color.black,
            WHITE_COLOR = Color.white,
            BLACK_COLOR = Color.black;

    /** Margins. */
    static final int
            OFFSET = 2,
            MARGIN = 16;

    /** Side of single square and of board (in pixels). */
    static final int
            SQUARE_SIDE = 30,
            BOARD_SIDE = SQUARE_SIDE * SIZE + 2 * OFFSET + MARGIN;

    /** The font in which to render the "K" in the king. */
    static final Font KING_FONT = new Font("Serif", Font.BOLD, 18);
    /** The font for labeling rows and columns. */
    static final Font ROW_COL_FONT = new Font("SanSerif", Font.PLAIN, 10);

    /** Squares adjacent to the throne. */
    static final Square[] ADJACENT_THRONE = {
            Board.NTHRONE, Board.ETHRONE, Board.STHRONE, Board.WTHRONE
    };
    /** 2. */
    private static final int TWO = 2;

    /** 26. */
    private static final int TWENTYSIX = 26;

    /** King PIC. */
    private BufferedImage kingPic;

    /** WHITE. */
    private BufferedImage whitePic;

    /** BLACK. */
    private  BufferedImage blackPic;

    /** 10. */
    private static final int TEN = 10;

    /** 10. */
    private static final int FORTYFIVE = 45;

    /** 10. */
    private static final int TWENTY = 20;

    /** 10. */
    private static final double ONEONE = 0.11;
    /** 10. */
    private static final double OSEVEN = 0.07;
    /** 10. */
    private static final double OEIGHT = 0.08;
    /** 10. */
    private static final double SEVENSIX = 0.076;
    /** 10. */
    private static final double ONEFOUR = 0.14;

    /**
     * The number of king moves to all the corners.
     * @param img im
     * @param newW ne
     * @param newH ne
     * @return Score
     */
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        int wid = img.getWidth(); int height = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, wid, height, null);
        g.dispose();
        return dimg;
    }

    /** A graphical representation of a Tablut board that sends commands
     *  derived from mouse clicks to COMMANDS.  */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        setMouseHandler("click", this::mouseClicked);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);
        _acceptingMoves = false;
        try {
            kingPic = ImageIO.read(Utils.getResource("king.png"));
            int kWidth = (int) (kingPic.getWidth() * ONEONE);
            int kHeight = (int) (kingPic.getHeight() * ONEFOUR);
            kingPic = resize(kingPic, kWidth, kHeight);
            whitePic = ImageIO.read(Utils.getResource("Defender.png"));
            int whiteWidth = (int) (whitePic.getWidth() * SEVENSIX);
            int whiteHeight = (int) (whitePic.getHeight() * OSEVEN);
            whitePic = resize(whitePic, whiteWidth, whiteHeight);
            blackPic = ImageIO.read(Utils.getResource("Attacker.png"));
            int blackWidth = (int) (blackPic.getWidth() * OEIGHT);
            int blackHeight = (int) (blackPic.getHeight() * OSEVEN);
            blackPic = resize(blackPic, blackWidth, blackHeight);
        } catch (IOException excp) {
            System.err.println("Bad image path.");
            System.exit(1);
        }
    }

    /** Draw the bare board G.  */
    private void drawGrid(Graphics2D g) {
        g.setColor(SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);
        g.setColor(THRONE_COLOR);
        g.fillRect(cx(Board.THRONE), cy(Board.THRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.setColor(ADJACENT_THRONE_COLOR);
        g.fillRect(cx(Board.ETHRONE), cy(Board.ETHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.fillRect(cx(Board.WTHRONE), cy(Board.WTHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.fillRect(cx(Board.NTHRONE), cy(Board.NTHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.fillRect(cx(Board.STHRONE), cy(Board.STHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.setColor(GRID_LINE_COLOR);
        for (int k = 0; k <= SIZE; k += 1) {
            g.drawLine(cx(0), cy(k - 1), cx(SIZE), cy(k - 1));
            g.drawLine(cx(k), cy(-1), cx(k), cy(SIZE - 1));
        }
        if (first != null) {
            g.setColor(Color.red);
            g.fillRect(cx(first), cy(first),
                    SQUARE_SIDE, SQUARE_SIDE);
        }
        g.setColor(Color.black);
        g.drawString("a", cx(sq("a", "1")) + TEN, cy(sq("a", "1")) + FORTYFIVE);
        g.drawString("b", cx(sq("b", "1")) + TEN, cy(sq("b", "1")) + FORTYFIVE);
        g.drawString("c", cx(sq("c", "1")) + TEN, cy(sq("c", "1")) + FORTYFIVE);
        g.drawString("d", cx(sq("d", "1")) + TEN, cy(sq("d", "1")) + FORTYFIVE);
        g.drawString("e", cx(sq("e", "1")) + TEN, cy(sq("e", "1")) + FORTYFIVE);
        g.drawString("f", cx(sq("f", "1")) + TEN, cy(sq("f", "1")) + FORTYFIVE);
        g.drawString("g", cx(sq("g", "1")) + TEN, cy(sq("g", "1")) + FORTYFIVE);
        g.drawString("h", cx(sq("h", "1")) + TEN, cy(sq("h", "1")) + FORTYFIVE);
        g.drawString("i", cx(sq("i", "1")) + TEN, cy(sq("i", "1")) + FORTYFIVE);
        g.drawString("1", cx(sq("a", "1")) - TEN, cy(sq("a", "1")) + TWENTY);
        g.drawString("2", cx(sq("a", "2")) - TEN, cy(sq("a", "2")) + TWENTY);
        g.drawString("3", cx(sq("a", "3")) - TEN, cy(sq("a", "3")) + TWENTY);
        g.drawString("4", cx(sq("a", "4")) - TEN, cy(sq("a", "4")) + TWENTY);
        g.drawString("5", cx(sq("a", "5")) - TEN, cy(sq("a", "5")) + TWENTY);
        g.drawString("6", cx(sq("a", "6")) - TEN, cy(sq("a", "6")) + TWENTY);
        g.drawString("7", cx(sq("a", "7")) - TEN, cy(sq("a", "7")) + TWENTY);
        g.drawString("8", cx(sq("a", "8")) - TEN, cy(sq("a", "8")) + TWENTY);
        g.drawString("9", cx(sq("a", "9")) - TEN, cy(sq("a", "9")) + TWENTY);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawGrid(g);
        Square.SQUARE_LIST.iterator().forEachRemaining(s -> drawPiece(g, s));
    }

    /** Draw the contents of S on G. */
    private void drawPiece(Graphics2D g, Square s) {
        if (_board.get(s).equals(KING)) {
            g.setColor(WHITE_COLOR);
            if (s.equals(first)) {
                g.setColor(Color.red);
            }
            g.fillOval(cx(s) + TWO, cy(s) + TWO, TWENTYSIX, TWENTYSIX);
            g.drawImage(kingPic, cx(s) + 6, cy(s) + 7, null);
        }
        if (_board.get(s).equals(WHITE)) {
            g.setColor(WHITE_COLOR);
            if (s.equals(first)) {
                g.setColor(Color.red);
            }
            g.fillOval(cx(s) + TWO, cy(s) + TWO, TWENTYSIX, TWENTYSIX);
            g.drawImage(whitePic, cx(s) + 6, cy(s) + 7, null);
        } else if (_board.get(s).equals(BLACK)) {
            g.setColor(BLACK_COLOR);
            if (s.equals(first)) {
                g.setColor(Color.red);
            }
            g.fillOval(cx(s) + TWO, cy(s) + TWO, TWENTYSIX, TWENTYSIX);
            g.drawImage(blackPic, cx(s) + 6, cy(s) + 7, null);
        }

    }
    /** Handle a click on S. */
    private void click(Square s) {
        if (first == null) {
            first = s;
        } else {
            if (s.equals(first)) {
                first = null;
            } else {
                Move m = Move.mv(first, s);
                if (m != null) {
                    _commands.add(m.toString());
                    first = null;
                } else {
                    first = s;
                }
            }
        }
        repaint();
    }

    /** Handle mouse click event E. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int xpos = e.getX(), ypos = e.getY();
        int x = (xpos - OFFSET - MARGIN) / SQUARE_SIDE,
                y = (OFFSET - ypos) / SQUARE_SIDE + SIZE - 1;
        if (_acceptingMoves
                && x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            click(sq(x, y));
        }
    }

    /** Revise the displayed board according to BOARD. */
    synchronized void update(Board board) {
        _board.copy(board);
        repaint();

    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.  When move collection is off, ignore clicks on
     *  the board. */
    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
     *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE + OFFSET + MARGIN;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
     *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (SIZE - y - 1) * SQUARE_SIDE + OFFSET;
    }

    /** Return x-pixel coordinate of the left corner of S
     *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
     *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;

    /** Save click. */
    private Square first;

}
