package com.appian.rpa.snippets.ftp;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

@Nano
public class RpaFtpClientLibraries implements INano {

	/** Server module */
	private IJidokaServer<?> server;

	/** FTPClient */
	private RpaFtpClient rpaFtpClient = null;

	/**
	 * Connect to FTP
	 * 
	 * @param host
	 * @param username
	 * @param password
	 * @param workingDirectory
	 * @param downloadDirectory
	 */
	@JidokaMethod(name = "Configure FTP", description = "Configure the FTP connection parameters")
	public void configureFTP(@JidokaParameter(defaultValue = "", name = "FTP Host * ") String host,
			@JidokaParameter(defaultValue = "", name = "FTP Username * ") String userName,
			@JidokaParameter(defaultValue = "", name = "FTP credentials * ") String application,
			@JidokaParameter(defaultValue = "", name = "FTP Starting directory * ") String workingDirectory,
			@JidokaParameter(defaultValue = "", name = "Download directory ") String downloadDirectory) {

		this.server = JidokaFactory.getServer();

		if (downloadDirectory.isEmpty()) {
			downloadDirectory = System.getProperty("user.dir");
		}

		rpaFtpClient = RpaFtpClient.getInstance();

		String password = getFtpPassword(application, userName);

		server.debug("Parameter to connect : Host-> " + host + " User: " + userName + " Pass: " + password
				+ " FTP dir: " + " DownloadDir: " + downloadDirectory);

		rpaFtpClient.init(host, userName, password, workingDirectory, downloadDirectory);

		// Print in console all the content in the selected directory to test
		// connection.
		List<String> arrayFtpFiles = rpaFtpClient.getAllFilesName();

		arrayFtpFiles.stream().forEach(f -> server.debug(f));

		server.debug("FTP library initialized");

	}

	/**
	 * Upload the {@link File} to the FTP working folder.
	 * 
	 * @param fileToUpload
	 * @return
	 */
	@JidokaMethod(name = "Upload file", description = "Upload a file to the FTP working folder")
	public void uploadFile(
			@JidokaParameter(defaultValue = "", name = "Path of the file to upload *") String fileToUpload,
			@JidokaParameter(defaultValue = "", name = "FTP directory to upload the file. If empty is FTP root directory. (format: '/dirInRoot/subDir/.../uploadDir/'") String ftpDirectory) {

		// First we are going to create a new directory in the FTP
		rpaFtpClient.mkDirFtp(ftpDirectory);

		// Now we select that directory to work with it
		rpaFtpClient.setWorkingDirectory(ftpDirectory);

		boolean uploadComplete = rpaFtpClient.uploadFile(new File(fileToUpload));

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
	 * Download files in the current FTP working directory to a specific folder in
	 * the resource.
	 */
	@JidokaMethod(name = "Download a file", description = "Download a specific file")
	public String downloadFile(
			@JidokaParameter(defaultValue = "", name = "Path of the file to download *") String fileToDownload,
			@JidokaParameter(defaultValue = "", name = "Download directory. If empty is the last defined download directory.") String downloadDirectory) {

		if (StringUtils.isNotBlank(downloadDirectory)) {

			rpaFtpClient.setDownloadDirectory(downloadDirectory);
		}

		String fileName = "";

		try {
			Pattern regex = Pattern.compile("([^\\\\/:*?\"<>|\r\n]+$)");
			Matcher regexMatcher = regex.matcher(fileToDownload);
			if (regexMatcher.find()) {
				fileName = regexMatcher.group(1);
			}
		} catch (PatternSyntaxException ex) {
			throw new JidokaItemException("Error in the file path to download");
		}

		String previousWorkingDir = rpaFtpClient.getWorkingDirectory();

		rpaFtpClient.setWorkingDirectory(fileToDownload.replace(fileName, ""));

		File localFile = rpaFtpClient.downloadFile(fileName);

		rpaFtpClient.setWorkingDirectory(previousWorkingDir);

		return localFile.getPath();

	}

	@JidokaMethod(name = "Delete File", description = "Delete a specific file in the FTP")
	public boolean deleteFile(@JidokaParameter(defaultValue = "", name = "File to delete") String filePath) {

		String fileName = "";
		try {
			Pattern regex = Pattern.compile("([^\\\\/:*?\"<>|\r\n]+$)");
			Matcher regexMatcher = regex.matcher(filePath);
			if (regexMatcher.find()) {
				fileName = regexMatcher.group(1);
			}
		} catch (PatternSyntaxException ex) {
			throw new JidokaItemException("Error in the file path to download");
		}

		String previousWorkingDir = rpaFtpClient.getWorkingDirectory();

		rpaFtpClient.setWorkingDirectory(filePath.replace(fileName, ""));

		boolean deleteResult = rpaFtpClient.deleteFile(fileName);

		rpaFtpClient.setWorkingDirectory(previousWorkingDir);

		return deleteResult;
	}

	@JidokaMethod(name = "Delete Files", description = "Delete a list of files in the FTP")
	public boolean deleteFiles(
			@JidokaParameter(defaultValue = "", name = "List of files to delete") List<String> filePathList) {

		boolean deleteResult = (filePathList != null) && !filePathList.isEmpty();

		for (String path : filePathList) {

			if (!deleteFile(path)) {
				server.debug("There is an error deleting the file: " + path);
				deleteResult = false;
			}
		}

		return deleteResult;
	}

	@JidokaMethod(name = "List Files", description = "List the files that contains one FTP directory")
	public List<String> listFilesInFolder(
			@JidokaParameter(defaultValue = "", name = "Folder to search. If empty, last configured folder is used") String folder) {

		String previousWorkingDir = rpaFtpClient.getWorkingDirectory();

		rpaFtpClient.setWorkingDirectory(folder);

		List<String> arrayFtpFiles = rpaFtpClient.getAllFilesName();

		rpaFtpClient.setWorkingDirectory(previousWorkingDir);

		return arrayFtpFiles;

	}

	private String getFtpPassword(String application, String userName) {

		List<IUsernamePassword> ftpUsers = server.getCredentials(application);

		for (IUsernamePassword userCredential : ftpUsers) {

			if (userName.contentEquals(userCredential.getUsername())) {

				return userCredential.getPassword();
			}
		}

		throw new JidokaFatalException("There is no credential defined in the console for the application: "
				+ application + " and the user: " + userName);
	}

}
