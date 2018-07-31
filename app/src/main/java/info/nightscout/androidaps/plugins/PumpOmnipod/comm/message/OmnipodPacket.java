package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import android.util.Log;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.HexDump;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PacketType;

/**
 * Created by andy on 6/1/18.
 */
// FIXME: This needs to be changed. this is just copy of MedtronicPumpMessage, so I imagine this file will have different structure
// in Omnipod I assume
public class OmnipodPacket implements RLMessage {

    private int packetAddress = 0;
    private PacketType packetType = PacketType.Invalid;
    private int packetNumber = 0;
    private byte[] encodedMessage = null;

    public OmnipodPacket() {

    }

    public PacketType getPacketType() {
        return packetType;
    }

    public OmnipodPacket(int packetAddress, PacketType packetType, int packetNumber, byte[] encodedMessage) {

        this.packetAddress = packetAddress;
        this.packetType = packetType;
        this.packetNumber = packetNumber;
        this.encodedMessage = encodedMessage;
    }


    public boolean isErrorResponse() {
        return false;
    }

    @Override
    public byte[] getTxData() {
        return new byte[0];
    }

    @Override
    public boolean isValid() {
        return false;
    }


}
