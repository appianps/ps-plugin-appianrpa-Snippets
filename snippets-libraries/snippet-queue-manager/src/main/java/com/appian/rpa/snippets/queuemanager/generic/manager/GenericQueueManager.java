package com.appian.rpa.snippets.queuemanager.generic.manager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.appian.rpa.snippets.queuemanager.annotations.AItemFile;
import com.appian.rpa.snippets.queuemanager.check.CheckModelUtils;
import com.appian.rpa.snippets.queuemanager.utils.annotations.AnnotationUtils;
import com.appian.rpa.snippets.queuemanager.utils.conversion.ConversionUtils;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
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
public class GenericQueueManager {

	/** Server instance */
	private IJidokaServer<?> server;

	/** Current queue id */
	private String currentQueueId;

	/** IQueueManager instance */
	private IQueueManager queueManager;

	/** Model class */
	private Class<?> modelClass;

	/**
	 * GenericQueueManager constructor
	 * 
	 * @param clazz Class of the model object
	 */
	private GenericQueueManager(Class<?> clazz) {

		server = JidokaFactory.getServer();
		queueManager = server.getQueueManager();

		this.modelClass = clazz;

		checkClass();
	}

	/**
	 * Checks if the class meets the requirements for the library to work
	 */
	private void checkClass() {

		CheckModelUtils.hasNoArgumentsPublicConstructor(modelClass);

		CheckModelUtils.checkGettersSetters(modelClass);

		CheckModelUtils.checkFieldsWithAnnotations(modelClass);

		CheckModelUtils.checkExtendsSerializable(modelClass);

	}

	/**
	 * Creates a new queue naming it with the given {@code queueName}.<br>
	 * It creates the queue with the given queue name, a {@linkplain EPriority#HIGH}
	 * priority and 1 attempt by default.
	 * 
	 * @param clazz     Class of the model object
	 * @param queueName The new queue name
	 * @return {@linkplain GenericQueueManager} instance
	 * 
	 */
	public static GenericQueueManager createAndAssingNewQueue(Class<?> clazz, String queueName) {
		CreateQueueParameters queueParams = new CreateQueueParameters();

		queueParams.setDescription("Queue " + queueName);

		queueParams.setName(queueName);
		queueParams.setPriority(EPriority.HIGH);
		queueParams.setAttemptsByDefault(1);

		return createAndAssingNewQueue(clazz, queueParams);
	}

	/**
	 * Creates a new queue using the given customized {@code queueParams}
	 * 
	 * @param clazz       Class of the model object
	 * @param queueParams Parameters to create the new queue
	 * @return {@linkplain GenericQueueManager} instance
	 * 
	 */
	public static GenericQueueManager createAndAssingNewQueue(Class<?> clazz, CreateQueueParameters queueParams) {

		try {
			GenericQueueManager instance = new GenericQueueManager(clazz);

			instance.currentQueueId = instance.queueManager.createQueue(queueParams);

			if (StringUtils.isBlank(instance.currentQueueId)) {
				throw new JidokaFatalException("Queue not created correctly");
			}

			instance.server.info("Queue " + queueParams.getName() + " created with id: " + instance.currentQueueId);

			return instance;
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error creating the queue", e);
		}

	}

	/**
	 * Search for a queue which is named as same as the given {@code queueName}
	 * 
	 * @param clazz     Class of the model object
	 * @param queueName The queue name to search
	 * 
	 * @return {@linkplain GenericQueueManager} instance. If it doesn't find a queue
	 *         with the given name, it returns null.
	 * 
	 */
	public static GenericQueueManager assignExistingQueue(Class<?> clazz, String queueName) {
		try {

			GenericQueueManager instance = new GenericQueueManager(clazz);

			if (StringUtils.isBlank(queueName)) {
				throw new JidokaFatalException("The queue name can't be null or empty. Name given: " + queueName);
			}
			instance.server.debug("Checking existing queue with name " + queueName);
			FindQueuesParameters fqp = new FindQueuesParameters();
			fqp.nameRegex(queueName);

			List<IQueue> foundQueueList = instance.queueManager.findQueues(fqp);

			IQueue foundQueue;

			if (!foundQueueList.isEmpty()) {
				foundQueue = foundQueueList.get(0);
				instance.server.debug("Queue already exists");
			} else {
				instance.server.debug("Queue not found");
				return null;
			}

			if (foundQueue.state().equals(EQueueCurrentState.FINISHED)) {
				ReserveQueueParameters rqp = new ReserveQueueParameters();
				rqp.queueId(foundQueue.queueId());

				instance.queueManager.reserveQueue(rqp);

				ReleaseQueueParameters rlqp = new ReleaseQueueParameters().closed(false)
						.state(EQueueCurrentState.PENDING);
				instance.queueManager.releaseQueue(rlqp);
			}

			AssignQueueParameters aqp = new AssignQueueParameters();
			aqp.queueId(foundQueue.queueId());

			instance.queueManager.assignQueue(aqp);

			instance.currentQueueId = foundQueue.queueId();

			instance.server.debug("Queue assigned");

			return instance;

		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error getting the queue with the given name '" + queueName + "'", e);
		}
	}

	/**
	 * Get the pending items if the queue is open
	 * 
	 */
	public int getPendingItems() {
		try {
			if (queueManager.currentQueue() != null) {
				return queueManager.currentQueue().pendingItems();
			} else {
				return 0;
			}
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error getting pending Items");
		}
	}

	/**
	 * Reserves and closes the queue. Only one robot can close the queue.
	 * 
	 */
	public void closeQueue() {
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
			throw new JidokaFatalException("Error closing the queue", e);
		}
	}

	/**
	 * Adds the given {@code object} to the queue, mapping it to a queue item. If
	 * the model contains fields of type Path or File, the files pointed by these
	 * paths or saved on this File objects are going to be added to the item.
	 * 
	 * @param object Object to map to a queue item
	 *
	 */
	public <T> void addItem(T object) {

		try {
			CreateItemParameters cip = new CreateItemParameters();

			String keyValue = AnnotationUtils.getKeyFieldValue(object);

			if (StringUtils.isBlank(keyValue)) {
				throw new JidokaFatalException("The value of the key field must not be null or blank");
			}

			cip.setKey(keyValue);
			cip.setPriority(EPriority.NORMAL);
			cip.setQueueId(currentQueueId);

			Map<String, String> map = ConversionUtils.object2Map(object);

			// Gets the fields with annotation @AItemField
			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(modelClass, AItemFile.class);

			List<Path> filesList = new ArrayList<>();

			// Searches for Path and File fields and adds their values to the paths list
			for (Field field : fields) {
				if (File.class.isAssignableFrom(field.getType())) {
					File file = (File) AnnotationUtils.getFieldValue(object, field);

					Path path = file.toPath();

					if (filesList.contains(path)) {
						throw new JidokaFatalException(
								"The file " + file.getName() + " can't be duplicated on the item files list");
					}
					filesList.add(path);
					map.put(field.getName(), path.getFileName().toString());
				}
			}

			if (!filesList.isEmpty()) {
				cip.setFiles(filesList);
			}

			cip.setFunctionalData(map);

			String itemId = queueManager.createItem(cip);

			if (StringUtils.isBlank(itemId)) {
				throw new JidokaFatalException("The item " + keyValue + " was not created correctly");
			}

			server.debug(String.format("Added item to queue %s with id %s", cip.getQueueId(), cip.getKey()));
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error adding the given item to the queue", e);
		}
	}

	/**
	 * Saves the given {@link Object} {@code object} as a queue item. By default, it
	 * sets that the retries number decrement by 1 and release process
	 * {@link EQueueItemReleaseProcess#SYSTEM}.
	 * 
	 * @param object The T object to save as an item.
	 * 
	 */
	public <T> void updateItem(T object) {

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
	 */
	public <T> void updateItem(T object, EQueueItemReleaseRetry retries) {

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
	 */
	public <T> void updateItem(T object, EQueueItemReleaseRetry retries, EQueueItemReleaseProcess releaseProcess) {

		try {

			Map<String, String> functionalData = ConversionUtils.object2Map(object);

			ReleaseItemWithOptionalParameters tiop = new ReleaseItemWithOptionalParameters();
			tiop.setProcess(releaseProcess);
			tiop.setRetry(retries);

			// Gets the fields with annotation @AItemFile
			tiop.removePreviousFiles(true);
			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(modelClass, AItemFile.class);
			List<Path> filesList = new ArrayList<>();
			// Searches for Path and File fields and adds their values to the paths list
			for (Field field : fields) {
				if (File.class.isAssignableFrom(field.getType())) {
					File file = (File) AnnotationUtils.getFieldValue(object, field);

					Path path = file.toPath();

					if (filesList.contains(path)) {
						throw new JidokaFatalException(
								"The file " + file.getName() + " can't be duplicated on the item files list");
					}
					filesList.add(path);
					functionalData.put(field.getName(), path.getFileName().toString());
				}
			}
			if (!filesList.isEmpty()) {
				tiop.filesToAdd(filesList);
			}
			tiop.functionalData(functionalData);
			queueManager.releaseItem(tiop);

		} catch (Exception e) {
			throw new JidokaFatalException("Error updating the item: ", e);
		}
	}

	/**
	 * Returns items whose key matches the given regular expression {@code keyRegex}
	 * 
	 * @param key key to search for
	 * @return The list of T objects resulting from the search
	 * 
	 */
	public <T> List<T> findItems(String keyRegex) {
		return findItems(keyRegex, new ArrayList<EQueueItemCurrentState>());
	}

	/**
	 * Returns items whose key matches the given regular expression {@code keyRegex}
	 * and are on one of the given states {@code states}
	 * 
	 * @param key    key to search for
	 * @param states List of queue item states to filter by
	 * @return The list of T objects resulting from the search
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findItems(String keyRegex, List<EQueueItemCurrentState> states) {

		try {
			DownloadQueueParameters dqp = new DownloadQueueParameters().queueId(currentQueueId.trim());
			IDownloadedQueue downloadedQueue = queueManager.downloadQueue(dqp);

			String escapedKey = Pattern.quote(keyRegex);

			List<IQueueItem> filteredItems = downloadedQueue.items().stream().filter(i -> i.key().matches(escapedKey))
					.collect(Collectors.toList());

			if (!states.isEmpty()) {
				return filteredItems.stream().filter(i -> states.contains(i.state()))
						.map(i -> (T) ConversionUtils.map2Object(i.functionalData(), this.modelClass))
						.collect(Collectors.toList());
			} else {
				return filteredItems.stream()
						.map(i -> (T) ConversionUtils.map2Object(i.functionalData(), this.modelClass))
						.collect(Collectors.toList());
			}

		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error finding the given item", e);
		}
	}

	/**
	 * Gets the queue next item, mapping the item functional data to an object with
	 * the model type.
	 * 
	 * @return The next item of type T
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getNextItem() {

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

			T object = (T) ConversionUtils.map2Object(currentQueueItem.functionalData(), this.modelClass);

			ConversionUtils.setObjectKeyValue(object, currentQueueItem.key(), this.modelClass);

			ConversionUtils.setObjectFiles(object, currentQueueItem, this.modelClass);

			return object;

		} catch (Exception e) {
			throw new JidokaFatalException("Error getting the next queue item", e);
		}
	}

	/**
	 * Returns the current queue.
	 * 
	 * @return The current queue.
	 * 
	 */
	public IQueue getQueue() {
		try {

			FindQueuesParameters fqp = new FindQueuesParameters();
			fqp.queueId(currentQueueId);

			List<IQueue> foundQueueList = queueManager.findQueues(fqp);

			if (!foundQueueList.isEmpty()) {
				return foundQueueList.get(0);
			} else {
				return null;
			}

		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error getting the current queue", e);
		}
	}

	/**
	 * Returns the current instantiated queue manager.
	 * 
	 * @return The current instantiated queue manager.
	 */
	public IQueueManager getQueueManager() {

		return queueManager;
	}

}
