package controller;

import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import ui.GameCli;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {
    private static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final int QoS = 1;
    private static final String FIELD_CHOOSE_TOPIC = "connect4/board/fieldChoose";

    private ConnectFour gameLogic;
    private GameCli gameCli;
    private MqttClient broker;

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(ConnectFour gameLogic, GameCli gameCli) {
        this.gameLogic = gameLogic;
        this.gameCli = gameCli;
    }

    //----------------------------------------------------------------------------------------------------------------//
    private void connectToMqtt() {
        try {
            broker = new MqttClient(SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.connect();
        } catch (MqttException e) {
            gameCli.serverError("Can't connect with MQTT protocol: " + e.getMessage());
            gameCli.waitForAnyInput();
            System.exit(1);
        }
    }

    private void publish(String topic, String message) {
        try {
            broker.publish(topic, message.getBytes(UTF_8), QoS, false);
        } catch (MqttException e) {
            gameCli.serverError("Error while publishing message via MQTT protocol: " + e.getMessage());
            gameCli.waitForAnyInput();
            System.exit(1);
        }
    }

    public void startGame() {
        connectToMqtt();
        gameCli.printStartedMsg();
        while (true) {
            gameCli.printActualTurn(gameLogic.getCurrentPlayer());
            gameCli.printBoard(gameLogic.getBoard());
            int column = gameCli.readColumn();
            publish(FIELD_CHOOSE_TOPIC, Integer.toString(column));
            //todo: dodac odbieranie planszy
        }
    }
}
