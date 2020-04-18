package controller;

import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import ui.GameCli;

import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {
    private static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final int QoS = 1;

    private static final String GAME_CONFIG_TOPIC = "connect4/configuration";
    private static final String FIELD_CHOOSE_TOPIC = "connect4/board/fieldChoose";

    //client subscribes
    private static final String SERVER_ERROR_TOPIC = "connect4/error";
    private static final String GAME_EXCEPTION_TOPIC = "connect4/exception";
    private static final String GAME_RESULTS_TOPIC = "connect4/result";
    private static final String GAME_OTHERS_TOPIC = "connect4/others";


    private static final String MSG_DELIMITER = ":";

    private static final String WRONG_COLUMN = "WRONG_COLUMN";
    private static final String FULL_COLUMN = "FULL_COLUMN";
    private static final String RESTART_REQUEST = "RESTART_REQUEST";
    private static final String FIELD_REQUEST = "FIELD_REQUEST";
    private static final String DRAW = "DRAW";
    private static final String WINNER = "WINNER";
    private static final String START_GAME = "START_GAME";

    private ConnectFour gameLogic;
    private GameCli gameCli;
    private MqttClient broker;

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(ConnectFour gameLogic, GameCli gameCli) {
        this.gameLogic = gameLogic;
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
                    gameCli.printWrongColumnError();
                else if (message.toString().equals(FULL_COLUMN))
                    gameCli.printFullColumnError();
                else
                    System.out.println("INNY EXCEPTION: " + message.toString());
            } else if (topic.equals(GAME_RESULTS_TOPIC)) {
                if (message.toString().equals(DRAW))
                    gameCli.printDrawMsg();
                else if (message.toString().contains(WINNER)) {
                    String[] decoded = message.toString().split(MSG_DELIMITER);
                    gameCli.printWinnerMsg(decoded[1].charAt(0));
                } else System.out.println("INNY RESULT: " + message.toString());
            } else if (topic.equals(GAME_OTHERS_TOPIC)) {
                if (message.toString().equals(RESTART_REQUEST)) {
                    readRestartInput();
                } else if (message.toString().equals(START_GAME))
                    gameCli.printStartedMsg();
                else if (message.toString().equals(FIELD_REQUEST))
                    readFieldChoose();
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

    private void subscribeTopics() {
        try {
            broker.subscribe(SERVER_ERROR_TOPIC, QoS);
            broker.subscribe(GAME_EXCEPTION_TOPIC, QoS);
            broker.subscribe(GAME_RESULTS_TOPIC, QoS);
            broker.subscribe(GAME_OTHERS_TOPIC, QoS);
            broker.subscribe(GAME_EXCEPTION_TOPIC, QoS);
            broker.subscribe(GAME_RESULTS_TOPIC, QoS);
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
        gameCli.printStartedMsg();
        readFieldChoose();
    }

    private void readRestartInput() {
        boolean restart = gameCli.readRestartGame();
        publish(GAME_CONFIG_TOPIC, RESTART_REQUEST + MSG_DELIMITER + restart);
        if (!restart)
            System.exit(0);
    }

    private void readFieldChoose() {
        gameCli.printActualTurn(gameLogic.getCurrentPlayer());
//            gameCli.printBoard(gameLogic.getBoard());
        int column = gameCli.readColumn();
        publish(FIELD_CHOOSE_TOPIC, Integer.toString(column));
    }
}
