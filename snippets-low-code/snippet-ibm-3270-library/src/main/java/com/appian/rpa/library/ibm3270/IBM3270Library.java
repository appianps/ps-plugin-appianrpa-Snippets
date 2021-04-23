package com.appian.rpa.library.ibm3270;

import com.appian.rpa.library.ibm3270.clients.PCOMMEmulatorCommons;
import com.appian.rpa.library.ibm3270.clients.WC3270EmulatorCommons;
import com.appian.rpa.library.ibm3270.ehll.EHll;
import com.appian.rpa.library.ibm3270.ehll.EHllApi;
import com.appian.rpa.library.ibm3270.ehll.HllApiInvocationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novayre.jidoka.client.api.*;
import com.novayre.jidoka.client.api.annotations.FieldLink;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;
import com.novayre.jidoka.client.api.multios.IClient;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import jodd.util.StringUtil;

import java.io.IOException;
import java.io.Serializable;
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
	private static final String BULK_TEXT_AND_COORDINATES = "Bulk Text & Rol/Col Coordinates (JSON)";

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
	EHll ehll;
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
			) SDKParameterMap parameters) throws HllApiInvocationException {
		String path = parameters.get(DLL_FOLDER_PATH).toString();
		String name = parameters.get(DLL_FILE_NAME).toString();
		String sessionLetter = parameters.get(SESSION_LETTER).toString();
		ehll = EHll.create(path,name);
		ehll.connect(sessionLetter);
//		ehll.setSessionParams("ATTRB,EAB");
//		ehll.setSessionParams("EAB");
//		ehll.setSessionParams("ATTRB");

		apiCommons = new HllApiCommons();
		System.setProperty("jna.library.path", parameters.get(DLL_FOLDER_PATH).toString());
		eHllApi = Native.loadLibrary(parameters.get(DLL_FILE_NAME).toString(), EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);

	}

	/**
	 * Maximize Window
	 */
	@JidokaMethod(name = "IBM Maximize Window", description = "IBM3270Library:v2.0.0: Maximizes the window")
	public void maximizeWindow(@JidokaParameter(
			name = "Nested parameters",
			type = EJidokaParameterType.NESTED,
			nestedParameters = {
					@JidokaNestedParameter(
							name = SESSION_LETTER,
							id = SESSION_LETTER
					)
			}
	) SDKParameterMap parameters)
			throws JidokaFatalException, HllApiInvocationException {
//		String sessionLetter = parameters.get(SESSION_LETTER).toString();
//		char sessionChar = sessionLetter.charAt(0);
//		ehll.maximizeWindow(sessionChar);
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
			) SDKParameterMap parameters) throws JidokaFatalException, HllApiInvocationException {

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
			ehll.sendKey(credential.getUsername());
		} else {
			String textToWrite = credential.getPassword();
			ehll.sendKey(credential.getPassword());
		}
	}

	/**
	 * Action 'Find Text'
	 */
	@JidokaMethod(name = "IBM Find Text", description = "IBM3270Library:v2.0.0: Takes in a text string and returns the row/col location (integer array) in the emulator")
	public List<Integer> findText(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = TEXT_TO_LOCATE,
									id = TEXT_TO_LOCATE
							),
							@JidokaNestedParameter(
									name = SESSION_LETTER,
									id = SESSION_LETTER
							)
					}
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {

		String text = parameters.get(TEXT_TO_LOCATE).toString();
		int loc = ehll.search(text,true);
		server.debug("PS location is: "+loc);

//		String sessionLetter = parameters.get(SESSION_LETTER).toString();
//		char sessionChar = sessionLetter.charAt(0);
//		EHll.RowColumn coords = ehll.convertPositionToRowCol(sessionChar,loc);
//		List<Integer> result = Arrays.asList(coords.getRow(),coords.getCol());

		List<Integer> result = Arrays.asList(loc);
		return result;
	}

	/**
	 * Action 'Get Text at Line'
	 */
	@JidokaMethod(name = "IBM Get Text at Line", description = "IBM3270Library:v2.0.0: Takes in a line number (emulator row number, starts at 1) and returns the text at that line")
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
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {
		String screen;
		screen = ehll.copyScreen(1920);
		server.debug("Screen is: "+screen);
		int line = Integer.valueOf(parameters.get(LINE_NUMBER).toString());
		int begin = 0+80*(line-1);
		int end = 79+(80*(line-1));
		String rowText = screen.substring(begin,end);
		server.debug("Row text is : "+rowText);
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
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {
		String text = parameters.get(TEXT_TO_LOCATE).toString();
		Integer colOff = Integer.valueOf(parameters.get(COLUMN_OFFSET).toString());
		Integer rowOff = Integer.valueOf(parameters.get(ROW_OFFSET).toString());
		ehll.setCursorPosition(colOff); //update this once it supports row col
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
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {
		Integer column = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer row = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		Integer loc = ((row-1)*80)+column;
		ehll.setCursorPosition(loc); //update this once it supports row col
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
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {
		String text = parameters.get(TEXT_TO_WRITE).toString();
		ehll.sendKey(text);
	}

	/**
	 * Action 'Write at Coordinates'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Write at Coordinates", description = "IBM3270Library:v2.0.0: Enters text in emulator at specified location")
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
							),
							@JidokaNestedParameter(
									name = SESSION_LETTER,
									id = SESSION_LETTER
							)
					}
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {

		//		move to coordinates
		Integer column = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer row = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		Integer loc = ((row-1)*80)+column; // update once supports row col
		server.debug("Old calc is: "+loc);
//		String sessionLetter = parameters.get(SESSION_LETTER).toString();
//		char sessionChar = sessionLetter.charAt(0);
//		int loc2 = ehll.convertRowColToCursorPosition(sessionChar,row,column);
//		server.debug("New calc is: "+loc2);
		String text = parameters.get(TEXT_TO_WRITE).toString();
		ehll.sendKeyAtCoordinates(text,loc);
	}

	/**
	 * Action 'Bulk Write at Coordinates'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "IBM Bulk Write at Coordinates", description = "IBM3270Library:v2.0.0: Enters text in bulk at specified locations.")
	public void bulkWriteAtCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = BULK_TEXT_AND_COORDINATES,
									id = BULK_TEXT_AND_COORDINATES,
									instructionalText = "Expects JSON list object with fields 'text', 'row', and 'column' - for example a!toJson({{text:\"test1\",row:1,column:1},{text:\"test2\",row:5,column:5}})"
							)
					}
			) SDKParameterMap parameters) throws JidokaException, IOException, HllApiInvocationException {
		String json = parameters.get(BULK_TEXT_AND_COORDINATES).toString();
		ObjectMapper objectMapper = new ObjectMapper();
		List<HashMap> hmap = objectMapper.readValue(json, List.class);
//		server.debug("Map is: "+hmap);
		for (int i = 0; i < hmap.size(); i++) {
//			server.debug("begin loop, Key: "+hmap.get(i).keySet() + " & Value: " + hmap.get(i).entrySet());

			String text = hmap.get(i).get("text").toString();
			Integer column = Integer.valueOf((int) hmap.get(i).get("column"));
			Integer row = Integer.valueOf((int) hmap.get(i).get("row"));
			Integer loc = ((row-1)*80)+column; // update once supports row col
			ehll.sendKeyAtCoordinates(text,loc);

//			server.debug("end loop, Key: "+hmap.get(i).keySet() + " & Value: " + hmap.get(i).entrySet());
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
			) SDKParameterMap parameters) throws JidokaException, HllApiInvocationException {
		String field = parameters.get(FIELD_LABEL).toString();
		int loc = ehll.search(field,true);
		server.debug("Location is: "+loc);
		int colOff = Integer.valueOf(parameters.get(COLUMN_OFFSET).toString());
		int rowOff = Integer.valueOf(parameters.get(ROW_OFFSET).toString());
		String text = parameters.get(FIELD_LABEL).toString();
		ehll.sendKeyAtCoordinates(text,loc); // update once supports row col
	}
}
