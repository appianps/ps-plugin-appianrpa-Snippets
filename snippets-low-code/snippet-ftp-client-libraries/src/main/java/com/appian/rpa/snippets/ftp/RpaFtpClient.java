package com.appian.rpa.snippets.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;

/**
 * This library offers a set of functionalities to interact with an FTP server,
 * such as connection, file uploading and file downloading, among others.
 */
@Nano
public class RpaFtpClient implements INano {

	/** FTP active directory, must exist */
	private String workingDirectory;

	/** Folder to store files obtained from FTP */
	private String downloadDirectory;

	/** Server module */
	private IJidokaServer<?> server;

	/** FTPClient */
	private FTPClient ftpClient = null;

	/**
	 * Connect to FTP
	 * 
	 * @param host
	 * @param username
	 * @param password
	 * @param workingDirectory
	 * @param downloadDirectory
	 */
	@JidokaMethod(name = "Connect to FTP", description = "Connect to FTP")
	public void connect(@JidokaParameter(defaultValue = "", name = "FTP Host * ") String host,
			@JidokaParameter(defaultValue = "", name = "FTP Username * ") String username,
			@JidokaParameter(defaultValue = "", name = "FTP Password * ") String password,
			@JidokaParameter(defaultValue = "", name = "Working directory * ") String workingDirectory,
			@JidokaParameter(defaultValue = "", name = "Download directory ") String downloadDirectory) {

		ftpClient = null;

		init(host, username, password, workingDirectory, downloadDirectory);

		JidokaItemException lastException = null;

		for (int i = 1; i < 5; i++) {

			server.debug(String.format("Connecting to FTP [%s]. Attempt %d", host, i));
			ftpClient = new FTPClient();
			ftpClient.setConnectTimeout(10000);
			ftpClient.setDefaultTimeout(10000);

			try {

				ftpClient.connect(host, 21);
				ftpClient.setSoTimeout(10000);

				if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					ftpClient.disconnect();
					server.warn("FTP server refused connection.");
					continue;
				}

			} catch (IOException e) {
				lastException = new JidokaItemException("There is an error connecting to FTP");
				continue;
			}

			try {

				ftpClient.enterLocalPassiveMode();
				if (!ftpClient.login(username, password)) {
					server.debug("Login failed");
					continue;
				}

				server.debug("Status -> " + ftpClient.getStatus() + " -> ReplyCode " + ftpClient.getReplyCode());

			} catch (Exception e) {
				lastException = new JidokaItemException("Login error");
				continue;
			}

			if (StringUtils.isNotBlank(workingDirectory)) {

				try {

					if (!ftpClient.changeWorkingDirectory(workingDirectory)) {
						server.debug("Working directory not changed.");
						continue;
					}

				} catch (IOException e) {
					lastException = new JidokaItemException(
							"There is an error changing to the ftp working directory: " + workingDirectory);
					continue;
				}
			}

			return;
		}

		if (ftpClient != null) {
			disconnect();
		}

		if (lastException == null) {
			throw new JidokaItemException("FTP connection failed: " + host);
		}

		throw lastException;
	}

	/**
	 * Disconnection from FTP
	 */
	@JidokaMethod(name = "Disconnect", description = "Disconnect FTP")
	public void disconnect() {

		if (ftpClient == null) {
			return;
		}

		if (!ftpClient.isConnected()) {
			return;
		}

		try {

			ftpClient.logout();

		} catch (IOException e) {
			server.warn("FTP logout error", e);
		}

		try {

			ftpClient.disconnect();

		} catch (IOException e) {
			server.warn("Error disconnecting from FTP", e);
		}
	}

	/**
	 * Delete the {@link File} in the current active directory with the given name.
	 * Return <code>true</code> if the files are removed, <code>false</code> for a
	 * different case.
	 * 
	 * @param fileName
	 * @return
	 */
	@JidokaMethod(name = "Delete file", description = "Delete file by name")
	public void deleteFile(@JidokaParameter(defaultValue = "", name = "File name * ") String fileName) {

		List<String> filesName = new ArrayList<>();

		filesName.add(fileName);

		Boolean deleted = deleteFiles(filesName);
		
		if(!deleted) {
			throw new JidokaItemException("The file could not be deleted");
		}
	}

	/**
	 * Download the first {@link File} with the given regExp.
	 * 
	 * @param fileNameRegExp
	 * @return
	 */
	@JidokaMethod(name = "Download file", description = "Download file the first file with the given regExp")
	public void downloadFile(
			@JidokaParameter(defaultValue = "", name = "RegExp of the file name * ") String fileNameRegExp,
			@JidokaParameter(defaultValue = "", name = "File Destination Variable Name * ") String rpVariableName) {

		File donwloadedFile = downloadFile(f -> Pattern.matches(fileNameRegExp, f.getName()));
		JidokaFactory.getServer().getWorkflowVariables().get(rpVariableName).setValue(donwloadedFile);
	}

	/**
	 * Upload the {@link File} to the FTP working folder.
	 * 
	 * @param fileToUpload
	 * @return
	 */
	@JidokaMethod(name = "Upload file", description = "Upload a file to the FTP working folder")
	public void uploadFile(@JidokaParameter(defaultValue = "", name = "Name of the instruction that contains the file * ") String fileToUpload) {

		String parameter = JidokaFactory.getServer().getParameters().get(fileToUpload);

		File fileUpload = Paths.get(JidokaFactory.getServer().getCurrentDir(), parameter).toFile();

		ArrayList<File> toUpload = new ArrayList<>();

		toUpload.add(fileUpload);

		Boolean uploaded = uploadFiles(toUpload);
		
		if(!uploaded) {
			throw new JidokaItemException("The file could not be uploaded");
		}
	}

	/**
	 * Returns a {@link List} with the name of all the files in the FTP.
	 * 
	 * @return
	 */
	@JidokaMethod(name = "Get all files", description = "Returns a list with the name of all the files in the FTP")
	public List<String> getAllFilesName() {

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}

		server.debug("Obtaining files in the directory: " + workingDirectory);

		try {

			FTPFile[] listFiles = ftpClient.listFiles();

			if (listFiles == null) {
				server.debug("The directory is empty.");
				return new ArrayList<>();
			}

			return Arrays.asList(listFiles).stream()
					.filter(f -> f != null && (!f.getName().equals(".") && !f.getName().equals("..")))
					.map(f -> f.getName()).collect(Collectors.toList());

		} catch (Exception e) {
			throw new JidokaItemException("There is an error retriving the file list from the ftp.");
		}
	}

	/**
	 * Returns a {@link List} with the name of all the files in the FTP.
	 * 
	 * @return
	 */
	@JidokaMethod(name = "Get all directories", description = "Returns a list with the name of all the directories in the FTP")
	public List<String> getAllDirectories() {

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}

		server.debug("Obtaining directories in the current working directory: " + workingDirectory);
		try {

			FTPFile[] listDir = ftpClient.listDirectories();

			if (listDir == null) {
				server.debug("The directory is empty.");
				return new ArrayList<>();
			}

			return Arrays.asList(listDir).stream()
					.filter(f -> f != null && (!f.getName().equals(".") && !f.getName().equals("..")))
					.map(f -> f.getName()).collect(Collectors.toList());

		} catch (Exception e) {
			throw new JidokaItemException("There is an error retriving the directories list from the ftp.");
		}
	}

	/**
	 * Create a new directory in the FTP following the format
	 * "/dir_in_root/sub_dir/". If the directory is created return <code>true</code>
	 * any other case return <code>false</code>
	 * 
	 * @param dir
	 * @return
	 */
	@JidokaMethod(name = "Make a directory", description = "Create a new directory in the FTP")
	public void mkDirFtp(@JidokaParameter(defaultValue = "", name = "Directory name * ") String dir) {

		String previousWorkingDir = workingDirectory;

		setWorkingDirectory("");

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}

		try {

			if (!ftpClient.makeDirectory(dir)) {
				throw new JidokaItemException("The directory could not be created");
			}

		} catch (Exception e) {
			throw new JidokaItemException("The directory could not be created");
		} finally {
			setWorkingDirectory(previousWorkingDir);
		}

		server.debug("Directory " + dir + " created in the FTP.");

	}

	/**
	 * Remove a empty directory in the FTP following the format
	 * "/dir_in_root/sub_dir/...". If the directory is removed return
	 * <code>true</code> any other case return <code>false</code>
	 * 
	 * @param dir
	 * @return
	 */
	@JidokaMethod(name = "Remove empty directory", description = "Remove a empty directory")
	public void rmDirFtp(@JidokaParameter(defaultValue = "", name = "Directory name * ") String dir) {
		rmDirFtp(dir, false);
	}

	/**
	 * Remove a directory in the FTP following the format
	 * "/dir_in_root/sub_dir/...". If the directory is removed return
	 * <code>true</code> any other case return <code>false</code>
	 * 
	 * @param dir
	 * @param removeContent if is <code>true</code> remove all content in the
	 *                      directory too, else the directory must be empty to be
	 *                      removed.
	 * @return
	 */
	@JidokaMethod(name = "Remove directory", description = "Remove a directory")
	public void rmDirFtp(@JidokaParameter(defaultValue = "", name = "Directory name * ") String dir,
			@JidokaParameter(defaultValue = "", name = "Remove all content in the directory? * ") boolean removeContent) {

		if (removeContent) {
			removeDirContent(dir);
		}

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}

		try {

			if (!ftpClient.removeDirectory(dir)) {
				server.warn("The directory " + dir + " can't be deleted.");
				throw new JidokaItemException("The directory could not be deleted");
			}

		} catch (Exception e) {
			server.warn("Error deleting directory " + dir);
			throw new JidokaItemException("The directory could not be deleted");
		}
		server.debug("Folder " + dir + " removed from the FTP.");

	}

	/**
	 * Remove all the content in the current working directory, this directory is
	 * not removed.
	 * 
	 * @return
	 */
	@JidokaMethod(name = "Remove directory content", description = "Remove all the content in the current working directory, this directory is not removed")
	public void removeDirContent() {

		removeDirContent(workingDirectory);
	}

	/**
	 * Remove all the content in the given FTP directory. This method removed all
	 * files and directories in a recursive way, but don't remove the given
	 * directory itself.
	 * 
	 * @param dir
	 * @return
	 */
	@JidokaMethod(name = "Remove directory content", description = "Remove all the content in the current working directory in a recursive way")
	public void removeDirContent(@JidokaParameter(defaultValue = "", name = "Directory name * ") String dir) {

		String previousWorkingDir = workingDirectory;

		setWorkingDirectory(dir);

		try {
			List<String> arrayFtpFiles = getAllFilesName();

			List<String> arrayDirectories = getAllDirectories();

			for (String directory : arrayDirectories) {

				rmDirFtp(workingDirectory + directory, true);

			}
			arrayFtpFiles.removeAll(arrayDirectories);

			deleteFiles(arrayFtpFiles);

		} finally {

			setWorkingDirectory(previousWorkingDir);
		}

	}

	/**
	 * Init the FTP manager, including a starting FTP working and download directory
	 * 
	 * @param host
	 * @param username
	 * @param password
	 * @param workingDirectory
	 * @param downloadDirectory
	 */
	private void init(String host, String username, String password, String workingDirectory,
			String downloadDirectory) {

		setWorkingDirectory(workingDirectory);
		if (downloadDirectory.isEmpty()) {
			setDownloadDirectory(System.getProperty("user.dir"));
		} else {
			setDownloadDirectory(downloadDirectory);
		}
		this.server = JidokaFactory.getServer();
	}

	/**
	 * Upload a file to the FTP
	 * 
	 * @param ftpClient
	 * @param localFile
	 * @return
	 */
	private boolean uploadFile(FTPClient ftpClient, File localFile) {

		for (int i = 1; i < 5; i++) {

			try (FileInputStream stream = new FileInputStream(localFile)) {

				if (!ftpClient.storeFile(localFile.getName(), stream)) {
					continue;
				}

				return true;

			} catch (Exception e) {
				server.warn("An error occurred while uploading the file to the FTP. Attempt: " + i + "//4");
			}
		}

		server.warn("An error occurred trying to upload the file " + localFile.getName() + " to the FTP");

		return false;
	}

	/**
	 * Download the first {@link File} that match the given {@link Predicate}.
	 * 
	 * @param predicate
	 * @return
	 */
	private File downloadFile(Predicate<FTPFile> predicate) {

		List<File> files = downloadFiles(predicate);

		if (files.isEmpty()) {
			return null;
		}

		return files.get(0);
	}

	/**
	 * Download all {@link File} matching the given predicate.
	 * 
	 * @param predicate
	 * @return
	 */
	private List<File> downloadFiles(Predicate<FTPFile> predicate) {

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}
		List<File> results = new ArrayList<>();

		try {

			FTPFile[] listFiles = ftpClient.listFiles();

			if (listFiles == null) {
				return results;
			}

			for (FTPFile ftpFile : listFiles) {

				if (!ftpFile.isFile()) {
					continue;
				}

				if (!predicate.test(ftpFile)) {
					continue;
				}

				File file = downloadFile(ftpClient, ftpFile);
				results.add(file);
			}

		} catch (JidokaItemException e) {
			throw e;
		} catch (Exception e) {
			throw new JidokaItemException("There is an error downloading the file.");
		}

		return results;
	}

	private File downloadFile(FTPClient ftpClient, FTPFile ftpFile) {

		File file = new File(downloadDirectory + ftpFile.getName());
		JidokaItemException lastException = null;

		for (int i = 1; i < 5; i++) {

			try (FileOutputStream stream = new FileOutputStream(file)) {

				if (!ftpClient.retrieveFile(ftpFile.getName(), stream)) {
					continue;
				}

				return file;

			} catch (Exception e) {
				lastException = new JidokaItemException("There is an error downloading the file from the FTP");
			}
		}

		if (lastException == null) {
			throw new JidokaItemException("There is an error downloading the file from the FTP");
		}

		throw lastException;
	}

	/**
	 * Upload a {@link List} of {@link File} to the FTP working folder.
	 * 
	 * @param filesToUpload
	 * @return
	 */
	@JidokaMethod(name = "Upload files", description = "Upload a list of files to the FTP working folder")
	private boolean uploadFiles(List<File> filesToUpload) {

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}

		try {

			for (File file : filesToUpload) {

				if (!uploadFile(ftpClient, file)) {
					return false;
				}
			}

		} catch (JidokaFatalException e) {
			throw e;
		}

		return true;
	}

	/**
	 * Delete the {@link File} {@link List} in the current directory with the given
	 * exacts names. Return <code>true</code> if the files are removed,
	 * <code>false</code> for a different case.
	 * 
	 * @param fileNameList
	 * @return
	 */
	private boolean deleteFiles(List<String> fileNameList) {

		if (ftpClient == null) {
			throw new JidokaFatalException("Connect to the FPT before performing this action");
		}

		try {

			for (String fileName : fileNameList) {
				server.debug("Deleting file: " + workingDirectory + fileName);
				if (!ftpClient.deleteFile(workingDirectory + fileName)) {
					return false;
				}
			}

		} catch (IOException e) {
			throw new JidokaItemException("There is an error deleting the file");
		}

		return true;
	}

	/**
	 * @param workingDirectory the workingDirectory to set
	 */
	private void setWorkingDirectory(String workingDirectory) {

		if (!workingDirectory.endsWith("/")) {
			workingDirectory += "/";
		}
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Set the download directory, if the directory to set doesn't exist it will be
	 * created
	 * 
	 * @param downloadDirectory the absolute path of the download directory to set
	 */
	private void setDownloadDirectory(String downloadDirectory) {
		if (!downloadDirectory.endsWith("/")) {
			downloadDirectory += "/";
		}

		if (!Files.exists(Paths.get(downloadDirectory))) {

			File dir = new File(downloadDirectory);

			if (!dir.mkdir()) {
				throw new JidokaFatalException("Cannot create directory for downloads: " + dir.getPath());
			}
		}
		this.downloadDirectory = downloadDirectory;
	}
}
