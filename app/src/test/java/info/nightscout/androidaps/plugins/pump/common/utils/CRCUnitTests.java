package info.nightscout.androidaps.plugins.pump.common.utils;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CRCUnitTests {

    @Test
    public void crc8_NullArray_ReturnsZero()
    {
        byte crc = CRC.crc8(null, 0);
        assertEquals(0, crc);
    }

    @Test
    public void crc8_EmptyArray_ReturnsZero()
    {
        byte crc = CRC.crc8(new byte[0]);
        assertEquals(0, crc);
    }

    @Test
    public void crc8_TestData1_Valid()
    {
        // Validated with online crc8 calculator
        byte crc = CRC.crc8(ByteUtil.fromHexString("42"));
        assertEquals((byte)0x65, crc);
    }

    @Test
    public void crc8_TestData2_Valid()
    {
        // Validated with online crc8 calculator
        byte crc = CRC.crc8(ByteUtil.fromHexString("0cb68fa0a4b6665a75a3ba8a0608a97f"));
        assertEquals((byte)0x92, crc);
    }


    @Test
    public void calculate16CCITT_NullArray_ReturnsFFFF()
    {
        byte[] crc = CRC.calculate16CCITT(null);
        assertArrayEquals(new byte[] {-1,-1}, crc);
    }

    @Test
    public void calculate16CCITT_EmptyArray_ReturnsFFFF()
    {
        byte[] crc = CRC.calculate16CCITT(new byte[0]);
        assertArrayEquals(new byte[] {-1,-1}, crc);
    }

    @Test
    public void calculate16CCITT_TestData1_Valid()
    {
        // Validated with online CRC16_CCITT_FALSE calculator
        byte[] crc = CRC.calculate16CCITT(ByteUtil.fromHexString("128b9a5dff09bc849764259e"));
        assertArrayEquals(new byte[] {(byte)0xff,(byte)0x8d}, crc);
    }

    @Test
    public void calculate16CCITT_TestData2_Valid()
    {
        // Validated with online CRC16_CCITT_FALSE calculator
        byte[] crc = CRC.calculate16CCITT(ByteUtil.fromHexString("0cb68fa0a4b6665a75a3ba8a0608a97f"));
        assertArrayEquals(new byte[] {(byte)0x31,(byte)0xf3}, crc);
    }

}
