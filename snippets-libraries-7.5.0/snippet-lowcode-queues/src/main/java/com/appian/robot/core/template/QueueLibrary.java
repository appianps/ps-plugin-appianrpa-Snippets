
package com.appian.robot.core.template;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novayre.jidoka.client.api.EJidokaParameterType;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.AssignQueueParameters;
import com.novayre.jidoka.client.api.queue.EQueueCurrentState;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseProcess;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseRetry;
import com.novayre.jidoka.client.api.queue.FindQueuesParameters;
import com.novayre.jidoka.client.api.queue.IQueue;
import com.novayre.jidoka.client.api.queue.IQueueItem;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.novayre.jidoka.client.api.queue.IReservedQueue;
import com.novayre.jidoka.client.api.queue.ReleaseItemWithOptionalParameters;
import com.novayre.jidoka.client.api.queue.ReleaseQueueParameters;
import com.novayre.jidoka.client.api.queue.ReserveItemParameters;
import com.novayre.jidoka.client.api.queue.ReserveQueueParameters;
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
	public void setQueue(
			@JidokaParameter(defaultValue = "", name = "Name of the variable to store current item") String itemVariable)
			throws IOException, JidokaQueueException {

		String pQueueId = queueManager.preselectedQueue();

		FindQueuesParameters fqp = new FindQueuesParameters();
		fqp.queueId(pQueueId);

		List<IQueue> foundQueueList = queueManager.findQueues(fqp);

		wVariable = server.getWorkflowVariables().get(itemVariable);

		if (!foundQueueList.isEmpty()) {
			queue = foundQueueList.get(0);
			if (queue.state().equals(EQueueCurrentState.CLOSED) || queue.state().equals(EQueueCurrentState.FINISHED)) {
				throw new JidokaFatalException("The queue " + queue.name() + " is on the state " + queue.state()
						+ " and it can't be procesed.");
			}
		} else {
			throw new JidokaFatalException("No queue found with id '" + pQueueId + "'");
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

	@JidokaMethod(name = "Updates current queue item", description = "Updates and release the current queue item. It should be WARN or OK.", iconClass = "jf-console")
	public void updateItem(@JidokaParameter(name = "Item Result", defaultValue = "OK") String itemResult) throws IOException, JidokaQueueException {

		ReleaseItemWithOptionalParameters riop = new ReleaseItemWithOptionalParameters();
		riop.setProcess(EQueueItemReleaseProcess.SYSTEM);
		riop.setRetry(EQueueItemReleaseRetry.DECREMENT_BY_1);

		if (itemResult.equals("OK")) {
			server.setCurrentItemResultToOK();
			riop.functionalData(new ObjectMapper().readValue(wVariable.getValue().toString(), HashMap.class));
		} else {
			server.setCurrentItemResultToWarn();
		}

		queueManager.releaseItem(riop);
	}

	@JidokaMethod(name = "Closes the queue", description = "Closes the queue")
	public void closeQueue() throws IOException, JidokaQueueException {

		ReserveQueueParameters rqp = new ReserveQueueParameters().queueId(queue.queueId());
		IReservedQueue reservedQueue = queueManager.reserveQueue(rqp);

		if (reservedQueue == null || !EQueueCurrentState.FINISHED.equals(reservedQueue.queue().state())
				|| reservedQueue.queue().pendingItems() != 0 || reservedQueue.queue().inProcessItems() != 0) {
			server.info("The current queue can't be reserved because it is not finished yet");
			if (reservedQueue != null && reservedQueue.queue() != null && reservedQueue.queue().state() != null) {
				server.info(String.format("State: %s, Pending Items: %d, InProcess Items: %d",
						reservedQueue.queue().state().name(), reservedQueue.queue().pendingItems(),
						reservedQueue.queue().inProcessItems()));
			}

			if (reservedQueue != null) {
				ReleaseQueueParameters releaseQueueParameters = new ReleaseQueueParameters();
				releaseQueueParameters.setClosed(reservedQueue.queue().state().equals(EQueueCurrentState.CLOSED));
				queueManager.releaseQueue(releaseQueueParameters);
			}
		}

		ReleaseQueueParameters releaseQueueParameters = new ReleaseQueueParameters();
		releaseQueueParameters.closed(true);
		queueManager.releaseQueue(releaseQueueParameters);

		server.info(String.format("Queue %s closed", queue.queueId()));
	}

}
