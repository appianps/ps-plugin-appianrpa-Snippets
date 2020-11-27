package com.appian.rpa.snippets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
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
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * This snippet utility enable the generation of QRCodes, as its translation.
 * Please, have a look to the tests to get familiar with its usage.
 */
@Nano
public class QRSnippetLibraries implements INano {

	/**
	 * Given a text String, an image format and the output path as inputs, this
	 * method generates a QR Image encoding that text information.
	 *
	 * @param qrFilename     Name of the QR file to be generated.
	 * @param qrCodeText     The text information as String to be encoded as an
	 *                       image.
	 * @param size           The QR image size (width and height are always the
	 *                       same, as it is a square image)
	 * @param fileType       The image output format.
	 * @param rpVariableName Name of the Low Code variable to save the QR image
	 * @throws JidokaFatalException, surrounding WriterException & IOException
	 *
	 */
	@JidokaMethod(name = "Create a QR image", description = "Create a QR image encoding the text information")
	public void createQRImage(
			@JidokaParameter(defaultValue = "", name = "Name of the QR file to be generated * ") String qrFilename,
			@JidokaParameter(defaultValue = "", name = "Text information to be encoded * ") String qrCodeText,
			@JidokaParameter(defaultValue = "125", name = "QR image size * ") int size,
			@JidokaParameter(defaultValue = "png", name = "Image output format * ") String fileType,
			@JidokaParameter(defaultValue = "", name = "Image Destination Variable Name * ") String rpVariableName) {

		// Create the ByteMatrix for the QR-Code that encodes the given String
		Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
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
			File qrFile = Paths.get(JidokaFactory.getServer().getCurrentDir(), qrFilename).toFile();
			ImageIO.write(image, fileType, qrFile);

			JidokaFactory.getServer().getWorkflowVariables().get(rpVariableName).setValue(qrFile.getAbsolutePath());

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
	 * Given a QR Image url, decodes its text value.
	 *
	 * @param imageUrl       Url of QR image
	 * @param rpVariableName Name of the Low Code variable to save the QR text
	 */
	@JidokaMethod(name = "Read QR image from URL", description = "Read QR image from URL and returns the text value to the given robotic process variables")
	public String readQRImageFromUrl(@JidokaParameter(defaultValue = "", name = "QR image URL * ") String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			BufferedImage qrImage = ImageIO.read(url);

			return readQRImage(qrImage);

		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage());
		}

	}

	/**
	 * Given a QR Image file, decodes its text value.
	 *
	 * @param instructionName Name of the instruction that contains the image
	 * @param rpVariableName  Name of the Low Code variable to save the QR text
	 */
	@JidokaMethod(name = "Read QR from file", description = "Read QR image from a file and returns the text value to the given robotic process variables")
	public String readQRImageFromFile(
			@JidokaParameter(defaultValue = "", name = "Name of the instruction that contains the image * ") String rpVariableName) {

		try {

			String parameter = JidokaFactory.getServer().getWorkflowVariables().get(rpVariableName).getFileNameValue();

			File imageFile = Paths.get(JidokaFactory.getServer().getCurrentDir(), parameter).toFile();

			BufferedImage qrImage = ImageIO.read(imageFile);

			return readQRImage(qrImage);

		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage());
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
	public String readQRImage(BufferedImage image) {
		try {
			Map<DecodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
			BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));

			Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
			return qrCodeResult.getText();
		} catch (NotFoundException e) {
			throw new JidokaFatalException(e.getMessage());
		}

	}

}
