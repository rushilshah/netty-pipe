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
package gash.router.client;

import global.Global;
import pipe.common.Common.Header;
import routing.Pipe;
import storage.Storage;

/**
 * front-end (proxy) to our service - functional-based
 * 
 * @author gash
 * 
 */
public class MessageClient {
	// track requests
	private long curID = 0;

	public MessageClient(String host, int port) {
		init(host, port);
	}

	private void init(String host, int port) {
		CommConnection.initConnection(host, port);
	}

	public void addListener(CommListener listener) {
		CommConnection.getInstance().addListener(listener);
	}

	public void ping() {
		// construct the message to send
		Header.Builder hb = createHeader(999,6,"999","4668");

		Global.GlobalCommandMessage.Builder rb = Global.GlobalCommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setPing(true);

		try {
			// direct no queue
			// CommConnection.getInstance().write(rb.build());

			// using queue
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void message(String message) {
		// construct the message to send
		Header.Builder hb = createHeader(999,5,"999","4668");

		Global.GlobalCommandMessage.Builder rb = Global.GlobalCommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setMessage(message);

		try {
			// direct no queue
			// CommConnection.getInstance().write(rb.build());
			// using queue
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void query(String value) {
		// construct the message to send
		Header.Builder hb = createHeader(999,5,"999","4668");

		Storage.Query.Builder qb = Storage.Query.newBuilder();
		qb.setAction(Storage.Action.GET);
		qb.setKey(value);

		Global.GlobalCommandMessage.Builder rb = Global.GlobalCommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setQuery(qb);

		try {
			// direct no queue
			// CommConnection.getInstance().write(rb.build());
			// using queue
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public void release() {
		CommConnection.getInstance().release();
	}

	/**
	 * Since the service/server is asychronous we need a unique ID to associate
	 * our requests with the server's reply
	 * 
	 * @return
	 */
	private synchronized long nextId() {
		return ++curID;
	}

	public static Header.Builder createHeader(int nodeID,int destination,String sourceHost, String destinationHost){
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(nodeID);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(destination);

		hb.setSourceHost(sourceHost);
		hb.setDestinationHost(destinationHost);

		return hb;
	}
}
