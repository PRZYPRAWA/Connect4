package applicationLogic;

import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;

import java.util.Random;
import java.util.function.Predicate;

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
        if (isWin(currentPlayer))
            return currentPlayer;
        else return (boardIsFull()) ? DRAW : EMPTY;
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
        return isDiagonal(player, true) || isDiagonal(player, false);
    }

    private boolean isDiagonal(char player, boolean isRight) {
        int actualRow = board.getLastDiscRow();
        int discsAmount = 0;

        Predicate<Integer> condition = row -> row >= 0 && row < Board.ROWS;

        for (int column = board.getLastDiscColumn(); column < Board.COLUMNS && condition.test(actualRow); column++) {
            if (board.getSign(actualRow, column) == player) {
                discsAmount++;
                if (isRight) actualRow--;
                else actualRow++;
            } else break;
        }
        actualRow = board.getLastDiscRow();
        for (int column = board.getLastDiscColumn(); column >= 0 && condition.test(actualRow); column--) {
            if (board.getSign(actualRow, column) == player) {
                discsAmount++;
                if (isRight) actualRow++;
                else actualRow--;
            } else break;
        }
        discsAmount--;
        return discsAmount > 3;
    }

    public void setCurrentPlayer(char currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
