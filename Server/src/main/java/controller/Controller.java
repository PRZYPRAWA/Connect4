package controller;

import applicationLogic.Board;
import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

//todo: fix tests
public class Controller {
    private ConnectFour gameLogic;
    private IMqttClient broker;
    private MqttCallbackHandler callbackHandler;

    //    private List<Character> availablePlayerSigns;
    private Map<Character, String> signWitClientID;

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
            System.out.println("SEC:" + messageFromCurrentPlayer(topic));
            System.out.println("1) " + topic + " = " + message.toString());

            //todo: dac komunikat ze juz jest 2 graczy
            String textMessage = message.toString();
            if (topic.contains(MqttProperty.SPECIFIED_PLAYER_TOPICS) && (messageFromCurrentPlayer(topic) || !isCorrectPlayersQty()))
                checkTopicsForSpecifiedPlayer(topic, textMessage, getCurrentPlayer());
        }

        private boolean messageFromCurrentPlayer(String topic) {
            String currentPlayerID;
            if (signWitClientID.get(getCurrentPlayer()) == null)
                currentPlayerID = "null";
            else currentPlayerID = signWitClientID.get(getCurrentPlayer());
            return topic.contains(currentPlayerID);
        }

        private boolean isCorrectPlayersQty() {
            return signWitClientID.size() == 2;
        }

        private void checkTopicsForSpecifiedPlayer(String topic, String message, char player) {
            if (topic.contains(MqttProperty.FIELD_TOPIC)) {
                if (message.contains(MqttProperty.FIELD_CHOOSE_MSG))
                    processFieldChooseMsg(message, player);
            } else if (topic.contains(MqttProperty.PLAYER_PREPARATION_TOPIC)) {
                if (message.contains(MqttProperty.CLIENT_CONNECTED_MSG))
                    processClientConnectedMsg(player, message);
                else if (message.equals(MqttProperty.RESTART_REPLY_MSG))
                    processRestartReplyMsg(message);
                else if (message.equals(MqttProperty.START_GAME)) {
                    String topicPrefix = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
                    publish(topicPrefix + MqttProperty.PLAYER_PREPARATION_TOPIC, MqttProperty.START_GAME);
                }
            }
        }

        private void processFieldChooseMsg(String message, char player) {
            String topicPrefix = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            try {
                String[] splitedMsg = message.split(MqttProperty.DELIMITER);
                int column = Integer.parseInt(splitedMsg[1]);
                nextTurn(column, player);
            } catch (NumberFormatException e) {
                publish(topicPrefix + MqttProperty.FIELD_TOPIC, MqttProperty.WRONG_COLUMN_MSG);
                sendFieldRequestWithBoardMessage(player);
            }
        }

        private String getBoardLookMsg() {
            StringBuilder builder = new StringBuilder(Board.ROWS + MqttProperty.DELIMITER + Board.COLUMNS);
            for (int row = 0; row < Board.ROWS; row++)
                for (int col = 0; col < Board.COLUMNS; col++)
                    builder.append(MqttProperty.DELIMITER).append(gameLogic.getBoard().getSign(row, col));
            return builder.toString();
        }

        private void sendFieldRequestWithBoardMessage(char player) {
            String topicPrefix = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            publish(MqttProperty.BOARD_LOOK_TOPIC, getBoardLookMsg()); //todo: dac tylko dla poszczegolnych graczy
            publish(topicPrefix + MqttProperty.FIELD_TOPIC, MqttProperty.FIELD_REQUEST_MSG);
        }

        private void processClientConnectedMsg(char player, String message) {
            String[] splited = message.split(MqttProperty.DELIMITER);
//            char randomSign = getRandomAvailableSign();
            signWitClientID.put(player, splited[1]);

            String topicPrefix = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            String signMessage = MqttProperty.GIVEN_SIGN_MSG + MqttProperty.DELIMITER + player;
            publish(topicPrefix + MqttProperty.PLAYER_PREPARATION_TOPIC, signMessage);

            if (!isCorrectPlayersQty()) {
                publish(topicPrefix + MqttProperty.PLAYER_PREPARATION_TOPIC, MqttProperty.WAITING_FOR_PLAYER_MSG);
                gameLogic.changePlayer();
            } else {
                gameLogic.changePlayer();
                sendFieldRequestWithBoardMessage(getCurrentPlayer());
            }
        }

        //todo: dodac danie restartu gdy dwoje graczy sie zgodzi
        private void processRestartReplyMsg(String message) {
            String[] splited = message.split(MqttProperty.DELIMITER);
            boolean restart = Boolean.parseBoolean(splited[1]);
            finishOrRestartGame(restart);
            sendFieldRequestWithBoardMessage(gameLogic.getNextPlayer());
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
        callbackHandler = new MqttCallbackHandler();
//        availablePlayerSigns = new ArrayList<>(Arrays.asList(ConnectFour.FIRST_PLAYER, ConnectFour.SECOND_PLAYER));
        signWitClientID = new HashMap<>(2);
    }

    public void connectToMqtt() {
        try {
            broker = new MqttClient(MqttProperty.SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.setCallback(callbackHandler);
            broker.connect();
            broker.subscribe(MqttProperty.SPECIFIED_PLAYER_TOPICS + "/#", MqttProperty.QoS); //all topics from player
        } catch (MqttException e) {
            criticalErrorAction("Can't connect with MQTT: " + e.getMessage());
        }
    }

    private void criticalErrorAction(String message) {
        System.out.println(message);
        publish(MqttProperty.SERVER_ERROR_TOPIC, message);
        System.exit(1);
    }

    private void publish(String topic, String message) {
        try {
            broker.publish(topic, message.getBytes(UTF_8), MqttProperty.QoS, false);
        } catch (MqttException e) {
            System.out.println("Error while publishing message via MQTT protocol: " + e.getMessage());
            System.exit(1);
        }
    }

    public void nextTurn(int col, char player) {
        try {
            gameLogic.dropDisc(col, player);
        } catch (FullColumnException e) {
            String topicPrefix = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            publish(topicPrefix + MqttProperty.FIELD_TOPIC, MqttProperty.FULL_COLUMN_MSG);
            callbackHandler.sendFieldRequestWithBoardMessage(player);
            return;
        } catch (WrongColumnOrRowException e) {
            String topicPrefix = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            publish(topicPrefix + MqttProperty.FIELD_TOPIC, MqttProperty.WRONG_COLUMN_MSG);
            callbackHandler.sendFieldRequestWithBoardMessage(player);
            return;
        }
        checkGameStatus(player);
        gameLogic.changePlayer();
    }

    private void checkGameStatus(char player) {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY) {
            if (result == ConnectFour.DRAW)
                publish(MqttProperty.RESULTS_TOPIC, MqttProperty.DRAW_MSG);
            else
                publish(MqttProperty.RESULTS_TOPIC, MqttProperty.WINNER_MSG + MqttProperty.DELIMITER + result);
            publish(MqttProperty.BOARD_LOOK_TOPIC, callbackHandler.getBoardLookMsg());
            String topicPlayer1 = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(getCurrentPlayer()) + MqttProperty.PLAYER_PREPARATION_TOPIC;
            String topicPlayer2 = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(gameLogic.getNextPlayer()) + MqttProperty.PLAYER_PREPARATION_TOPIC;
            publish(topicPlayer1, MqttProperty.RESTART_REQUEST_MSG);
            publish(topicPlayer2, MqttProperty.RESTART_REQUEST_MSG);
        } else {
            char nextPlayer = player == ConnectFour.FIRST_PLAYER ? ConnectFour.SECOND_PLAYER : ConnectFour.FIRST_PLAYER;
            callbackHandler.sendFieldRequestWithBoardMessage(nextPlayer);
        }
    }

    private void finishOrRestartGame(boolean restart) {
        if (restart) {
            gameLogic.restartGame();
            String player1Topic = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(getCurrentPlayer()) + MqttProperty.PLAYER_PREPARATION_TOPIC;
            String player2Topic = MqttProperty.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(gameLogic.getNextPlayer()) + MqttProperty.PLAYER_PREPARATION_TOPIC;
            publish(player1Topic, MqttProperty.START_GAME);
            publish(player2Topic, MqttProperty.START_GAME);
        } else {
            disconnect();
            System.exit(0);
        }
    }

    private void disconnect() {
        try {
            broker.disconnect();
        } catch (MqttException e) {
            System.out.println("Can't disconnect with MQTT protocol: " + e.getMessage());
            System.exit(1);
        }
    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }

//    private char getRandomAvailableSign() {
//        int randIndex = (int) (Math.random() * availablePlayerSigns.size());
//        char sign = availablePlayerSigns.get(randIndex);
//        availablePlayerSigns.remove(randIndex);
//        return sign;
//    }

}
