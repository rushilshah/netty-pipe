package gash.router.server.resources;

import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public class Failure implements Resource {

    public Failure(){

    }
    @Override
    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to be implement
    }

    @Override
    public void handleWork(Work.WorkRequest msg) {
        Common.Failure err = msg.getPayload().getErr();
        logger.error("failure from " + msg.getHeader().getNodeId());
    }
}
