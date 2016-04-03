package gash.router.server.resources;

import gash.router.server.queue.PerChannelWorkQueue;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public class Task implements Resource {

    PerChannelWorkQueue sq;

    public Task(PerChannelWorkQueue sq){
        this.sq = sq;
    }

    @Override
    public void handleCommand(Pipe.CommandRequest msg) {

    }

    @Override
    public void handleWork(Work.WorkRequest msg) {
        Work.Task t = msg.getPayload().getTask();
        sq.gerServerState().getTasks().addTask(t);
    }
}
