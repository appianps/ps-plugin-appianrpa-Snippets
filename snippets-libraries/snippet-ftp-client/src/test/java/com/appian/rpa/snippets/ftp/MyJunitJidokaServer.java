package com.appian.rpa.snippets.ftp;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.EShapeType;
import com.novayre.jidoka.client.api.IJidokaEncryption;
import com.novayre.jidoka.client.api.IJidokaGlobalContext;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IMessageOptions;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.ItemData;
import com.novayre.jidoka.client.api.ItemData.ESubResult;
import com.novayre.jidoka.client.api.appian.expression.IAppianBinding;
import com.novayre.jidoka.client.api.execution.IExecution;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.novayre.jidoka.client.lowcode.IRobotVariable;

public class MyJunitJidokaServer implements IJidokaServer<Serializable> {

	/**
	 * Log.
	 */
	private final PrintStream log;

	/**
	 * Parameters of execution.
	 */
	private final Map<String, String> parameters;

	/**
	 * Persistent context.
	 */
	private Serializable context;

	/**
	 * Available log levels .
	 *
	 * @author Juan Manuel Reina Morales
	 *
	 */
	private enum Level {
		STAT, TRACE, DEBUG, INFO, WARN, ERROR, FATAL
	};

	/**
	 * Constructor.
	 *
	 * @param log
	 * @param parameters
	 */
	public MyJunitJidokaServer(PrintStream log, Map<String, String> parameters) {
		this.log = log;
		this.parameters = parameters;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setNumberOfItems(int)
	 */
	@Override
	public void setNumberOfItems(int numberOfItems) {

		if (log == null) {
			return;
		}

		log.append(String.format("setNumberOfItems %d", numberOfItems));
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItem(int,
	 *      java.lang.String)
	 */
	@Override
	public void setCurrentItem(int itemIndex, String itemKey) {

		if (log == null) {
			return;
		}

		log.append(String.format("setCurrentItem %d - %s", itemIndex, itemKey));
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToOK()
	 */
	@Override
	public void setCurrentItemResultToOK() {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToOK");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToOK(java.lang.String)
	 */
	@Override
	public void setCurrentItemResultToOK(String detail) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToOK-detail");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToOK(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public void setCurrentItemResultToOK(String detail, Map<String, String> properties) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToOK-detail-properties");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToWarn()
	 */
	@Override
	public void setCurrentItemResultToWarn() {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToWarn");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToWarn(java.lang.String)
	 */
	@Override
	public void setCurrentItemResultToWarn(String detail) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToWarn-detail");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToWarn(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public void setCurrentItemResultToWarn(String detail, Map<String, String> properties) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToWarn-detail-properties");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToFail()
	 */
	@Override
	public void setCurrentItemResultToFail() {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToFail");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToFail(java.lang.String)
	 */
	@Override
	public void setCurrentItemResultToFail(String detail) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToFail-detail");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToFail(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public void setCurrentItemResultToFail(String detail, Map<String, String> properties) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToFail-detail-properties");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToIgnore()
	 */
	@Override
	public void setCurrentItemResultToIgnore() {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToIgnore");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToIgnore(java.lang.String)
	 */
	@Override
	public void setCurrentItemResultToIgnore(String detail) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToIgnore-detail");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResultToIgnore(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public void setCurrentItemResultToIgnore(String detail, Map<String, String> properties) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResultToIgnore-detail-properties");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setCurrentItemResult(com.novayre.jidoka.client.api.ItemData)
	 */
	@Override
	public void setCurrentItemResult(ItemData item) {

		if (log == null) {
			return;
		}

		log.append("setCurrentItemResult-item");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setItemsKeyAndResult(java.util.List)
	 */
	@Override
	public void setItemsKeyAndResult(List<ItemData> items) {

		if (log == null) {
			return;
		}

		log.append("setItemsKeyAndResult-items");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setDumpSection(java.lang.String)
	 */
	@Override
	public void setDumpSection(String dumpSection) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setResultProperties(java.util.Map)
	 */
	@Override
	public void setResultProperties(Map<String, String> resultProperties) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#getItemSubResultTexts()
	 */
	@Override
	public Map<ESubResult, String> getItemSubResultTexts() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaStatistics#setItemSubResultTexts(java.util.Map)
	 */
	@Override
	public void setItemSubResultTexts(Map<ESubResult, String> subResultTexts) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getParameters()
	 */
	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getCurrentDir()
	 */
	@Override
	public String getCurrentDir() {
		return "";
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#sendScreenIfCaptureModeEnabled(java.lang.String)
	 */
	@Override
	public void sendScreenIfCaptureModeEnabled(String description) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#sendScreen(java.lang.String)
	 */
	@Override
	public void sendScreen(String description) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getScreen()
	 */
	@Override
	public BufferedImage getScreen() throws AWTException {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getScreen(java.awt.Rectangle)
	 */
	@Override
	public BufferedImage getScreen(Rectangle rectangle) throws AWTException {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getPersistentContextValue()
	 */
	@Override
	public Serializable getPersistentContextValue() {
		return context;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#setPersistentContextValue(java.io.Serializable)
	 */
	@Override
	public void setPersistentContextValue(Serializable value) {
		context = value;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#resetPersistenContextValue()
	 */
	@Override
	public void resetPersistenContextValue() {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getGlobalContext()
	 */
	@Override
	public <C extends Serializable> IJidokaGlobalContext<C> getGlobalContext() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getMessageInstance()
	 */
	@Override
	public IMessageOptions getMessageInstance() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#sendQuestion(com.novayre.jidoka.client.api.IMessageOptions)
	 */
	@Override
	public void sendQuestion(IMessageOptions messageOptions) throws IOException {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getAnswer()
	 */
	@Override
	public IMessageOptions getAnswer() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getExecution(int)
	 */
	@Override
	public IExecution getExecution(int numberOfOlderExecutionsToGet) {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getPreviousAction()
	 */
	@Override
	public String getPreviousAction() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getCurrentAction()
	 */
	@Override
	public String getCurrentAction() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getFieldLinks()
	 */
	@Override
	public Map<String, Object> getFieldLinks() {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#callNano(java.lang.String)
	 */
	@Override
	public void callNano(String nanoAction) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#callNanoObject(java.lang.String,
	 *      java.lang.Object[])
	 */
	@Override
	public Object callNanoObject(String nanoAction, Object... parameters) {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getCredentials(java.lang.String)
	 */
	@Override
	public List<IUsernamePassword> getCredentials(String application) {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#updateCredential(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public boolean updateCredential(String application, String username, String password) {
		return false;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#enableCredential(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void enableCredential(String application, String username) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#disableCredential(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void disableCredential(String application, String username) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#reserveCredential(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public boolean reserveCredential(String application, String username) {
		return false;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#releaseCredential(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void releaseCredential(String application, String username) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getCredential(java.lang.String,
	 *      boolean, com.novayre.jidoka.client.api.ECredentialSearch)
	 */
	@Override
	public IUsernamePassword getCredential(String application, boolean reserve, ECredentialSearch search) {
		return null;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#downloadSupportFile(java.lang.String)
	 */
	@Override
	public void downloadSupportFile(String relativePath) throws IOException {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#registerEvent(java.lang.String)
	 */
	@Override
	public void registerEvent(String eventDetailText) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return false;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#credentialDecrypt(java.lang.String)
	 */
	@Override
	public String credentialDecrypt(String credential) {
		return credential;
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#protectDesktop(boolean)
	 */
	@Override
	public void protectDesktop(boolean protect) {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IJidokaContext#getQueueManager()
	 */
	@Override
	public IQueueManager getQueueManager() {
		return null;
	}

	/**
	 * Write message (and optionally stack trace if throwable is passed) to console
	 * output logger
	 *
	 * @param level
	 * @param message
	 * @param t
	 */
	private void log(Level level, Object message, Throwable t) {

		if (log == null) {
			return;
		}

		log.append(String.format("[%s] - %s\n", level.name(), message));

		if (t == null) {
			return;
		}

		t.printStackTrace(log);
	}

	/**
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object)
	 */
	@Override
	public void debug(Object message) {
		log(Level.DEBUG, message, null);
	}

	/**
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	@Override
	public void debug(Object message, Throwable t) {
		log(Level.DEBUG, message, t);
	}

	/**
	 * @see org.apache.commons.logging.Log#error(java.lang.Object)
	 */
	@Override
	public void error(Object message) {
		log(Level.ERROR, message, null);
	}

	/**
	 * @see org.apache.commons.logging.Log#error(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	@Override
	public void error(Object message, Throwable t) {
		log(Level.ERROR, message, t);
	}

	/**
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
	 */
	@Override
	public void fatal(Object message) {
		log(Level.FATAL, message, null);
	}

	/**
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	@Override
	public void fatal(Object message, Throwable t) {
		log(Level.FATAL, message, t);
	}

	/**
	 * @see org.apache.commons.logging.Log#info(java.lang.Object)
	 */
	@Override
	public void info(Object message) {
		log(Level.INFO, message, null);
	}

	/**
	 * @see org.apache.commons.logging.Log#info(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	@Override
	public void info(Object message, Throwable t) {
		log(Level.INFO, message, t);
	}

	/**
	 * @see org.apache.commons.logging.Log#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	/**
	 * @see org.apache.commons.logging.Log#isErrorEnabled()
	 */
	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	/**
	 * @see org.apache.commons.logging.Log#isFatalEnabled()
	 */
	@Override
	public boolean isFatalEnabled() {
		return true;
	}

	/**
	 * @see org.apache.commons.logging.Log#isInfoEnabled()
	 */
	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	/**
	 * @see org.apache.commons.logging.Log#isTraceEnabled()
	 */
	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	/**
	 * @see org.apache.commons.logging.Log#isWarnEnabled()
	 */
	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	/**
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object)
	 */
	@Override
	public void trace(Object message) {
		log(Level.TRACE, message, null);
	}

	/**
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	@Override
	public void trace(Object message, Throwable t) {
		log(Level.TRACE, message, t);
	}

	/**
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object)
	 */
	@Override
	public void warn(Object message) {
		log(Level.WARN, message, null);
	}

	/**
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	@Override
	public void warn(Object message, Throwable t) {
		log(Level.WARN, message, t);
	}

	@Override
	public Map<String, String> getParametersDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getEnvironmentVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLastResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Path> getSupportFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRobot getRobot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addFileToCleanUp(String file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFileParameterToCleanUp(String file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executionNeedless(String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> pluginRequest(String pluginMessage, Map<String, String> request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IJidokaEncryption getEncryption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void activeThirdPartyLog(boolean active) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, IRobotVariable> getWorkflowVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, IRobotVariable> getWorkflowParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, IAppianBinding> getBindingContextMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EShapeType getCurrentActionShapeType() {
		// TODO Auto-generated method stub
		return null;
	}

}
