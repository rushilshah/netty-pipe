package gash.router.server.resources;

import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/3/16.
 */
public class Request implements Resource{

    @Override
    public void handleCommand(Pipe.CommandRequest msg) {
        Pipe.Payload payload = msg.getPayload();
        if(payload.hasRequest()){
            handleRequest(payload.getRequest());
        }
    }

    @Override
    public void handleWork(Work.WorkRequest msg) {
        Work.Payload payload = msg.getPayload();
        if(payload.hasRequest()){
            handleRequest(payload.getRequest());
        }
    }

    private void handleRequest(Common.Request request){
        switch (request.getType()){
            case SAVE:
                break;
            case SEARCH:
                break;
        }
    }
}
