package gash.router.server.resources;

import com.google.protobuf.ByteString;
import database.dao.MongoDAO;
import database.model.DataModel;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import pipe.common.Common;
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
        //If this have to handle on the same node
        //TODO: change the logic so that it has to be dependent on configuration and intra cluster node space dependent.
        if (msg.getHeader().getDestination() == ((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId()) {
            switch (query.getAction()) {
                case GET:
                    PrintUtil.printGlobalCommand(msg);
                    ArrayList<DataModel> arrRespData = MongoDAO.getData("temp",new DataModel(query.getKey(),null));
                    for(DataModel dataModel : arrRespData){
                        Global.GlobalCommandMessage.Builder gb = Global.GlobalCommandMessage.newBuilder();

                        Common.Header.Builder hb = Common.Header.newBuilder();

                        Storage.Response.Builder rb = Storage.Response.newBuilder();
                        rb.setAction(Storage.Action.GET);
                        rb.setSuccess(true);
                        rb.setKey(dataModel.getName());
                        rb.setSequenceNo(dataModel.getSeqNumber());
                        rb.setData(ByteString.copyFrom(dataModel.getDataChunk()));

                        sq.enqueueRequest(gb.build(),((PerChannelGlobalCommandQueue)sq).getChannel());
                    }
                    break;
                case STORE:
                    int result = MongoDAO.saveData("temp",new DataModel(query.getKey(),query.getSequenceNo(),query.getData().toByteArray()));
                    Storage.Response.Builder rb = Storage.Response.newBuilder();
                    rb.setAction(Storage.Action.GET);
                    rb.setSuccess(result>0);
                    PrintUtil.printGlobalCommand(msg);
                    break;
                case UPDATE:
                case DELETE:
                    break;
            }

        }else { //message doesn't belong to current node. Forward on other edges
            boolean msgDropFlag = true;
            if (MessageServer.getEmon() != null) {// forward if Comm-worker port is active
                for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
                    if (ei.isActive() && ei.getChannel() != null) {// check if channel of outboundWork edge is active
                        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

                        Common.Header.Builder hb = Common.Header.newBuilder();
                        hb.setNodeId(((PerChannelGlobalCommandQueue)sq).getRoutingConf().getNodeId());
                        hb.setTime(msg.getHeader().getTime());
                        hb.setDestination(msg.getHeader().getDestination());
                        hb.setSourceHost(((PerChannelGlobalCommandQueue)sq).getRoutingConf().getNodeId() + "_" + msg.getHeader().getSourceHost());
                        hb.setDestinationHost(msg.getHeader().getDestinationHost());
                        hb.setMaxHops(5);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setQuery(msg.getQuery()));

                        Work.WorkRequest work = wb.build();

                        PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
                        edgeQueue.enqueueResponse(work, ei.getChannel());
                        msgDropFlag = false;
                        logger.info("Work message queued");
                    }
                }
                if (msgDropFlag)
                    logger.info("Message dropped <node,ping,destination>: <" + msg.getHeader().getNodeId() + "," + msg.getPing() + "," + msg.getHeader().getDestination() + ">");
            } else {// drop the message or queue it for limited time to send to connected node
                //todo
                logger.info("No outbound edges to forward. To be handled");
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
