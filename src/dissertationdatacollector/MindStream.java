package dissertationdatacollector;

import java.io.File;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Connects to the ThinkGear connector.
 * @author josephyearsley
 */
public class MindStream extends Service {

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MindStream.class);

    final String HOST = "127.0.0.1";
    final int PORT = 13854;
    final String FILE_LOCATION = "../Data/";
    final String TMP = "tmp.csv";
    final ThinkGearSocketClient client = new ThinkGearSocketClient();
    public JSONObject eegPower;
    public JSONObject json;
    private Boolean write = false;
    private FileWriter writer = null;

    /**
     * Connects to the connector if not already connected, otherwise disconnects.
     * @return if the headset is connected to the ThinkGear.
     */
    public boolean clientConnect() {
        if (!client.isConnected()) {
            try {
                client.setHost(HOST);
                client.setPort(PORT);
                client.connect();
            } catch (IOException ex) {
                logger.debug(ex);
            }

        } else {
            try {
                client.close();
            } catch (IOException ex) {
                logger.debug(ex);
            }
        }

        return client.isConnected();
    }

    ;

    /**
     * Task to display and write EEG data.
     * @return A task for displaying EEG data.
     */
    @Override
    protected synchronized Task<Void> createTask() {
        return new Task<Void>() {
            protected Void call() throws Exception {
                while (client.isConnected()) {
                    SimpleDateFormat fmt = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                    while (write) {
                        logger.debug("Writing...");
                        try {
                            String clientData = client.getData();
                            logger.debug(clientData);
                            json = new JSONObject(clientData);
                            String newLine = System.getProperty("line.separator");
                            if (!json.isNull("eegPower")) {

                                String timeStamp = fmt.format(new Date());
                                writer.append(timeStamp + ',');

                                eegPower = json.getJSONObject("eegPower");
                                logger.debug(eegPower);
                                writer.append(Integer.toString(eegPower.getInt("lowAlpha")) + ',');
                                writer.append(Integer.toString(eegPower.getInt("highAlpha")) + ',');
                                writer.append(Integer.toString(eegPower.getInt("lowBeta")) + ',');
                                writer.append(Integer.toString(eegPower.getInt("highBeta")));
                                writer.append(newLine);

                            } else {
                                logger.debug("eegPower is null!");
                            }
                        } catch (IOException e) {
                            logger.debug(e);
                        }

                    }
                }
                return null;
            }
        };
    }
    
    /**
     * Makes the writer, write to file.
     * @param x If it should write or not.
     */
    public synchronized void setWrite(Boolean x) {
        if (x) {
            String csvFile = FILE_LOCATION + TMP;
            String newLine = System.getProperty("line.separator");
            try {
                writer = new FileWriter(csvFile);
            } catch (IOException e1) {
                logger.debug(e1);
            }

            // HEADER
            try {
                writer.append("TIMESTAMP,LOW_ALPHA,HIGH_ALPHA,LOW_BETA,HIGH_BETA");
                writer.append(newLine);
            } catch (IOException e2) {
                logger.debug(e2);
            }
            write = x;
        } else {
            write = x;
            onCancelled();
        }
    }

    /**
     * 
     * @return if its writing or not.
     */
    public Boolean getWrite() {
        return write;
    }

    /**
     * flushes the written data to file.
     */
    protected synchronized void onCancelled() {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (IOException ex) {
            logger.debug(ex);
        }
    }

    /**
     * Saves the temporary file to a new file.
     * @param fileName Name of which to call the file.
     * @return If rename is successful or not.
     */
    protected synchronized Boolean save(String fileName) {
        File tmp = new File(FILE_LOCATION + TMP);
        File newFile = new File(FILE_LOCATION + fileName + ".csv");
        return tmp.renameTo(newFile);
    }

    /**
     * Close everything up.
     */
    protected synchronized void close() {
        try {
            if (writer != null) {
                writer.close();
            }
            client.close();
            logger.debug("Closed Everything!");
        } catch (Exception e) {
            logger.error(e);
        }
    }

};
