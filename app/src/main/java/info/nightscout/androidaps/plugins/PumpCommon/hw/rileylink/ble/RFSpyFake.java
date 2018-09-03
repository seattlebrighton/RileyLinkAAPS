package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.CC111XRegister;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;

public class RFSpyFake implements IRFSpy {



    @Override
    public RileyLinkFirmwareVersion getRLVersionCached() {
        return RileyLinkFirmwareVersion.Version_2_2;
    }

    @Override
    public String getBLEVersionCached() {
        return "1.3";
    }

    @Override
    public void startReader() {

    }

    @Override
    public void initializeRileyLink() {

    }

    @Override
    public void newDataIsAvailable() {

    }

    @Override
    public String getVersion() {
        return "subg_rfspy 2.2";
    }

    private int commsCount = 0;

    @Override
    public RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount) {
        return transmitThenReceive(pkt, sendChannel, repeatCount, delay_ms, listenChannel, timeout_ms, retryCount, 0);

    }

    @Override
    public RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount, int extendPreamble_ms) {
        byte[] bytesToSend = pkt.getRaw();
        String myString = ByteUtil.shortHexString(bytesToSend);
        byte[] assignAddressRequest = ByteUtil.fromHexString("ffffffffaaffffffff000607041f05e70b834ce2");
        byte[] assignAddressResponse = ByteUtil.fromHexString("000000ffffffffebffffffff0417011502070002070002020000aaa700099d818e1f05e70b8321a6");
        byte[] confirmPairingRequest = ByteUtil.fromHexString("ffffffffadffffffff001503131f05e70b140408121214050000aaa700099d81016802");
        byte[] confirmPairingResponse1 = ByteUtil.fromHexString("000000ffffffffeeffffffff041d011b13881008340a5002070002070002030000aaa700099d8120");
        byte[] ackForConfirmPairingResponse1 = ByteUtil.fromHexString("FFFFFFFF4F1F05E70BAB"); //baga
        byte[] conForConfirmPairingResponse1 = ByteUtil.fromHexString("000000ffffffff901f05e70b035dc1");


        switch(commsCount++) {
            case 0:
                if (ByteUtil.compare(bytesToSend, assignAddressRequest) == 0 )
                    return new RFSpyResponse(assignAddressResponse);
                else
                    return null;
            case 1:
                return new RFSpyResponse(new byte[] {(byte) 0xAA});
            case 2:
                if (ByteUtil.compare(bytesToSend, confirmPairingRequest) == 0 )
                    return new RFSpyResponse(confirmPairingResponse1);
                else
                    return null;
            case 3:
                if (ByteUtil.compare(bytesToSend, ackForConfirmPairingResponse1) == 0 )
                    return new RFSpyResponse(conForConfirmPairingResponse1);
                else
                    return null;

            case 4:
                return new RFSpyResponse(new byte[] {(byte) 0xAA});
            default:
                return null;

        }
        //return null;

    }

    @Override
    public RFSpyResponse updateRegister(CC111XRegister reg, int val) {
        return new RFSpyResponse(new byte[] {(byte) 0xDD});
    }

    @Override
    public void setBaseFrequency(double freqMHz) {
        RileyLinkUtil.setEncoding(RileyLinkEncodingType.Manchester);

    }
}
