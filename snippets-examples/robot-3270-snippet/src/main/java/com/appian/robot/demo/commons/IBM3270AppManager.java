package com.appian.robot.demo.commons;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
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
	 * Default Constructor
	 * 
	 * @param server
	 * @param windows
	 * @param robot
	 * @param currentCredential
	 */
	@SuppressWarnings("unchecked")
	public IBM3270AppManager(IRobot robot) {

		this.server = (IJidokaServer<Serializable>) JidokaFactory.getServer();
		this.windows = IWindows.getInstance(robot);
		this.waitFor = windows.getWaitFor(robot);
	}

	/**
	 * Open the 3270 terminal, a symbolic link is used to open it
	 * 
	 * @param processName
	 * @param titleExpected
	 */
	public void openIBM3270(String processName, String titleExpected) {

		String app = String.format("%s\\config\\%s.lnk", server.getCurrentDir(), processName);

		for (int i = 1; i <= APP_MAX_OPEN_RETRIES; i++) {

			server.debug(String.format("Trying open [%s] with title [%s]. %s de %s", app, titleExpected, i,
					APP_MAX_OPEN_RETRIES));

			try {
				Runtime.getRuntime().exec(String.format("rundll32 SHELL32.DLL,ShellExec_RunDLL %s", app));
			} catch (IOException e1) {
				server.debug(e1);
				continue;
			}

			AtomicReference<HWND> ieAtomic = new AtomicReference<>();

			try {
				waitFor.wait(IBM3270Constants.LONG_WAIT_SECONDS, "Waiting for the application to open", false, () -> {
					WindowInfo window = windows.getWindow(titleExpected);
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
				return;
			}

		}

		throw new JidokaFatalException(String.format("The App %s could not be opened ", app));
	}

	/**
	 * Close 3270 terminal.
	 */
	public void closeIBM3270(String process, String windowTitle) {

		try {

			List<Process> processes = windows.getProcesses(process, true);

			if (CollectionUtils.isEmpty(processes)) {
				return;
			}

		} catch (IOException e1) {
			;
		}

		quit(windowTitle);

		windows.pause(1000);

		// Checks that all instances of the application were closed
		try {
			windows.killAllProcesses(process, 1);
		} catch (IOException e) {
		}
	}

	/**
	 * Quit terminal
	 * @param windowTitle
	 */
	public void quit(String windowTitle) {

		windows.activateWindow(windowTitle);

		windows.pause(1000);

		windows.getKeyboard().alt("n").pause(IBM3270Constants.WAIT_MENU_MILLISECONDS).end()
				.pause(IBM3270Constants.WAIT_MENU_MILLISECONDS).enter();
	}
}
