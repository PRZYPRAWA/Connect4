import controller.ClientController;
import ui.GameCli;

public class Main {
    public static void main(String[] args) {
        new ClientController(new GameCli()).startGame();
    }
}
