package com.appian.rpa.library.ibm3270.ehll;

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

    int HA_SET_SESSION_PARMS = 9;

//    Search parameters
//    The following session parameters affect all search functions.
//    SRCHALL (default) - Scans the entire PS or field.
//    SRCHFROM - Starts the scan from a specified location in the PS or field.
//    SCRCHFRWD (default) - Performs the scan in an ascending direction.
//    SRCHBKWD - Performs the scan in a descending direction through the PS or field.
//    example is sending "SRCHFROM,SRCHFRWD" as dta

    int HA_SET_CURSOR = 40; /* set the cursor in the absolute position (row-1 * 80) + column). */

    int HA_CONVERT_POS_ROW_COL = 99;

//    For data string "AP" means for "Session A" convert from PS position to row-column coordinates
//    "BR" would mean for "Session B" convert from row-column coordinates to PS position

//    If R, the input row goes in len (3) and input columns goes in pos (4)
//    If P, the len (3) is not used and PS position goes in pos (4)

    long hllapi(IntByReference functionNbr, Pointer dataBuffer, IntByReference bufferLen, IntByReference posOrReturnCode );

}
