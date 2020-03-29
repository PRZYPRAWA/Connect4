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
    void whenPlayerSelectsWrongCloumnThenThrowException() {
        int col1 = -1;
        int col2 = 6;

        assertThrows(WrongColumnOrRowException.class, () -> connectFour.dropDisc(col1));
        assertThrows(WrongColumnOrRowException.class, () -> connectFour.dropDisc(col2));
    }

}
