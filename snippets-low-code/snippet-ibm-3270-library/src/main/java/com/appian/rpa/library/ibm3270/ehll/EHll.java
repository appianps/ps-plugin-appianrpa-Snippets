package com.appian.rpa.library.ibm3270.ehll;

/**
 * Interact with mainframe application through ehll dll
 */
public interface EHll {

    /**
     * Creates EHll instance
     *
     * @param pathToDll - path to dll
     * @param dllName - dll name
     * @return Default EHll instance
     */
    static EHll create(String pathToDll, String dllName) {
        return new EHllImpl(pathToDll, dllName);
    }

    /**
     * Creates EHll instance
     *
     * @param pathToDll - path to dll
     * @param dllName - dll name
     * @param encoding - valid character encoding
     * @return Default EHll instance
     */
    static EHll create(String pathToDll, String dllName, String encoding) {
        return new EHllImpl(pathToDll, dllName, encoding);
    }

    /**
     * Connect to a session
     *
     * Return codes:
     * <p>
     *  0 The function was successful.
     *  1 An invalid host PS short session ID was entered.
     *  4 The host PS was busy.
     *  5 The host PS was locked.
     *  9 A system error occurred.
     *  11 The requested PS was in use by another application.
     * </p>
     *
     * @param sessionName - name of the session to connect to
     */
    void connect(String sessionName) throws HllApiInvocationException;

    /**
     * Disconnects an application from its currently connected PS and releases
     * any PS keyboard reservation, but does not reset session parameters to defaults.
     * After calling this function, the application cannot call functions that depend on
     * connection to a PS.
     *
     * Return Code
     * <p>
     * 0 The function was successful.
     * 1 The application was not connected with a host PS.
     * 9 A system error occurred.
     * </p>
     *
     */
    void disconnect() throws HllApiInvocationException;

    /**
     * Copies the presentation space to a string
     *
     * Applicable values set in {@link EHll#setSessionParams(String)}
     * <p>
     * NOATTRB (default)
     * Attribute bytes and other characters not displayable in ASCII are translated into
     * blanks.
     * ATTRB
     * Attribute bytes and other characters not displayable in ASCII are not translated.
     * EAB
     * Extended Attribute Bytes (EABs) are copied. Two characters are placed in the
     * application data string for each one that appears in the PS. The EAB is the second
     * character. To accommodate this, the application program must allocate a data string
     * that is twice the number of displayable characters to be copied from the presentation
     * space of the current display model.
     * NOEAB (default)
     * EABs are not copied.
     * XLATE
     * 3270 display codes are translated into ASCII and EABs are translated to CGA text
     * mode attributes.
     * NOXLATE (default)
     * 3270 display codes are copied and EABs are not translated.
     * DISPLAY (default)
     * Non-display fields are copied to the target buffer in the same manner as the display
     * fields.
     * NODISPLAY
     * Non-display fields are copied to the target buffer as a string of nulls. This allows an
     * application program to display the copied buffer in the presentation window without
     * displaying confidential information, such as passwords.
     * </p>
     *
     * Data Model (expected bytes)
     * <p>
     * model           bytes
     *  2       1920 (3840 with EABs)
     *  3       2560 (5120 with EABs)
     *  4       3440 (6880 with EABs)
     *  5       3564 (7128 with EABs)
     * </p>
     *
     * <p>
     * 0 Success; text from the PS has been copied to data string.
     * 1 The application was not connected with a host PS.
     * 4 The copy was successful, but the PS is busy.
     * 5 The copy was successful, but the keyboard is locked.
     * 9 A system error occurred.
     * </p>
     *
     * @return - screen as a string
     */
    String copyScreen(int screenSize) throws HllApiInvocationException;

    /**
     * Send a key to the current cursor position
     *
     * Return codes:
     * <p>
     * 0 The function was successful.
     * 1 The application was not connected with a host PS.
     * 2 An incorrect parameter was entered.
     * 4 Host session was busy; not all keystrokes were sent.
     * 5 Host session was inhibited, not all keystrokes were sent.
     * 9 A system error occurred.
     * </p>
     *
     * @param key - A string of maximum 255 characters
     */
    void sendKey(String key) throws HllApiInvocationException;

    /**
     * Send a key to the provided cursor position
     *
     * Return codes:
     * <p>
     * 0 The function was successful.
     * 1 The application was not connected with a host PS.
     * 2 An incorrect parameter was entered.
     * 4 Host session was busy; not all keystrokes were sent.
     * 5 Host session was inhibited, not all keystrokes were sent.
     * 7 An invalid PS position was specified.
     * 9 A system error occurred.
     * </p>
     *
     * @param key - A string of maximum 255 characters
     * @param cursorPosition - cursor position to send key too
     */
    void sendKeyAtCoordinates(String key, int cursorPosition) throws HllApiInvocationException;

    /**
     * Sets session parameters using comma separated string
     *
     * Valid Parameters
     * <p>
     * Copy parameters
     * ATTRB -  characters that cannot be translated to displayable ASCII characters are not translated
     * NOATTRB (default) Characters that cannot be translated to displayable ASCII characters are translated to blanks (0x20).
     * EAB - Extended Attribute Bytes are copied along with data.
     * NOEAB (default) - EABs are not copied (data only).
     * STRLEN (default) - String parameters are passed with an explicit length (specified in Data length).
     * STREOT - String parameters are passed with the character specified in the EOT session parameter denoting the string end.
     * EOT - Character denoting the end of a string when the STREOT session parameter has been set. Null (/0) is the default value.
     * XLATE - Copied Extended Attribute Bytes are translated to CGA color codes.
     * NOXLATE (default) -Copied Extended Attribute Bytes are returned without translation.
     * DISPLAY (default) -Non-display fields are copied to the target buffer in the same manner as the display fields.
     * NODISPLAY - Non-display fields are copied to the target buffer as nulls.
     * BLANK (default) -Null characters are converted to spaces (returned as X'20').
     * NOBLANK -Null characters are not converted (returned as X'00').
     *
     * Connect parameters
     * The following session parameters affect Function 1, “Connect Presentation Space,”
     * and Function 2, “Disconnect Presentation Space.”
     * CONLOG (default)
     *  When Function 1, “Connect Presentation Space,” is called, the emulator session
     *  corresponding to the target PS does not become the active application. The calling
     *  application remains active. Likewise, when Function 2, “Disconnect Presentation
     *  Space,” is called, the calling application remains active.
     * CONPHYS
     *  Calling Function 1, “Connect Presentation Space,” makes the emulator session
     *  corresponding to the target PS the active application (does a physical connect). Note
     *  that this parameter is honored only when there is host access software attached to
     *  the session. During Function 2, “Disconnect Presentation Space,” the host access
     *  software becomes the active application.
     * WRITE_SUPER (default)
     *  This parameter is set by a client application program that requires write access and
     *  allows only supervisory applications to connect to its PS.
     * WRITE_WRITE
     *  This parameter is set by a client application program that requires write access and
     *  allows other applications that have predictable behavior to connect to its PS.
     * WRITE_READ
     *  This parameter is set by a client application program that requires write access and
     *  allows other applications to use read-only functions on its PS.
     * WRITE_NONE
     *  This parameter is set by a client application program that requires exclusive access
     *  to its PS. No other applications will have access to its PS.
     * SUPER_WRITE
     *  This parameter is set by a supervisory client application program that allows
     *  applications with write access to share the connected PS. The client application
     *  program setting this parameter will not cause errors for other applications, but will
     *  provide only supervisory-type functions.
     * WRITE_READ
     *  This parameter is set by a client application program that requires read-only access
     *  and allows other applications that perform read-only functions to connect to its PS.
     * KEY$nnnnnnnn
     *  This parameter allows the client application program to restrict sharing the PS. The
     *  keyword must be exactly 8 bytes long.
     * NOKEY (default)
     *  This parameter allows the client application program to be compatible with existing
     *  applications that do not specify the KEY parameter.
     *
     * Esc/Reset parameters
     * The following session parameters affect Function 3, “Send Key,” and Function
     * 51,“Get Key.”
     * ESC= char
     *  Specifies the escape character for keystroke mnemonics (“@” is the default). Blank is
     *  not a valid escape value.
     * AUTORESET (default)
     *  Attempts to reset all inhibited conditions by adding the prefix RESET to all keystroke
     *  strings sent using Function 3, “Send Key. ”
     * NORESET
     *  Does not add RESET prefix to function 3 key strings.
     *
     * Search parameters
     * The following session parameters affect all search functions.
     * SRCHALL (default) - Scans the entire PS or field.
     * SRCHFROM - Starts the scan from a specified location in the PS or field.
     * SCRCHFRWD (default) - Performs the scan in an ascending direction.
     * SRCHBKWD -Performs the scan in a descending direction through the PS or field.
     *
     * Wait parameters
     * The following session parameters affect Function 4, “Wait,” and Function 51, “Get
     * Key.”
     * TWAIT (default)
     * For Function 4, “Wait,” TWAIT waits up to a minute before timing out on XCLOCK or
     * XSYSTEM.
     * For Function 51, “Get Key,” TWAIT does not return control to the client application
     * program until it has intercepted a key (a normal or AID key, based on the option
     * code specified under Function 50, “Start Keystroke Intercept” ).
     * LWAIT
     * For Function 4, “Wait,” LWAIT waits until XCLOCK / XSYSTEM clears. This option is
     * not recommended because XSYSTEM or permanent XCLOCK will prevent control
     * being returned to the application.
     * For Function 51, “Get Key,” LWAIT does not return control to your application until it
     * has intercepted a key. The intercepted key could be a normal or AID key, based on
     * the option specified under Function 50, “Start Keystroke Intercept.”
     * NWAIT
     * For Function 4, “Wait,” NWAIT checks status and returns immediately (no wait).
     * For Function 51, “Get Key,” NWAIT returns code 25 (keystroke not available) if
     * nothing matching the option specified under Function 50, “Start Keystroke
     * Intercept,” is queued.
     * Pause parameters
     * The following session parameters affect Function 18, “Pause,” determining the type
     * of pause to perform.
     * NOTE: An application can make multiple Function 23 calls, and an event satisfying
     * any of the calls will interrupt the pause.
     * FPAUSE (default)
     * Full-duration pause. Control returns to the calling application when the number of
     * half-second intervals specified in the Function 18 call have elapsed.
     * IPAUSE
     * EHLLAPI LANGUAGE REFERENCE
     * Prepared by Attachmate Technical Support 30
     * Interruptible pause; Control returns to the calling application when a system even
     * specified in a preceding Function 23, “Start Host Notification,” call has occurred, or
     * the number of half-second intervals specified in the Function 18 call have elapsed.
     * Time parameters
     * The following session parameters affect Function 90, “Send File,” and Function 91,
     * “Receive File.”
     * NOQUIET (default)
     * Displays SEND and RECEIVE messages showing progress of the file transfer.
     * QUIET
     * Does not display SEND and RECEIVE messages.
     * TIMEOUT=char
     * Specifies how many 30-second cycles shall elapse before CTRL BREAK is issued to
     * terminate an in-progress file transfer. (Blank is not accepted.)
     * Character Minutes
     * 1 0.5
     * 2 1.0
     * 3 1.5
     * 4 2.0
     * 5 2.5
     * 6 3.0
     * 7 3.5
     * 8 4.0
     * 9 4.5
     * J 5.0
     * K 5.5
     * L 6.0
     * M 6.5
     * N 7.0
     * OIA parameters
     * The following session parameters affect Function 13, “Copy OIA,” and specify the
     * format for the data returned by the function.
     * OLDOIA (default)
     * OIA data is returned in EBCDIC. Since OIA data is always returned in ASCII format in
     * 5250 support, OLDOIA is accepted but ignored.
     * NEWOIA
     * OIA data is returned in ASCII format.
     * PS size parameters
     * The following session parameters affect Function 10, “Query Sessions.”
     * NOCFGSIZE
     * Function 10 returns the current size of the connected PS.
     * CFGSIZE (default)
     * EHLLAPI LANGUAGE REFERENCE
     * Prepared by Attachmate Technical Support 31
     * Function 10 ignores any override of the PS by the host and returns the configured
     * size of the PS.
     * Blank parameters
     * The following session parameters affect Function 5, “Copy Presentation Space”;
     * Function 8, “Copy Presentation Space to String”; and Function 34, “Copy Field to
     * String.”
     * BLANK (default)
     * Null characters are converted to spaces (returned as X'20').
     * NOBLANK
     * Null characters are not converted (returned as X'00')
     * </p>
     *
     * Response Code:
     * <p>
     *  0 The function was successful.
     *  2 One or more parameter names were not recognized; all recognized parameters were accepted.
     *  9 A system error occurred.
     * </p>
     *
     * @param params - Session parameters separated by commas
     */
    void setSessionParams(String params) throws HllApiInvocationException;

    /**
     * Sets the current cursor position
     *
     * Return codes:
     * <p>
     * 0 The function was successful
     * 1 The application was not connected with a host presentation space
     * 4 The presentation space is busy
     * 7 An invalid PS position was specified.
     * 9 A system error occurred.
     * </p>
     *
     * @param cursorPosition - single number representing the cursor position
     */
    void setCursorPosition(int cursorPosition) throws HllApiInvocationException;


    /**
     * Scans the entire current presentation space for a specified string
     * <p>
     * 0 The function was successful (the specified text was found).
     * 1 The application was not connected with a host PS.
     * 2 An incorrect parameter was entered.
     * 7 An invalid PS position was specified for beginning the search.
     * 9 A system error occurred.
     * 24 The specified text was not found.
     * </p>
     * @param textToSearch - the text to search
     * @param searchForwards - if true, search will find the first occurrence of the specified string from the cursorPosition
     *                          if false, search will find the last occurrence of the specified string from the cursorPosition
     * @return
     */
    int search(String textToSearch, boolean searchForwards) throws HllApiInvocationException;


    /**
     * Scans the current presentation space at the given location for a specified string
     * <p>
     * 0 The function was successful (the specified text was found).
     * 1 The application was not connected with a host PS.
     * 2 An incorrect parameter was entered.
     * 7 An invalid PS position was specified for beginning the search.
     * 9 A system error occurred.
     * 24 The specified text was not found.
     * </p>
     * @param textToSearch - the text to search
     * @param searchForwards - if true, search will find the first occurrence of the specified string from the cursorPosition
     *                         if false, search will find the last occurrence of the specified string from the cursorPosition
     * @param cursorPosition - single number representing the cursor position
     * @return
     */
    int search(String textToSearch, boolean searchForwards, int cursorPosition) throws HllApiInvocationException;

    /**
     * Connects to the window, Maximizes the window and Disconnects from the window
     *
     * Byte Description
     * <p>
     * 1 A 1-character session short name.
     * 2 X01 – Set status
     * 3–4 The status set bits. The following codes are valid:
     * • X’0001’ — Change window size
     * • X’0002’ — Move window
     * • X’0004’ — ZORDER window replacement
     * • X’0008’ — Set window to visible
     * • X’0010’ — Set window to invisible
     * • X’0080’ — Activate window
     * • X’0100’ — Deactivate window
     * • X’0400’ — Minimize window
     * • X’0800’ — Maximize window
     * • X’1000’ — Restore window
     * 5–6 The X-window position coordinate in pixels. (These bytes are ignored if
     * the move option is not set).
     * 7–8 The Y-window position coordinate in pixels. (These bytes are ignored if
     * the move option is not set).
     * 9–10 The X-window size in pixels. (These bytes are ignored if the size option is not set).
     * 11–12 The Y-window size in pixels. (These bytes are ignored if the size option is not set).
     * 13–16 The window handle for relative window placement. (These bytes are
     * ignored if the ZORDER option is not set.)
     * • X’00000003’ — Place window in front of siblings
     * • X’00000004’ — Place window behind siblings
     * </p>
     *
     * Return Codes
     * <p>
     * 0 The function was successful.
     * 1 An invalid session short name was specified.
     * 2 A parameter error was detected.
     * 9 A system error occurred.
     * 12 The host session was stopped.
     * </p>
     * @param shortSessionName - character name of the session
     */
    void maximizeWindow(char shortSessionName) throws HllApiInvocationException;

    /**
     * Converts an absolute cursor position on the presentation space to a RowColumn object
     * Return codes are:
     * <p>
     *     * 0 An invalid PS position or column was specified.
     *     * >0 The PS position or column number, depending on the type of
     *          conversion being performed.
     *     * 9998 An invalid session short name was specified.
     *     * 9999 Second character in data string was not an uppercase “P” or “R.”
     * </p>
     *
     * @param shortSessionName - short name of the session
     * @param cursorPosition - current absolute cursor position
     * @return absolute position as row, column
     * @throws HllApiInvocationException - when api invocation returns invalid code
     */
    RowColumn convertPositionToRowCol(char shortSessionName, int cursorPosition) throws HllApiInvocationException;

    /**
     * Converts rows and columns to absolute cursor position on the presentation space
     *
     * Response Code
     * <p>
     *     0 An invalid PS position or column was specified.
     *     >0 The PS position or column number, depending on the type of
     *     conversion being performed.
     *     9998 An invalid session short name was specified.
     *     9999 Second character in data string was not an uppercase “P” or “R.”
     * </p>
     * @param shortSessionName - character session name
     * @param row - row
     * @param col - col
     * @return absolute position
     * @throws HllApiInvocationException - response code is invalid
     */
    int convertRowColToCursorPosition(char shortSessionName, int row, int col) throws HllApiInvocationException;

    /**
     * Used to invoke functions not defined in this API
     *
     * Prefer using functions defined in API. For functions not implemented here this generic invoke can be used
     *
     * @param function - function number to invoke
     * @param data - value to send
     * @param dataLength - most of the time will be length of data array, in some operations this can mean something else
     * @param cursorPosition - position of cursor, in some operations this can mean something else
     * @return All values returned by invoking dll
     * @throws HllApiInvocationException - if response code is not 0
     */
    HllApiValue invoke(int function, byte[] data, int dataLength, int cursorPosition) throws HllApiInvocationException;

    class RowColumn {
        private final int row;
        private final int col;

        public RowColumn(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }
}
