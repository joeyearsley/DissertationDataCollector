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
import org.apache.log4j.BasicConfigurator;

/**
 * <p>
 * Title: MindStreamSystemTray</p><br>
 * <p>
 * Description: Description: System tray app for streaming data from the
 * Neurosky MindSet/MindWave</p><br>
 *
 * @author          <a href="http://eric-blue.com">Eric Blue</a><br>
 *
 * $Date: 2014-01-26 19:36:10 $ $Author: ericblue76 $ $Revision: 1.9 $
 *
 */
public class MindStreamSystemTray extends Service {

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MindStreamSystemTray.class);

    /**
     * System tray launcher
     *
     * @param args
     * @return void
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
    }

    // TODO Cleanup all System.out/.err with log4j calls
    final String HOST = "127.0.0.1";
    final int PORT = 13854;
    final String FILE_LOCATION = "/Users/josephyearsley/Documents/University/Dissertation/Data/";
    final String TMP = "tmp.csv";
    final ThinkGearSocketClient client = new ThinkGearSocketClient();
    public JSONObject eegPower;
    public JSONObject json;
    private Boolean write = false;
    private FileWriter writer = null;

    /**
     *
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

    //Multi-task to take load off GUI thread
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
                            /*
                             * JH: check just in case it's not there due
                             * to poorSignallevel
                             */
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

    public Boolean getWrite() {
        return write;
    }

    protected synchronized void onCancelled() {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (IOException ex) {
            logger.debug(ex);
        }
    }

    protected synchronized Boolean save(String fileName) {
        File tmp = new File(FILE_LOCATION + TMP);
        File newFile = new File(FILE_LOCATION + fileName + ".csv");
        return tmp.renameTo(newFile);
    }

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
