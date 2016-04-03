package database.dbconnetor;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by manthan on 4/2/16.
 */
public class MongoConnector {

    protected static Logger logger = LoggerFactory.getLogger("mongo");

    static Properties configProperty;
    static InputStream inputStream;

    private static MongoClient getConnection(){

        MongoClient mc = null;

        configProperty = new Properties();
        inputStream = MongoConnector.class.getClassLoader().getResourceAsStream("mongo-config.properties");

        if(inputStream == null){
            logger.error("Unable to load mongo property file");
        }
        else {

            try {
                configProperty.load(inputStream);
                mc = new MongoClient(configProperty.getProperty("host"), Integer.parseInt(configProperty.getProperty("port")));
            } catch (UnknownHostException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return mc;
    }

    public static DBCollection connectToCollection(String collectionName){
        DBCollection dbCollection = null;
        MongoClient mongo = getConnection();
        if(mongo != null){
            DB db = mongo.getDB(configProperty.getProperty("dbName"));
            if(db != null){
                return db.getCollection(collectionName);
            }
        }
        return dbCollection;
    }
}
