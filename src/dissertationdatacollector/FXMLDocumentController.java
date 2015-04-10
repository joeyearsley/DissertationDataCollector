
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
 * Starts up the GUI, acts as controller.
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
    //To ensure 10 seconds
    private final countdownTimer countdownTime = new countdownTimer();
    protected final String FILE_LOCATION = "/Users/josephyearsley/Documents/University/Dissertation/Data/";
    private MindStream M = new MindStream();
    private Boolean connected = M.clientConnect();
    private static final Logger logger = Logger.getLogger(FXMLDocumentController.class);

    /**
     * Logic for connect button.
     * @param event Connect button clicked.
     */
    @FXML
    private void connect(ActionEvent event) {
        if (M.clientConnect()) {
            connected = true;
            connectedLabel.setText("Connected");
            connectButton.setVisible(false);
            startButton.setVisible(true);
        }
    }

    /**
     * Starts data collection.
     * @param event Start button clicked.
     */
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

    /**
     * Stops data collection.
     * @param event Stop button clicked.
     */
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

    /**
     * Stops the data collection after 10 seconds.
     */
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

    /**
     * Starts up the GUI, making sure everything is setup correctly.
     * @param url
     * @param rb 
     */
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
