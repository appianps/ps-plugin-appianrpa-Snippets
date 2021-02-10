
package com.appian.robot.core.template;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.AssignQueueParameters;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseProcess;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseRetry;
import com.novayre.jidoka.client.api.queue.FindQueuesParameters;
import com.novayre.jidoka.client.api.queue.IQueue;
import com.novayre.jidoka.client.api.queue.IQueueItem;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.novayre.jidoka.client.api.queue.ReleaseItemWithOptionalParameters;
import com.novayre.jidoka.client.api.queue.ReserveItemParameters;
import com.novayre.jidoka.client.lowcode.IRobotVariable;

/**
 * The Class RobotBlankTemplate.
 */
@Nano
public class QueueLibrary implements INano {

	/** The server. */
	private IJidokaServer<?> server;

	/** The client. */
	private IQueueManager queueManager;

	/** Selected queue */
	private IQueue queue;

	/** Workflow variable to store current item */
	private IRobotVariable wVariable;
	
	/** Current item index */
	private int currentItemIndex = 1;

	@Override
	public void init() throws Exception {

		server = JidokaFactory.getServer();
		queueManager = server.getQueueManager();

		INano.super.init();
	}

	@JidokaMethod(name = "Sets the queue to process", description = "Sets the queue to process")
	public void setQueue(@JidokaParameter(defaultValue = "", name = "Queue Id * ") String queueId,
			@JidokaParameter(defaultValue = "", name = "Name of the variable to store current item") String itemVariable)
			throws IOException, JidokaQueueException {
		FindQueuesParameters fqp = new FindQueuesParameters();
		fqp.queueId(queueId);

		List<IQueue> foundQueueList = queueManager.findQueues(fqp);

		wVariable = server.getWorkflowVariables().get(itemVariable);

		if (!foundQueueList.isEmpty()) {
			queue = foundQueueList.get(0);
		} else {
			throw new JidokaFatalException("No queue found with id '" + queueId + "'");
		}

		AssignQueueParameters aqp = new AssignQueueParameters();
		aqp.queueId(queue.queueId());

		queueManager.assignQueue(aqp);
		
		server.setNumberOfItems(queue.pendingItems());
	}

	@JidokaMethod(name = "Gets the next queue item", description = "Gets the next queue item")
	public Boolean getNextItem() throws IOException, JidokaQueueException {

		ReserveItemParameters reserveItemsParameters = new ReserveItemParameters();
		reserveItemsParameters.setUseOnlyCurrentQueue(true);

		IQueueItem currentQueueItem = queueManager.reserveItem(reserveItemsParameters);

		if (currentQueueItem == null) {
			return false;
		}

		server.setCurrentItem(currentItemIndex, currentQueueItem.key());
		
		currentItemIndex++;

		String json = new ObjectMapper().writeValueAsString(currentQueueItem.functionalData());

		wVariable.setValue(json);

		return true;
	}
	
	@SuppressWarnings("unchecked")
	@JidokaMethod(name = "Updates current queue item", description = "Updates and release the current queue item")
	public void updateItem() throws IOException, JidokaQueueException {
		
		server.setCurrentItemResultToOK();

		ReleaseItemWithOptionalParameters riop = new ReleaseItemWithOptionalParameters();
		riop.setProcess(EQueueItemReleaseProcess.SYSTEM);
		riop.setRetry(EQueueItemReleaseRetry.DECREMENT_BY_1);
		
		riop.functionalData(new ObjectMapper().readValue(wVariable.getValue().toString(), HashMap.class));

		queueManager.releaseItem(riop);
	}

}
