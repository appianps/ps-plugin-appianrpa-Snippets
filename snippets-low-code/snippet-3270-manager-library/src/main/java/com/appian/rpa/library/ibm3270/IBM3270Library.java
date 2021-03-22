package com.appian.rpa.library.ibm3270;

import com.appian.rpa.library.ibm3270.clients.PCOMMEmulatorCommons;
import com.appian.rpa.library.ibm3270.clients.WC3270EmulatorCommons;
import com.novayre.jidoka.client.api.*;
import com.novayre.jidoka.client.api.annotations.FieldLink;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Nano
public class IBM3270Library implements INano {

	private static final String TEXT_TO_LOCATE = "Text to Locate";
	private static final String WINDOW_TITLE_XPATH = "Window Title Xpath";
	private static final String EMULATOR_TYPE = "Emulator Type";
	private static final String X_COORDINATE = "X Coordinate";
	private static final String Y_COORDINATE = "Y Coordinate";
	private static final String TEXT_TO_WRITE = "Text to Write";
	private static final String FIELD_LABEL = "Field Label";
	private static final String X_OFFSET = "X Offset";
	private static final String Y_OFFSET = "Y Offset";

	/** Server instance */
	private IJidokaServer<Serializable> server;
	/** Client Module instance */
	protected IClient client;

	@FieldLink("::robot")
	private IRobot robot;

	IBM3270Commons ibm3270Commons;

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
	@JidokaMethod(name = "Set Emulator", description ="IBM3270Library:v1.0.0: Sets the correct emulator type (PCOMM or W3270)")
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
			throw new JidokaFatalException("Only [WC3270] , [PCOMM] or [PARTENON] emulTypes are valid");
		}
	}

	/**
	 * Action 'Find Text'
	 */
	@JidokaMethod(name = "Find Text", description = "IBM3270Library:v1.0.0: Takes in a text string and returns the xy location (integer array) in the emulator")
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
	 * Action 'Go to Text Position'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Go to Text Position", description ="IBM3270Library:v1.0.0: Takes in a text string and goes to that position in emulator")
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
		ibm3270Commons.moveToCoordinates(parameters.get(TEXT_TO_LOCATE).toString(), 0, 0, 3);
	}

	/**
	 * Action 'Go to Text Position'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Go to Coordinates", description ="IBM3270Library:v1.0.0: Takes in a x y int list coordinates and goes to that position in emulator")
	public void goToCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = X_COORDINATE,
									id = X_COORDINATE
							),
							@JidokaNestedParameter(
									name = Y_COORDINATE,
									id = Y_COORDINATE
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		ibm3270Commons.moveToCoordinates(Integer.valueOf(parameters.get(X_COORDINATE).toString()),Integer.valueOf(parameters.get(Y_COORDINATE).toString()));
	}

	/**
	 * Action 'Write Here'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Write Here", description ="IBM3270Library:v1.0.0: Writes text in emulator at current location (handles slow typing & special characters")
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
		ibm3270Commons.write(parameters.get(TEXT_TO_WRITE).toString());
	}

	/**
	 * Action 'Write at Coordinates'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Write at Coordinates", description ="IBM3270Library:v1.0.0: Writes text in emulator at specified location (handles slow typing & special characters")
	public void writeAtCoordinates(
			@JidokaParameter(
					name = "Nested parameters",
					type = EJidokaParameterType.NESTED,
					nestedParameters = {
							@JidokaNestedParameter(
									name = X_COORDINATE,
									id = X_COORDINATE
							),
							@JidokaNestedParameter(
									name = Y_COORDINATE,
									id = Y_COORDINATE
							),
									@JidokaNestedParameter(
									name = TEXT_TO_WRITE,
									id = TEXT_TO_WRITE
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		ibm3270Commons.moveToCoordinates(Integer.valueOf(parameters.get(X_COORDINATE).toString()),Integer.valueOf(parameters.get(Y_COORDINATE).toString()));
		ibm3270Commons.write(parameters.get(TEXT_TO_WRITE).toString());
	}

	/**
	 * Action 'Write at Label'.
	 *
	 * @throws JidokaException
	 */
	@JidokaMethod(name = "Write at Label", description ="IBM3270Library:v1.0.0: Writes text in emulator at specified label with optional offset (handles slow typing & special characters")
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
									name = X_OFFSET,
									id = X_OFFSET
							),
							@JidokaNestedParameter(
									name = Y_OFFSET,
									id = Y_OFFSET
							),
							@JidokaNestedParameter(
									name = TEXT_TO_WRITE,
									id = TEXT_TO_WRITE
							)
					}
			) SDKParameterMap parameters) throws JidokaException {
		ibm3270Commons.moveToCoordinates(parameters.get(FIELD_LABEL).toString(),Integer.valueOf(parameters.get(X_OFFSET).toString()),Integer.valueOf(parameters.get(Y_OFFSET).toString()),3);
		ibm3270Commons.write(parameters.get(TEXT_TO_WRITE).toString());
	}

}
