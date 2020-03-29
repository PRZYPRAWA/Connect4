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


}
