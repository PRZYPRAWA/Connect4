import java.util.Arrays;

public class Board {
    public static final int ROWS = 7;
    public static final int COLUMNS = 6;
    private char[][] board;

    //----------------------------------------------------------------------------------------------------------------//
    public Board() {
        board = new char[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; i++) Arrays.fill(board[i], ConnectFour.EMPTY);
    }


    public int getDiscsInColumnQty(int col) {
        int discsQty = 0;
        for (int i = 0; i < ROWS; i++)
            if (board[i][col] != ConnectFour.EMPTY)
                discsQty++;
        return discsQty;
    }

    public void dropDisc(int col, char player) {
        board[getDiscsInColumnQty(col)][col] = player;
    }
}
