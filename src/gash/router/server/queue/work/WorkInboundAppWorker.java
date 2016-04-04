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
package gash.router.server.queue.work;

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

					//PrintUtil.printWork(req);
					if (payload.hasBeat()) {
						//Work.Heartbeat hb = payload.getBeat();
						logger.info("heartbeat from " + req.getHeader().getNodeId());
					} else if (payload.hasPing()) {
						new Ping(sq).handle(req);

					} else if (payload.hasErr()) {
						new Failure().handle(req);
						// PrintUtil.printFailure(err);
					} else if (payload.hasTask()) {
						new Task(sq).handle(req);
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