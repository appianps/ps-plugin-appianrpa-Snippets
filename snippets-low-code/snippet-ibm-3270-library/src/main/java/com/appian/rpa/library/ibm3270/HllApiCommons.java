package com.appian.rpa.library.ibm3270;

import com.appian.rpa.library.ibm3270.ehll.EHllApi;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class HllApiCommons {
    /**
     * Call JNA object representing entry point EHLLAPI32 / PCSHL32 DLL to control emulator.
     * @param func HLLAPI function number
     * @param dta HLLAPI data buffer for input and output
     * @param dtaLen HLLAPI length of data buffer
     * @param pos HLLAPI position in data buffer
     */

    public HllApiVal hllApi(EHllApi eHllApi, int func, byte[] dta, int dtaLen, int pos)
    {
        // object to return with results from call to hllApi
        HllApiVal hav = new HllApiVal();
        int inputDtaLength = dta.length;
        Pointer dtaPtr = new Memory(inputDtaLength);
        if (dta != null )
            dtaPtr.write(0, dta, 0, inputDtaLength);
        IntByReference funcIntPtr = new IntByReference(func);
        IntByReference dtaLenPtr = new IntByReference(dtaLen);
        IntByReference posIntPtr = new IntByReference(pos);

        eHllApi.hllapi(funcIntPtr, dtaPtr, dtaLenPtr, posIntPtr);

        hav.hllApiCod = posIntPtr.getValue();       // code returned by function call
        hav.hllApiLen = dtaLenPtr.getValue();       // length of byte array returned
        hav.hllApiDta = dtaPtr.getByteArray(0,inputDtaLength);      // byte array returned
        hav.srcDataLen = inputDtaLength;            // length of input byteArray

        return hav;
    }
}
