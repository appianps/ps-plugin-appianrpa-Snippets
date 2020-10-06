package com.appian.rpa.snippets.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.appian.rpa.snippets.QRSnippetLibraries;

public class QRTest {

	String filePath = "test.png";
	String qrCodeText = "https://home.appian.com/suite/sites/home";
	QRSnippetLibraries qrUtils = new QRSnippetLibraries();

	@Test
	public void testQREncoding() {
		int size = 125;
		String fileType = "png";
		File qrFile = new File(filePath);
		try {
			qrUtils.createQRImage(filePath, qrCodeText, size, fileType, "");
			assertTrue(qrFile.exists());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Test 1 Finished.");
	}

	@Test
	public void testQRDecoder() {
		try {
			String QRDecodeResult = qrUtils.readQRImage(ImageIO.read(new FileInputStream(filePath)));
			System.out.println(QRDecodeResult);
			assertEquals(qrCodeText, QRDecodeResult);

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Test 2 Finished.");
	}
}
