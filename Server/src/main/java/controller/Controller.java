package controller;

import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import ui.GameCli;

import java.util.Arrays;

public class Controller {
    private static final int QoS = 1;
    private static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String FIELD_CHOOSE_TOPIC = "connect4/board/fieldChoose";

    private ConnectFour gameLogic;
    private GameCli gameCli;
    private IMqttClient broker;

    //----------------------------------------------------------------------------------------------------------------//
    private static class MqttCallbackHandler implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            //todo:
            //Called when the client lost the connection to the broker
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            System.out.println(topic + ": " + Arrays.toString(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //todo
            //Called when a outgoing publish is complete
        }
    }

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(ConnectFour gameLogic, GameCli gameCli) {
        this.gameLogic = gameLogic;
        this.gameCli = gameCli;
    }

    public void connectToMqtt() {
        try {
            broker = new MqttClient(SERVER_URI, MqttClient.generateClientId(), new MemoryPersistence());
            broker.setCallback(new MqttCallbackHandler());
            broker.connect();
            broker.subscribe(FIELD_CHOOSE_TOPIC, QoS);
        } catch (MqttException e) {
            gameCli.serverError("Can't connect with MQTT protocol: " + e.getMessage());
            gameCli.waitForAnyInput();
            System.exit(1);
        }
    }

    //todo: wywolac w odpowiednim miejscu
    private void disconnect() {
        try {
            broker.disconnect();
        } catch (MqttException e) {
            gameCli.serverError("Can't disconnect with MQTT protocol: " + e.getMessage());
            gameCli.waitForAnyInput();
            System.exit(1);
        }
    }

    public void nextTurn(int col) {
        try {
            gameLogic.dropDisc(col, gameLogic.getCurrentPlayer());
        } catch (FullColumnException e) {
            gameCli.printFullColumnError();
            return;
        } catch (WrongColumnOrRowException e) {
            gameCli.printWrongColumnError();
            return;
        }
        checkGameStatus();
        gameLogic.changePlayer();
    }

    private void checkGameStatus() {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY) {
            if (result == ConnectFour.DRAW)
                gameCli.printDrawMsg();
            else {
                gameCli.printWinnerMsg(result);
                gameCli.printBoard(gameLogic.getBoard());
            }
            finishOrRestartGame(gameCli.readRestartGame());
        }
    }

    private void finishOrRestartGame(boolean restart) {
        if (restart) {
            gameLogic.restartGame();
            gameCli.printStartedMsg();
        } else System.exit(0);

    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }
}
