package com.appian.rpa.snippets.libraries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.lowcode.IRobotVariable;

/**
 * Library containing some basic methods of manipulating PDF files.
 */
@Nano
public class PDFLibrary implements INano {

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** PDDocument instance */
	private PDDocument document;
	
	/** PDPage instance */
	private PDPage page;
	
	/** PDF file Name */
	private String fileName;
	
	/** Font size */
	private final float FONT_SIZE = 12;
	
	/** Font */
	private final PDFont FONT = PDType1Font.TIMES_ROMAN;
	
	/** Leading */
	private final float LEADING = -1.5f * FONT_SIZE;

	/**
	 * Initialize libraries
	 * 
	 * @throws JidokaFatalException
	 */
	public void init() throws Exception {
		server = JidokaFactory.getServer();
		document = null;
	}

	/**
	 * This method creates a new PDF with an blank page
	 */
	@JidokaMethod(name = "Create", description = "Creates a new PDF")
	public void create(@JidokaParameter(defaultValue = "", name = "File name * ") String fileName) {

		server.info("Creating new PDF: " + fileName);
		this.fileName = fileName;

		document = new PDDocument();
		try {
			addNewPage();
			document.save(fileName);
			
		} catch (IOException e) {

			throw new JidokaFatalException("Save", e);
		}

		server.info("PDF created");
	}

	/**
	 * This method extracts the text of a PDF document
	 * 
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Extract Text from PDF", description = "Extracts the text from the PDF document that has been passed as an intruction to the robot.")
	public void extractFileText(
			@JidokaParameter(name = "Instruction name * ") String InstructionName,
			@JidokaParameter(name = "Process variable where the text will be saved * ") String pvName) {

		server.info("Init PDF text extraction");

		Path pdfDoc;
		PDFTextStripper stripper = null;

		try {

			String param = server.getParameters().get(InstructionName);

			pdfDoc = Paths.get(server.getCurrentDir(), param);

			stripper = new PDFTextStripper();

			document = PDDocument.load(new File(pdfDoc.toString()));
			
			String textExtracted =  stripper.getText(document);
			
			server.info("Text extracted: " + textExtracted);
			
			IRobotVariable textAux = server.getWorkflowVariables().get(pvName);
			
			textAux.setValue(textExtracted);
			
			server.getWorkflowVariables().put(pvName, textAux);
			
			document.close();

		} catch (Exception e) {
			throw new JidokaFatalException("Error extracting text", e);

		}

	}
	
	
	/**
	 * Add a new page to a PDF document
	 */
	@JidokaMethod(name = "Add new page", description = "Add a new page to a PDF document.")
	public void addNewPage() {
		
		if(document == null) {
			document = new PDDocument();
		}
		page = new PDPage();
		document.addPage(page);
	}
	
	
	/**
	 * Add a new text to a PDF document
	 */
	@JidokaMethod(name = "Add new content", description = "Add a new content to the actual page.")
	public void addContent(@JidokaParameter(defaultValue = "", name = "Content to add * ") String content) {
		
		if(document == null) {
			server.info("Creating a new PDF file.");
			create("PDFFile.pdf");
		}
				
		try {
			
			PDPageContentStream cont = new PDPageContentStream(document, page);
			
			cont.beginText();
            cont.setFont(FONT, FONT_SIZE);
            cont.setLeading(14.5f);
            cont.newLineAtOffset(50, 700);
            addParagraph(cont, 500, 0, -FONT_SIZE, content.replace("\n", "").replace("\r", ""), true);
            cont.endText();
            cont.close();
            
            server.info("Saving PDF: " + this.fileName);
            document.save(this.fileName);
			
		} catch (IOException e) {
			throw new JidokaFatalException("Error adding content.", e);
		}
	}

	
	
	/**
	 * This method close the object document
	 */
	@JidokaMethod(name = "Close document", description = "Close PDF document")
	public void closeDocument() {
		try {
			document.close();
		} catch (IOException e) {
			throw new JidokaFatalException("Error closing document.", e);
		}
	}

	/**
	 * Create a new paragraph
	 * @param contentStream
	 * @param width
	 * @param sx
	 * @param sy
	 * @param text
	 * @param justify
	 * @throws IOException
	 */
	private void addParagraph(PDPageContentStream contentStream, float width, float sx,
			float sy, String text, boolean justify) throws IOException {
		
		List<String> lines = parseLines(text, width);
		contentStream.setFont(FONT, FONT_SIZE);
		contentStream.newLineAtOffset(sx, sy);
		for (String line : lines) {
			float charSpacing = 0;
			if (justify) {
				if (line.length() > 1) {
					float size = FONT_SIZE * FONT.getStringWidth(line) / 1000;
					float free = width - size;
					if (free > 0 && !lines.get(lines.size() - 1).equals(line)) {
						charSpacing = free / (line.length() - 1);
					}
				}
			}
			contentStream.setCharacterSpacing(charSpacing);
			contentStream.showText(line);
			contentStream.newLineAtOffset(0, LEADING);
		}
	}

	/**
	 * Parse lines
	 * @param text
	 * @param width
	 * @return
	 * @throws IOException
	 */
	private List<String> parseLines(String text, float width) throws IOException {
		
		List<String> lines = new ArrayList<String>();
		int lastSpace = -1;
		
		while (text.length() > 0) {
			int spaceIndex = text.indexOf(' ', lastSpace + 1);
			if (spaceIndex < 0)
				spaceIndex = text.length();
			String subString = text.substring(0, spaceIndex);
			float size = FONT_SIZE * FONT.getStringWidth(subString) / 1000;
			if (size > width) {
				if (lastSpace < 0) {
					lastSpace = spaceIndex;
				}
				subString = text.substring(0, lastSpace);
				lines.add(subString);
				text = text.substring(lastSpace).trim();
				lastSpace = -1;
			} else if (spaceIndex == text.length()) {
				lines.add(text);
				text = "";
			} else {
				lastSpace = spaceIndex;
			}
		}
		return lines;
	}
}
