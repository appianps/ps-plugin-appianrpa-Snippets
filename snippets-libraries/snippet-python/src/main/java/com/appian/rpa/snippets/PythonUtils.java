package com.appian.rpa.snippets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This snippet enables the execution of Python scripts. Keep in mind that a
 * working version of Python must be installed in your Appian RPA Resource for
 * its usage.
 *
 */
public class PythonUtils {

	private String pythonPath;

	/**
	 *
	 * @param pythonPath is the file path to pythons installed version.
	 */

	public PythonUtils(String pythonPath) {
		this.pythonPath = pythonPath;
	}

	/**
	 * Given a Python script path and its parameters (if not needed, just pass ""),
	 * it will execute the script using the Python version previously given during
	 * the Object initialization.
	 *
	 * @param scriptPath       the Python script path to be executed by PythonUtils.
	 * @param scriptParameters if not required, just pass an empty string, as ""
	 * @throws IOException
	 */

	public List<String> runScript(String scriptPath, String scriptParameters) throws IOException {
		List<String> output = new ArrayList<String>();
		try {

			Process execution = Runtime.getRuntime().exec(pythonPath + " " + scriptPath + " " + scriptParameters);
			String executionOutput = null;
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(execution.getInputStream()));
			// generate output
			while ((executionOutput = stdInput.readLine()) != null) {
				output.add(executionOutput);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return output;
	}
}
