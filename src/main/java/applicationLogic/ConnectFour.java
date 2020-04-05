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

    public boolean isWin(char player) {
        return isHorizontal(player) || isVertical(player) || isDiagonal(player);
    }

    private boolean isHorizontal(char player) {
        int discAmount = 0;

        int lastRow = board.getLastDiscRow();
        int lastCol = board.getLastDiscColumn();

        for (int i = lastCol; i < Board.COLUMNS; i++) {
            if (board.getSign(lastRow, i) == player)
                discAmount++;
            else break;
        }
        for (int i = lastCol - 1; i >= 0; i--) {
            if (board.getSign(lastRow, i) == player)
                discAmount++;
            else break;
        }

        return discAmount > 3;
    }

    private boolean isVertical(char player) {
        int discAmount = 0;
        for (int i = board.getLastDiscRow(); i < Board.ROWS; i++) {
            if (board.getSign(i, board.getLastDiscColumn()) == player)
                discAmount++;
            else return (discAmount > 3);
        }
        return (discAmount > 3);
    }

    private boolean isDiagonal(char player) {
        return isRightDiagonal(player) || isLeftDiagonal(player);
    }

    private boolean isRightDiagonal(char player) {
        int actualRow = board.getLastDiscRow();
        int discsQty = 0;
        for (int column = board.getLastDiscColumn(); column < Board.COLUMNS && actualRow >= 0; column++) {
            if (board.getSign(actualRow, column) == player) {
                discsQty++;
                actualRow--;
            } else break;
        }
        actualRow = board.getLastDiscRow();
        for (int column = board.getLastDiscColumn(); column >= 0 && actualRow < Board.ROWS; column--) {
            if (board.getSign(actualRow, column) == player)
                discsQty++;
            else break;
            actualRow++;
        }
        discsQty--;
        return discsQty > 3;
    }

    private boolean isLeftDiagonal(char player) {
        int actualRow = board.getLastDiscRow();
        int discsQty = 0;
        for (int column = board.getLastDiscColumn(); column >= 0 && actualRow >= 0; column--) {
            if (board.getSign(actualRow, column) == player) {
                discsQty++;
                actualRow--;
            } else break;
        }
        actualRow = board.getLastDiscRow();
        for (int column = board.getLastDiscColumn(); column < Board.COLUMNS && actualRow < Board.ROWS; column++) {
            if (board.getSign(actualRow, column) == player)
                discsQty++;
            else break;
            actualRow++;
        }
        discsQty--;
        return discsQty > 3;
    }

    public void setCurrentPlayer(char currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
