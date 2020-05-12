package com.appian.rpa.snippets.commons.queues.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.appian.rpa.snippets.commons.queues.annotations.QueueAnnotationUtil;
import com.appian.rpa.snippets.commons.queues.conversion.QueueConversionUtils;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.AssignQueueParameters;
import com.novayre.jidoka.client.api.queue.CreateItemParameters;
import com.novayre.jidoka.client.api.queue.CreateQueueParameters;
import com.novayre.jidoka.client.api.queue.DownloadQueueParameters;
import com.novayre.jidoka.client.api.queue.EPriority;
import com.novayre.jidoka.client.api.queue.EQueueCurrentState;
import com.novayre.jidoka.client.api.queue.EQueueItemCurrentState;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseProcess;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseRetry;
import com.novayre.jidoka.client.api.queue.FindQueuesParameters;
import com.novayre.jidoka.client.api.queue.IDownloadedQueue;
import com.novayre.jidoka.client.api.queue.IQueue;
import com.novayre.jidoka.client.api.queue.IQueueItem;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.novayre.jidoka.client.api.queue.IReservedQueue;
import com.novayre.jidoka.client.api.queue.ReleaseItemWithOptionalParameters;
import com.novayre.jidoka.client.api.queue.ReleaseQueueParameters;
import com.novayre.jidoka.client.api.queue.ReserveItemParameters;
import com.novayre.jidoka.client.api.queue.ReserveQueueParameters;

/**
 * Class to manage a queue which is going to be updated constantly when new data
 * arrives.
 */
public class QueueFromGenericSource {

	/** Server instance */
	private IJidokaServer<?> server;

	/** Current queue id */
	String currentQueueId;

	/** IQueueManager instance */
	private IQueueManager queueManager;

	/** Model class */
	private Class<?> modelClass;

	/**
	 * QueueFromGenericSource constructor
	 */
	public QueueFromGenericSource(Class<?> clazz) {

		this.server = JidokaFactory.getServer();
		this.queueManager = server.getQueueManager();

		this.modelClass = clazz;
	}

	/**
	 * Creates a new queue naming it with the given {@code queueName}.<br>
	 * It creates the queue with the given queue name, a {@link EPriority#HIGH}
	 * priority and 1 attempt by default.
	 * 
	 * @param queueName The new queue name
	 * 
	 * @throws JidokaQueueException
	 */
	public void createQueue(String queueName) throws JidokaQueueException {
		CreateQueueParameters queueParams = new CreateQueueParameters();

		queueParams.setDescription("Queue " + queueName);

		queueParams.setName(queueName);
		queueParams.setPriority(EPriority.HIGH);
		queueParams.setAttemptsByDefault(1);

		createQueue(queueParams);
	}

	/**
	 * Creates a new queue using the given customized {@code queueParams}
	 * 
	 * @param queueParams Parameters to create the new queue
	 * 
	 * @throws JidokaQueueException
	 */
	public void createQueue(CreateQueueParameters queueParams) throws JidokaQueueException {

		try {

			currentQueueId = queueManager.createQueue(queueParams);

			server.info("Queue " + queueParams.getName() + " created with id: " + currentQueueId);
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaQueueException("Error creating the queue", e);
		}

	}

	/**
	 * Search for a queue which is named as same as the given {@code queueName}
	 * 
	 * @param queueName The queue name to search
	 * 
	 * @return The queue found. If it doesn't found a queue, it returns null
	 * 
	 * @throws JidokaQueueException
	 */
	public IQueue findQueue(String queueName) throws JidokaQueueException {
		try {

			FindQueuesParameters fqp = new FindQueuesParameters();
			fqp.nameRegex(queueName);

			List<IQueue> foundQueueList = queueManager.findQueues(fqp);

			IQueue foundQueue;

			if (!foundQueueList.isEmpty()) {
				foundQueue = foundQueueList.get(0);
			} else {
				return null;
			}

			if (foundQueue.state().equals(EQueueCurrentState.FINISHED)) {
				ReserveQueueParameters rqp = new ReserveQueueParameters();
				rqp.queueId(foundQueue.queueId());

				queueManager.reserveQueue(rqp);

				ReleaseQueueParameters rlqp = new ReleaseQueueParameters().closed(false)
						.state(EQueueCurrentState.PENDING);
				queueManager.releaseQueue(rlqp);
			}

			AssignQueueParameters aqp = new AssignQueueParameters();
			aqp.queueId(foundQueue.queueId());

			queueManager.assignQueue(aqp);

			currentQueueId = foundQueue.queueId();
			return foundQueue;

		} catch (IOException | JidokaQueueException e) {
			throw new JidokaQueueException("Error getting the queue from the given id", e);
		}
	}

	/**
	 * Reserves and closes the queue. Only one robot can close the queue.
	 * 
	 * @throws JidokaQueueException
	 */
	public void closeQueue() throws JidokaQueueException {
		try {

			ReserveQueueParameters rqp = new ReserveQueueParameters().queueId(currentQueueId.trim());
			IReservedQueue reservedQueue = queueManager.reserveQueue(rqp);

			if (reservedQueue == null || !EQueueCurrentState.FINISHED.equals(reservedQueue.queue().state())
					|| reservedQueue.queue().pendingItems() != 0 || reservedQueue.queue().inProcessItems() != 0) {
				server.info("This execution cannot reserve the current queue because is not finished yet");
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

			server.info(String.format("Queue %s closed", currentQueueId));

		} catch (Exception e) {
			throw new JidokaQueueException(e);
		}
	}

	/**
	 * Adds the given {@code object} to the queue, mapping it to a queue item
	 * 
	 * @param object Object to map to a queue item
	 *
	 * @throws JidokaQueueException
	 */
	public <T> void addItem(T object) throws JidokaQueueException {

		try {
			CreateItemParameters cip = new CreateItemParameters();

			String keyValue = QueueAnnotationUtil.getKeyFieldValue(object);

			cip.setKey(keyValue);
			cip.setPriority(EPriority.NORMAL);
			cip.setQueueId(currentQueueId);

			Map<String, String> map = QueueConversionUtils.object2Map(object);

			cip.setFunctionalData(map);

			queueManager.createItem(cip);

			server.debug(String.format("Added item to queue %s with id %s", cip.getQueueId(), cip.getKey()));
		} catch (JidokaException | IOException | JidokaQueueException e) {
			throw new JidokaQueueException("Error adding the given item to the queue");
		}
	}

	/**
	 * Saves the given {@link Object} {@code object} as a queue item. By default, it
	 * sets that the retries number decrement by 1 and release process
	 * {@link EQueueItemReleaseProcess#SYSTEM}.
	 * 
	 * @param object The T object to save as an item.
	 * 
	 * @throws JidokaQueueException
	 */
	public <T> void updateItem(T object) throws JidokaQueueException {

		updateItem(object, EQueueItemReleaseRetry.DECREMENT_BY_1);
	}

	/**
	 * Saves the given {@link Object} {@code object} as a queue item. By default, it
	 * sets the {@link EQueueItemReleaseProcess} to
	 * {@link EQueueItemReleaseProcess#SYSTEM}
	 * 
	 * @param object  The T object to save as an item.
	 * @param retries Item retries number.
	 * 
	 * @throws JidokaQueueException
	 */
	public <T> void updateItem(T object, EQueueItemReleaseRetry retries) throws JidokaQueueException {

		updateItem(object, retries, EQueueItemReleaseProcess.SYSTEM);
	}

	/**
	 * Saves the given {@link Object} {@code object} as a queue item. It maps the
	 * object to a map with the item functional data and release the item with the
	 * given retries number and sets the process to the given release process.
	 * 
	 * @param object         The item to save
	 * @param retries        Item retries
	 * @param releaseProcess ReleaseProcess
	 * 
	 * @throws JidokaQueueException
	 */
	public <T> void updateItem(T object, EQueueItemReleaseRetry retries, EQueueItemReleaseProcess releaseProcess)
			throws JidokaQueueException {

		try {

			Map<String, String> functionalData = QueueConversionUtils.object2Map(object);

			ReleaseItemWithOptionalParameters tiop = new ReleaseItemWithOptionalParameters();
			tiop.functionalData(functionalData);
			tiop.setProcess(releaseProcess);
			tiop.setRetry(retries);

			queueManager.releaseItem(tiop);

		} catch (Exception e) {
			throw new JidokaQueueException("Error updating the item: ", e);
		}
	}

	/**
	 * Find the list of items that have the same key as the given {@code key}
	 * 
	 * @param key key to search for
	 * @return The list of T objects resulting from the search
	 * 
	 * @throws JidokaQueueException
	 */
	public <T> List<T> findItems(String key) throws JidokaQueueException {
		return findItems(key, new ArrayList<EQueueItemCurrentState>());
	}

	/**
	 * Find the list of items that have the same key as the given {@code key}
	 * 
	 * @param key    key to search for
	 * @param states List of queue item states to filter by
	 * @return The list of T objects resulting from the search
	 * 
	 * @throws JidokaQueueException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findItems(String itemKey, List<EQueueItemCurrentState> states) throws JidokaQueueException {

		try {
			DownloadQueueParameters dqp = new DownloadQueueParameters().queueId(currentQueueId.trim());
			IDownloadedQueue downloadedQueue = queueManager.downloadQueue(dqp);

			List<IQueueItem> filteredItems = downloadedQueue.items().stream().filter(i -> i.key().equals(itemKey))
					.collect(Collectors.toList());

			if (!states.isEmpty()) {
				return filteredItems.stream().filter(i -> states.contains(i.state())).map(i -> {
					try {
						return (T) QueueConversionUtils.map2Object(i.functionalData(), this.modelClass);
					} catch (JidokaException e) {
						return null;
					}
				}).collect(Collectors.toList());
			} else {
				return filteredItems.stream().map(i -> {
					try {
						return (T) QueueConversionUtils.map2Object(i.functionalData(), this.modelClass);
					} catch (JidokaException e) {
						return null;
					}
				}).collect(Collectors.toList());
			}

		} catch (IOException | JidokaQueueException e) {
			throw new JidokaQueueException("Error finding the given item", e);
		}
	}

	/**
	 * Gets the queue next item, mapping the item functional data to an object with
	 * the model type.
	 * 
	 * @return The next item of type T
	 * 
	 * @throws JidokaQueueException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getNextItem() throws JidokaQueueException {

		try {

			if (StringUtils.isBlank(currentQueueId)) {
				return null;
			}

			ReserveItemParameters reserveItemsParameters = new ReserveItemParameters();
			reserveItemsParameters.setUseOnlyCurrentQueue(true);

			IQueueItem currentQueueItem = queueManager.reserveItem(reserveItemsParameters);

			if (currentQueueItem == null || currentQueueItem.state().equals(EQueueItemCurrentState.FINISHED_OK)
					|| currentQueueItem.state().equals(EQueueItemCurrentState.FINISHED_WARN)) {
				return null;
			}

			return (T) QueueConversionUtils.map2Object(currentQueueItem.functionalData(), this.modelClass);

		} catch (Exception e) {
			throw new JidokaQueueException("Error getting the next queue item", e);
		}
	}

	/**
	 * Returns the current queue.
	 * 
	 * @return The current queue.
	 * 
	 * @throws JidokaQueueException
	 */
	public IQueue getQueue() throws JidokaQueueException {
		try {
			AssignQueueParameters qqp = new AssignQueueParameters();
			qqp.queueId(this.currentQueueId);

			return queueManager.assignQueue(qqp);
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaQueueException("Error getting the current queue");
		}
	}

}
