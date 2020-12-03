package com.appian.rpa.snippets.libraries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * PDF Utilities class
 * @author appian
 *
 */
public class PDFUtilities {
	
		
	/** Font size */
	private final static float FONT_SIZE = 12;
	
	/** Font */
	private final static PDFont FONT = PDType1Font.TIMES_ROMAN;
	
	/** Leading */
	private final static float LEADING = -1.5f * FONT_SIZE;
	
	
	/**
	 * 
	 * @param content
	 */
	public static void addText(PDDocument document, PDPage page, String fileName,  String content) {
		try {
			
			PDPageContentStream cont = new PDPageContentStream(document, page);
			
			cont.beginText();
            cont.setFont(FONT, FONT_SIZE);
            cont.setLeading(14.5f);
            cont.newLineAtOffset(50, 700);
            PDFUtilities.addParagraph(cont, 500, 0, -FONT_SIZE, content.replace("\n", "").replace("\r", ""), true);
            cont.endText();
            cont.close();
            
            document.save(fileName);
			
		} catch (IOException e) {
			throw new JidokaFatalException("Error adding content.", e);
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
	public static void addParagraph(PDPageContentStream contentStream, float width, float sx,
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
	public static List<String> parseLines(String text, float width) throws IOException {
		
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
