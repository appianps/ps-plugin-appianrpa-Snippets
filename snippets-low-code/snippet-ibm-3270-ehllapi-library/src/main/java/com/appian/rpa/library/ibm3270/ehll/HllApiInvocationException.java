package com.appian.rpa.library.ibm3270.ehll;

/**
 * Thrown when non 0 return code is returned from dll
 *
 * See documentation in {@link Ehll} method for additional return code information
 */
public class HllApiInvocationException extends Exception {

    /**
     * response code from API
     */
    private final int responseCode;

    /**
     * Invocation exception for dll
     *
     * @param responseCode - return code
     */
    public HllApiInvocationException(int responseCode) {
        super(String.format("Invocation of DLL failed with response code %d", responseCode));
        this.responseCode = responseCode;
    }

    /**
     * Custom message for invocation exception for the dll
     */
    public HllApiInvocationException(String message) {
        super(message);
        this.responseCode = -1;
    }

    /**
     * Response code from API can be keyed off of
     *
     * @return - response code
     */
    public int getResponseCode() {
        return responseCode;
    }
}
