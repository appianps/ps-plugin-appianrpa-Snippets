package com.appian.rpa.library.ibm3270.ehll;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

class EHllImpl implements EHll {

    private static final String SEARCH_ALL_PARAM = "SRCHALL";

    private static final String SEARCH_FORWARD_PARAM = "SRCHFRWD";

    private static final String SEARCH_BACKWARD_PARAM = "SRCHBKWD";

    private static final String SEARCH_FROM_PARAM = "SRCHFROM";

    private final EHllApi ehllApi;
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
    EHllImpl(String pathToDlls, String dllName) {
        System.setProperty("jna.library.path", pathToDlls);
        ehllApi = Native.loadLibrary(dllName, EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);
    }

    /**
     * Create EHll
     *
     * @param pathToDlls - path to dll
     * @param dllName    - name of dll
     * @param encoding   - must be a valid characterset
     */
    EHllImpl(String pathToDlls, String dllName, String encoding) {
        System.setProperty("jna.library.path", pathToDlls);
        System.setProperty("jna.encoding", encoding);
        ehllApi = Native.loadLibrary(dllName, EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);
    }

    @Override
    public void connect(String sessionName) throws HllApiInvocationException {
        invokeHllApi(EHllApi.HA_CONNECT_PS, sessionName, 0);
    }

    @Override
    public void disconnect() throws HllApiInvocationException {
        invokeHllApi(EHllApi.HA_DISCONNECT_PS, "ignored", 0);
    }

    @Override
    public String copyScreen(int screenSize) throws HllApiInvocationException {
        HllApiValue hllApiValue = invokeHllApi(EHllApi.HA_COPY_PS, new byte[screenSize], 0);
        return hllApiValue.getDataString();
    }

    @Override
    public void sendKey(String key) throws HllApiInvocationException {
        invokeHllApi(EHllApi.HA_SENDKEY, key, 0);
    }

    @Override
    public void sendKeyAtCoordinates(String key, int cursorPosition) throws HllApiInvocationException {
        setCursorPosition(cursorPosition);
        invokeHllApi(EHllApi.HA_SENDKEY, key, 0);
    }

    @Override
    public void setSessionParams(String params) throws HllApiInvocationException {
        invokeHllApi(EHllApi.HA_SET_SESSION_PARMS, params, 0);
    }

    @Override
    public int search(String textToSearch, boolean searchForwards) throws HllApiInvocationException {
        setSessionParams(SEARCH_ALL_PARAM);
        return searchInternal(textToSearch, searchForwards, 0);
    }

    @Override
    public int search(String textToSearch, boolean searchForwards, int cursorPosition) throws HllApiInvocationException {
        setSessionParams(SEARCH_FROM_PARAM);
        return searchInternal(textToSearch, searchForwards, cursorPosition);
    }

    private int searchInternal(String textToSearch, boolean firstOccurrence, int cursorPosition) throws HllApiInvocationException {
        if (firstOccurrence) {
            setSessionParams(SEARCH_FORWARD_PARAM);
        } else {
            setSessionParams(SEARCH_BACKWARD_PARAM);
        }
        HllApiValue hllApiValue = invokeHllApi(EHllApi.HA_SEARCH_PS, textToSearch, cursorPosition);
        return hllApiValue.getDataLength();
    }

    @Override
    public void setCursorPosition(int cursorPosition) throws HllApiInvocationException {
        invokeHllApi(EHllApi.HA_SET_CURSOR, "ignored", cursorPosition);
    }

    @Override
    public void maximizeWindow(char shortSessionName) throws HllApiInvocationException {
        //Connect to window service
        invokeHllApi(EHllApi.HA_CONNECT_WINDOW_SERVICES, String.valueOf(shortSessionName), 0);

        byte[] command = new byte[]{
                (byte) shortSessionName,       //short session name
                0x01,
                0x00,
                0x08,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        };

        //Maximize window
        invokeHllApi(EHllApi.WINDOW_STATUS, command, 0);

        //Disconnect from window services
        invokeHllApi(EHllApi.HA_DISCONNECT_WINDOW_SERVICES, String.valueOf(shortSessionName), 0);
    }

    @Override
    public RowColumn convertPositionToRowCol(char shortSessionName, int cursorPosition) throws HllApiInvocationException {
        byte[] command = new byte[] {
                (byte) shortSessionName,
                (byte) 'P'
        };

        HllApiValue hllApiValue = invokeHllApi(EHllApi.HA_CONVERT_POS_ROW_COL, command, command.length, cursorPosition, INVALID_POSITION_CONVERSION_CODES, false);
        return new RowColumn(hllApiValue.getDataLength(), hllApiValue.getResponseCode());
    }

    @Override
    public int convertRowColToCursorPosition(char shortSessionName, int row, int col) throws HllApiInvocationException {
        byte[] command = new byte[] {
                (byte) shortSessionName,
                (byte) 'R'
        };

        //If response code is one of 0, 9998 or 9999 throw an exception. All other codes represent position
        HllApiValue hllApiValue = invokeHllApi(EHllApi.HA_CONVERT_POS_ROW_COL, command, row, col, INVALID_POSITION_CONVERSION_CODES, false);

        return hllApiValue.getResponseCode();
    }

    @Override
    public HllApiValue invoke(int function, byte[] data, int dataLength, int cursorPosition) throws HllApiInvocationException{
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
    private HllApiValue invokeHllApi(int functionNumber, String input, int cursorPosition) throws HllApiInvocationException {
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
    private HllApiValue invokeHllApi(int functionNumber, byte[] data, int cursorPosition) throws HllApiInvocationException {
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
    private HllApiValue invokeHllApi(int functionNumber, byte[] data, int inputDtaLength, int cursorPosition) throws HllApiInvocationException {
        Set<Integer> set = new HashSet<>();
        set.add(0);
        return invokeHllApi(functionNumber, data, inputDtaLength, cursorPosition, set, true);
    }

    private HllApiValue invokeHllApi(int functionNumber, byte[] data, int inputDtaLength, int cursorPosition, Set<Integer> responseCodes, boolean inclusive) throws HllApiInvocationException {
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
        return new HllApiValue(funcIntPtr.getValue(), responseCode, dtaLenPtr.getValue(), dtaPtr.getString(0));
    }


    /**
     * Handle response code throwing exception if not 0
     *
     * @param responseCode - dll invocation response code
     * @param responseCodes - response codes
     * @param inclusive - only accept response codes in the set else only accept codes out of the set
     * @throws HllApiInvocationException - if response code is non 0
     */
    private void checkResponseCode(int responseCode, Set<Integer> responseCodes, boolean inclusive) throws HllApiInvocationException {
        if(inclusive) {
            if(!responseCodes.contains(responseCode)) {
                throw new HllApiInvocationException(responseCode);
            }
        } else  {
            if(responseCodes.contains(responseCode)) {
                throw new HllApiInvocationException(responseCode);
            }
        }
    }
}
