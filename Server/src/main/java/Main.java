import applicationLogic.ConnectFour;
import controller.Server;

public class Main {
    public static void main(String[] args) {
        new Server(new ConnectFour()).connectToMqtt();
    }
}
