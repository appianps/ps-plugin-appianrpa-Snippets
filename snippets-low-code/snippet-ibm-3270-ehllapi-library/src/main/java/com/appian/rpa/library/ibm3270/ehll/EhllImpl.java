package com.appian.rpa.library.ibm3270.ehll;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

class EhllImpl implements Ehll {

    private static final String SEARCH_ALL_PARAM = "SRCHALL";

    private static final String SEARCH_FORWARD_PARAM = "SRCHFRWD";

    private static final String SEARCH_BACKWARD_PARAM = "SRCHBKWD";

    private static final String SEARCH_FROM_PARAM = "SRCHFROM";

    private static final int EMPTY = 0x00;

    private final EhllApi ehllApi;
    public static final Set<Integer> INVALID_POSITION_CONVERSION_CODES = new HashSet<Integer>() {{
        add(0);
        add(9998);
        add(9999);
    }};

    /**
     * Create EHll
     *
     * @param pathToDlls - path to dll
     * @param dllName    - name of dll
     */
    EhllImpl(String pathToDlls, String dllName) {
        System.setProperty("jna.library.path", pathToDlls);
        ehllApi = Native.loadLibrary(dllName, EhllApi.class, W32APIOptions.DEFAULT_OPTIONS);
    }

    /**
     * Create EHll
     *
     * @param pathToDlls - path to dll
     * @param dllName    - name of dll
     * @param encoding   - must be a valid characterset
     */
    EhllImpl(String pathToDlls, String dllName, String encoding) {
        System.setProperty("jna.library.path", pathToDlls);
        System.setProperty("jna.encoding", encoding);
        ehllApi = Native.loadLibrary(dllName, EhllApi.class, W32APIOptions.DEFAULT_OPTIONS);
    }

    @Override
    public void connect(String sessionName) throws HllApiInvocationException {
        invokeHllApi(EhllApi.HA_CONNECT_PS, sessionName, 0);
    }

    @Override
    public void disconnect() throws HllApiInvocationException {
        invokeHllApi(EhllApi.HA_DISCONNECT_PS, "ignored", 0);
    }

    @Override
    public String copyScreen(int screenSize) throws HllApiInvocationException {
        HllApiValue hllApiValue = invokeHllApi(EhllApi.HA_COPY_PS, new byte[screenSize], 0);
        return hllApiValue.getDataString();
    }

    @Override
    public String copyField(int cursorPosition, int fieldSize) throws HllApiInvocationException {
        HllApiValue hllApiValue = invokeHllApi(EhllApi.HA_COPY_FIELD_TO_STRING, new byte[fieldSize], cursorPosition);
        return hllApiValue.getDataString();
    }

    @Override
    public void sendKey(String key) throws HllApiInvocationException {
        invokeHllApi(EhllApi.HA_SENDKEY, key, 0);
    }

    @Override
    public void sendKeyAtCoordinates(String key, int cursorPosition) throws HllApiInvocationException {
        setCursorPosition(cursorPosition);
        invokeHllApi(EhllApi.HA_SENDKEY, key, 0);
    }

    @Override
    public void setSessionParams(String params) throws HllApiInvocationException {
        invokeHllApi(EhllApi.HA_SET_SESSION_PARMS, params, 0);
    }

    @Override
    public int search(String textToSearch, boolean searchForwards) throws HllApiInvocationException {
        setSessionParams(SEARCH_ALL_PARAM);
        return searchInternal(textToSearch, searchForwards, 0);
    }

    @Override
    public int search(String textToSearch, boolean searchForwards, int cursorPosition)
        throws HllApiInvocationException {
        setSessionParams(SEARCH_FROM_PARAM);
        return searchInternal(textToSearch, searchForwards, cursorPosition);
    }

    private int searchInternal(String textToSearch, boolean firstOccurrence, int cursorPosition)
        throws HllApiInvocationException {
        if (firstOccurrence) {
            setSessionParams(SEARCH_FORWARD_PARAM);
        } else {
            setSessionParams(SEARCH_BACKWARD_PARAM);
        }
        HllApiValue hllApiValue = invokeHllApi(EhllApi.HA_SEARCH_PS, textToSearch, cursorPosition);
        return hllApiValue.getDataLength();
    }

    @Override
    public void setCursorPosition(int cursorPosition) throws HllApiInvocationException {
        invokeHllApi(EhllApi.HA_SET_CURSOR, "ignored", cursorPosition);
    }

    @Override
    public void maximizeWindow(String shortSessionName) throws HllApiInvocationException {
        //Connect to window service
        invokeHllApi(EhllApi.HA_CONNECT_WINDOW_SERVICES, String.valueOf(shortSessionName), 0);

        short x = 0x0800;
        byte[] data = new byte[2];
        data[0] = (byte)x;
        data[1] = (byte)(x >>> 8);

        byte[] command = new byte[] {getSessionNameAsByte(shortSessionName),       //short session name
            EMPTY, EMPTY, EMPTY, 0x01, EMPTY, data[0], data[1], EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
            EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};

        //Maximize window
        invokeHllApi(EhllApi.WINDOW_STATUS, command, 0);

        //Disconnect from window services
        invokeHllApi(EhllApi.HA_DISCONNECT_WINDOW_SERVICES, String.valueOf(shortSessionName), 0);
    }

    @Override
    public RowColumn convertPositionToRowCol(String shortSessionName, int cursorPosition)
        throws HllApiInvocationException {
        byte[] command = new byte[] {getSessionNameAsByte(shortSessionName), 0x00, 0x00, 0x00, (byte)'P',
            0x00, 0x00, 0x00,};

        HllApiValue hllApiValue = invokeHllApi(EhllApi.HA_CONVERT_POS_ROW_COL, command, command.length,
            cursorPosition, INVALID_POSITION_CONVERSION_CODES, false);
        return new RowColumn(hllApiValue.getDataLength(), hllApiValue.getResponseCode());
    }

    @Override
    public int convertRowColToCursorPosition(String shortSessionName, int row, int col)
        throws HllApiInvocationException {
        byte[] command = new byte[] {getSessionNameAsByte(shortSessionName), 0x00, 0x00, 0x00, (byte)'R',
            0x00, 0x00, 0x00,};

        //If response code is one of 0, 9998 or 9999 throw an exception. All other codes represent position
        HllApiValue hllApiValue = invokeHllApi(EhllApi.HA_CONVERT_POS_ROW_COL, command, row, col,
            INVALID_POSITION_CONVERSION_CODES, false);

        return hllApiValue.getResponseCode();
    }

    @Override
    public SessionStatus querySessionStatus() throws HllApiInvocationException {
        HllApiValue hllApiValue = invokeHllApi(22, new byte[20], 0);
        byte[] sessionBytes = hllApiValue.getBytes();
        char sessionChar = (char)sessionBytes[0];
        String string = new String(Arrays.copyOfRange(sessionBytes, 4, 11));
        char sessionType = (char)sessionBytes[12];
        byte booleanByte = sessionBytes[13];
        boolean isExtended = isBitTrue(booleanByte, 0);
        boolean doesSupportSymbols = isBitTrue(booleanByte, 1);
        int row = sessionBytes[14] ^ sessionBytes[15] << 8;
        int col = sessionBytes[16] ^ sessionBytes[17] << 8;
        int page = sessionBytes[18] ^ sessionBytes[19] << 8;
        return new SessionStatus(sessionChar, string, sessionType, isExtended, doesSupportSymbols, row, col,
            page);
    }

    /**
     * Utility for checking bit status
     */
    private static boolean isBitTrue(byte b, int position) {
        return ((b >> position) & 1) != 0;
    }

    @Override
    public HllApiValue invoke(int function, byte[] data, int dataLength, int cursorPosition)
        throws HllApiInvocationException {
        return invokeHllApi(function, data, dataLength, cursorPosition);
    }

    /**
     * Interacts with EHLL java native library
     *
     * @param functionNumber - function number to call
     * @param input          - string to pass to java native library
     * @param cursorPosition - cursor position in the target presentation space
     * @return Collection with modified values
     * @throws HllApiInvocationException - when response code != 0 is returned
     */
    private HllApiValue invokeHllApi(int functionNumber, String input, int cursorPosition)
        throws HllApiInvocationException {
        input = input == null ? "ignored" : input;
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return invokeHllApi(functionNumber, data, cursorPosition);
    }

    /**
     * Interact with EHLL java native library
     *
     * @param functionNumber - function number to call
     * @param data           - byte[] to pass to java native library. May be filled
     * @param cursorPosition - cursor position in the target presentation space
     * @return Collection with modified values
     * @throws HllApiInvocationException - when response code != 0 is returned
     */
    private HllApiValue invokeHllApi(int functionNumber, byte[] data, int cursorPosition)
        throws HllApiInvocationException {
        int inputDtaLength = data.length;

        return invokeHllApi(functionNumber, data, inputDtaLength, cursorPosition);
    }

    /**
     * Interact with EHLL java native library
     *
     * @param functionNumber - function number to call
     * @param data           - byte[] to pass to java native library. May be filled
     * @param inputDtaLength - depending on function has different meanings
     * @param cursorPosition - cursor position in the target presentation space
     * @return API value
     * @throws HllApiInvocationException
     */
    private HllApiValue invokeHllApi(int functionNumber, byte[] data, int inputDtaLength, int cursorPosition)
        throws HllApiInvocationException {
        Set<Integer> set = new HashSet<>();
        set.add(0);
        return invokeHllApi(functionNumber, data, inputDtaLength, cursorPosition, set, true);
    }

    private HllApiValue invokeHllApi(
        int functionNumber,
        byte[] data,
        int inputDtaLength,
        int cursorPosition,
        Set<Integer> responseCodes,
        boolean inclusive) throws HllApiInvocationException {
        Pointer dtaPtr = new Memory(data.length);
        dtaPtr.write(0, data, 0, data.length);
        IntByReference funcIntPtr = new IntByReference(functionNumber);
        IntByReference dtaLenPtr = new IntByReference(inputDtaLength);
        IntByReference posIntPtr = new IntByReference(cursorPosition);

        ehllApi.hllapi(funcIntPtr, dtaPtr, dtaLenPtr, posIntPtr);

        // posIntPtr is filled with the response code
        int responseCode = posIntPtr.getValue();
        checkResponseCode(responseCode, responseCodes, inclusive);

        //Make sure to set the default string encoding to be appropriate using jna.encoding
        return new HllApiValue(funcIntPtr.getValue(), responseCode, dtaLenPtr.getValue(), dtaPtr.getString(0),
            dtaPtr.getByteArray(0, data.length));
    }

    /**
     * Handle response code throwing exception if not 0
     *
     * @param responseCode  - dll invocation response code
     * @param responseCodes - response codes
     * @param inclusive     - only accept response codes in the set else only accept codes out of the set
     * @throws HllApiInvocationException - if response code is non 0
     */
    private void checkResponseCode(int responseCode, Set<Integer> responseCodes, boolean inclusive)
        throws HllApiInvocationException {
        if (inclusive) {
            if (!responseCodes.contains(responseCode)) {
                throw new HllApiInvocationException(responseCode);
            }
        } else {
            if (responseCodes.contains(responseCode)) {
                throw new HllApiInvocationException(responseCode);
            }
        }
    }

    /**
     * Returns a single character representation of a session name
     *
     * @param sessionName - string representation of session name
     * @return short session name represented as a single byte
     */
    private byte getSessionNameAsByte(String sessionName) throws HllApiInvocationException {
        if (sessionName.isEmpty()) {
            throw new HllApiInvocationException("Session name must be provided");
        }
        return (byte)sessionName.charAt(0);
    }
}
