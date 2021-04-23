package com.appian.rpa.library.ibm3270.ehll;

import com.appian.rpa.library.ibm3270.EHllApi;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import java.nio.charset.StandardCharsets;

class EHllImpl implements EHll {

    private static final String SEARCH_ALL_PARAM = "SRCHALL";

    private static final String SEARCH_FORWARD_PARAM = "SRCHFRWD";

    private static final String SEARCH_BACKWARD_PARAM = "SRCHBKWD";

    private static final String SEARCH_FROM_PARAM = "SRCHFROM";

    private final EHllApi ehllApi;

    /**
     * Create EHll
     *
     * @param pathToDlls - path to dll
     * @param dllName - name of dll
     */
    EHllImpl(String pathToDlls, String dllName) {
        System.setProperty("jna.library.path", pathToDlls);
        ehllApi = Native.loadLibrary(dllName, EHllApi.class, W32APIOptions.DEFAULT_OPTIONS);
    }

    /**
     * Create EHll
     *
     * @param pathToDlls - path to dll
     * @param dllName - name of dll
     * @param encoding - must be a valid characterset
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
        invokeHllApi(EHllApi.HA_DISCONNECT_PS, "", 0);
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
    public int search(String textToSearch, boolean firstOccurrence) throws HllApiInvocationException {
       setSessionParams(SEARCH_ALL_PARAM);
       return searchInternal(textToSearch, firstOccurrence, 0);
    }

    @Override
    public int search(String textToSearch, boolean firstOccurrence, int cursorPosition) throws HllApiInvocationException {
        setSessionParams(SEARCH_FROM_PARAM);
        return searchInternal(textToSearch, firstOccurrence, cursorPosition);
    }

    private int searchInternal(String textToSearch, boolean firstOccurrence, int cursorPosition) throws HllApiInvocationException {
        if(firstOccurrence) {
            setSessionParams(SEARCH_FORWARD_PARAM);
        } else {
            setSessionParams(SEARCH_BACKWARD_PARAM);
        }
        HllApiValue hllApiValue = invokeHllApi(EHllApi.HA_SEARCH_PS, textToSearch, cursorPosition);
        return hllApiValue.getDataLength();
    }

    @Override
    public void setCursorPosition(int cursorPosition) throws HllApiInvocationException {
        invokeHllApi(EHllApi.HA_SET_CURSOR, "", cursorPosition);
    }

    /**
     * Used to invoke functions not defined in this API
     *
     * Prefer using functions defined in API. For functions not implemented here this generic invoke can be used
     *
     * @param function - function number to invoke
     * @param data - value to send
     * @param cursorPosition - position of cursor
     * @return All values returned by invoking dll
     * @throws HllApiInvocationException - if response code is not 0
     */
    public HllApiValue invoke(int function, byte[] data, int cursorPosition) throws HllApiInvocationException{
        return invokeHllApi(function, data, cursorPosition);
    }

    /**
     * Interacts with EHLL java native library
     *
     * @param functionNumber - function number to call
     * @param input - string to pass to java native library
     * @param cursorPosition - cursor position in the target presentation space
     * @return Collection with modified values
     * @throws HllApiInvocationException - when response code != 0 is returned
     */
    private HllApiValue invokeHllApi(int functionNumber, String input, int cursorPosition) throws HllApiInvocationException {
        input = input == null ? "" : input;
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return invokeHllApi(functionNumber, data, cursorPosition);
    }

    /**
     * Interact with EHLL java native library
     * @param functionNumber - function number to call
     * @param data - byte[] to pass to java native library. May be filled
     * @param cursorPosition - cursor position in the target presentation space
     * @return Collection with modified values
     * @throws HllApiInvocationException - when response code != 0 is returned
     */
    private HllApiValue invokeHllApi(int functionNumber, byte[] data, int cursorPosition) throws HllApiInvocationException {
        int inputDtaLength = data.length;
        Pointer dtaPtr = new Memory(inputDtaLength);
        dtaPtr.write(0, data, 0, inputDtaLength);
        IntByReference funcIntPtr = new IntByReference(functionNumber);
        IntByReference dtaLenPtr = new IntByReference(inputDtaLength);
        IntByReference posIntPtr = new IntByReference(cursorPosition);

        ehllApi.hllapi(funcIntPtr, dtaPtr, dtaLenPtr, posIntPtr);

        // posIntPtr is filled with the response code
        int responseCode = posIntPtr.getValue();
        checkResponseCode(responseCode);

        //Make sure to set the default string encoding to be appropriate using jna.encoding
        return new HllApiValue(funcIntPtr.getValue(), responseCode, dtaLenPtr.getValue(), dtaPtr.getString(0));
    }

    /**
     * Handle response code throwing exception if not 0
     *
     * @param responseCode - dll invocation response code
     * @throws HllApiInvocationException - if response code is non 0
     */
    private void checkResponseCode(int responseCode) throws HllApiInvocationException {
        if (responseCode != 0) {
            throw new HllApiInvocationException(responseCode);
        }
    }
}
