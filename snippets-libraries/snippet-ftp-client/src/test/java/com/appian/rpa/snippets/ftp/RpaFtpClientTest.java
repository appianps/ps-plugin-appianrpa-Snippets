package com.appian.rpa.snippets.ftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;

@Ignore
public class RpaFtpClientTest {

	static RpaFtpClient ftpClient;
	private static IJidokaServer<?> server;

//	@Ignore
	@BeforeClass
	public static void beforeClass() throws Exception {

		MyJunitJidokaServer jUnitJidokaServer = new MyJunitJidokaServer(System.out, null);
		JidokaFactory.setServer(jUnitJidokaServer);

		server = jUnitJidokaServer;

		ftpClient = RpaFtpClient.getInstance();

		ftpClient.init("ftp.dlptest.com", "dlpuser@dlptest.com", "eUj8GeW55SvYaswqUyDSm5v6N");
	}

	@Ignore
	@Test
	public void testFtpUpload() {

		ftpClient.mkDirFtp("/test-jid/");
		ftpClient.setWorkingDirectory("/test-jid/");

		List<String> arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			System.out.println(fileString);
		}

		List<File> listFiles = new ArrayList<>();

		listFiles.add(new File("C:/VM/RPA_TEST_1"));
		listFiles.add(new File("C:/VM/RPA_TEST_2"));
		listFiles.add(new File("C:/VM/RPA_TEST_3"));

		ftpClient.setWorkingDirectory("/test-jid/");
		ftpClient.uploadFiles(listFiles);

		arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}
	}

	@Ignore
	@Test
	public void testFtpDowload() {
		ftpClient.mkDirFtp("/test-jid/");
		ftpClient.setWorkingDirectory("/test-jid/");

		List<String> arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}

		List<File> listFiles = new ArrayList<>();

		listFiles.add(new File("C:/VM/RPA_TEST_1"));
		listFiles.add(new File("C:/VM/RPA_TEST_2"));
		listFiles.add(new File("C:/VM/RPA_TEST_3"));

		ftpClient.uploadFiles(listFiles);

		arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}

		ftpClient.downloadFile("RPA_TEST_.*");
	}

	@Ignore
	@Test
	public void testFtpDelete() {

		ftpClient.mkDirFtp("/test_jid/");

		List<String> arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}

		ftpClient.setWorkingDirectory("/test_jid/");

		arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}

		List<File> listFiles = new ArrayList<>();

		listFiles.add(new File("C:/VM/RPA_TEST_1"));
		listFiles.add(new File("C:/VM/RPA_TEST_2"));
		listFiles.add(new File("C:/VM/RPA_TEST_3"));

		ftpClient.uploadFiles(listFiles);

		arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}

//		for (File file : listFiles) {
//			ftpClient.deleteFile(file.getName());
//		}

		ftpClient.setWorkingDirectory("");

		boolean removed = ftpClient.rmDirFtp("/test_jid/", true);
		System.out.println("removed:" + removed);
		arrayFtpFiles = ftpClient.getAllFilesName();

		for (String fileString : arrayFtpFiles) {

			server.debug(fileString);
		}

	}

	@Test
	public void testDirContent() {

		ftpClient.rmDirFtp("/test-jid/", true);
	}

}
