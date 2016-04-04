package gash.router.server.resources;

import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;
import storage.Storage;

/**
 * Created by rushil on 4/3/16.
 */
public class Query extends Resource {

    Storage.Query query;

    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

        query = msg.getQuery();
        switch (query.getAction()){
            case GET:
                break;
            case STORE:
                break;
            case UPDATE:
            case DELETE:
                break;
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
