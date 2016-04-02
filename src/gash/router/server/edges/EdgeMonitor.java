/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server.edges;

import gash.router.container.RoutingConf;
import gash.router.server.RoutingConfObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.container.RoutingConf.RoutingEntry;
import gash.router.server.ServerState;
import pipe.common.Common.Header;
import pipe.work.Work;
import pipe.work.Work.Heartbeat;
import pipe.work.Work.WorkState;

import gash.router.client.CommConnection;

import java.util.Collection;

public class EdgeMonitor implements EdgeListener, Runnable{
	protected static Logger logger = LoggerFactory.getLogger("edge monitor");

	private EdgeList outboundEdges;
	private EdgeList inboundEdges;
	private long dt = 2000;
	private ServerState state;
	private boolean forever = true;

	public EdgeMonitor(ServerState state) {
		if (state == null)
			throw new RuntimeException("state is null");

		this.outboundEdges = new EdgeList();
		this.inboundEdges = new EdgeList();
		this.state = state;
		this.state.setEmon(this);

		EdgeInfo newOutboundEdge;
		if (state.getConf().getRouting() != null) {
			for (RoutingEntry e : state.getConf().getRouting()) {
				newOutboundEdge = outboundEdges.addNode(e.getId(), e.getHost(), e.getPort());
				if(newOutboundEdge!= null)
					onAdd(newOutboundEdge);
			}
		}

		// cannot go below 2 sec
		if (state.getConf().getHeartbeatDt() > this.dt)
			this.dt = state.getConf().getHeartbeatDt();

		newOutboundEdge = null;
	}

	public void createInboundIfNew(int ref, String host, int port) {
		inboundEdges.createIfNew(ref, host, port);
	}

	private Work.WorkRequest createHB(EdgeInfo ei) {
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		Heartbeat.Builder bb = Heartbeat.newBuilder();
		bb.setState(sb);

		Work.Payload.Builder py= Work.Payload.newBuilder();
		py.setBeat(bb);

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(-1);
		hb.setTime(System.currentTimeMillis());

		Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
		wb.setHeader(hb);
		wb.setPayload(py);
		wb.setSecret(12345678);//added by Manthan
		return wb.build();
	}

	public void shutdown() {
		forever = false;
	}

	@Override
	public void run() {
		while (forever) {
			try {
				for (EdgeInfo ei : this.outboundEdges.map.values()) {
					if (ei.isActive() && ei.getChannel() != null) {
						Work.WorkRequest wm = createHB(ei);
						ei.getChannel().writeAndFlush(wm);
					} else {
						// TODO create a client to the node // added by Manthan
						logger.info("trying to connect to node " + ei.getRef());
						CommConnection commC = CommConnection.initConnection(ei.getHost(),ei.getPort());
						ei.setChannel(commC.getChannel());
						ei.setActive(true);
						logger.info("connected to node " + ei.getRef() + ei.isActive());
					}
				}

				Thread.sleep(dt);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Author : Manthan
	 * */
	@Override
	public synchronized void onAdd(EdgeInfo ei) {
		// TODO check connection //added by Manthan
		if(!ei.isActive() || ei.getChannel() == null){
			logger.info("New edge added, trying to connect to node " + ei.getRef());
			CommConnection commC = CommConnection.initConnection(ei.getHost(),ei.getPort());
			ei.setChannel(commC.getChannel());
			ei.setActive(true);
			logger.info("New edge added and connected to node " + ei.getRef() + ei.isActive());
		}
	}

	/**
	 * Author : Manthan
	 * */
	@Override
	public synchronized void onRemove(EdgeInfo ei) {
		// TODO ? //added by Manthan
		if(ei.isActive() || ei.getChannel() != null){
			logger.info("Edge removed, trying to disconnect to node " + ei.getRef());		
			ei.getChannel().close();
			ei.setActive(false);
			logger.info("Edge removed and disconnected from node " + ei.getRef() + ei.isActive());
		}
	}

	/**
	 * Author : Manthan
	 * */
	public Collection<EdgeInfo> getOutboundEdgeInfoList(){
		return outboundEdges.map.values();
	}

	/**
	 * Author : Manthan
	 * */
	public void updateState(ServerState newState){
		EdgeInfo newOutboundEdge = null;
		this.state = newState;
		this.state.setEmon(this);

		if (state.getConf().getRouting() != null) {
			for (RoutingEntry e : state.getConf().getRouting()) {
				newOutboundEdge = outboundEdges.createIfNew(e.getId(), e.getHost(), e.getPort());
				if(newOutboundEdge!= null)
					onAdd(newOutboundEdge);
			}
		}

		// cannot go below 2 sec
		if (state.getConf().getHeartbeatDt() > this.dt)
			this.dt = state.getConf().getHeartbeatDt();

		newOutboundEdge = null;
	}
}
