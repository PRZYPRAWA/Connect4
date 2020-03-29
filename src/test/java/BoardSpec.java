import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class BoardSpec {

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
    void whenTheGameStartsAndPlayerDropsDiscThenAnotherPlayerDrops() throws  WrongColumnOrRowException, FullColumnException {
        connectFour.startGame();
        char firstPlayer = connectFour.getCurrentPlayer();

        int col = 0;
        connectFour.nextTurn(col);
        char secondPlayer = connectFour.getCurrentPlayer();

        assertNotEquals(firstPlayer, secondPlayer);
    }

    @Test
    void whenTheColumnIsFullThenReturnsProperString() {
        int col = 0;
        for (int i = 0; i < Board.ROWS; i++)
            connectFour.nextTurn(col);
        assertEquals(connectFour.COLUMN_IS_FULL, connectFour.nextTurn(col));
    }
}
