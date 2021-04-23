package com.appian.rpa.library.ibm3270;

import com.appian.rpa.library.ibm3270.clients.PCOMMEmulatorCommons;
import com.appian.rpa.library.ibm3270.clients.WC3270EmulatorCommons;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novayre.jidoka.client.api.*;
import com.novayre.jidoka.client.api.annotations.FieldLink;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;
import com.novayre.jidoka.client.api.multios.IClient;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import jodd.util.StringUtil;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import java.lang.*;

@Nano
public class IBM3270Library implements INano {

	private static final String WINDOW_TITLE_XPATH = "Window Title Xpath";
	private static final String EMULATOR_TYPE = "Emulator Type";
	private static final String SESSION_LETTER = "Session Letter (A for example)";
	private static final String DLL_FOLDER_PATH = "Path to folder containing DLL file";
	private static final String DLL_FILE_NAME = "Name of DLL file (extension NOT needed)";
	private static final String TEXT_TO_LOCATE = "Text to Locate";
	private static final String LINE_NUMBER = "Line Number";
	private static final String ROW_NUMBER = "Row number (Starts with 1 at top of emulator)";
	private static final String COLUMN_NUMBER = "Column number (Starts with 1 at left of emulator)";
	private static final String TEXT_LENGTH = "Text Length to Read";
	private static final String TEXT_TO_WRITE = "Text to Write";
	private static final String FIELD_LABEL = "Field Label";
	private static final String COLUMN_OFFSET = "Column Offset";
	private static final String ROW_OFFSET = "Row Offset";
	private static final String CREDS_APPLICATION = "Application Name for Credentials";
	private static final String CREDS_USERNAME = "Username for Credentials";
	private static final String CREDS_TYPE_IS_USERNAME = "Is Username? Otherwise Password";
	private static final String BULK_TEXT_AND_COORDINATES = "Bulk Test & XY Coordinates (JSON)";

	/**
	 * Server instance
	 */
	private IJidokaServer<Serializable> server;
	/**
	 * Client Module instance
	 */
	protected IClient client;

	@FieldLink("::robot")
	private IRobot robot;

	IBM3270Commons ibm3270Commons;
	HllApiCommons apiCommons;
	EHllApi eHllApi;

	/**
	 * Initialization of the library. This method is called prior to any other
	 * method in the library.
	 */
	@Override
	public void init(
	) {
		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();
		client = IClient.getInstance(robot);
	}

	/**
	 * Set emulator (PCOMM or W3270)
	 */
	@JidokaMethod(name = "IBM Set Emulator", description ="IBM3270Library:v2.0.0: Sets the correct emulator type (PCOMM or W3270)")
	public void setEmulator(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
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
		if (parameters.get(EMULATOR_TYPE).toString().toUpperCase().equals("WC3270")) {
			ibm3270Commons = new WC3270EmulatorCommons(robot, parameters.get(WINDOW_TITLE_XPATH).toString());
		} else if (parameters.get(EMULATOR_TYPE).toString().toUpperCase().equals("PCOMM")) {
			ibm3270Commons = new PCOMMEmulatorCommons(robot, parameters.get(WINDOW_TITLE_XPATH).toString());
		} else {
			throw new JidokaFatalException("Only [WC3270] or [PCOMM] emulTypes are valid");
		}
	}

	/**
	 * Connect to emulator
	 */
	@JidokaMethod(name = "IBM Connect to Emulator", description = "IBM3270Library:v2.0.0: Connects to a running emulator based on the session letter")
	public void connectToEmulator(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = DLL_FOLDER_PATH,
									id = DLL_FOLDER_PATH
							),
							@JidokaNestedParameter(
									name = DLL_FILE_NAME,
									id = DLL_FILE_NAME
							),
							@JidokaNestedParameter(
									name = SESSION_LETTER,
									id = SESSION_LETTER
							)
					}
			) SDKParameterMap parameters) {
		System.setProperty("jna.library.path", parameters.get(DLL_FOLDER_PATH).toString());
		eHllApi = Native.loadLibrary(parameters.get(DLL_FILE_NAME).toString(), EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);
		String sessionLetter = parameters.get(SESSION_LETTER).toString();
		int pos = 0;
		apiCommons = new HllApiCommons();
		apiCommons.hllApi(eHllApi,EHllApi.HA_CONNECT_PS,sessionLetter.getBytes(StandardCharsets.UTF_8),sessionLetter.length(),pos);
	}

	/**
	 * Maximize Window
	 */
	@JidokaMethod(name = "IBM Maximize Window", description = "IBM3270Library:v2.0.0: Maximizes the window")
	public void maximizeWindow()
			throws JidokaFatalException {
		ibm3270Commons.maximizeWindow();
	}

	/**
	 * Enters credentials for an application with specific username
	 * handling special character entry
	 */
	@JidokaMethod(name = "IBM Enter Credential", description = "IBM3270Library:v2.0.0: Enters credentials into emulator")
	public void enterCredentialByUsername(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = CREDS_APPLICATION,
									id = CREDS_APPLICATION
							),
							@JidokaNestedParameter(
									name = CREDS_USERNAME,
									id = CREDS_USERNAME
							),
							@JidokaNestedParameter(
									name = CREDS_TYPE_IS_USERNAME,
									id = CREDS_TYPE_IS_USERNAME,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
							)
					}
			) SDKParameterMap parameters) throws JidokaFatalException {

		CredentialsUtils credentialUtils = CredentialsUtils.getInstance();
		EJidokaParameterBoolean isUsername = (EJidokaParameterBoolean) parameters.get(CREDS_TYPE_IS_USERNAME);
		String application = parameters.get(CREDS_APPLICATION).toString();
		String username = parameters.get(CREDS_USERNAME).toString();
		IUsernamePassword credential = null;
		Boolean reserveBool = true;

		if (StringUtil.isBlank(username)) {
			credential = credentialUtils.getCredentials(application, reserveBool, ECredentialSearch.DISTRIBUTED, 5);
		} else {
			credential = credentialUtils.getCredentialsByUser(application, username, reserveBool, 5);
		}

		if (credential == null) {
			server.warn("Haven't found a credential for application " + application);
			return;
		}

		server.debug("Found a credential for application " + application);

		if (isUsername == EJidokaParameterBoolean.YES) {
			String textToWrite = credential.getUsername();
			int pos = 0;
			apiCommons.hllApi(eHllApi,EHllApi.HA_SENDKEY,textToWrite.getBytes(StandardCharsets.UTF_8),textToWrite.length(),pos);
		} else {
			String textToWrite = credential.getPassword();
			int pos = 0;
			apiCommons.hllApi(eHllApi,EHllApi.HA_SENDKEY,textToWrite.getBytes(StandardCharsets.UTF_8),textToWrite.length(),pos);
		}
	}

	/**
	 * Action 'Find Text'
	 */
	@JidokaMethod(name = "IBM Find Text", description = "IBM3270Library:v1.0.0: Takes in a text string and returns the xy location (integer array) in the emulator")
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
		TextInScreen displayedText = ibm3270Commons.locateText(1, false, null, parameters.get(TEXT_TO_LOCATE).toString());
		if (displayedText != null) {
			Point textLocation = displayedText.getPointInScreen();
			List<Integer> result = Arrays.asList(textLocation.getLocation().x, textLocation.getLocation().y);
			return result;
		}
		return null;
	}

	/**
	 * Action 'Get Text at Line'
	 */
	@JidokaMethod(name = "IBM Get Text at Line", description = "IBM3270Library:v1.0.0: Takes in a line number (emulator y coordinate starting at 1) and returns the text at that line")
	public String getTextAtLine(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = LINE_NUMBER,
									id = LINE_NUMBER
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		List<String> screen;
		screen = ibm3270Commons.scrapeScreen();
		String rowText = screen.get(Integer.valueOf(parameters.get(LINE_NUMBER).toString()) - 1);
		return rowText;
	}

	/**
	 * Action 'Get Text at Coordinate'
	 */
	@JidokaMethod(name = "IBM Get Text at Coordinate", description = "IBM3270Library:v1.0.0: Takes in a coordinate(emulator x y coordinate starting at 1) and length of text to return from that line, then returns the text located there")
	public String getTextAtCoordinate(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = ROW_NUMBER,
									id = ROW_NUMBER
							),
							@JidokaNestedParameter(
									name = COLUMN_NUMBER,
									id = COLUMN_NUMBER
							),
							@JidokaNestedParameter(
									name = TEXT_LENGTH,
									id = TEXT_LENGTH
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		List<String> screen;
		screen = ibm3270Commons.scrapeScreen();
		Integer y = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		Integer x = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer length = Integer.valueOf(parameters.get(TEXT_LENGTH).toString());
		String rowText = screen.get(y - 1);
		Integer min = x - 1;
		Integer max = x - 1 + length;
		if (max > rowText.length()) {
			max = rowText.length();
		}
		String coordinateText = rowText.substring(min, max);
		return coordinateText;
	}

	/**
	 * Action 'Go to Text Position'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Go to Text Position", description = "IBM3270Library:v1.0.0: Takes in a text string and goes to that position in emulator")
	public void goToTextPosition(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = TEXT_TO_LOCATE,
									id = TEXT_TO_LOCATE
							),
							@JidokaNestedParameter(
									name = ROW_OFFSET,
									id = ROW_OFFSET
							),
							@JidokaNestedParameter(
									name = COLUMN_OFFSET,
									id = COLUMN_OFFSET
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		ibm3270Commons.moveToCoordinates(parameters.get(TEXT_TO_LOCATE).toString(), Integer.valueOf(parameters.get(COLUMN_OFFSET).toString()), Integer.valueOf(parameters.get(ROW_OFFSET).toString()), 3);
	}

	/**
	 * Action 'Go to Coordinates'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Go to Coordinates", description = "IBM3270Library:v2.0.0: Takes in a row and column then moves the cursor to that position in emulator")
	public void goToCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = ROW_NUMBER,
									id = ROW_NUMBER
							),
							@JidokaNestedParameter(
									name = COLUMN_NUMBER,
									id = COLUMN_NUMBER
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		Integer columns = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer rows = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		Integer loc = ((rows-1)*80)+columns;
		int pos = ((int) loc); // 4th parameter is cursor position
		String text = new String("not used");
		byte[] dta = text.getBytes(StandardCharsets.UTF_8); //not used
		int len = text.length(); //not used
		apiCommons.hllApi(eHllApi,EHllApi.HA_SET_CURSOR,dta,len,pos);
	}

	/**
	 * Action 'Write Here'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Write Here", description = "IBM3270Library:v2.0.0: Enters text in emulator at current location")
	public void writeHere(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = TEXT_TO_WRITE,
									id = TEXT_TO_WRITE
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		String textToWrite = parameters.get(TEXT_TO_WRITE).toString();
		int pos = 0;
		apiCommons.hllApi(eHllApi,EHllApi.HA_SENDKEY,textToWrite.getBytes(StandardCharsets.UTF_8),textToWrite.length(),pos);
	}

	/**
	 * Action 'Write at Coordinates'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Write at Coordinates", description = "IBM3270Library:v1.0.0: Writes text in emulator at specified location (handles slow typing & special characters")
	public void writeAtCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = ROW_NUMBER,
									id = ROW_NUMBER
							),
							@JidokaNestedParameter(
									name = COLUMN_NUMBER,
									id = COLUMN_NUMBER
							),
							@JidokaNestedParameter(
									name = TEXT_TO_WRITE,
									id = TEXT_TO_WRITE
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		ibm3270Commons.moveToCoordinates(Integer.valueOf(parameters.get(COLUMN_NUMBER).toString()), Integer.valueOf(parameters.get(ROW_NUMBER).toString()), false);
		ibm3270Commons.write(parameters.get(TEXT_TO_WRITE).toString(), false);
	}

	/**
	 * Action 'Bulk Write at Coordinates'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Bulk Write at Coordinates", description = "IBM3270Library:v1.0.0: Writes text in bulk at specified locations (handles slow typing & special characters")
	public void bulkWriteAtCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = BULK_TEXT_AND_COORDINATES,
									id = BULK_TEXT_AND_COORDINATES,
									instructionalText = "Expects JSON list object with fields 'text', 'x', and 'y' - for example a!toJson({{text:\"test1\",x:1,y:1},{text:\"test2\",x:5,y:5}})"
							)
					}
			) SDKParameterMap parameters) throws JidokaException, IOException {
		String json = parameters.get(BULK_TEXT_AND_COORDINATES).toString();
		ObjectMapper objectMapper = new ObjectMapper();
		List<HashMap> hmap = objectMapper.readValue(json, List.class);
//		server.debug("Map is: "+hmap);
		for (int i = 0; i < hmap.size(); i++) {
//			server.debug("begin loop, Key: "+hmap.get(i).keySet() + " & Value: " + hmap.get(i).entrySet());
			ibm3270Commons.moveToCoordinates(Integer.valueOf((int) hmap.get(i).get("x")), Integer.valueOf((int) hmap.get(i).get("y")), false);
			ibm3270Commons.write(hmap.get(i).get("text").toString(), false);
		}
	}

	/**
	 * Action 'Write at Label'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Write at Label", description = "IBM3270Library:v1.0.0: Writes text in emulator at specified label with optional offset (handles slow typing & special characters")
	public void writeAtLabel(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = FIELD_LABEL,
									id = FIELD_LABEL
							),
							@JidokaNestedParameter(
									name = ROW_OFFSET,
									id = ROW_OFFSET
							),
							@JidokaNestedParameter(
									name = COLUMN_OFFSET,
									id = COLUMN_OFFSET
							),
							@JidokaNestedParameter(
									name = TEXT_TO_WRITE,
									id = TEXT_TO_WRITE
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		ibm3270Commons.moveToCoordinates(parameters.get(FIELD_LABEL).toString(), Integer.valueOf(parameters.get(COLUMN_OFFSET).toString()), Integer.valueOf(parameters.get(ROW_OFFSET).toString()), 3);
		ibm3270Commons.write(parameters.get(TEXT_TO_WRITE).toString(), false);
	}

	@JidokaMethod(name = "IBM Try EHLAPI", description = "IBM3270Library:v2.0.0: Trying EHLAPI")
	public void tryEhlapi() throws JidokaException, InterruptedException, NoSuchFieldException, IllegalAccessException {
//	1
		System.setProperty("jna.library.path", "C:\\Program Files (x86)\\IBM\\Personal Communications");

//		String prop = System.getProperty("jna.library.path");
//		if (prop == null || prop.isEmpty()) {
//			prop = "C:/Program Files (x86)/IBM/Personal Communications";
//		} else {
//			prop += ";C:/Program Files (x86)/IBM/Personal Communications";
//		}
//		System.setProperty("jna.library.path", prop);

		//Load the ehll api
		EHllApi eHllApi = Native.loadLibrary("pcshll32", EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);

//		Hard-coding inputs for now

		String example = "A";
		byte[] dta = example.getBytes();
		int func = EHllApi.HA_CONNECT_PS;
		int pos = 0;

		// object to return with results from call to hllApi
		HllApiVal hav = new HllApiVal();
		int inputDtaLength = dta.length;

		Pointer dtaPtr = new Memory(inputDtaLength);
		if (dta != null)
			dtaPtr.write(0, dta, 0, inputDtaLength);
		IntByReference funcIntPtr = new IntByReference(func);
		IntByReference dtaLenPtr = new IntByReference(inputDtaLength);
		IntByReference posIntPtr = new IntByReference(pos);
		eHllApi.hllapi(funcIntPtr, dtaPtr, dtaLenPtr, posIntPtr);

		hav.hllApiCod = posIntPtr.getValue();       // code returned by function call
		hav.hllApiLen = dtaLenPtr.getValue();       // length of byte array returned
		hav.hllApiDta = dtaPtr.getByteArray(0,inputDtaLength);      // byte array returned
		hav.srcDataLen = inputDtaLength;            // length of input byteArray

		server.debug("hav1 is: " + new String(hav.hllApiDta));

		//		Hard-coding inputs for now

		String example2 = "Entering Test Data";
		byte[] dta2 = example2.getBytes();
		int func2 = EHllApi.HA_SENDKEY;
		int pos2 = 0;

		// object to return with results from call to hllApi
		HllApiVal hav2 = new HllApiVal();
		int inputDtaLength2 = dta2.length;

		Pointer dtaPtr2 = new Memory(inputDtaLength2);
		if (dta2 != null)
			dtaPtr2.write(0, dta2, 0, inputDtaLength2);
		IntByReference funcIntPtr2 = new IntByReference(func2);
		IntByReference dtaLenPtr2 = new IntByReference(inputDtaLength2);
		IntByReference posIntPtr2 = new IntByReference(pos2);
		eHllApi.hllapi(funcIntPtr2, dtaPtr2, dtaLenPtr2, posIntPtr2);

		hav2.hllApiCod = posIntPtr2.getValue();       // code returned by function call
		hav2.hllApiLen = dtaLenPtr2.getValue();       // length of byte array returned
		hav2.hllApiDta = dtaPtr2.getByteArray(0,inputDtaLength2);      // byte array returned
		hav2.srcDataLen = inputDtaLength2;            // length of input byteArray

		server.debug("hav2 is: " + new String(hav2.hllApiDta));
	}
}
