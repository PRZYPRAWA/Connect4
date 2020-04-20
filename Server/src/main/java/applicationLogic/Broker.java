package applicationLogic;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Broker {
    public static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    public static final int QoS = 1;

    public static final String DELIMITER = ":";
    private static final String SERVER_ID_PREFIX = "serv";

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
    public static final String SPECIF_PLAYER_TOP = "connect4/player/";
    public static final String ALL_PLAYERS_TOP = "connect4/all/";

    //published topics
    public static final String RESULTS_TOP = "result";
    public static final String BOARD_TOP = "board/look";
    public static final String ERROR_TOP = "serverError";

    //published + subscribed
    public static final String FIELD_TOP = "board/field";
    public static final String PREPARE_TOP = "preparation";

    //----------------------------------------------------------------------------------------------------------------//
    private IMqttClient broker;

    //----------------------------------------------------------------------------------------------------------------//
    public void connect(MqttCallback callback) {
        try {
            broker = new MqttClient(Broker.SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.setCallback(callback);
            broker.connect();
            broker.subscribe(getServerId() + "/" + Broker.SPECIF_PLAYER_TOP + "#", Broker.QoS); //all topics from player
        } catch (MqttException e) {
            criticalErrorAction("Can't connect with MQTT: " + e.getMessage());
        }
    }

    private void criticalErrorAction(String message) {
        System.err.println(message);
        publish(Broker.ERROR_TOP, message);
        System.exit(1);
    }

    public void publish(String topic, String message) {
        try {
            broker.publish(getServerId() + "/" + topic, message.getBytes(UTF_8), Broker.QoS, false);
        } catch (MqttException e) {
            System.err.println("Error while publishing message via MQTT protocol: " + e.getMessage());
            System.exit(1);
        }
    }

    public String getServerId() {
        return SERVER_ID_PREFIX + broker.getClientId();
    }
}
