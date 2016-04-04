package gash.router.server.resources;

import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelCommandQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public class Ping extends Resource {

    public Ping(ChannelQueue sq){
        super(sq);
    }

    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

        if(!(sq instanceof PerChannelGlobalCommandQueue)){
            logger.info("Setup queue is not global queue");
            return;
        }
        boolean msgDropFlag;

        
        if(msg.getHeader().getDestination() == ((PerChannelGlobalCommandQueue)sq).getRoutingConf().getNodeId()){
            logger.info("ping from " + msg.getHeader().getNodeId());
        }
        else{ //message doesn't belong to current node. Forward on other edges
            msgDropFlag = true;
            if(MessageServer.getEmon() != null){// forward if Comm-worker port is active
                for(EdgeInfo ei :MessageServer.getEmon().getOutboundEdgeInfoList()){
                    if(ei.isActive() && ei.getChannel() != null){// check if channel of outboundWork edge is active
                        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

                        Common.Header.Builder hb = Common.Header.newBuilder();
                        hb.setNodeId(((PerChannelGlobalCommandQueue)sq).getRoutingConf().getNodeId());
                        hb.setTime(msg.getHeader().getTime());
                        hb.setDestination(msg.getHeader().getDestination());
                        hb.setSourceHost(((PerChannelGlobalCommandQueue)sq).getRoutingConf().getNodeId()+"_"+msg.getHeader().getSourceHost());
                        hb.setDestinationHost(msg.getHeader().getDestinationHost());
                        hb.setMaxHops(5);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setPing(true));

                        Work.WorkRequest work = wb.build();

                        PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
                        edgeQueue.enqueueResponse(work,ei.getChannel());
                        msgDropFlag = false;
                        logger.info("Workmessage queued");
                    }
                }
                if(msgDropFlag)
                    logger.info("Message dropped <node,ping,destination>: <" + msg.getHeader().getNodeId()+"," + msg.getPing()+"," + msg.getHeader().getDestination()+">");
            }
            else{// drop the message or queue it for limited time to send to connected node
                //todo
                logger.info("No outbound edges to forward. To be handled");
            }

        }



    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to implement over here
        //handle message by self
        if(!(sq instanceof PerChannelCommandQueue)){
            logger.info("Setup queue is not command queue");
            return;
        }

        boolean msgDropFlag;
        if(msg.getHeader().getDestination() == ((PerChannelCommandQueue)sq).getRoutingConf().getNodeId()){
            logger.info("ping from " + msg.getHeader().getNodeId());
        }
        else{ //message doesn't belong to current node. Forward on other edges
            msgDropFlag = true;
            if(MessageServer.getEmon() != null){// forward if Comm-worker port is active
                for(EdgeInfo ei :MessageServer.getEmon().getOutboundEdgeInfoList()){
                    if(ei.isActive() && ei.getChannel() != null){// check if channel of outboundWork edge is active
                        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

                        Common.Header.Builder hb = Common.Header.newBuilder();
                        hb.setNodeId(((PerChannelCommandQueue)sq).getRoutingConf().getNodeId());
                        hb.setTime(msg.getHeader().getTime());
                        hb.setDestination(msg.getHeader().getDestination());
                        hb.setSourceHost(((PerChannelCommandQueue)sq).getRoutingConf().getNodeId()+"_"+msg.getHeader().getSourceHost());
                        hb.setDestinationHost(msg.getHeader().getDestinationHost());
                        hb.setMaxHops(5);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setPing(true));

                        Work.WorkRequest work = wb.build();

                        PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
                        edgeQueue.enqueueResponse(work,ei.getChannel());
                        msgDropFlag = false;
                        logger.info("Workmessage queued");
                    }
                }
                if(msgDropFlag)
                    logger.info("Message dropped <node,ping,destination>: <" + msg.getHeader().getNodeId()+"," + msg.getPayload().getPing()+"," + msg.getHeader().getDestination()+">");
            }
            else{// drop the message or queue it for limited time to send to connected node
                //todo
                logger.info("No outbound edges to forward. To be handled");
            }

        }
        

    }

    public void handleWork(Work.WorkRequest msg) {
        //handle message by self

        if(!(sq instanceof PerChannelWorkQueue)){
            logger.info("Setup queue is not work queue");
            return;
        }

        boolean msgDropFlag;

        logger.info("ping from <node,host> : <" + msg.getHeader().getNodeId() + ", " + msg.getHeader().getSourceHost()+">");
        PrintUtil.printWork(msg);
        
        
        if(msg.getHeader().getDestination() == ((PerChannelWorkQueue)sq).gerServerState().getConf().getNodeId()){
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
        else { //message doesn't belong to current node. Forward on other edges
            msgDropFlag = true;
            if (msg.getHeader().getMaxHops() > 0 && MessageServer.getEmon() != null) {// forward if Comm-worker port is active
                for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
                    if (ei.isActive() && ei.getChannel() != null) {// check if channel of outbound edge is active
                        logger.debug("Workmessage being sent");
                        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

                        Common.Header.Builder hb = Common.Header.newBuilder();
                        hb.setNodeId(((PerChannelWorkQueue)sq).gerServerState().getConf().getNodeId());
                        hb.setTime(msg.getHeader().getTime());
                        hb.setDestination(msg.getHeader().getDestination());
                        hb.setSourceHost(((PerChannelWorkQueue)sq).gerServerState().getConf().getNodeId()+"_"+msg.getHeader().getSourceHost());
                        hb.setDestinationHost(msg.getHeader().getDestinationHost());
                        hb.setMaxHops(msg.getHeader().getMaxHops() -1);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setPing(true));
                        //ei.getChannel().writeAndFlush(wb.build());
                        PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
                        edgeQueue.enqueueResponse(wb.build(),ei.getChannel());
                        msgDropFlag = false;
                        logger.debug("Workmessage sent");
                    }
                }
                if (msgDropFlag)
                    logger.info("Message dropped <node,ping,destination>: <" + msg.getHeader().getNodeId() + "," + msg.getPayload().getPing() + "," + msg.getHeader().getDestination() + ">");
            } else {// drop the message or queue it for limited time to send to connected node
                //todo
                logger.info("No outbound edges to forward. To be handled");
            }
        }

    }



}
