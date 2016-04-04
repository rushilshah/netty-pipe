package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.command.PerChannelCommandQueue;
import gash.router.server.queue.global.PerChannelGlobalCommandQueue;
import gash.router.server.queue.work.PerChannelWorkQueue;
import global.Global;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public class Task extends Resource {

    ChannelQueue sq;

    public Task(ChannelQueue sq){
        this.sq = sq;
    }

    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

        if(!(sq instanceof PerChannelGlobalCommandQueue)){
            logger.info("Setup queue is not global queue");
            return;
        }

    }

    public void handleCommand(Pipe.CommandRequest msg) {

        if(!(sq instanceof PerChannelCommandQueue)){
            logger.info("Setup queue is not command queue");
            return;
        }
    }

    public void handleWork(Work.WorkRequest msg) {

        if(!(sq instanceof PerChannelWorkQueue)){
            logger.info("Setup queue is not work queue");
            return;
        }
        Work.Task t = msg.getPayload().getTask();
        ((PerChannelWorkQueue)sq).gerServerState().getTasks().addTask(t);
    }

    @Override
    public void handle(GeneratedMessage msg) {

        if(msg instanceof Global.GlobalCommandMessage){

        }else if(msg instanceof Pipe.CommandRequest){

        }else if(msg instanceof Work.WorkRequest){

        }

    }

}
