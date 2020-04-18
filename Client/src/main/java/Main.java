import controller.Controller;
import ui.GameCli;

public class Main {
    public static void main(String[] args) {
        new Controller(new GameCli()).startGame();
    }
}
