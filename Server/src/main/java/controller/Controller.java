package controller;

import applicationLogic.Board;
import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {
    private static final int QoS = 1;
    private static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String SERVER_ERROR_TOPIC = "connect4/error";
    private static final String GAME_EXCEPTION_TOPIC = "connect4/exception";
    private static final String GAME_RESULTS_TOPIC = "connect4/result";
    private static final String GAME_OTHERS_TOPIC = "connect4/others";

    private static final String FIELD_REQUEST_TOPIC = "connect4/board/field/request";
    private static final String BOARD_LOOK_TOPIC = "connect4/board/look";
    private static final String PLAYER_SIGN_TOPIC = "connect4/playerSign";

    //server subcription
    private static final String FIELD_CHOOSE_TOPIC = "connect4/board/field/choose";
    private static final String GAME_CONFIG_TOPIC = "connect4/configuration";

    private static final String MSG_DELIMITER = ":";

    private static final String WRONG_COLUMN = "WRONG_COLUMN";
    private static final String FULL_COLUMN = "FULL_COLUMN";
    private static final String RESTART_GAME_REQUEST = "RESTART_REQUEST";
    private static final String DRAW = "DRAW";
    private static final String WINNER = "WINNER";
    private static final String START_GAME = "START_GAME";
    private static final String FIELD_REQUEST = "FIELD_REQUEST";
    private static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";


    private ConnectFour gameLogic;
    private IMqttClient broker;
    private List<Character> availablePlayerSigns;

    //----------------------------------------------------------------------------------------------------------------//
    private class MqttCallbackHandler implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            //todo:
            //Called when the client lost the connection to the broker
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            if (topic.equals(FIELD_CHOOSE_TOPIC)) {
                try {
                    int column = Integer.parseInt(message.toString());
                    nextTurn(column);
                } catch (NumberFormatException e) {
                    publish(GAME_EXCEPTION_TOPIC, WRONG_COLUMN);
                    publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
                    publish(FIELD_REQUEST_TOPIC, FIELD_REQUEST);
                }
            } else if (topic.equals(GAME_CONFIG_TOPIC)) {
                if (message.toString().contains(RESTART_GAME_REQUEST)) {
                    String[] decoded = message.toString().split(MSG_DELIMITER);
                    boolean restart = Boolean.parseBoolean(decoded[1]);
                    finishOrRestartGame(restart);
                    publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
                    publish(FIELD_REQUEST_TOPIC, FIELD_REQUEST);
                } else if (message.toString().equals(CLIENT_CONNECTED)) {
                    publish(PLAYER_SIGN_TOPIC, Character.toString(getRandomAvailableSign())); //todo: dodac kontrole na max 2 klientow
                    publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
                    publish(FIELD_REQUEST_TOPIC, FIELD_REQUEST);
                } else System.out.println("OTHER GAME CONFIG:" + message.toString());
            }
            System.out.println(topic + ": " + message.toString());
        }


        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //todo
            //Called when a outgoing publish is complete
        }
    }

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(ConnectFour gameLogic) {
        this.gameLogic = gameLogic;
        availablePlayerSigns = new ArrayList<>(Arrays.asList(ConnectFour.FIRST_PLAYER, ConnectFour.SECOND_PLAYER));
    }

    public void connectToMqtt() {
        try {
            broker = new MqttClient(SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.setCallback(new MqttCallbackHandler());
            broker.connect();
            broker.subscribe(FIELD_CHOOSE_TOPIC, QoS);
            broker.subscribe(GAME_CONFIG_TOPIC, QoS);
        } catch (MqttException e) {
            internalError("Server can't connect with MQTT: " + e.getMessage());
        }
    }

    private void publish(String topic, String message) {
        try {
            broker.publish(topic, message.getBytes(UTF_8), QoS, false);
        } catch (MqttException e) {
            System.out.println("Error while publishing message via MQTT protocol: " + e.getMessage());
            System.exit(1);
        }
    }

    private void disconnect() {
        try {
            broker.disconnect();
        } catch (MqttException e) {
            internalError("Can't disconnect with MQTT protocol: " + e.getMessage());
        }
    }

    public void nextTurn(int col) {
        try {
            gameLogic.dropDisc(col, gameLogic.getCurrentPlayer());
        } catch (FullColumnException e) {
            publish(GAME_EXCEPTION_TOPIC, FULL_COLUMN);
            publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
            publish(FIELD_REQUEST_TOPIC, FIELD_REQUEST);
            return;
        } catch (WrongColumnOrRowException e) {
            publish(GAME_EXCEPTION_TOPIC, WRONG_COLUMN);
            publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
            publish(FIELD_REQUEST_TOPIC, FIELD_REQUEST);
            return;
        }
        checkGameStatus();
        gameLogic.changePlayer();
    }

    private void checkGameStatus() {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY) {
            if (result == ConnectFour.DRAW)
                publish(GAME_RESULTS_TOPIC, DRAW);
            else
                publish(GAME_RESULTS_TOPIC, WINNER + MSG_DELIMITER + result);
            publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
            publish(GAME_OTHERS_TOPIC, RESTART_GAME_REQUEST);
        } else {
            publish(BOARD_LOOK_TOPIC, getBoardLookMsg());
            publish(FIELD_REQUEST_TOPIC, FIELD_REQUEST);
        }
    }

    private void finishOrRestartGame(boolean restart) {
        if (restart) {
            gameLogic.restartGame();
            publish(GAME_OTHERS_TOPIC, START_GAME);
        } else {
            disconnect();
            System.exit(0);
        }

    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }

    private void internalError(String msg) {
        String message = "INTERNAL ERROR: " + msg;
        System.out.println(message);
        publish(SERVER_ERROR_TOPIC, message);
        System.exit(1);
    }


    private String getBoardLookMsg() {
        //todo: zoptymalizowac
        String msg = Board.ROWS + MSG_DELIMITER + Board.COLUMNS;
        for (int row = 0; row < Board.ROWS; row++)
            for (int col = 0; col < Board.COLUMNS; col++)
                msg += MSG_DELIMITER + gameLogic.getBoard().getSign(row, col);
        return msg;
    }

    private char getRandomAvailableSign() {
        int randIndex = (int) (Math.random() * availablePlayerSigns.size());
        char sign = availablePlayerSigns.get(randIndex);
        availablePlayerSigns.remove(randIndex);
        return sign;
    }

}
