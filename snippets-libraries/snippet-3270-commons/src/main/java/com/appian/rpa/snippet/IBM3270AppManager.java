package com.appian.rpa.snippet;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.windows.api.IWindows;
import com.novayre.jidoka.windows.api.Process;
import com.novayre.jidoka.windows.api.WindowInfo;
import com.sun.jna.platform.win32.WinDef.HWND;


/**
 * Class for opening and closing the 3270 App 
 */
public class IBM3270AppManager {
	
	/**
	 * 
	 */
	public static final int APP_MAX_OPEN_RETRIES = 3;

	/**
	 * 
	 */
	private IJidokaServer<Serializable> server;

	/**
	 * 
	 */
	private IWindows windows;

	/**
	 * 
	 */
	private IWaitFor waitFor;
	

	/**
	 * Constructor
	 * @param server
	 * @param windows
	 * @param robot
	 * @param currentCredential
	 */
	public IBM3270AppManager(IJidokaServer<Serializable> server, IWindows windows, IRobot robot) {

		this.server = server;
		this.windows = windows;
		this.waitFor = windows.getWaitFor(robot);
	}
	
	
	/**
	 * 
	 */
	public void openIBM3270(String app, String tittleExpected) {

		for(int i = 1; i <= APP_MAX_OPEN_RETRIES; i++) {

			server.debug(String.format("Trying open [%s] with title [%s]. %s de %s", app, tittleExpected, i, APP_MAX_OPEN_RETRIES));

			try {
				Runtime.getRuntime().exec(String.format("rundll32 SHELL32.DLL,ShellExec_RunDLL %s", app));
			} catch (IOException e1) {
				server.debug(e1);
				continue;
			}
			
			AtomicReference<HWND> ieAtomic = new AtomicReference<>();
			
			try {
				waitFor.wait(IBM3270Constants.LONG_WAIT_SECONDS, "Waiting for the application to open", false,
						() -> {
							WindowInfo window = windows.getWindow(tittleExpected);
							if(window != null) {
								ieAtomic.set(window.gethWnd());
								return true;
							}
							
							return false;
						});
			} catch (JidokaUnsatisfiedConditionException e) {
			}

		}

		throw new JidokaFatalException(String.format("The App %s could not be opened ", app));
	}
	
	/**
	 * Close crm.
	 */
	public void closeIBM3270() {

		try {

			List<Process> processes = windows.getProcesses("wc3270.exe");

			if (CollectionUtils.isEmpty(processes)) {
				return;
			}

		} catch (IOException e1) {
		}

		quit();
		
		windows.pause(1000);
		
		// We made sure that all instances of the application were closed
		try {
			windows.killAllProcesses("wc3270.exe", 1);
		} catch (IOException e) {
		}
	}

	/**
	 * Quit terminal
	 */
	public void quit() {
		
		windows.activateWindow(IBM3270Commons.IBM3270_LOGIN_TITLE_REGEX);
		
		windows.pause(1000);
		
		windows.getKeyboard().alt("n").pause(IBM3270Constants.WAIT_MENU_MILLISECONDS).end().pause(IBM3270Constants.WAIT_MENU_MILLISECONDS).enter();
	}
}
