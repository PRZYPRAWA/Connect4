package applicationLogic;

import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;

import java.util.Random;
import java.util.function.Predicate;

public class ConnectFour {
    private Board board;
    private int droppedDiscs;
    private char currentPlayer;

    public final static char FIRST_PLAYER = 'X', SECOND_PLAYER = 'Q', EMPTY = 'o', DRAW = 'd';

    //----------------------------------------------------------------------------------------------------------------//

    public ConnectFour() {
        restartGame();
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void restartGame() {
        board = new Board();
        droppedDiscs = 0;
        startGame();
    }

    private void startGame() {
        Random r = new Random();
        currentPlayer = r.nextBoolean() ? FIRST_PLAYER : SECOND_PLAYER;
    }

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

    public char getCurrentPlayer() {
        return this.currentPlayer;
    }

    public void changePlayer() {
        currentPlayer = getNextPlayer();
    }

    public char getNextPlayer() {
        return currentPlayer == FIRST_PLAYER ? SECOND_PLAYER : FIRST_PLAYER;
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

        if (lastRow < 0 || lastCol < 0)
            return false;

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
        if (board.getLastDiscRow() < 0)
            return false;
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
        if (actualRow < 0)
            return false;
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

    public Board getBoard() {
        return board;
    }
}
