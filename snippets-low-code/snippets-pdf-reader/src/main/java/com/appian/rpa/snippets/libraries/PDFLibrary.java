package com.appian.rpa.snippets.libraries;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

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
	public String extractFileText(
			@JidokaParameter(name = "PV name * ", defaultValue = "file") String pvName) {

		server.info("Init PDF text extraction");

		PDFTextStripper stripper = null;
		String textExtracted = null;

		try {
			
			String name = server.getWorkflowVariables().get(pvName).getFileNameValue();
			
			document = PDDocument.load(Paths.get(server.getCurrentDir(), name).toFile());

			stripper = new PDFTextStripper();

			textExtracted =  stripper.getText(document);
			
			document.close();

		} catch (Exception e) {
			throw new JidokaFatalException("Error extracting text", e);

		}
		return textExtracted;
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
				
		PDFUtilities.addText(document, page, this.fileName, content);
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

}
