package gash.router.server.election;

/**
 * Created by pranav on 4/3/16.
 */

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

public class ConnectionManager {
    protected static Logger logger = LoggerFactory.getLogger("management");

    /** node ID to channel */
    private static HashMap<Integer, Channel> appconnections = new HashMap<Integer, Channel>();
    private static HashMap<Integer, Channel> connections = new HashMap<Integer, Channel>();
    private static HashMap<Integer, Channel> mgmtConnections = new HashMap<Integer, Channel>();
    public static enum connectionState {SERVERAPP, SERVERWORk, CLIENTAPP };

    public static void addConnection(Integer nodeId, Channel channel, connectionState state) {
        logger.info("ConnectionManager adding connection to " + nodeId);

        if (state == connectionState.SERVERWORk)
            mgmtConnections.put(nodeId, channel);
        else if(state == connectionState.CLIENTAPP ){
            connections.put(nodeId, channel);
            logger.info("Client Added in Connection Manager " + nodeId);
        }
        else if(state == connectionState.SERVERAPP){
            appconnections.put(nodeId, channel);
            logger.info("Server Added in Connection Manager " + nodeId);
        }
    }

    public static Channel getConnection(Integer nodeId, connectionState state) {
        if (state == connectionState.SERVERWORk)
            return mgmtConnections.get(nodeId);
        else if(state == connectionState.CLIENTAPP ){
            logger.info("Client got in Connection Manager " + nodeId);
            return connections.get(nodeId);

        }
        else if(state == connectionState.SERVERAPP)
        {
            logger.info("Node got in Connection Manager " + nodeId);
            return appconnections.get(nodeId);
        }
        return null;

    }

    public synchronized static void removeConnection(Integer nodeId, connectionState state) {
        if (state == connectionState.SERVERWORk)
            mgmtConnections.remove(nodeId);
        else if(state == connectionState.CLIENTAPP ){
            connections.remove(nodeId);
        }
        else if(state == connectionState.SERVERAPP)
            appconnections.remove(nodeId);
    }

    public synchronized static void removeConnection(Channel channel, connectionState state) {
        if (state == connectionState.SERVERWORk)
        {
            if (!mgmtConnections.containsValue(channel)) {
                return;
            }
            logger.info("Management Connections Size : " + ConnectionManager.getNumMgmtConnections());
            for (Integer nid : mgmtConnections.keySet()) {
                if (channel == mgmtConnections.get(nid)) {
                    mgmtConnections.remove(nid);
                    break;
                }
            }
            logger.info("Management Connections Size After Removal: " + ConnectionManager.getNumMgmtConnections());
        }
        else if(state == connectionState.CLIENTAPP ){
            if (!connections.containsValue(channel)) {
                return;
            }

            for (Integer nid : connections.keySet()) {
                if (channel == connections.get(nid)) {
                    connections.remove(nid);
                    break;
                }
            }
        }
        else if(state == connectionState.SERVERAPP)
        {
            if (!appconnections.containsValue(channel)) {
                return;
            }

            for (Integer nid : appconnections.keySet()) {
                if (channel == appconnections.get(nid)) {
                    appconnections.remove(nid);
                    break;
                }
            }

        }
    }



/*

    public synchronized static void broadcast( req) {
        if (req == null)
            return;

        for (Channel ch : connections.values())
            ch.writeAndFlush(req);
    }

    public synchronized static void broadcastServers(Request req) {
        if (req == null)
            return;
        System.out.println("Broadcast to servers");
        System.out.println("Length " + appconnections.keySet().size() );
        for (Channel ch : appconnections.values())
        {
            System.out.println("app connections");
            ch.writeAndFlush(req);
        }
    }
*/

    public synchronized static void broadcast(Work.WorkRequest mgmt) {
        if (mgmt == null)
            return;


        for (Map.Entry<Integer,Channel> entry : mgmtConnections.entrySet())
            if(entry.getValue().isOpen() && entry.getValue().isActive())
                entry.getValue().write(mgmt);
            else
            {
                mgmtConnections.remove(entry.getKey());
            }
    }

    public synchronized static void broadcastAndFlush(Work.WorkRequest mgmt) {
        if (mgmt == null)
            return;

        for (Map.Entry<Integer,Channel> entry : mgmtConnections.entrySet())
            if(entry.getValue().isOpen() && entry.getValue().isActive())
                entry.getValue().writeAndFlush(mgmt);
            else
            {
                mgmtConnections.remove(entry.getKey());
            }
    }

    public static int getNumMgmtConnections() {
        return mgmtConnections.size();
    }
    /* public synchronized static void sendToNode(Work.WorkRequest req,
                                                Integer destination) {
         if (req == null)
             return;
         if (appconnections.get(destination) != null){
             appconnections.get(destination).writeAndFlush(req);
         } else
             System.out.println("No destination found");
     }

     public synchronized static void sendToClient(Work.WorkRequest req,
                                                  Integer destination) {
         if (req == null)
             return;
         if (connections.get(destination) != null){
             connections.get(destination).writeAndFlush(req);
         } else
             System.out.println("No clients found");
     }*/
    public static Set<Integer> getKeySetConnections(connectionState state){
        if(state == connectionState.CLIENTAPP)
            return connections.keySet();
        else if(state == connectionState.SERVERAPP)
            return appconnections.keySet();
        else if(state == connectionState.SERVERWORk)
            return mgmtConnections.keySet();
        else
            return null;
    }
    public static boolean checkClient(connectionState state, int key)
    {
        if(state == connectionState.CLIENTAPP)
            return connections.containsKey(key);
        else if(state == connectionState.SERVERAPP)
            return appconnections.containsKey(key);
        else if(state == connectionState.SERVERWORk)
            return mgmtConnections.containsKey(key);
        else
            return false;
    }
}

