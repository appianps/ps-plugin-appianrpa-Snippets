package com.appian.rpa.library.ibm3270;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface EHllApi extends StdCallLibrary {
    int HA_CONNECT_PS = 1;      /* Connect PS */

    int HA_DISCONNECT_PS = 2;      /* Disconnect PS */

    int HA_SENDKEY = 3;      /* Sendkey function */

    int HA_WAIT = 4;      /* Wait function */

    int HA_COPY_PS = 5;      /* Copy PS function  */

    int HA_SEARCH_PS = 6;      /* Search PS function */

    int HA_QUERY_CURSOR_LOC = 7;      /*  Query Cursor Location function */

    long hllapi(IntByReference functionNbr, Pointer dataBuffer, IntByReference bufferLen, IntByReference posOrReturnCode );

}
