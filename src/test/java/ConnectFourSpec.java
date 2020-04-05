import applicationLogic.Board;
import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectFourSpec {
    private ConnectFour connectFour;

    @BeforeEach
    void initializeBoard() {
        connectFour = new ConnectFour();
    }

    @Test
    void whenGameInitializeThenDroppedDiscsQtyIs0() {
        assertEquals(0, connectFour.getDroppedDiscsQty());
    }

    @Test
    void whenPlayerSelectsWrongColumnThenThrowException() {
        int col1 = -1;
        int col2 = 6;
        assertThrows(WrongColumnOrRowException.class, () -> connectFour.dropDisc(col1, ConnectFour.FIRST_PLAYER));
        assertThrows(WrongColumnOrRowException.class, () -> connectFour.dropDisc(col2, ConnectFour.SECOND_PLAYER));
    }

    @Test
    void whenColumnIsNotFullThenDiscAddedAndIncrementCounter() throws WrongColumnOrRowException, FullColumnException {
        int col = 0;
        connectFour.dropDisc(col, ConnectFour.FIRST_PLAYER);
        assertEquals(1, connectFour.getDroppedDiscsQty());
        assertEquals(1, connectFour.getDiscsInColumnQty(col));
    }

    @Test
    void whenColumnIsFullThenThrowException() throws WrongColumnOrRowException, FullColumnException {
        int col = 0;
        for (int i = 0; i < Board.ROWS; i++)
            connectFour.dropDisc(col, ConnectFour.FIRST_PLAYER);
        assertThrows(FullColumnException.class, () -> connectFour.dropDisc(col, ConnectFour.FIRST_PLAYER));
    }

    @Test
    void whenNoMoreDiscsCanBeThrownTheGameShouldEndInDraw() throws WrongColumnOrRowException, FullColumnException {
        int size = Board.COLUMNS * Board.ROWS;

        for (int i = 0; i < Board.COLUMNS; i++)
            for (int j = 0; j < Board.ROWS; j++)
                connectFour.dropDisc(i, ConnectFour.FIRST_PLAYER);

        assertEquals(connectFour.getDroppedDiscsQty(), size);
        assertEquals(connectFour.getResult(), connectFour.DRAW);
    }

    @Test
    void whenMoreThan3DiscsVerticalThenCurrentPlayerShouldWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 4; i++)
            connectFour.dropDisc(1, ConnectFour.FIRST_PLAYER);
        assertEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenDiscsAreNotInOneLineThenPlayerShouldNotWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 2; i++)
            connectFour.dropDisc(1, ConnectFour.FIRST_PLAYER);
        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        for (int i = 0; i < 2; i++)
            connectFour.dropDisc(1, ConnectFour.FIRST_PLAYER);

        assertEquals(ConnectFour.EMPTY, connectFour.getResult());
    }

    @Test
    void whenMoreThan3DiscsHorizontalFromLeftThenCurrentPlayerShouldWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 4; i++)
            connectFour.dropDisc(i, ConnectFour.FIRST_PLAYER);
        assertEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenMoreThan3DiscsHorizontalFromRightThenCurrentPlayerShouldWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 4; i++)
            connectFour.dropDisc(Board.COLUMNS - i - 1, ConnectFour.FIRST_PLAYER);
        assertEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenMoreThan3DiscsHorizontalInCenterThenCurrentPlayerShouldWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 2; i++)
            connectFour.dropDisc(i, ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 2; i++)
            connectFour.dropDisc(Board.COLUMNS - 2 - i, ConnectFour.FIRST_PLAYER);
        connectFour.dropDisc(2, ConnectFour.FIRST_PLAYER);
        assertEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenDiscsNotInHorizontalLinePlayerShouldNotWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);
        for (int i = 0; i < 2; i++)
            connectFour.dropDisc(i, ConnectFour.FIRST_PLAYER);
        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);
        for (int i = 3; i < 5; i++)
            connectFour.dropDisc(i, ConnectFour.FIRST_PLAYER);
        assertNotEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenMoreThan3DiscsInRightDiagonalPlayerShouldWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(0, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(1, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(2, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(3, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(3, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(3, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(3, ConnectFour.FIRST_PLAYER);

        assertEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenMoreThan3DiscsInLeftDiagonalPlayerShouldWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(3, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(2, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(1, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.FIRST_PLAYER);

        assertEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }

    @Test
    void whenDiscsNotInLeftDiagonalPlayerShouldNotWin() throws WrongColumnOrRowException, FullColumnException {
        connectFour.setCurrentPlayer(ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(4, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(3, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(3, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(2, ConnectFour.SECOND_PLAYER);

        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(1, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(1, ConnectFour.FIRST_PLAYER);

        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.SECOND_PLAYER);
        connectFour.dropDisc(0, ConnectFour.FIRST_PLAYER);

        assertNotEquals(ConnectFour.FIRST_PLAYER, connectFour.getResult());
    }
}
