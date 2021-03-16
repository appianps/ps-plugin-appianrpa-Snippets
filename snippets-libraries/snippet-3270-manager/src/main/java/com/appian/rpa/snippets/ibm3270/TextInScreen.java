package com.appian.rpa.snippets.ibm3270;

import java.awt.Point;
import java.util.List;

/**
 * Class representing text found on a screen
 */
public class TextInScreen {

	/**
	 * Point on the screen where the text is located
	 */
	private Point pointOnScreen;

	/**
	 * Text to find
	 */
	private String text;

	/**
	 * List of text lines in screen
	 */
	private List<String> screen;

	/**
	 * Empty constructor
	 */
	public TextInScreen() {

	}

	/**
	 * Constructor
	 * 
	 * @param text
	 * @param pointInScreen
	 */
	public TextInScreen(String text, Point pointOnScreen) {

		this.text = text;
		this.pointOnScreen = pointOnScreen;
	}

	/**
	 * Indicates if the text passed as a parameter matches the text of the object
	 * 
	 * @param data
	 * @return
	 */
	public boolean isTextFound(String data) {
		if (getText() == null || data == null) {
			return false;
		}

		return getText().equals(data);
	}

	public Point getPointInScreen() {
		return pointOnScreen;
	}

	public void setPointInScreen(Point pointOnScreen) {
		this.pointOnScreen = pointOnScreen;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<String> getScreen() {
		return screen;
	}

	public void setScreen(List<String> screen) {
		this.screen = screen;
	}

}
