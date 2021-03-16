package com.appian.rpa.snippets.examples;

import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Arrays;

import com.novayre.jidoka.client.api.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippets.ibm3270.IBM3270AppManager;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

@Robot
public class IBM3270ManagerRobot implements IRobot {

	private static final String TEXT_TO_LOCATE = "Text to Locate";
	private static final String INSTALL_PATH = "Install Path";
	private static final String WINDOW_TITLE_XPATH = "Window Title Xpath";
	private static final String EMULATOR_TYPE = "Emulator Type";
	/** Jidoka server instance */
	private IJidokaServer<Serializable> server;

	/** Client Module instance */
	protected IClient client;

	IBM3270AppManager intanceIBM3270AppManager;


	/**
	 * Startup method. This <code>startUp</code> method is called prior to calling
	 * the first workflow method defined
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean startUp() throws Exception {

		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();
		client = IClient.getInstance(this);

		return true;
	}

	/**
	 * Action 'Init'.
	 * <p>
	 * Initializes Jidoka modules. Instances of the emulator type passed as a
	 * parameter are loaded
	 */
	public void init(
	) {
		try {
			server.debug("Robot initialized");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing");
		}
	}

	/**
	 * Action 'Open 3270' terminal
	 */
	@JidokaMethod(name = "Open IBM Terminal", description ="Opens 3270/pcomm terminal")
	public void open3270(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = INSTALL_PATH,
									id = INSTALL_PATH
							),
							@JidokaNestedParameter(
									name = WINDOW_TITLE_XPATH,
									id = WINDOW_TITLE_XPATH
							),
							@JidokaNestedParameter(
									name = EMULATOR_TYPE,
									id = EMULATOR_TYPE
							)
					}
			) SDKParameterMap parameters) {
		server.info("Opening 3270 terminal");
		intanceIBM3270AppManager = IBM3270AppManager.openIBM3270(parameters.get(INSTALL_PATH).toString(), parameters.get(WINDOW_TITLE_XPATH).toString(), this, parameters.get(EMULATOR_TYPE).toString());
		client.pause(2000);
//		server.sendScreen("Screenshot after opening the terminal");
	}

	/**
	 * Action 'Find Text'
	 */
	@JidokaMethod(name = "Find Text", description = "Takes in a text string and returns the xy location (integer array) in the emulator")
	public List<Integer> findText(
			@JidokaParameter(
			name = "Nested parameters",
			type = EJidokaParameterType.NESTED,
			nestedParameters = {
					@JidokaNestedParameter(
							name = TEXT_TO_LOCATE,
							id = TEXT_TO_LOCATE
					)
			}
			) SDKParameterMap parameters) throws JidokaException {
//		Point textLocation = intanceIBM3270AppManager.getTextPosition(server.getWorkflowVariables().get("textToLocate").getValue().toString());
		Point textLocation = intanceIBM3270AppManager.getTextPosition(parameters.get(TEXT_TO_LOCATE).toString());
		if (textLocation==null) {
			return null;
		}
		List<Integer> result = Arrays.asList(textLocation.getLocation().x, textLocation.getLocation().y);
		return result;
	}

	/**
	 * Action 'Go to Text Position'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Go to Text Position", description ="Takes in a text string and goes to that position in emulator")
	public void goToTextPosition(
			@JidokaParameter(
			name = "Nested parameters",
			type = EJidokaParameterType.NESTED,
			nestedParameters = {
					@JidokaNestedParameter(
							name = TEXT_TO_LOCATE,
							id = TEXT_TO_LOCATE
					)
			}
	) SDKParameterMap parameters) throws JidokaException {
		intanceIBM3270AppManager.write(parameters.get(TEXT_TO_LOCATE).toString(), 0, 0, "", 3);
	}

	/**
	 * Action 'Close 3270'.
	 * @throws IOException
	 */
	public void close3270() throws IOException {
		server.info("Closing IBM3270 terminal");
		intanceIBM3270AppManager.close();
	}

	/**
	 * Action 'End'.
	 */
	public void end() {
		// continue the process, here the robot ends its execution
	}

	/**
	 * Clean up.
	 *
	 * @return the string[]
	 * @throws Exception the exception
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		if (intanceIBM3270AppManager!=null) {
			intanceIBM3270AppManager.close();
		}
		return IRobot.super.cleanUp();
	}

	/**
	 * Manage exception.
	 *
	 * @param action    action where the original exception was thrown
	 * @param exception original exception
	 * @return the action where the robot will continue its execution
	 * @throws Exception the exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		// We get the message of the exception
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// Send a screenshot to the log so the user can see the screen in the moment
		// of the error. This is a very useful thing to do
		server.sendScreen("Screenshot of the error");

		// If we have a FatalException we should abort the execution.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, we don't know
		// what has happened

		return IRobot.super.manageException(action, exception);
	}

}
