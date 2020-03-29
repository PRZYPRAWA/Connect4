import java.lang.reflect.Array;
import java.util.Arrays;

public class Board {
    private static final int ROWS = 7;
    private static final int COLUMNS = 6;
    private char[][] board;

    private char firstPlayer = 'r', secondPlayer = 'g', empty = 'o';

    public Board() {
        board = new char[ROWS][COLUMNS];

        for (int i = 0; i < ROWS; i++) {
            Arrays.fill(board[i], empty);
        }
    }
}
