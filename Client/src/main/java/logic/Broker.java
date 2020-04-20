package logic;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import ui.GameCli;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Broker {
    public static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    public static final int QoS = 1;

    public static final String DELIMITER = ":";

    //messages: field
    public static final String WRONG_COLUMN_MSG = "WRONG_COLUMN";
    public static final String FULL_COLUMN_MSG = "FULL_COLUMN";
    public static final String FIELD_REQUEST_MSG = "FIELD_REQUEST";
    public static final String FIELD_CHOOSE_MSG = "FIELD_CHOOSE";
    public static final String WAITING_FOR_PLAYER_MSG = "WAITING_FOR_PLAYER";
    public static final String OPPONENT_MOVE_MSG = "WAITING_FOR_OPPONENT_MOVE";

    //messages: preparation
    public static final String RESTART_REQUEST_MSG = "RESTART_REQUEST";
    public static final String RESTART_REPLY_MSG = "RESTART_REPLY";
    public static final String CLIENT_CONNECTED_MSG = "CLIENT_CONNECTED";
    public static final String START_GAME = "START_GAME";
    public static final String OTHER_PLAYERS_IN_GAME = "OTHER_PLAYERS_ALREADY_IN_GAME";

    //messages: results
    public static final String DRAW_MSG = "DRAW";
    public static final String WINNER_MSG = "WINNER";
    public static final String END_GAME = "END_GAME";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //topics prefixes
    private static final String SPECIF_PLAYER_TOP = "connect4/player/";
    public static final String ALL_PLAYERS_TOP = "connect4/all/";

    //subscribed topics
    public static final String RESULTS_TOP = "result";
    public static final String BOARD_TOP = "board/look";
    public static final String SERVER_ERROR_TOP = "serverError";

    //published + subscribed
    public static final String FIELD_TOP = "board/field";
    public static final String PREPARE_TOP = "preparation";

    //----------------------------------------------------------------------------------------------------------------//
    private IMqttClient broker;
    private GameCli gameCli;
    private String serverID = "unknown";

    //----------------------------------------------------------------------------------------------------------------//
    public Broker(GameCli gameCli) {
        this.gameCli = gameCli;
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void connect(MqttCallback callback, String serverID) {
        this.serverID = serverID;
        try {
            broker = new MqttClient(Broker.SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.setCallback(callback);
            broker.connect();
            subscribeTopics();
        } catch (MqttException e) {
            criticalErrorAction("Can't connect with MQTT: " + e.getMessage());
        }
    }

    private void subscribeTopics() {
        try {
            broker.subscribe(serverID + "/" + SPECIF_PLAYER_TOP + getClientId() + "/#");
            broker.subscribe(serverID + "/" + ALL_PLAYERS_TOP + "#");
        } catch (MqttException e) {
            criticalErrorAction("Error while subscribing messages via MQTT protocol: " + e.getMessage());
        }
    }

    public void criticalErrorAction(String message) {
        gameCli.connectionError(message);
        gameCli.waitForAnyInput();
        System.exit(1);
    }

    public void publish(String topic, String message) {
        try {
            broker.publish(serverID + "/" + topic, message.getBytes(UTF_8), Broker.QoS, false);
        } catch (MqttException e) {
            System.err.println("Error while publishing message via MQTT protocol: " + e.getMessage());
        }
    }

    public String getClientId() {
        return broker.getClientId();
    }

    public String getPlayerTopic() {
        return Broker.SPECIF_PLAYER_TOP + getClientId() + "/";
    }
}
