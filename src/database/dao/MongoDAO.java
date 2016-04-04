package database.dao;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.dbconnetor.MongoConnector;
import database.model.DataModel;

import java.util.ArrayList;

/**
 * Created by manthan on 4/3/16.
 */
public class MongoDAO {

    public static int saveData(String collectionName, DataModel data){
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

    public static ArrayList<DataModel> getData(String collectionName, DataModel data){
        ArrayList<DataModel> result = null;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            DBObject query = BasicDBObjectBuilder.start().add("name", data.getName()).get();
            DBCursor cursor = dbCollection.find(query);
            if(cursor.size() > 0) {
                result = new ArrayList<>();
                while (cursor.hasNext()) {
                    result.add((DataModel) cursor.next());
                }
            }
        }
        return result;
    }

    public static DataModel getSingleData(String collectionName, DataModel data){
        DataModel result = null;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            DBObject query = BasicDBObjectBuilder.start().add("name", data.getName()).add("seqNumber",data.getSeqNumber()).get();
            DBCursor cursor = dbCollection.find(query);
            if(cursor.size() == 1) {
                while (cursor.hasNext()) {
                    result = (DataModel) cursor.next();
                }
            }
        }
        return result;
    }
}
