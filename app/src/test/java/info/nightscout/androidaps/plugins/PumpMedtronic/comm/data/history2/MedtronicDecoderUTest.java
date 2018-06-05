package info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.history2;

import org.junit.Test;

import info.nightscout.androidaps.plugins.PumpMedtronic.util.MedtronicUtil;

/**
 * Created by andy on 6/4/18.
 */

public class MedtronicDecoderUTest {


    @Test
    public void testDecoding() {
        byte[] rawData = {(byte) 0x7c, 00};
        // A7 31 65 51 73 02 06 8F 00
        // 06 8E

        //byte cf = new Byte(8e);

        float value = MedtronicUtil.makeUnsignedShort(0x06, (byte) 0x8F) / 10.0f;

        System.out.println("Value: " + value);


        float value2 = MedtronicUtil.makeUnsignedShort((byte) 0x8F, 0x06) / 10.0f;

        System.out.println("Value: " + value2);

        // 02 06 = 153.8
        // 06 8f = 205.4
        // 8f 00 =


    }


}
