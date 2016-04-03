package database.dao;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.dbconnetor.MongoConnector;
import database.model.MongoDataModel;

import java.util.ArrayList;

/**
 * Created by manthan on 4/3/16.
 */
public class MongoDAO {

    public static int saveData(String collectionName, MongoDataModel data){
        int result = 0;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();

            docBuilder.append("name", data.getName());
            docBuilder.append("seqNumber", data.getSeqNumber());
            docBuilder.append("dataChunk", data.getDataChunk());

            DBObject doc = docBuilder.get();
            result = dbCollection.insert(doc).getN();
        }
        return result;
    }

    public static ArrayList<MongoDataModel> getData(String collectionName, MongoDataModel data){
        ArrayList<MongoDataModel> result = null;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            DBObject query = BasicDBObjectBuilder.start().add("name", data.getName()).get();
            DBCursor cursor = dbCollection.find(query);
            if(cursor.size() > 0) {
                result = new ArrayList<>();
                while (cursor.hasNext()) {
                    result.add((MongoDataModel) cursor.next());
                }
            }
        }
        return result;
    }

    public static MongoDataModel getSingleData(String collectionName, MongoDataModel data){
        MongoDataModel result = null;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            DBObject query = BasicDBObjectBuilder.start().add("name", data.getName()).add("seqNumber",data.getSeqNumber()).get();
            DBCursor cursor = dbCollection.find(query);
            if(cursor.size() == 1) {
                while (cursor.hasNext()) {
                    result = (MongoDataModel) cursor.next();
                }
            }
        }
        return result;
    }
}
