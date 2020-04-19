package controller;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import ui.GameCli;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {
    public final static char FIRST_PLAYER_SIGN = 'X', SECOND_PLAYER_SIGN = 'Q', DRAW_SIGN = 'd'; //EMPTY = 'o',

    private GameCli gameCli;
    private MqttClient broker;
    private MqttProperty property;

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
            //todo: tmp
            System.out.println(topic + " = " + message.toString());


            String textMessage = message.toString();
            if (topic.equals(MqttProperty.BOARD_LOOK_TOPIC)) {
                char[][] board = decodeBoardLookMsg(message.toString());
                gameCli.setBoardColumnsQty(board[0].length);
                gameCli.printBoard(board);
            } else if (topic.equals(property.getFieldTopic()))
                fieldTopicMsg(textMessage);
            else if (topic.equals(MqttProperty.RESULTS_TOPIC))
                resultTopicMsg(textMessage);
            else if (topic.equals(property.getPreparationTopic()))
                preparationTopicMsg(textMessage);
            else if (topic.equals(MqttProperty.SERVER_ERROR_TOPIC))
                criticalErrorAction("Server internal error: " + textMessage);
        }

        private char[][] decodeBoardLookMsg(String msg) {
            String[] decoded = msg.split(MqttProperty.DELIMITER);
            int rows = getNumberFromMsg(decoded[0]);
            int columns = getNumberFromMsg(decoded[1]);
            char[][] board = new char[rows][columns];

            for (int row = 0; row < rows; row++)
                for (int col = 0; col < columns; col++)
                    board[row][col] = decoded[columns * row + col + 2].charAt(0);
            return board;
        }

        private int getNumberFromMsg(String number) {
            int result = 0;
            try {
                result = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                criticalErrorAction("Cant read board look from server " + e.getMessage());
            }
            return result;
        }

        private void resultTopicMsg(String message) {
            switch (message) {
                case MqttProperty.DRAW_MSG:
                    gameCli.printDrawMsg();
                    break;
                case MqttProperty.WINNER_MSG: {
                    String[] decoded = message.split(MqttProperty.DELIMITER);
                    gameCli.printWinnerMsg(decoded[1].charAt(0));
                }
                break;
            }
        }

        private void fieldTopicMsg(String message) {
            switch (message) {
                case MqttProperty.FIELD_REQUEST_MSG:
                    fieldChooseInput();
                    break;
                case MqttProperty.WRONG_COLUMN_MSG:
                    gameCli.printWrongColumnError();
                    break;
                case MqttProperty.FULL_COLUMN_MSG:
                    gameCli.printFullColumnError();
                    break;
            }
        }

        private void preparationTopicMsg(String message) {
            if (message.equals(MqttProperty.WAITING_FOR_PLAYER_MSG)) {
                gameCli.printWaitingForPlayers();
            } else if (message.contains(MqttProperty.GIVEN_SIGN_MSG)) {
                String[] splited = message.split(MqttProperty.DELIMITER);
                property.setPlayerSign(splited[1].charAt(0));
                gameCli.printStartedMsg();
            } else if (message.equals(MqttProperty.START_GAME))
                gameCli.printStartedMsg();
            else if (message.equals(MqttProperty.RESTART_REQUEST_MSG))
                restartInput();
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
            String clientID = MqttClient.generateClientId();
            broker = new MqttClient(MqttProperty.SERVER_URI, clientID, new MemoryPersistence());
            this.property = new MqttProperty(clientID);
            broker.setCallback(new MqttCallbackHandler());
            broker.connect();
            subscribeTopics();
        } catch (MqttException e) {
            criticalErrorAction("Can't connect with MQTT protocol: " + e.getMessage());
        }
    }

    private void criticalErrorAction(String message) {
        gameCli.connectionError(message);
        gameCli.waitForAnyInput();
        System.exit(1);
    }

    private void subscribeTopics() {
        try {
            broker.subscribe(MqttProperty.RESULTS_TOPIC, MqttProperty.QoS);
            broker.subscribe(MqttProperty.BOARD_LOOK_TOPIC, MqttProperty.QoS);
            broker.subscribe(property.getPreparationTopic(), MqttProperty.QoS);
            broker.subscribe(property.getFieldTopic(), MqttProperty.QoS);
            broker.subscribe(MqttProperty.SERVER_ERROR_TOPIC, MqttProperty.QoS);
        } catch (MqttException e) {
            criticalErrorAction("Error while subscribing message via MQTT protocol: " + e.getMessage());
        }
    }

    private void publish(String topic, String message) {
        try {
            broker.publish(topic, message.getBytes(UTF_8), MqttProperty.QoS, false);
        } catch (MqttException e) {
            criticalErrorAction("Error while publishing message via MQTT protocol: " + e.getMessage());
        }
    }

    public void startGame() {
        connectToMqtt();
        String message = MqttProperty.CLIENT_CONNECTED_MSG + MqttProperty.DELIMITER + broker.getClientId();
        publish(property.getPreparationTopic(), message);
    }

    private void restartInput() {
        boolean restart = gameCli.restartGameInput();
        publish(property.getPreparationTopic(), MqttProperty.RESTART_REPLY_MSG + MqttProperty.DELIMITER + restart);
        if (!restart) {
//            disconnect(); //todo: daje wyjątek (?)
            System.exit(0);
        }
    }

    private void disconnect() {
        try {
            broker.disconnect();
        } catch (MqttException e) {
            gameCli.connectionError("Can't disconnect with MQTT protocol: " + e.getMessage());
        }
    }

    private void fieldChooseInput() {
        gameCli.printActualTurn(property.getPlayerSign());
        publish(property.getFieldTopic(), MqttProperty.FIELD_CHOOSE_MSG + MqttProperty.DELIMITER + gameCli.readColumn());
    }


}
