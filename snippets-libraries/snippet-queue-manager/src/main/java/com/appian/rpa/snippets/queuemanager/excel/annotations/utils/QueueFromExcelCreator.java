package com.appian.rpa.snippets.queuemanager.excel.annotations.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;

import com.appian.rpa.snippets.queuemanager.excel.mapper.AbstractItemFieldsMapper;
import com.appian.rpa.snippets.queuemanager.utils.annotations.AnnotationUtils;
import com.appian.rpa.snippets.queuemanager.utils.conversion.ConversionUtils;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.AssignQueueParameters;
import com.novayre.jidoka.client.api.queue.CreateItemParameters;
import com.novayre.jidoka.client.api.queue.CreateQueueParameters;
import com.novayre.jidoka.client.api.queue.EPriority;
import com.novayre.jidoka.client.api.queue.IQueue;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.novayre.jidoka.data.provider.api.IExcel;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider.Provider;
import com.novayre.jidoka.data.provider.api.IJidokaExcelDataProvider;

import jodd.io.FileUtil;

/**
 * Class for creating a queue from an Excel file
 * 
 * @param <T>
 */
public class QueueFromExcelCreator<T> {

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** IRobot instance */
	private IRobot robot;

	/** IQueueManager instance */
	private IQueueManager queueManager;

	/** IJidokaExcelDataProvider instance */
	private IJidokaExcelDataProvider<T> dp;

	/** Excel input file full path */
	private String excelFileFullPath;

	/** Mapper for mapping the Excel file */
	private AbstractItemFieldsMapper<T> mapper;

	/** Current queue */
	private IQueue currentQueue;

	/**
	 * Constructor to create a queue from an Excel
	 * 
	 * @param robot             IRobot instance
	 * @param excelFileFullPath The Excel file full path
	 * @param mapper            The mapper for mapping the Excel file
	 * 
	 */
	public QueueFromExcelCreator(IRobot robot, String excelFileFullPath, AbstractItemFieldsMapper<T> mapper) {

		this.server = JidokaFactory.getServer();
		this.excelFileFullPath = excelFileFullPath;
		this.mapper = mapper;
		this.queueManager = server.getQueueManager();
		this.robot = robot;

		try {

			File fileInput = Paths.get(excelFileFullPath).toFile();

			String createdQueue = createQueue(fileInput, null);

			currentQueue = getQueueFromId(createdQueue);

			if (currentQueue == null) {
				throw new JidokaFatalException("The queue could not be created");
			}

		} catch (Exception e) {
			throw new JidokaFatalException("The queue could not be created", e);
		}
	}

	/**
	 * Gets the current queue
	 * 
	 * @return the current queue
	 */
	public IQueue getQueue() {

		return currentQueue;
	}

	/**
	 * Creates the queue based on the file. The {@code propertiesInResults} can be
	 * {@code null}.
	 * 
	 * @return The created queue id
	 */
	private String createQueue(File fileInput, List<String> propertiesInResults) {

		try {
			String fileName = fileInput.getName();

			if (propertiesInResults == null) {
				propertiesInResults = new ArrayList<>();
			}

			server.info("Creating queue from file: " + fileName);

			CreateQueueParameters queueParams = new CreateQueueParameters();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd - HH.mm");
			queueParams.setDescription("Queue created from file:" + fileName + " on " + format.format(new Date()));
			queueParams.setFileName(fileName);

			String queueName = server.getExecution(0).getRobotName() + " - "
					+ getDateFormated(new Date(), "yyyy-MM-dd - HH.mm");

			queueParams.setName(queueName);
			queueParams.setPriority(EPriority.HIGH);
			queueParams.setAttemptsByDefault(mapper.getAttemptsByDefault());
			queueParams.setFunctionalDataVisibleKeys(propertiesInResults);

			queueParams.setFileContent(FileUtil.readBytes(fileInput));

			String createdQueueId = queueManager.createQueue(queueParams);

			server.info("Queue created: " + createdQueueId);

			addItemsToQueue(createdQueueId);

			return createdQueueId;

		} catch (Exception e) {
			throw new JidokaFatalException("Error creating queue: " + e.getMessage(), e);
		}
	}

	/**
	 * Returns a {@linkplain Date} formated.
	 * 
	 * @param date       The date to format
	 * @param dateFormat The format to apply
	 * @return The formatted date
	 */
	public static String getDateFormated(Date date, String dateFormat) {

		if (date == null) {
			return "";
		}

		return new SimpleDateFormat(dateFormat).format(date);
	}

	/**
	 * Gets the queue from the given id {@code queueId}.
	 *
	 * @param queueId The queue id
	 * @return The queue with id equals to {@code queueId}
	 * 
	 */
	private IQueue getQueueFromId(String queueId) {

		try {
			AssignQueueParameters qqp = new AssignQueueParameters();
			qqp.queueId(queueId);

			return queueManager.assignQueue(qqp);
		} catch (IOException | JidokaQueueException e) {
			throw new JidokaFatalException("Error getting the queue from the given id", e);
		}
	}

	/**
	 * Adds the items to the queue corresponding to the given id
	 * {@code createdQueueId}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addItemsToQueue(String createdQueueId) {

		try {
			dp = IJidokaDataProvider.getInstance(robot, Provider.EXCEL);

			dp.init(excelFileFullPath, mapper.getSheetName(), mapper.getFirstRow(), mapper);

			while (dp.nextRow()) {

				if (isHiddenRow()) {

					// It skips the hidden row
					continue;
				}

				Object excelRow = dp.getCurrentItem();

				CreateItemParameters cip = new CreateItemParameters();

				String keyValue = AnnotationUtils.getKeyFieldValue(excelRow);

				cip.setKey(keyValue);
				cip.setPriority(EPriority.NORMAL);
				cip.setQueueId(createdQueueId);
				cip.setReference(String.valueOf(dp.getCurrentItemNumber()));

				Map mapa = ConversionUtils.object2Map(excelRow);

				cip.setFunctionalData(mapa);

				queueManager.createItem(cip);

				server.debug(String.format("Added item to queue %s with id %s", cip.getQueueId(), cip.getKey()));
			}

			mapper.resetHeaders();

			closeDataProvider();
		} catch (Exception e) {
			throw new JidokaFatalException("Error adding the items to the queue", e);
		}
	}

	/**
	 * Checks if a row is a hidden row
	 * 
	 * @return If the current data provider row is a hidden row
	 */
	private boolean isHiddenRow() {

		IExcel excel = dp.getExcel();

		int rowNumber = dp.getCurrentItemNumber() + mapper.getFirstRow() - 1;

		List<Cell> rowCellsList = excel.getEntireRow(rowNumber);

		Cell firstCell = rowCellsList.get(0);

		return firstCell.getRow().getZeroHeight();

	}

	/**
	 * Closes the data provider
	 */
	private void closeDataProvider() {

		if (dp != null) {
			try {
				dp.close();
			} catch (Exception e) {
				server.warn(String.format("The dataprovider could not be closed: %s", e.getMessage()), e);
			}
		}
	}

}
