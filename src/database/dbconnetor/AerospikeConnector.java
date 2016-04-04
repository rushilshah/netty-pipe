package database.dbconnetor;

import com.aerospike.client.AerospikeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by manthan on 4/3/16.
 */
public class AerospikeConnector {
    protected static Logger logger = LoggerFactory.getLogger("aerospike");

    static Properties configProperty;
    static InputStream inputStream;

    private static AerospikeClient getConnection(){

        AerospikeClient ac = null;

        configProperty = new Properties();
        inputStream = MongoConnector.class.getClassLoader().getResourceAsStream("aerospike-config.properties");

        if(inputStream == null){
            logger.error("Unable to load aerospike property file");
        }
        else {
            try {
                configProperty.load(inputStream);
                ac = new AerospikeClient(configProperty.getProperty("host"), Integer.parseInt(configProperty.getProperty("port")));
            } catch (UnknownHostException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return ac;
    }
}
