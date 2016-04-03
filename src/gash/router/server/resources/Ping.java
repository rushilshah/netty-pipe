package gash.router.server.resources;

import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public class Ping implements Resource {

    ChannelQueue sq;

    public Ping(ChannelQueue sq){
        this.sq = sq;
    }

    @Override
    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to implement over here
        //handle message by self
        System.out.println("Message for me: "+ msg.getPayload().getMessage() + " from "+ msg.getHeader().getSourceHost());

    }

    @Override
    public void handleWork(Work.WorkRequest msg) {
        //handle message by self

        logger.info("Ping for me: " + " from "+ msg.getHeader().getSourceHost());

        Work.WorkRequest.Builder rb = Work.WorkRequest.newBuilder();

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(((PerChannelWorkQueue)sq).gerServerState().getConf().getNodeId());
        hb.setTime(System.currentTimeMillis());
        hb.setDestination(Integer.parseInt(msg.getHeader().getSourceHost().substring(msg.getHeader().getSourceHost().lastIndexOf('_')+1)));
        hb.setSourceHost(msg.getHeader().getSourceHost().substring(msg.getHeader().getSourceHost().indexOf('_')+1));
        hb.setDestinationHost(msg.getHeader().getSourceHost());
        hb.setMaxHops(5);

        rb.setHeader(hb);
        rb.setSecret(1234567809);
        rb.setPayload(Work.Payload.newBuilder().setPing(true));
        //channel.writeAndFlush(rb.build());
        sq.enqueueResponse(rb.build(),((PerChannelWorkQueue)sq).getChannel());
    }
}
