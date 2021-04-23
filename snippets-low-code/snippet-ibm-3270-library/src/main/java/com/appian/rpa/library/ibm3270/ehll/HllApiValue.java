package com.appian.rpa.library.ibm3270.ehll;

/**
 * Wraps all returned parameters from dll invocation
 */
public final class HllApiValue {

    /**
     * Passed in function pointer
     *
     * Meaning is dependent on function called. Refer to the original HLLAPI documentation for the function specified
     * For some functions will have no meaning and just be the passed in data
     */
    private final int function;

    /**
     * Response Code returned from method call. In the HLLAPI documentation, this is referred to as PS Position
     * but often times this field is used to store the response code so it is referenced in this way
     */
    private final int responseCode;

    /**
     * Parameter passed as source data length.
     *
     * Meaning is dependent on function called. Refer to the original HLLAPI documentation for the function specified
     * For some functions will have no meaning and just be the passed in data
     */
    private final int dataLength;

    /**
     * Data passed as source data
     *
     * Meaning is dependent on function called. Refer to the original HLLAPI documentation for the function specified
     * For some functions will have no meaning and just be the passed in data
     */
    private final String dataString;

    public HllApiValue(int function, int responseCode, int dataLength, String dataString) {
        this.function = function;
        this.responseCode = responseCode;
        this.dataLength = dataLength;
        this.dataString = dataString;
    }

    public int getFunction() {
        return function;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getDataLength() {
        return dataLength;
    }

    public String getDataString() {
        return dataString;
    }
}
