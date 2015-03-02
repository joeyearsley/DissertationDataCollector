/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dissertationdatacollector;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;

/**
 *
 * @author josephyearsley
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label connectedLabel;
    public Button connectButton;
    public Button startButton;
    public Button stopButton;
    public Label timerLabel;
    public TextField saveFileName;
    private final countdownTimer countdownTime = new countdownTimer();
    protected final String FILE_LOCATION = "/Users/josephyearsley/Documents/University/Dissertation/Data/";
    private MindStreamSystemTray M = new MindStreamSystemTray();
    private final Boolean connected = M.clientConnect();
    private static final Logger logger = Logger.getLogger(FXMLDocumentController.class);

    @FXML
    private void connect(ActionEvent event) {
        if (M.clientConnect()) {
            connectedLabel.setText("Connected");
            connectButton.setVisible(false);
            startButton.setVisible(true);
        }
    }

    @FXML
    private void start(ActionEvent event) {
        if (saveFileName.getText().length() > 0) {
            if (!(new File(FILE_LOCATION + saveFileName.getText() + ".csv").isFile())) {
                if (!connected) {
                    connectedLabel.setText("Connect First!");
                } else {
                    connectedLabel.setText("Recording...");
                    startButton.setVisible(false);
                    stopButton.setVisible(true);
                    M.setWrite(true);
                    countdownTime.restart();
                    countdownTime.cont = true;
                    saveFileName.setDisable(true);

                }
            } else {
                connectedLabel.setText("File Name Taken!");
            }

        } else {
            connectedLabel.setText("Please Enter a filename first!");
        }
    }

    @FXML
    private void stop(ActionEvent event) {
        M.setWrite(false);
        countdownTime.cont = false;
        if (!M.save(saveFileName.getText())) {
            connectedLabel.setText("Error File Not Saved");
        } else {
            connectedLabel.setText("Saved");
        }
        startButton.setVisible(true);
        stopButton.setVisible(false);
        saveFileName.setDisable(false);
    }

    public void stop() {
        M.setWrite(false);
        countdownTime.cont = false;
        if (!M.save(saveFileName.getText())) {
            connectedLabel.setText("Error File Not Saved");
        } else {
            connectedLabel.setText("Saved");
        }
        startButton.setVisible(true);
        stopButton.setVisible(false);
        saveFileName.setDisable(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        if (connected) {
            connectButton.setVisible(false);
            connectedLabel.setText("Connected");
            startButton.setVisible(true);
            try {
                M.start();
            } catch (Exception e) {
                logger.debug(e);
            }
        } else {
            connectButton.setVisible(true);
            startButton.setVisible(false);
        }
        timerLabel.textProperty().bind(countdownTime.messageProperty());

        countdownTime.messageProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                    Object newVal) {
                if (newVal == "0") {
                    stop();
                }
            }
        });

        countdownTime.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                M.close();
            }
        });
    }

}
