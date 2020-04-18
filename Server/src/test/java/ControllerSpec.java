import applicationLogic.ConnectFour;
import controller.Controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ControllerSpec {
    private Controller gameController;

    @BeforeEach
    void initialize() {
        gameController = new Controller(new ConnectFour());
    }

    @Test
    void whenTheGameStartsAndPlayerDropsDiscThenAnotherPlayerDrops() {
        char firstPlayer = gameController.getCurrentPlayer();
        int col = 0;
        gameController.nextTurn(col);
        char secondPlayer = gameController.getCurrentPlayer();
        assertNotEquals(firstPlayer, secondPlayer);
    }
}
