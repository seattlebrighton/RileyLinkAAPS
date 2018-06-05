package info.nightscout.androidaps.plugins.PumpMedtronic.comm.message;

import android.util.Log;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.MedtronicCommandType;

/**
 * Created by geoff on 5/29/16.
 */
public class PumpMessage implements RLMessage {

    public PacketType packetType = PacketType.Carelink;
    public byte[] address = new byte[]{0, 0, 0};
    public MedtronicCommandType commandType;
    public MessageBody messageBody = new MessageBody();


    public PumpMessage() {
    }


    public PumpMessage(byte[] rxData) {
        init(rxData);
    }


    //    public void init(PacketType packetType, byte[] address, MessageType messageType, MessageBody messageBody) {
    //        this.packetType = packetType;
    //        this.address = address;
    //        this.messageType = messageType;
    //        this.messageBody = messageBody;
    //    }


    public void init(PacketType packetType, byte[] address, MedtronicCommandType commandType, MessageBody messageBody) {
        this.packetType = packetType;
        this.address = address;
        this.commandType = commandType;
        this.messageBody = messageBody;
    }


    public void init(byte[] rxData) {
        if (rxData == null) {
            return;
        }
        if (rxData.length > 0) {
            this.packetType = PacketType.getByValue(rxData[0]);
        }
        if (rxData.length > 3) {
            this.address = ByteUtil.substring(rxData, 1, 3);
        }
        if (rxData.length > 4) {
            this.commandType = MedtronicCommandType.getByCode(rxData[4]);
        }
        if (rxData.length > 5) {
            this.messageBody = MedtronicCommandType.constructMessageBody(commandType, ByteUtil.substring(rxData, 5, rxData.length - 5));
        }
    }


    @Override
    public byte[] getTxData() {
        byte[] rval = ByteUtil.concat(new byte[]{(byte) packetType.getValue()}, address);
        rval = ByteUtil.concat(rval, commandType.getCommandCode());
        rval = ByteUtil.concat(rval, messageBody.getTxData());
        return rval;
    }


    public byte[] getContents() {
        return ByteUtil.concat(new byte[]{commandType.getCommandCode()}, messageBody.getTxData());
    }


    // rawContent = just response without code (contents-2, messageBody.txData-1);
    public byte[] getRawContent() {

        if ((messageBody == null) || (messageBody.getTxData() == null) || (messageBody.getTxData().length == 0))
            return null;

        byte[] data = messageBody.getTxData();

        int length = ByteUtil.asUINT8(data[0]); // length is not always correct so, we check whole array if we have data, after length
        boolean oldWay = false;

        for(int i = (length); i < data.length; i++) {
            if (data[i] != 0x00) {
                oldWay = true;
            }
        }

        if (oldWay) {
            length = data.length - 1;
        }

        byte[] arrayOut = new byte[length];

        System.arraycopy(messageBody.getTxData(), 1, arrayOut, 0, length);

        Log.d("PumpMessage", "Length: " + length + ", CommandType: " + commandType);

        return arrayOut;
    }


    public boolean isValid() {
        if (packetType == null)
            return false;
        if (address == null)
            return false;
        if (commandType == null)
            return false;
        if (messageBody == null)
            return false;
        return true;
    }


    public MessageBody getMessageBody() {
        return messageBody;
    }

}
