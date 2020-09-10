package com.appian.rpa.snippets.browsermanager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class ScreenshotsManager {

	/**
	 * This function will take screenshot of a visible element
	 * 
	 * @param screenShotPath
	 * @throws Exception
	 */
	public void visibleElementScreenShot(WebDriver webDriver, By visibleElementBy, Path screenShotPath) throws IOException {

		WebElement ele = webDriver.findElement(visibleElementBy);

		// Get entire page screenshot
		TakesScreenshot scrShot = ((TakesScreenshot) webDriver);
		File screenshot = scrShot.getScreenshotAs(OutputType.FILE);
		BufferedImage fullImg = ImageIO.read(screenshot);
		
		// Get the location of element on the page
		Point point = ele.getLocation();

		// Get width and height of the element
		int eleWidth = ele.getSize().getWidth();
		int eleHeight = ele.getSize().getHeight();

		// Crop the entire page screenshot to get only element screenshot
		BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
		ImageIO.write(eleScreenshot, "png", screenshot);

		// Copy the element screenshot to disk
		File screenshotLocation = new File(screenShotPath.toString());
		FileUtils.copyFile(screenshot, screenshotLocation);

	}
	
	/**
	 * This function will take screenshot of the visible page and store it in the given Path
	 * 
	 * @param screenshotPath
	 * @return 
	 * @throws IOException 
	 * @throws Exception
	 */
	public void visiblePageScreenShot(WebDriver webDriver, Path screenshotPath) throws IOException {
		
		File screenshot = visiblePageScreenShot(webDriver, "vp", screenshotPath.getFileName().toString());
		
		FileUtils.copyFile(screenshot, screenshotPath.toFile());
	}
	
	/**
	 * This function will take a screenshot of the visible page and store it in the default OS's temp folder 
	 * and return the generated File 
	 * 
	 * @param index - define the index name for the screenshot in temp folder
	 * @param subIndex - define the subindex or extension for the screenshot in temp folder
	 * @return
	 * @throws IOException 
	 */
	public File visiblePageScreenShot(WebDriver webDriver, String index, String subIndex) throws IOException { 
		// Get entire visible page screenshot
		TakesScreenshot scrShot = ((TakesScreenshot) webDriver);
		File screenshot = scrShot.getScreenshotAs(OutputType.FILE);
		
		Path tmpImagePath;
			
		// Create a temporal File in the default temp folder of the OS
		tmpImagePath = Files.createTempFile(index, subIndex);

		// Delete the temporal file on VM exit
		tmpImagePath.toFile().deleteOnExit();
		
		FileUtils.copyFile(screenshot, tmpImagePath.toFile());
		
		return tmpImagePath.toFile();
	}

}
