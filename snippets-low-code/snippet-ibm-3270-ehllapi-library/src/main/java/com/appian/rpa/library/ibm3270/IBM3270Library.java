package com.appian.rpa.library.ibm3270;

import com.appian.rpa.library.ibm3270.ehll.EHll;
import com.appian.rpa.library.ibm3270.ehll.EHllApi;
import com.appian.rpa.library.ibm3270.ehll.HllApiInvocationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novayre.jidoka.client.api.*;
import com.novayre.jidoka.client.api.annotations.FieldLink;
import com.novayre.jidoka.client.api.annotations.Nano;
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

	private static final String SESSION_LETTER = "Session Letter (A for example)";
	private static final String DLL_FOLDER_PATH = "Path to folder containing DLL file";
	private static final String DLL_FILE_NAME = "Name of DLL file (extension NOT needed)";
	private static final String TEXT_TO_LOCATE = "Text to Locate";
	private static final String ROW_NUMBER = "Row number (starts with 1 at top of emulator)";
	private static final String COLUMN_NUMBER = "Column number (starts with 1 at left of emulator)";
	private static final String TEXT_LENGTH = "Text Length to Read";
	private static final String TEXT_TO_WRITE = "Text to Write";
	private static final String CHARACTER_SEQUENCE = "Sequence of characters that make up the keyboard mnemonics, ASCII characters representing the special function keys of the keyboard in the workstation";
	private static final String FIELD_LABEL = "Field Label";
	private static final String COLUMN_OFFSET = "Column Offset";
	private static final String ROW_OFFSET = "Row Offset";
	private static final String CREDS_APPLICATION = "Application Name for Credentials";
	private static final String CREDS_USERNAME = "Username for Credentials";
	private static final String CREDS_TYPE_IS_USERNAME = "Is Username? Otherwise Password";
	private static final String GO_FIRST_OR_LAST_CHAR = "Go to first character of word? Otherwise last character";
	private static final String WRITE_FIRST_OR_LAST_CHAR = "Write at first character of word? Otherwise last character. Use last character & column offset of 2 to write text one full space to the right of a label end";
	private static final String BULK_TEXT_AND_COORDINATES = "Bulk Text & Rol/Col Coordinates (JSON)";
	private static final String BULK_COORDINATES = "Bulk Rol/Col Coordinates (JSON)";
	private static final String CLEAR_FIELD_FIRST = "Clear the current data in the field before writing?";

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

	EHll ehll;
	EHllApi eHllApi;
//	functionality first built based on the IBM Personal Communications v12 emulator:
//	https://www.ibm.com/docs/en/personal-communications/12.0?topic=SSEQ5Y_12.0.0/com.ibm.pcomm.doc/books/html/emulator_programming08.htm
	char escapeChar = "@".charAt(0);
//	this is the default escape character and unlikely to be different
//	if a mainframe app uses something else, expose a low-code RPA method to update this variable

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
		System.setProperty("jna.library.path", parameters.get(DLL_FOLDER_PATH).toString());
		eHllApi = Native.loadLibrary(parameters.get(DLL_FILE_NAME).toString(), EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);

	}

	@JidokaMethod(name = "IBM Disconnect from Emulator", description = "IBM3270Library:v2.0.0: Disconnects from the connected emulator session")
	public void disconnectFromEmulator() throws HllApiInvocationException {
		ehll.disconnect();
	}

	/**
	 * Maximize Window
	 */
	@JidokaMethod(name = "IBM Maximize Window", description = "IBM3270Library:v2.0.0: Maximizes the window")
	public void maximizeWindow()
			throws JidokaFatalException, HllApiInvocationException {
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		ehll.maximizeWindow(sessionLetter);
		}

	/**
	 * Enter Credential
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
			ehll.sendKey(credential.getPassword());
		}
	}

	/**
	 * Action 'Find Text'
	 */
	@JidokaMethod(name = "IBM Find Text", description = "IBM3270Library:v2.0.0: Takes in a text string and returns a map of the text location with fields: row & column. Returns null if not found. Save into an Appian variable of type map, single. Example output is: {\"column\":\"40\",\"row\":\"1\"}")
	public HashMap<String,String> findText(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = TEXT_TO_LOCATE,
									id = TEXT_TO_LOCATE
							)
					}
			) SDKParameterMap parameters) throws HllApiInvocationException {
		String text = parameters.get(TEXT_TO_LOCATE).toString();
		int loc;
		try {
			loc = ehll.search(text,true);
		}catch (HllApiInvocationException e){
			if(e.getResponseCode()==24){
				return null;
			}
			throw e;
		}
//		server.debug("PS location is: "+loc);
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		EHll.RowColumn coords = ehll.convertPositionToRowCol(sessionLetter,loc);
		HashMap<String,String> result = new HashMap<>();
		result.put("row", String.valueOf(coords.getRow()));
		result.put("column", String.valueOf(coords.getCol()));
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
									name = ROW_NUMBER,
									id = ROW_NUMBER
							)
					}
			) SDKParameterMap parameters) throws HllApiInvocationException, IOException { ;
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
//		server.debug("Session is : "+sessionStatus.toString());
		int rowSize = sessionStatus.getRow();
		int colSize = sessionStatus.getColumn();
		int screenSize = rowSize*colSize;
//		although usually 1x, depending on session settings screenSize input can need to be 2x or 4x the value of row*col
//		starting support at base session settings and support for different configurations could be added later
		String screen = ehll.copyScreen(screenSize);
//		server.debug("Screen is: "+screen);
		int rowNum = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		int begin = colSize*(rowNum-1);
		int end = colSize*(rowNum);
		String rowText = screen.substring(begin,end);
//		server.debug("Row text is : "+rowText);
		return rowText;
	}

	/**
	 * Action 'Get Text at Coordinate'
	 */
	@JidokaMethod(name = "IBM Get Text at Coordinate", description = "IBM3270Library:v2.0.0: Takes in a coordinate (emulator row/col coordinate starting at 1) and length of text to return from that line, then returns the text located there")
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
			) SDKParameterMap parameters) throws HllApiInvocationException {
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		int rowSize = sessionStatus.getRow();
		int colSize = sessionStatus.getColumn();
		int screenSize = rowSize*colSize;
//		although usually 1x, depending on session settings screenSize input can need to be 2x or 4x the value of row*col
//		starting support at base session settings and support for different configurations could be added later
		String screen = ehll.copyScreen(screenSize);
//		server.debug("Screen is: "+screen);
		Integer rowNum = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		Integer colNum = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer length = Integer.valueOf(parameters.get(TEXT_LENGTH).toString());
		int begin = (colSize*(rowNum-1))+colNum-1;
		int end = (colSize*(rowNum-1))+colNum+length-1;
//		the end index might go beyond the screen space, so capping the value
		if (end > rowSize*colSize) {
			end = rowSize*colSize;
		}
		String coordinateText = screen.substring(begin, end);
		return coordinateText;
	}

	/**
	 * Action 'Get Field at Coordinates'
	 */
	@JidokaMethod(name = "IBM Get Field at Coordinates", description = "IBM3270Library:v2.0.0: Takes in a coordinate pair (emulator row/col coordinate starting at 1), then returns the value of the field that the coordinate is within (copy always starts at the beginning of the field)")
	public String getFieldAtCoordinates(
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
			) SDKParameterMap parameters) throws HllApiInvocationException {
		Integer rowNum = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		Integer colNum = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		int colSize = sessionStatus.getColumn();
		int maxFieldSize = colSize*4;
		//		although usually 1x, depending on session settings fieldSize input can need to be 4x the actual field size
//		setting to 4x the full page width since a field is unlikely to be longer than the full page width
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		int loc = ehll.convertRowColToCursorPosition(sessionLetter,rowNum,colNum);
		String field = ehll.copyField(loc,maxFieldSize);
//		server.debug("Field is: "+field);
		return field;
	}

	/**
	 * Action 'Bulk Get Field at Coordinates'.
	 * @return
	 */
	@JidokaMethod(name = "IBM Bulk Get Field at Coordinates", description = "IBM3270Library:v2.0.0: Gets fields in bulk at specified locations. Save this into an Appian variable of type map (multiple). Example output is [{\"field\":\"Smith\",\"column\":\"20\",\"row\":\"6\"},{\"field\":\"John\",\"column\":\"20\",\"row\":\"7\"}]")
	public List<HashMap> bulkGetFieldAtCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = BULK_COORDINATES,
									id = BULK_COORDINATES,
									instructionalText = "Expects JSON list object with fields 'row', and 'column' - for example a!toJson({{row:1,column:1},{row:5,column:5}})"
							)
					}
			) SDKParameterMap parameters) throws IOException, HllApiInvocationException {
		String json = parameters.get(BULK_COORDINATES).toString();
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		int colSize = sessionStatus.getColumn();
		int maxFieldSize = colSize*4;
//		although usually 1x, depending on session settings fieldSize input can need to be 4x the actual field size
//		setting to 4x the full page width since a field is unlikely to be longer than the full page width
		ObjectMapper objectMapper = new ObjectMapper();
		List<HashMap> hmapInput = objectMapper.readValue(json, List.class);
//		hmapInput is the JSON text input turned into a hashmap
		List<HashMap> hmapOutput = new ArrayList();
//		hmapOutput is a hashmap that should be saved into an Appian map variable
//		server.debug("Map is: "+hmapInput);
		for (int i = 0; i < hmapInput.size(); i++) {
//			server.debug("begin loop, Key: "+hmapInput.get(i).keySet() + " & Value: " + hmapInput.get(i).entrySet());
			Integer column = Integer.valueOf((int) hmapInput.get(i).get("column"));
			Integer row = Integer.valueOf((int) hmapInput.get(i).get("row"));
			int loc = ehll.convertRowColToCursorPosition(sessionLetter,row,column);
//			server.debug("PS position is: "+loc);
			String field = ehll.copyField(loc,maxFieldSize);
			HashMap<String, String> tempHmap = new HashMap<>();
			tempHmap.put("row",row.toString());
			tempHmap.put("column",column.toString());
			tempHmap.put("field",field);
			hmapOutput.add(i,tempHmap);
//			server.debug("end loop, Key: "+hmapInput.get(i).keySet() + " & Value: " + hmapInput.get(i).entrySet());
		}
		return hmapOutput;
	}

	/**
	 * Action 'Go to Text Position'.
	 *
	 */
	@JidokaMethod(name = "IBM Go to Text Position", description = "IBM3270Library:v2.0.0: Takes in a text string (case-sensitive) and goes to that position in emulator")
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
									name = GO_FIRST_OR_LAST_CHAR,
									id = GO_FIRST_OR_LAST_CHAR,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
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
			) SDKParameterMap parameters) throws HllApiInvocationException {
		String text = parameters.get(TEXT_TO_LOCATE).toString();
		EJidokaParameterBoolean isFirst = (EJidokaParameterBoolean) parameters.get(GO_FIRST_OR_LAST_CHAR);
		Integer colOff = Integer.valueOf(parameters.get(COLUMN_OFFSET).toString());
		Integer rowOff = Integer.valueOf(parameters.get(ROW_OFFSET).toString());
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		int loc;
		try {
			loc = ehll.search(text,true);
		}catch (HllApiInvocationException e){
			if(e.getResponseCode()==24){
				server.debug("Not moving cursor as text string not found: "+text);
				return;
			}
			throw e;
		}
//		server.debug("PS position 1 is: "+loc);
		EHll.RowColumn coords = ehll.convertPositionToRowCol(sessionLetter,loc);
		int colNum;
		if(isFirst == EJidokaParameterBoolean.YES){
			colNum = coords.getCol()+colOff;
//			goes to the first character of the found word
		} else{
			colNum = coords.getCol()+colOff+text.length()-1;
//			goes to the last character of the found word
		}
		int rowNum=coords.getRow()+rowOff;
		loc = ehll.convertRowColToCursorPosition(sessionLetter,rowNum,colNum);
//		server.debug("PS position 2 is: "+loc);
		ehll.setCursorPosition(loc);
	}

	/**
	 * Action 'Go to Coordinates'.
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
			) SDKParameterMap parameters) throws HllApiInvocationException {
		Integer column = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer row = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		int loc = ehll.convertRowColToCursorPosition(sessionLetter,row,column);
//		server.debug("PS position is: "+loc);
		ehll.setCursorPosition(loc);
	}

	/**
	 * Action 'Send Special Control Key'.
	 */
	@JidokaMethod(name = "IBM Send Special Control Key", description = "IBM3270Library:v2.0.0: Sends special control key using keyboard mnemonic, which is escape character (default is @) + symbol. Examples: send @E for enter, send @T for tab.")
	public void sendSpecialControlKey(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = CHARACTER_SEQUENCE,
									id = CHARACTER_SEQUENCE
							)
					}
			) SDKParameterMap parameters) throws HllApiInvocationException {
		String sequence = parameters.get(CHARACTER_SEQUENCE).toString();
		ehll.sendKey(sequence);
	}

	/**
	 * Prep send key text
	 *
	 * By default @ symbol is the escape character (Enter is @E, for example) so sending the @ symbol requires
	 * sending it as @@
	 *
	 * @param text  - text to send to emulator
	 * @param escapeChar  - current escape character
	 * @throws HllApiInvocationException - if response code is non 0
	 */
	private String prepSendKeyText(String text, char escapeChar)
			throws HllApiInvocationException {
		if (text.isEmpty()) {
			throw new HllApiInvocationException("Text string must be provided");
		}
		String newText = "";
//		moves through each character to write, checking if it is escape character, usually @ symbol
//		if it is, the single @ symbol is replaced with a double @@ symbol so it gets written correctly
//		for example smith@ex.com gets translated to smith@@ex.com so that smith@ex.com gets written in the emulator
//		this is necessary since @e will actually send a special command code, rather than the text
		for (int i = 0; i < text.length(); i++) {
			if(text.charAt(i) != escapeChar){
				newText = newText + text.charAt(i);
			} else{
				newText = newText + escapeChar + escapeChar;
			}
		}
		return newText;
	}

	/**
	 * Action 'Write Here'.
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
							),
							@JidokaNestedParameter(
									name = CLEAR_FIELD_FIRST,
									id = CLEAR_FIELD_FIRST,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
							)
					}
			) SDKParameterMap parameters) throws HllApiInvocationException {
		EJidokaParameterBoolean clearField = (EJidokaParameterBoolean) parameters.get(CLEAR_FIELD_FIRST);
		if(clearField == EJidokaParameterBoolean.YES){
			ehll.sendKey(escapeChar+"F");
//			this clears the field by sending @F to the emulator
		}
		String text = parameters.get(TEXT_TO_WRITE).toString();
		String prepText = prepSendKeyText(text,escapeChar);
		ehll.sendKey(prepText);
	}

	/**
	 * Action 'Write at Coordinates'.
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
									name = CLEAR_FIELD_FIRST,
									id = CLEAR_FIELD_FIRST,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
							)
					}
			) SDKParameterMap parameters) throws HllApiInvocationException {
		Integer column = Integer.valueOf(parameters.get(COLUMN_NUMBER).toString());
		Integer row = Integer.valueOf(parameters.get(ROW_NUMBER).toString());
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		int loc = ehll.convertRowColToCursorPosition(sessionLetter,row,column);
//		server.debug("PS position is: "+loc);
		EJidokaParameterBoolean clearField = (EJidokaParameterBoolean) parameters.get(CLEAR_FIELD_FIRST);
		if(clearField == EJidokaParameterBoolean.YES){
			ehll.sendKeyAtCoordinates(escapeChar+"F",loc);
//			this clears the field by sending @F to the emulator
		}
		String text = parameters.get(TEXT_TO_WRITE).toString();
		String prepText = prepSendKeyText(text,escapeChar);
		ehll.sendKeyAtCoordinates(prepText,loc);
	}

	/**
	 * Action 'Bulk Write at Coordinates'.
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
							),
							@JidokaNestedParameter(
									name = CLEAR_FIELD_FIRST,
									id = CLEAR_FIELD_FIRST,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
							)
					}
			) SDKParameterMap parameters) throws IOException, HllApiInvocationException {
		String json = parameters.get(BULK_TEXT_AND_COORDINATES).toString();
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		ObjectMapper objectMapper = new ObjectMapper();
		List<HashMap> hmapInput = objectMapper.readValue(json, List.class);
//		hmapInput is the JSON text input turned into a hashmap
//		server.debug("Map is: "+hmapInput);
		for (int i = 0; i < hmapInput.size(); i++) {
//			server.debug("begin loop, Key: "+hmapInput.get(i).keySet() + " & Value: " + hmapInput.get(i).entrySet());
			Integer column = Integer.valueOf((int) hmapInput.get(i).get("column"));
			Integer row = Integer.valueOf((int) hmapInput.get(i).get("row"));
			int loc = ehll.convertRowColToCursorPosition(sessionLetter,row,column);
//			server.debug("PS position is: "+loc);
			EJidokaParameterBoolean clearField = (EJidokaParameterBoolean) parameters.get(CLEAR_FIELD_FIRST);
			if(clearField == EJidokaParameterBoolean.YES){
				ehll.sendKeyAtCoordinates(escapeChar+"F",loc);
//				this clears the field by sending @F to the emulator
			}
			String text = hmapInput.get(i).get("text").toString();
			String prepText = prepSendKeyText(text,escapeChar);
			ehll.sendKeyAtCoordinates(prepText,loc);
//			server.debug("end loop, Key: "+hmapInput.get(i).keySet() + " & Value: " + hmapInput.get(i).entrySet());
		}
	}

	/**
	 * Action 'Write at Label'.
	 */
	@JidokaMethod(name = "IBM Write at Label", description = "IBM3270Library:v2.0.0: Writes text in emulator at specified label (case-sensitive) with optional offset")
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
									name = WRITE_FIRST_OR_LAST_CHAR,
									id = WRITE_FIRST_OR_LAST_CHAR,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
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
							),
							@JidokaNestedParameter(
									name = CLEAR_FIELD_FIRST,
									id = CLEAR_FIELD_FIRST,
									clazz = "com.novayre.jidoka.client.api.EJidokaParameterBoolean",
									type = EJidokaParameterType.ENUMERATOR,
									rendition = {EJidokaParameterRendition.OPTIONS_EXPAND_HORIZONTALLY},
									optionsService = EOptionsService.JIDOKA_PARAMETER_BOOLEAN
							)
					}
			) SDKParameterMap parameters) throws HllApiInvocationException {
		String textToSearch = parameters.get(FIELD_LABEL).toString();
		Integer colOff = Integer.valueOf(parameters.get(COLUMN_OFFSET).toString());
		Integer rowOff = Integer.valueOf(parameters.get(ROW_OFFSET).toString());
		EHll.SessionStatus sessionStatus = ehll.querySessionStatus();
		char sessionChar = sessionStatus.getShortSessionId();
		String sessionLetter = Character.toString(sessionChar);
		int loc;
		try {
			loc = ehll.search(textToSearch,true);
		}catch (HllApiInvocationException e){
			if(e.getResponseCode()==24){
				server.debug("Not writing as text string not found: "+textToSearch);
				return;
			}
			throw e;
		}
//		server.debug("PS position 1 is: "+loc);
		EHll.RowColumn coords = ehll.convertPositionToRowCol(sessionLetter,loc);
//		server.debug("Coords are: "+coords.getRow()+","+coords.getCol());
		int colNum;
		EJidokaParameterBoolean isFirst = (EJidokaParameterBoolean) parameters.get(WRITE_FIRST_OR_LAST_CHAR);
		if(isFirst == EJidokaParameterBoolean.YES){
			colNum = coords.getCol()+colOff;
//			goes to the first character of the found label
		} else{
			colNum = coords.getCol()+colOff+textToSearch.length()-1;
//			goes to the last character of the found label
		}
		int rowNum=coords.getRow()+rowOff;
//		server.debug("Updated coords are: "+rowNum+","+colNum);
		loc = ehll.convertRowColToCursorPosition(sessionLetter,rowNum,colNum);
//		server.debug("PS position 2 is: "+loc);
		EJidokaParameterBoolean clearField = (EJidokaParameterBoolean) parameters.get(CLEAR_FIELD_FIRST);
		if(clearField == EJidokaParameterBoolean.YES){
			ehll.sendKeyAtCoordinates(escapeChar+"F",loc);
//			this clears the field by sending @F to the emulator
		}
		String text = parameters.get(TEXT_TO_WRITE).toString();
		String prepText = prepSendKeyText(text,escapeChar);
		ehll.sendKeyAtCoordinates(prepText,loc);
	}
}
