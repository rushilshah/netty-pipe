package gash.router.server.resources;

import database.dao.MongoDAO;
import database.model.DataModel;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import global.Global;
import pipe.work.Work;
import routing.Pipe;
import storage.Storage;

import java.util.ArrayList;

/**
 * Created by rushil on 4/3/16.
 */
public class Query extends Resource {

    Storage.Query query;

    public Query(ChannelQueue sq){
        super(sq);
    }


    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

        query = msg.getQuery();
        if (msg.getHeader().getDestination() == ((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId()) {
            switch (query.getAction()) {
                case GET:
                    ArrayList<DataModel> arrRespData = MongoDAO.getData("temp",new DataModel(query.getKey(),null));
                    for(int i=0;i<arrRespData.size();i++){

                    }
                    break;
                case STORE:
                    break;
                case UPDATE:
                case DELETE:
                    break;
            }

        }
    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to be implement
    }

    public void handleWork(Work.WorkRequest msg) {
        Storage.Query query = msg.getPayload().getQuery();
        logger.error("Query from " + msg.getHeader().getNodeId());
    }


}
