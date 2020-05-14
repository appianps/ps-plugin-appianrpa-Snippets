package com.appian.robot.demo.commons;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.appian.robot.demo.pages.WelcomePage;
import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.windows.api.IWindows;
import com.novayre.jidoka.windows.api.Process;
import com.novayre.jidoka.windows.api.WindowInfo;
import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * Class for opening and closing the 3270 Terminal
 */
public class IBM3270AppManager {

	/**
	 * Maximum number of attempts to open the APP
	 */
	public static final int APP_MAX_OPEN_RETRIES = 3;

	/**
	 * Server instance
	 */
	private IJidokaServer<Serializable> server;

	/**
	 * Windows module instance
	 */
	private IWindows windows;

	/**
	 * Waitfor module instance
	 */
	private IWaitFor waitFor;

	/**
	 * Process Name
	 */
	public String processName;

	/**
	 * App Name
	 */
	public String appName;

	/**
	 * Default Constructor
	 * 
	 * @param server
	 * @param windows
	 * @param robot
	 * @param emulator
	 * @param currentCredential
	 */
	@SuppressWarnings("unchecked")
	public IBM3270AppManager(IRobot robot, String emulator) {

		this.server = (IJidokaServer<Serializable>) JidokaFactory.getServer();
		this.windows = IWindows.getInstance(robot);
		this.waitFor = windows.getWaitFor(robot);

		loadVariables(emulator);

	}

	/**
	 * Open the 3270 terminal, a symbolic link is used to open it
	 * 
	 * @param processName
	 * @param titleExpected
	 * @throws JidokaException
	 */
	public IBM3270Page openIBM3270(IBM3270Commons commons) throws JidokaException {

		String app = String.format("%s\\config\\%s.lnk", server.getCurrentDir(), appName);

		for (int i = 1; i <= APP_MAX_OPEN_RETRIES; i++) {

			server.debug(String.format("Trying open [%s] with title [%s]. %s de %s", app, commons.getWindowTitleRegex(),
					i, APP_MAX_OPEN_RETRIES));

			try {
				Runtime.getRuntime().exec(String.format("rundll32 SHELL32.DLL,ShellExec_RunDLL %s", app));
			} catch (IOException e1) {
				server.debug(e1);
				continue;
			}

			AtomicReference<HWND> ieAtomic = new AtomicReference<>();

			try {
				waitFor.wait(IBM3270Constants.LONG_WAIT_SECONDS, "Waiting for the application to open", false, () -> {
					WindowInfo window = windows.getWindow(commons.getWindowTitleRegex());
					if (window != null) {
						ieAtomic.set(window.gethWnd());
						return true;
					}

					return false;
				});
			} catch (JidokaUnsatisfiedConditionException e) {
				;
			}

			if (ieAtomic.get() != null) {
				WelcomePage welcomePage = new WelcomePage(commons);

				try {
					welcomePage.assertIsThisPage();
				} catch (JidokaFatalException e) {
					server.debug(e);
					continue;
				}

				return welcomePage;
			}
		}

		throw new JidokaFatalException(String.format("The App %s could not be opened ", app));
	}

	/**
	 * Close 3270 terminal.
	 */
	public void closeIBM3270(String windowTitle) {

		try {

			List<Process> processes = windows.getProcesses(processName, true);

			if (processes.size() == 0) {
				return;
			}

		} catch (IOException e1) {
			;
		}

		quit(windowTitle);

		windows.pause(1000);

		// Checks that all instances of the application were closed
		try {
			windows.killAllProcesses(processName, 1);
		} catch (IOException e) {
		}
	}

	/**
	 * Quit terminal
	 * 
	 * @param windowTitle
	 */
	public void quit(String windowTitle) {

		windows.activateWindow(windowTitle);

		windows.pause(1000);

		windows.getKeyboard().alt("n").pause(IBM3270Constants.WAIT_MENU_MILLISECONDS).end()
				.pause(IBM3270Constants.WAIT_MENU_MILLISECONDS).enter();
	}

	/**
	 * Loads the variable fields according to the type of emulator
	 * 
	 * @param emulator
	 */
	private void loadVariables(String emulator) {
		if (emulator.equals("wc3270")) {
			appName = IBM3270Constants.APP_NAME_WC3270;
			processName = IBM3270Constants.PROCESS_NAME_WC3270;
		} else {
			appName = IBM3270Constants.APP_NAME_PCOMM;
			processName = IBM3270Constants.PROCESS_NAME_PCOMM;
		}
	}

}
