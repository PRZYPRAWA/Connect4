package applicationLogic;

import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;

import java.util.Random;

public class ConnectFour {
    private Board board;
    private int droppedDiscs = 0;
    private char currentPlayer;

    public final static char FIRST_PLAYER = 'r', SECOND_PLAYER = 'g', EMPTY = 'o', DRAW = 'd';

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

    public char getResult() {
        if (isWin(currentPlayer)) {
            return currentPlayer;
        } else if (boardIsFull()) {
            return DRAW;
        } else {
            return EMPTY;
        }
    }

    private boolean boardIsFull() {
        return droppedDiscs == Board.COLUMNS * Board.ROWS;
    }

    private boolean isWin(char player) {
        int verticalDiscs = 0;
        for (int i = board.getLastDiscRow(); i < Board.ROWS; i++) {
            if (board.getSign(i, board.getLastColumnDropIndex()) == player)
                verticalDiscs++;
            else return (verticalDiscs > 3);
        }
        return (verticalDiscs > 3);
    }

    public void setCurrentPlayer(char currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
