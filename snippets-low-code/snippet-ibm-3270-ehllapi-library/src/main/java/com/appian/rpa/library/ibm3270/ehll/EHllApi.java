package com.appian.rpa.library.ibm3270.ehll;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface EHllApi extends StdCallLibrary {
    int HA_CONNECT_PS = 1;

    int HA_DISCONNECT_PS = 2;

    int HA_SENDKEY = 3;

    int HA_COPY_PS = 5;

    int HA_SEARCH_PS = 6;

    int HA_SET_SESSION_PARMS = 9;

    int HA_SET_CURSOR = 40;

    int HA_CONVERT_POS_ROW_COL = 99;

    int HA_CONNECT_WINDOW_SERVICES = 101;

    int HA_DISCONNECT_WINDOW_SERVICES = 102;

    int WINDOW_STATUS = 104;

    /**
     * Generic function not present in the native library but instead is a tool to allow extension of this API and
     * call any arbitrary functions defined in the HLLAPI spec
     */
    long hllapi(IntByReference functionNbr, Pointer dataBuffer, IntByReference bufferLen, IntByReference posOrReturnCode );

}
