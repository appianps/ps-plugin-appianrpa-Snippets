package com.appian.rpa.snippets.examples.ftp;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.appian.rpa.snippets.credentials.CredentialsUtils;
import com.appian.rpa.snippets.ftp.RpaFtpClient;
import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

/**
*
*/

@Robot
public class FTPRobot implements IRobot {

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** FTP client instance */
	private RpaFtpClient rpaFtpClient;

	/** FTP client instance */
	private CredentialsUtils credentialsUtils;

	/** FTP application name */
	private static final String APPLICATION_NAME = "FTP_TEST";

	/** FTP Host */
	private String PARAM_HOST = "HOST";

	/** Credentials */
	private IUsernamePassword ftpCredentials;

	/** Support files */
	private List<File> supportFiles;

	private final String FTP_TEST_DIR = "/test_jid/";

	/**
	 * Overrides the startup method to initialize some variables involved in our
	 * process.
	 */
	@Override
	public boolean startUp() throws Exception {
		// Initialize server module
		server = JidokaFactory.getServer();
		credentialsUtils = CredentialsUtils.getInstance();
		rpaFtpClient = RpaFtpClient.getInstance();

		return true;
	}

	/**
	 * The start method initializes modules and global variables
	 */
	public void start() {
		// All files to work with
		supportFiles = server.getSupportFiles().stream().map(Path::toFile).collect(Collectors.toList());

		ftpCredentials = credentialsUtils.getCredentials(APPLICATION_NAME, false, ECredentialSearch.FIRST_LISTED, 30);
		// Initializing the FTP client. The default directory to connect is root but you
		// can choose a different one
		// that exists in the FTP
		rpaFtpClient.init(server.getParameters().get(PARAM_HOST), ftpCredentials.getUsername(),
				ftpCredentials.getPassword());
	}

	/**
	 * This method retrieves all files in the FTP root directory.
	 */
	public void getAllFilesInRoot() {

		// Here we obtain all the files name in the active directory (root)
		// If you want to scrap a different directory you need to change the active one
		// before
		// using rpaFtpClient.setWorkingDirectory("new/directory");
		List<String> arrayFtpFiles = rpaFtpClient.getAllFilesName();

		arrayFtpFiles.stream().forEach(f -> server.debug(f));
	}

	/**
	 * This method upload all files in the console support folders linked to the
	 * robot.
	 */
	public void uploadSupportFiles() {

		// First we are going to create a new directory in the FTP
		if (!rpaFtpClient.mkDirFtp(FTP_TEST_DIR)) {
			throw new JidokaFatalException("Error creating a new directory in the FTP");
		}
		// Now we select that directory to work with it
		rpaFtpClient.setWorkingDirectory(FTP_TEST_DIR);

		boolean uploadComplete = rpaFtpClient.uploadFiles(supportFiles);

		server.debug("Upload complete: " + uploadComplete);

		if (!uploadComplete) {
			throw new JidokaFatalException("There is a problem uploading files to the FTP");
		}

		// We are going to check the upload. The method 'getAllFilesName' search in the
		// current working directory.
		List<String> arrayFtpFiles = rpaFtpClient.getAllFilesName();

		arrayFtpFiles.stream().forEach(f -> server.debug(f));
	}

	/**
	 * Download all files in the current FTP working directory to a specific folder
	 * in the resource.
	 */
	public void downloadFiles() {

		// We are setting the directory to download files, by default is the robot
		// directory in the resource
		rpaFtpClient.setDownloadDirectory(System.getProperty("user.dir") + File.separator + "ftpDownloadTest");

		List<String> arrayFtpFiles = rpaFtpClient.getAllFilesName();

		List<File> localFiles = rpaFtpClient.downloadFiles(arrayFtpFiles);

		localFiles.stream().forEach(f -> server.debug(f));

	}

	/**
	 * Delete the files previously uploaded.
	 */
	public void deleteFiles() {

		List<String> arrayFtpFiles = rpaFtpClient.getAllFilesName();

		if (arrayFtpFiles.isEmpty()) {
			throw new JidokaFatalException("There is no files to delete in the FTP directory.");
		}

		// We can delete only a selected file
		boolean fileDelete = rpaFtpClient.deleteFile(arrayFtpFiles.get(0));

		server.debug("Single delete result: " + arrayFtpFiles.get(0) + " delete: " + fileDelete);

		// Now we are going to delete the complete directory. We set the boolean
		// parameter to true to force the deletion even if you have files in it.
		rpaFtpClient.rmDirFtp(FTP_TEST_DIR, true);

	}

	/**
	 * Any further actions to close the robot process can be performed here.
	 */
	public void end() {

		// continue the process, here the robot ends its execution

	}

	/**
	 * Overrides the cleanUp method.
	 * 
	 * We ensure that all the applications involved have been successfully closed,
	 * even if an exception was thrown during the process. This is a common practice
	 * to avoid undesired opened tasks in the following executions.
	 */

	@Override
	public String[] cleanUp() throws Exception {

		credentialsUtils.releaseAllCredentials(APPLICATION_NAME);

		return IRobot.super.cleanUp();
	}

	/**
	 * Manage exception.
	 *
	 * @param action    the action
	 * @param exception the exception
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		// We get the message of the exception
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// We send a screenshot to the log so the user can see the screen in the moment
		// of the error
		// This is a very useful thing to do
		server.sendScreen("Screenshot at the moment of the error");

		// If we have a FatalException we should abort the execution.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		// If the error is processing one items we must mark it as a warning and go on
		// with the next item
		if (ExceptionUtils.indexOfThrowable(exception, JidokaItemException.class) >= 0) {

			server.setCurrentItemResultToWarn(errorMessage);
			reset();
			return "end";
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, we don't know
		// what has happened

		return IRobot.super.manageException(action, exception);
	}

	/**
	 * This method reset the robot state to a stable state after a
	 * JidokaItemException is thrown
	 */
	private void reset() {
		// In this case the method is empty because an exception ends the execution.
	}

}
