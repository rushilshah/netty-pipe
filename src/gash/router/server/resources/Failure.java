package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public class Failure extends Resource {

    public Failure(){

    }

    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to be implement
    }

    public void handleWork(Work.WorkRequest msg) {
        Common.Failure err = msg.getPayload().getErr();
        logger.error("failure from " + msg.getHeader().getNodeId());
    }


}
