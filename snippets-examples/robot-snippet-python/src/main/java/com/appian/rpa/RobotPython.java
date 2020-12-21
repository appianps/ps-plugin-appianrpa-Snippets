package com.appian.rpa;

import java.io.IOException;
import java.util.List;

import com.appian.rpa.snippets.PythonUtils;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;

@Robot
public class RobotPython implements IRobot {

	private static final Object PYTHON_PATH = "pythonPath";
	private static final Object SCRIPT_PATH = "scriptPath";
	/**
	 * URL to navigate to.
	 */

	private IJidokaServer<?> server;
	String pythonPath;
	String scriptPath;
	PythonUtils pythonUtils;

	@Override
	public boolean startUp() throws Exception {

		server = JidokaFactory.getServer();
		pythonPath = server.getWorkflowParameters().get(PYTHON_PATH).getValue();
		scriptPath = server.getWorkflowParameters().get(SCRIPT_PATH).getValue();
		pythonUtils = new PythonUtils(pythonPath);
		return IRobot.super.startUp();

	}

	/**
	 * Action "start".
	 */
	public void start() {
		server.setNumberOfItems(1);
	}

	public void runPythonScript() {
		try {
			List<String> result = pythonUtils.runScript(scriptPath, "");
			for (String line : result) {
				server.info(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {

		return null;
	}

	/**
	 * Close the browser.
	 */

	/**
	 * Last action of the robot.
	 */
	public void end() {
		server.info("End process");
	}

}
