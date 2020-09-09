package com.appian.rpa.snippets.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.appian.rpa.snippets.PythonUtils;

public class PythonUtilsTest {

	String pythonPath = "C:\\Users\\javier.advani\\Anaconda3\\python";

	PythonUtils pythonRunner = new PythonUtils(pythonPath);

	@Test
	public void testPythonInstallPath() {
		assertTrue(new File(pythonPath + ".exe").exists());
	}

	@Test
	public void testPythonAndItsOutput() {
		try {
			pythonRunner.runScript("C:/Users/javier.advani/Desktop/test.py", "");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
