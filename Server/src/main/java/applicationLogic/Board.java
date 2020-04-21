package applicationLogic;

import java.util.Arrays;

public class Board {
    public static final int ROWS = 7;
    public static final int COLUMNS = 6;
    private char[][] board;
    private int lastColumnDropIndex = -1;

    //----------------------------------------------------------------------------------------------------------------//
    public Board() {
        board = new char[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; i++) Arrays.fill(board[i], ConnectFour.EMPTY);
    }

    //----------------------------------------------------------------------------------------------------------------//
    public int getDiscsInColumnQty(int col) {
        int discsQty = 0;
        for (int i = 0; i < ROWS; i++)
            if (board[i][col] != ConnectFour.EMPTY)
                discsQty++;
        return discsQty;
    }

    /**
     * @return row index of dropped disc
     */
    public int dropDisc(int col, char player) {
        int rowIndex = getDiscsInColumnQty(col);
        board[rowIndex][col] = player;
        lastColumnDropIndex = col;
        return rowIndex;
    }

    public int getLastDiscRow() {
        if (lastColumnDropIndex < 0 || lastColumnDropIndex > COLUMNS - 1)
            return -1;

        for (int i = ROWS - 1; i >= 0; i--) {
            if (board[i][lastColumnDropIndex] != ConnectFour.EMPTY)
                return ROWS - i - 1;
        }
        return -1;
    }

    public char getSign(int row, int column) {
        return board[ROWS - row - 1][column];
    }

    public int getLastDiscColumn() {
        return lastColumnDropIndex;
    }

}
