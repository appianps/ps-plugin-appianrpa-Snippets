package com.appian.snippets.examples;

import org.apache.poi.ss.formula.functions.T;

import com.appian.rpa.snippets.commons.queues.QueueItemsManager;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.annotations.Robot;

/**
 * Robot that gets a queue item and counts the words of the item content
 * functional data. Then it updates the queue item functional data.
 *
 */
@Robot
public class CountWordsRobot implements IRobot {

	/**
	 * Pending files folder directory
	 */
	private static final String FOLDER_DIR = "inputFiles";

	/** QueueItemsManager instance */
	private QueueItemsManager<FileModel> queueItemsManager;

	@Override
	public boolean startUp() throws Exception {

		// QueueItemsManager init
		queueItemsManager = new QueueItemsManager<>(this, new FileMapper());

		return IRobot.super.startUp();
	}

	/**
	 * Inits the robot variables
	 */
	public void init() {

	}

	/**
	 * Check if there are files without being processed
	 * 
	 * @return A list with the pending files
	 */
	public void createQueueIfNotExists() {

	}

	public String newItemsToAdd() {

	}

	public void addNewItems(T object) {

	}
}