/*
 * copyright 2015, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server.queue;

import com.google.protobuf.GeneratedMessage;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.resources.Failure;
import gash.router.server.resources.Ping;
import gash.router.server.resources.Task;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;

public class WorkInboundAppWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("server");

	int workerId;
	PerChannelWorkQueue sq;
	boolean forever = true;

	public WorkInboundAppWorker(ThreadGroup tgrp, int workerId, PerChannelWorkQueue sq) {
		super(tgrp, "inboundWork-" + workerId);
		this.workerId = workerId;
		this.sq = sq;

		if (sq.inbound == null)
			throw new RuntimeException("connection worker detected null inboundWork queue");
	}

	@Override
	public void run() {
		Channel conn = sq.getChannel();
		if (conn == null || !conn.isOpen()) {
			logger.error("connection missing, no inboundWork communication");
			return;
		}

		while (true) {
			if (!forever && sq.inbound.size() == 0)
				break;

			try {
				// block until a message is enqueued
				GeneratedMessage msg = sq.inbound.take();

				// process request and enqueue response

				if (msg instanceof Work.WorkRequest) {
					Work.WorkRequest req = ((Work.WorkRequest) msg);
					Work.Payload payload = req.getPayload();
					boolean msgDropFlag;

					//PrintUtil.printWork(req);
					if (payload.hasBeat()) {
						//Work.Heartbeat hb = payload.getBeat();
						logger.info("heartbeat from " + req.getHeader().getNodeId());
					} else if (payload.hasPing()) {
						logger.info("ping from <node,host> : <" + req.getHeader().getNodeId() + ", " + req.getHeader().getSourceHost()+">");
						PrintUtil.printWork(req);
						if(req.getHeader().getDestination() == sq.state.getConf().getNodeId()){
							new Ping(sq).handleWork(req);
						}
						else { //message doesn't belong to current node. Forward on other edges
							msgDropFlag = true;
							if (req.getHeader().getMaxHops() > 0 && MessageServer.getEmon() != null) {// forward if Comm-worker port is active
								for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
									if (ei.isActive() && ei.getChannel() != null) {// check if channel of outbound edge is active
										logger.debug("Workmessage being sent");
										Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

										Common.Header.Builder hb = Common.Header.newBuilder();
										hb.setNodeId(sq.state.getConf().getNodeId());
										hb.setTime(req.getHeader().getTime());
										hb.setDestination(req.getHeader().getDestination());
										hb.setSourceHost(sq.state.getConf().getNodeId()+"_"+req.getHeader().getSourceHost());
										hb.setDestinationHost(req.getHeader().getDestinationHost());
										hb.setMaxHops(req.getHeader().getMaxHops() -1);

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
									logger.info("Message dropped <node,ping,destination>: <" + req.getHeader().getNodeId() + "," + payload.getPing() + "," + req.getHeader().getDestination() + ">");
							} else {// drop the message or queue it for limited time to send to connected node
								//todo
								logger.info("No outbound edges to forward. To be handled");
							}
						}

					} else if (payload.hasErr()) {
						new Failure().handleWork(req);
						// PrintUtil.printFailure(err);
					} else if (payload.hasTask()) {
						new Task(sq).handleWork(req);
					} else if (payload.hasState()) {
						Work.WorkState s = payload.getState();
					}
				}
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected processing failure", e);
				break;
			}
		}

		if (!forever) {
			logger.info("Work incoming connection queue closing");
		}
	}
}