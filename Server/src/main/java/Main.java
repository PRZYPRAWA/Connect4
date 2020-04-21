import applicationLogic.ConnectFour;
import controller.ServerController;

public class Main {
    public static void main(String[] args) {
        new ServerController(new ConnectFour()).run();
    }
}
