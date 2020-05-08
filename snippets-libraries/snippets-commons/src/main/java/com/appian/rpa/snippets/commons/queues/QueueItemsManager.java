package com.appian.rpa.snippets.commons.queues;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.appian.rpa.snippets.commons.excel.annotations.utils.QueueFromExcelCreator;
import com.appian.rpa.snippets.commons.excel.mapper.AbstractItemFieldsMapper;
import com.appian.rpa.snippets.commons.excel.utils.ExcelUtils;
import com.appian.rpa.snippets.commons.utils.conversion.ConversionUtils;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.execution.ExecutionParameter;
import com.novayre.jidoka.client.api.execution.LaunchOptions;
import com.novayre.jidoka.client.api.execution.LaunchResult;
import com.novayre.jidoka.client.api.queue.AssignQueueParameters;
import com.novayre.jidoka.client.api.queue.EQueueCurrentState;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseProcess;
import com.novayre.jidoka.client.api.queue.EQueueItemReleaseRetry;
import com.novayre.jidoka.client.api.queue.IDownloadedQueue;
import com.novayre.jidoka.client.api.queue.IQueue;
import com.novayre.jidoka.client.api.queue.IQueueItem;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.novayre.jidoka.client.api.queue.IReservedQueue;
import com.novayre.jidoka.client.api.queue.ReleaseItemWithOptionalParameters;
import com.novayre.jidoka.client.api.queue.ReleaseQueueParameters;
import com.novayre.jidoka.client.api.queue.ReserveItemParameters;
import com.novayre.jidoka.client.api.queue.ReserveQueueParameters;
import com.novayre.jidoka.client.api.queue.UpdateQueueParameters;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider.Provider;
import com.novayre.jidoka.data.provider.api.IJidokaExcelDataProvider;

import jodd.io.FileUtil;

/**
 * Class to manage all the relative functions related to queues and queue items.
 *
 * @param <T> Generic type of the model
 */
public class QueueItemsManager<T> {

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** IQueueManager instance */
	private IQueueManager queueManager;

	/** ReserveItemsParameters instance */
	private ReserveItemParameters reserveItemsParameters;

	/** IQueue instance */
	private IQueue queue;

	/** AbstractItemFieldsMapper instance */
	private AbstractItemFieldsMapper<T> mapper;

	/** IRobot instance */
	private IRobot robot;

	/** Name of the instruction with the number of robotic processes executions */
	private static final String PARAM_NUMBER_OF_EXECUTIONS = "numberOfExecutions";

	/** Name of the instruction with the file name. */
	private static final String PARAM_INPUT_FILE = "inputFile";

	/** Number of robotic processes executions */
	private int numberOfExecutions;

	/** Queue output file */
	private File queueOutputFile;

	/**
	 * QueueItemsManager constructor
	 * 
	 * @param robot  {@link IRobot} instance
	 * @param mapper Mapper to map the data to an {@link AbstractItemFieldsMapper}
	 *               object
	 */
	public QueueItemsManager(IRobot robot, AbstractItemFieldsMapper<T> mapper) {

		this.mapper = mapper;

		this.server = JidokaFactory.getServer();

		this.queueManager = server.getQueueManager();

		this.reserveItemsParameters = new ReserveItemParameters();

		this.reserveItemsParameters.setUseOnlyCurrentQueue(true);

		this.robot = robot;

		String executionsNum = server.getParameters().get(PARAM_NUMBER_OF_EXECUTIONS);

		if (StringUtils.isBlank(executionsNum)) {
			numberOfExecutions = 1;
		} else {
			numberOfExecutions = Integer.valueOf(executionsNum);
		}

	}

	/**
	 * Gets the queue from the given id {@code queueId}. If the queue is closed, it
	 * reserves it and returns it. If not, it assigns the queue.
	 *
	 * @param queueId     The queue id
	 * @param closedQueue If the queue is closed
	 * @return the queue from id
	 * 
	 * @throws JidokaQueueException
	 */
	private IQueue getQueueFromId(String queueId) throws JidokaQueueException {

		try {

			AssignQueueParameters qqp = new AssignQueueParameters();
			qqp.queueId(queueId);

			return queueManager.assignQueue(qqp);

		} catch (IOException e) {
			throw new JidokaQueueException("Error getting the queue from the given id" + queueId, e);
		}

	}

	/**
	 * Gets the queue next item, mapping the item functional data to an object with
	 * the model type.
	 * 
	 * @return The next item of type {@link org.apache.poi.ss.formula.functions.T}
	 * 
	 * @throws JidokaQueueException
	 */
	public T getNextItem() throws JidokaQueueException {

		try {

			if (StringUtils.isBlank(this.queue.queueId())) {
				return null;
			}

			IQueueItem currentQueueItem = queueManager.reserveItem(reserveItemsParameters);

			if (currentQueueItem == null) {
				return null;
			}

			return (T) ConversionUtils.map2Object(currentQueueItem.functionalData(), mapper.getTClass());

		} catch (Exception e) {
			throw new JidokaQueueException("Error getting the next queue item", e);
		}
	}

	/**
	 * Saves the given {@link Object} {@code currentItem} as a queue item. By
	 * default, it sets that the retries number decrement by 1.
	 * 
	 * @param currentItem The T object to save as an item.
	 * 
	 * @throws JidokaQueueException
	 */
	public void saveItem(T currentItem) throws JidokaQueueException {

		saveItem(currentItem, EQueueItemReleaseRetry.DECREMENT_BY_1);
	}

	/**
	 * Saves the given {@link Object} {@code currentItem} as a queue item. By
	 * default, it sets the {@link EQueueItemReleaseProcess} to {@code SYSTEM}
	 * 
	 * @param currentItem The T object to save as an item.
	 * @param retries     Item retries number.
	 * 
	 * @throws JidokaQueueException
	 */
	public void saveItem(T currentItem, EQueueItemReleaseRetry retries) throws JidokaQueueException {

		saveItem(currentItem, retries, EQueueItemReleaseProcess.SYSTEM);
	}

	/**
	 * Saves the given {@link Object} {@code currentItem} as a queue item. It maps
	 * the object to a map with the item functional data and release the item with
	 * the given retries number and sets the process to the given release process.
	 * 
	 * @param currentItem    The item to save
	 * @param retries        Item retries
	 * @param releaseProcess ReleaseProcess
	 * 
	 * @throws JidokaQueueException
	 */
	public void saveItem(T currentItem, EQueueItemReleaseRetry retries, EQueueItemReleaseProcess releaseProcess)
			throws JidokaQueueException {

		try {

			Map<String, String> functionalData = ConversionUtils.object2Map(currentItem);

			ReleaseItemWithOptionalParameters tiop = new ReleaseItemWithOptionalParameters();
			tiop.functionalData(functionalData);
			tiop.setProcess(releaseProcess);
			tiop.setRetry(retries);

			queueManager.releaseItem(tiop);

		} catch (Exception e) {
			throw new JidokaQueueException("Error saving the item: ", e);
		}
	}

	/**
	 * Gets the current queue.
	 * 
	 * @return The current queue
	 */
	public IQueue getCurrentQueue() {

		return this.queue;
	}

	/**
	 * Assigns the queue to the current robot. If there is no preselected queue and
	 * there's an input Excel file, it creates the queue from the Excel file.<br>
	 * To run several robots with the same queue created from an Excel file, it must
	 * be created a String robot parameter on the console called
	 * 'numberOfExecutions'.
	 * 
	 * @throws JidokaQueueException
	 */
	public void assignQueue() throws JidokaQueueException {

		try {
			if (mapper != null) {

				if (queueManager != null && !StringUtils.isBlank(queueManager.preselectedQueue())) {

					this.queue = getQueueFromId(server.getQueueManager().preselectedQueue());
					server.debug(String.format("The queue %s has been selected directly in the console",
							server.getQueueManager().preselectedQueue()));

				} else if (!StringUtils.isBlank(server.getParameters().get(PARAM_INPUT_FILE))) {

					File inputFile = Paths.get(server.getCurrentDir(), server.getParameters().get(PARAM_INPUT_FILE))
							.toFile();

					this.queue = createQueueFromFile(inputFile);

					server.debug("It is used the received file as a parameter");

					if (numberOfExecutions > 1) {
						launchRobotsWithQueue();
					}

				} else {
					throw new JidokaFatalException(
							"There is no queue selected and no input file selected. Assign a queue or create an instruction called: "
									+ PARAM_INPUT_FILE);
				}
			}

			if (queue == null) {
				throw new JidokaQueueException("The queue was not correctly asssigned/created");
			}

			server.setNumberOfItems(queue.pendingItems());
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaQueueException("Error assigning the queue", e);
		}

	}

	/**
	 * 
	 * Create a queue from a file {@code inputFile}
	 * 
	 * @param inputFile File used to create the queue
	 * 
	 * @return The created queue
	 * 
	 * @throws JidokaQueueException
	 */
	private IQueue createQueueFromFile(File inputFile) throws JidokaQueueException {

		if (inputFile == null || !inputFile.exists()) {
			throw new JidokaQueueException("An input file is needed");
		}

		server.debug("Using file: " + inputFile.getAbsolutePath());

		return new QueueFromExcelCreator<T>(this.robot, inputFile.getAbsolutePath(), mapper).getQueue();

	}

	/**
	 * Closes the current queue and saves the queue results on the
	 * {@link EQueueResultTarget} selected.<br>
	 * The queue will be reserved before closing it, and it only can be closed if it
	 * is in FINISHED state and there is no pending or in process items
	 * 
	 * @param queueResultsTarget Queue results target
	 * 
	 * @return {@link QueueResults} of closing the queue
	 * 
	 * @throws Exception
	 */
	public QueueResults<T> closeQueue(EQueueResultTarget queueResultsTarget) throws JidokaQueueException {

		try {

			ReserveQueueParameters rqp = new ReserveQueueParameters().queueId(queue.queueId().trim());
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
				return null;
			}

			QueueResults<T> queueResults = updateQueueFileResults(reservedQueue, queueResultsTarget);

			ReleaseQueueParameters releaseQueueParameters = new ReleaseQueueParameters();
			releaseQueueParameters.closed(true);
			queueManager.releaseQueue(releaseQueueParameters);
			
			this.queueOutputFile = createQueueOutputFile();

			server.info(String.format("Queue %s closed", queue.queueId()));

			return queueResults;

		} catch (Exception e) {
			throw new JidokaQueueException(e);
		}
	}

	/**
	 * Updates the reserved queue results target.
	 * 
	 * @param reservedQueue      Reserver queue to update
	 * @param queueResultsTarget Queue results target
	 * @return {@link QueueResults} of updating the queue file
	 * 
	 * @throws JidokaQueueException
	 */
	private QueueResults<T> updateQueueFileResults(IReservedQueue reservedQueue, EQueueResultTarget queueResultsTarget)
			throws JidokaQueueException {

		try {
			QueueResults<T> queueResults = updateFile(reservedQueue, queueResultsTarget);

			// We update the file inside the queue with the results of the process
			UpdateQueueParameters updateQueueParameters = new UpdateQueueParameters();
			updateQueueParameters.fileContent(FileUtil.readBytes(queueResults.getExcelFile()));
			queueManager.updateQueue(updateQueueParameters);

			return queueResults;
		} catch (JidokaQueueException | IOException e) {
			throw new JidokaQueueException("Error updating the queue file results", e);
		}
	}

	/**
	 * Updates the downloaded queue results target.
	 *
	 * @param rq                 Queue to extract the data
	 * @param queueResultsTarget Queue results target
	 * @return {@link QueueResults} of updating the file
	 * 
	 * @throws JidokaQueueException
	 */
	private QueueResults<T> updateFile(IDownloadedQueue rq, EQueueResultTarget queueResultsTarget)
			throws JidokaQueueException {

		List<T> itemsResults = new ArrayList<>();

		File file = extractFilefromQueue(rq.queue());

		List<IQueueItem> items = rq.items();

		// Lambda expression with type information removed.
		Collections.sort(items, (i1, i2) -> Integer.parseInt(i1.reference()) - Integer.parseInt(i2.reference()));

		IJidokaExcelDataProvider<T> dpRes = IJidokaDataProvider.getInstance(robot, Provider.EXCEL);

		try {
			dpRes.init(file.getAbsolutePath(), mapper.getSheetName(), mapper.getFirstRow(), mapper);

			// We need to do this because sometimes the executions finish with two sheets
			// selected.
			ExcelUtils.selectAndActivateOnlyGivenSheet(dpRes.getExcel().getWorkbook(), mapper.getSheetName());

		} catch (Exception e) {
			throw new JidokaQueueException(e);
		}

		try {
			while (dpRes.nextRow()) {

				IQueueItem queueItem = items.stream()
						.filter(i -> i.reference().equals(String.valueOf(dpRes.getCurrentItemNumber()))).findFirst()
						.orElse(null);

				if (queueItem == null) {
					if (EQueueResultTarget.EXCEL.equals(queueResultsTarget)) {

						itemsResults.add(dpRes.getCurrentItem());
					}
					continue;
				}

				T currentItem = (T) ConversionUtils.map2Object(queueItem.functionalData(), mapper.getTClass());

				dpRes.updateItem(currentItem);

				itemsResults.add(currentItem);
			}

			mapper.resetHeaders();

		} catch (Exception e) {
			throw new JidokaQueueException(e);
		} finally {

			try {
				dpRes.close();
			} catch (IOException e) {
				throw new JidokaQueueException(e);
			}
		}

		QueueResults<T> res = new QueueResults<>();
		res.setItemsResults(itemsResults);
		res.setExcelFile(file);

		server.info("Output file updated: " + file.getAbsolutePath());

		return res;
	}

	/**
	 * Extracts the queue file.
	 * 
	 * @param queue Queue where to extract the file
	 * @return The queue file
	 * 
	 * @throws JidokaQueueException
	 */
	public File extractFilefromQueue(IQueue queue) throws JidokaQueueException {

		if (queue == null) {

			server.debug("There is no queue assigned or passed as a parameter");
			return null;
		}

		Path resFolderPath = Paths.get(server.getCurrentDir(), "res");

		if (!resFolderPath.toFile().exists()) {

			resFolderPath.toFile().mkdirs();
		}

		Path pathToFile = resFolderPath.resolve(queue.fileName());

		try {

			FileUtils.writeByteArrayToFile(pathToFile.toFile(), queue.fileContent());
		} catch (IOException e) {
			throw new JidokaQueueException(e);
		}

		return pathToFile.toFile();
	}

	/**
	 * Create the queue output file. It adds the sufix [UPDATED] to the name of the
	 * queue file.
	 *
	 * @return The output file
	 */
	private File createQueueOutputFile() {

		// Gets the queue file path
		Path resFolderPath = Paths.get(server.getCurrentDir(), "res");

		File queueFile = resFolderPath.resolve(queue.fileName()).toFile();

		int pointIndex = queueFile.getName().lastIndexOf('.');

		String name = queueFile.getName().substring(0, pointIndex);

		String ext = queueFile.getName().substring(pointIndex);

		// Renames the output file
		File outputFile = new File(name + " [UPDATED]" + ext);

		try {
			FileUtils.copyFile(queueFile, outputFile);
		} catch (IOException e) {
			throw new JidokaFatalException("Error getting the queue output file: ", e);
		}

		return outputFile;
	}

	/**
	 * Gets the queue output file.
	 * 
	 * @return The queue output file
	 */
	public File getQueueOutputFile() {
		return this.queueOutputFile;
	}

	/**
	 * Launchs the new robotics processes with the same parameters that the initial
	 * one.
	 */
	private void launchRobotsWithQueue() {

		try {

			LaunchOptions options = new LaunchOptions();
			options.queueId(this.queue.queueId());
			options.robotName(this.server.getExecution(0).getRobotName());
			options.executionsToLaunch(numberOfExecutions - 1);
			setParameters(options);

			List<LaunchResult> launchResults;

			launchResults = server.getExecution(0).launchRobots(options);

			if (!launchResults.get(0).isLaunched()) {
				throw new JidokaFatalException("Error launching the robot queue");
			}

			server.info(numberOfExecutions - 1 + " more robots launched with the queue " + this.queue.queueId()
					+ " assigned");

		} catch (IOException e) {
			throw new JidokaFatalException("Error launching the robot queue");
		}

	}

	/**
	 * Copy the executions parameters. It avoids to copy the input file parameter
	 * and the number of executions parameter.
	 * 
	 * @param options {@link LaunchOptions} options where to set the parameters
	 */
	private void setParameters(LaunchOptions options) {

		List<String> toAvoidParameters = new ArrayList<>();
		toAvoidParameters.add(PARAM_INPUT_FILE);
		toAvoidParameters.add(PARAM_NUMBER_OF_EXECUTIONS);

		List<ExecutionParameter> parameters = new ArrayList<>();

		for (Map.Entry<String, String> o : server.getParameters().entrySet()) {

			if (!toAvoidParameters.contains(o.getKey())) {
				ExecutionParameter e = new ExecutionParameter();

				e.setName(o.getKey());
				e.setValue(o.getValue());

				parameters.add(e);
			}

		}

		options.setParameters(parameters);
	}
}
