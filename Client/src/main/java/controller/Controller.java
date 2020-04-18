package controller;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import ui.GameCli;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {
    public final static char FIRST_PLAYER_SIGN = 'X', SECOND_PLAYER_SIGN = 'Q', DRAW_SIGN = 'd'; //EMPTY = 'o',

    private static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final int QoS = 1;

    private static final String GAME_CONFIG_TOPIC = "connect4/configuration";
    private static final String FIELD_CHOOSE_TOPIC = "connect4/board/field/choose";

    //client subscribes
    private static final String SERVER_ERROR_TOPIC = "connect4/error";
    private static final String GAME_EXCEPTION_TOPIC = "connect4/exception";
    private static final String GAME_RESULTS_TOPIC = "connect4/result";
    private static final String GAME_OTHERS_TOPIC = "connect4/others";
    private static final String FIELD_REQUEST_TOPIC = "connect4/board/field/request";
    private static final String BOARD_LOOK_TOPIC = "connect4/board/look";
    private static final String PLAYER_SIGN_TOPIC = "connect4/playerSign";


    private static final String MSG_DELIMITER = ":";

    private static final String WRONG_COLUMN = "WRONG_COLUMN";
    private static final String FULL_COLUMN = "FULL_COLUMN";
    private static final String RESTART_REQUEST = "RESTART_REQUEST";
    //    private static final String FIELD_REQUEST = "FIELD_REQUEST";
    private static final String DRAW = "DRAW";
    private static final String WINNER = "WINNER";
    private static final String START_GAME = "START_GAME";
    private static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";

    private GameCli gameCli;
    private MqttClient broker;
    private char playerSign = '\0';
    private int boardColumnsQty = 0;
    private int boardRowsQty = 0;

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(GameCli gameCli) {
        this.gameCli = gameCli;
    }

    //----------------------------------------------------------------------------------------------------------------//
    private class MqttCallbackHandler implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            //todo:
            //Called when the client lost the connection to the broker
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            if (topic.equals(GAME_EXCEPTION_TOPIC)) {
                if (message.toString().equals(WRONG_COLUMN))
                    gameCli.printWrongColumnError(boardColumnsQty);
                else if (message.toString().equals(FULL_COLUMN))
                    gameCli.printFullColumnError(boardColumnsQty);
                else
                    System.out.println("INNY EXCEPTION: " + message.toString());
            } else if (topic.equals(GAME_RESULTS_TOPIC)) {
                if (message.toString().equals(DRAW))
                    gameCli.printDrawMsg(boardColumnsQty);
                else if (message.toString().contains(WINNER)) {
                    String[] decoded = message.toString().split(MSG_DELIMITER);
                    gameCli.printWinnerMsg(decoded[1].charAt(0), boardColumnsQty);
                } else System.out.println("INNY RESULT: " + message.toString());
            } else if (topic.equals(FIELD_REQUEST_TOPIC))
                readFieldChoose();
            else if (topic.equals(BOARD_LOOK_TOPIC)) {
                char[][] board = decodeBoardLookMsg(message.toString());
                boardColumnsQty = board[0].length;
                boardRowsQty = board.length;
                gameCli.printBoard(board);
            } else if (topic.equals(PLAYER_SIGN_TOPIC)) {
                playerSign = message.toString().charAt(0);
                gameCli.printStartedMsg(boardColumnsQty); //todo: tutaj napis nie wysrodkuje bo bedzie 0
            } else if (topic.equals(GAME_OTHERS_TOPIC)) {
                if (message.toString().equals(RESTART_REQUEST)) {
                    readRestartInput();
                } else if (message.toString().equals(START_GAME))
                    gameCli.printStartedMsg(boardColumnsQty);
                else System.out.println("INNY: " + message.toString());
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
    private void connectToMqtt() {
        try {
            broker = new MqttClient(SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.setCallback(new MqttCallbackHandler());
            broker.connect();
            subscribeTopics();
        } catch (MqttException e) {
            gameCli.serverError("Can't connect with MQTT protocol: " + e.getMessage());
            gameCli.waitForAnyInput();
            System.exit(1);
        }
    }

    //todo: dodac rozlaczenie
    private void disconnect() {
        try {
            broker.disconnect();
        } catch (MqttException e) {
            gameCli.serverError("Can't disconnect with MQTT protocol: " + e.getMessage());
        }
    }

    private void subscribeTopics() {
        try {
            broker.subscribe(SERVER_ERROR_TOPIC, QoS);
            broker.subscribe(GAME_EXCEPTION_TOPIC, QoS);
            broker.subscribe(GAME_RESULTS_TOPIC, QoS);
            broker.subscribe(GAME_OTHERS_TOPIC, QoS);
            broker.subscribe(GAME_EXCEPTION_TOPIC, QoS);
            broker.subscribe(GAME_RESULTS_TOPIC, QoS);
            broker.subscribe(FIELD_REQUEST_TOPIC);
            broker.subscribe(BOARD_LOOK_TOPIC);
            broker.subscribe(PLAYER_SIGN_TOPIC);
        } catch (MqttException e) {
            gameCli.serverError("Error while subscribing message via MQTT protocol: " + e.getMessage());
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
        publish(GAME_CONFIG_TOPIC, CLIENT_CONNECTED);
    }

    private void readRestartInput() {
        boolean restart = gameCli.readRestartGame(boardColumnsQty);
        publish(GAME_CONFIG_TOPIC, RESTART_REQUEST + MSG_DELIMITER + restart);
        if (!restart)
            System.exit(0);
    }

    private void readFieldChoose() {
        gameCli.printActualTurn(playerSign, boardColumnsQty);
        int column = gameCli.readColumn();
        publish(FIELD_CHOOSE_TOPIC, Integer.toString(column));
    }

    private char[][] decodeBoardLookMsg(String msg) {
        String[] decoded = msg.split(MSG_DELIMITER);
        int rows = 0, columns = 0;
        try {
            rows = Integer.parseInt(decoded[0]);
            columns = Integer.parseInt(decoded[1]);
        } catch (NumberFormatException e) {
            gameCli.serverError("Error while publishing message via MQTT protocol: " + e.getMessage());
            gameCli.waitForAnyInput();
            System.exit(1);
        }
        char[][] board = new char[rows][columns];
        for (int row = 0; row < rows; row++)
            for (int col = 0; col < columns; col++)
                board[row][col] = decoded[columns * row + col + 2].charAt(0);
        return board;
    }
}
