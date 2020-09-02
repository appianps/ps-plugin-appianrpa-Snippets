package com.appian.rpa.snippets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * This snippet utility enable the generation of QRCodes, as its translation.
 * Please, have a look to the tests to get familiar with its usage.
 */
public class QRUtils {
	/**
	 * Given a text String, an image format and the output path as inputs, this
	 * method generates a QR Image encoding that text information.
	 *
	 * @param qrFile     the file path where the image is going to be saved. This
	 *                   must be passed as a File format.
	 * @param qrCodeText The text information as String to be encoded as an image.
	 * @param size       The QR image size (width and height are always the same, as
	 *                   it is a square image)
	 * @param fileType   The image output format.
	 * @throws JidokaFatalException, surrounding WriterException & IOException
	 *
	 */

	public void createQRImage(File qrFile, String qrCodeText, int size, String fileType) throws JidokaFatalException {
		// Create the ByteMatrix for the QR-Code that encodes the given String
		Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix;
		try {
			byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);

			// Make the BufferedImage that are to hold the QRCode
			int matrixWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			// Paint and save the image using the ByteMatrix
			graphics.setColor(Color.BLACK);
			drawQRCode(byteMatrix, matrixWidth, graphics);
			ImageIO.write(image, fileType, qrFile);
		} catch (WriterException e) {

		} catch (IOException e) {
			throw new JidokaFatalException(e.getMessage());
		}
	}

	private void drawQRCode(BitMatrix byteMatrix, int matrixWidth, Graphics2D graphics) {
		for (int i = 0; i < matrixWidth; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
	}

	/**
	 * Given a loaded QR Image, decodes its text value.
	 *
	 * @param image previously loaded as BufferedImage. Please, ensure in your code
	 *              that the QR image is successfully loaded
	 * @return the text value decoded as String.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NotFoundException
	 */

	public String readQRImage(BufferedImage image) throws JidokaFatalException {
		try {
			Map<DecodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<DecodeHintType, ErrorCorrectionLevel>();
			BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));

			Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
			return qrCodeResult.getText();
		} catch (NotFoundException e) {
			throw new JidokaFatalException(e.getMessage());
		}

	}

}
