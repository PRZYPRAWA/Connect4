package applicationLogic;

import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;

import java.util.Random;

public class ConnectFour {
    private Board board;
    private int droppedDiscs = 0;
    private char currentPlayer;

    public final static char FIRST_PLAYER = 'r', SECOND_PLAYER = 'g', EMPTY = 'o';

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
        currentPlayer = r.nextBoolean() ? FIRST_PLAYER : SECOND_PLAYER;
    }

    public char getCurrentPlayer() {
        return this.currentPlayer;
    }

    public void changePlayer() {
        currentPlayer = currentPlayer == FIRST_PLAYER ? SECOND_PLAYER : FIRST_PLAYER;
    }
}
