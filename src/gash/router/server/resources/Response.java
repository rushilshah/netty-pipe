package gash.router.server.resources;

import gash.router.server.queue.ChannelQueue;
import global.Global;
import pipe.work.Work;
import routing.Pipe;
import storage.Storage;

/**
 * Created by rushil on 4/3/16.
 */
public class Response extends Resource {

    Storage.Response response;

    public Response(ChannelQueue sq){
        super(sq);
    }

    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

        response = msg.getResponse();

        switch (response.getAction()){
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
        response = msg.getPayload().getResponse();
        logger.error("Query from " + msg.getHeader().getNodeId());
    }
}
