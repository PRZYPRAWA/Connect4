import applicationLogic.ConnectFour;
import controller.Controller;

public class Main {
    public static void main(String[] args) {
        new Controller(new ConnectFour()).connectToMqtt();
    }
}
