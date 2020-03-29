import java.util.Random;

public class ConnectFour {
    private Board board;
    private int droppedDiscs = 0;
    private char currentPlayer;

    public final static char FIRST_PLAYER = 'r', SECOND_PLAYER = 'g', EMPTY = 'o';

    public static final String COLUMN_IS_FULL = "COLUMN IS FULL", WRONG_COLUMN = "WRONG COLUMN";

    //----------------------------------------------------------------------------------------------------------------//
    public ConnectFour() {
        this.board = new Board();
    }

    //----------------------------------------------------------------------------------------------------------------//
    public int getDroppedDiscsQty() {
        return droppedDiscs;
    }

    public void dropDisc(int col, char player) throws WrongColumnOrRowException, FullColumnException {
        if (col < 0 || col >= Board.COLUMNS)
            throw new WrongColumnOrRowException();
        if (getDiscsInColumnQty(col) >= board.ROWS) {
            throw new FullColumnException();
        }
        board.dropDisc(col, player);
        droppedDiscs++;
    }

    public int getDiscsInColumnQty(int col) {
        return board.getDiscsInColumnQty(col);
    }

    public void startGame() {
        Random r = new Random();
        if (r.nextBoolean()) {
            currentPlayer = FIRST_PLAYER;
        } else {
            currentPlayer = SECOND_PLAYER;
        }
    }

    public void nextTurn(int col) throws WrongColumnOrRowException, FullColumnException {

        dropDisc(col, currentPlayer);


        if (currentPlayer == FIRST_PLAYER) {
            currentPlayer = SECOND_PLAYER;
        } else {
            currentPlayer = FIRST_PLAYER;
        }
    }

    public char getCurrentPlayer() {
        return this.currentPlayer;
    }
}
