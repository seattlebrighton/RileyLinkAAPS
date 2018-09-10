package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.Duration;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.DeliveryStatus;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PodAlarm;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.ReservoirStatus;
import info.nightscout.utils.Round;

public class StatusResponse extends MessageBlock {
    public final DeliveryStatus deliveryStatus;
    public final ReservoirStatus reservoirStatus;
    public final Duration activeTime;
    public final double insulin;
    public final double insulinNotDelivered;
    public final byte podMessageCounter;
    public final PodAlarm alarms;
    private final double reservoirLevel;

    public StatusResponse(byte[] encodedData) {
        super(encodedData);

        this.deliveryStatus = DeliveryStatus.fromByte((byte) ((encodedData[1] & 0xFF) >> 4));
        this.reservoirStatus = ReservoirStatus.fromByte((byte) (encodedData[1] & (byte)0x0F));
        int minutes = ((encodedData[7] & 0x7F) << 6) + ((encodedData[8] & 0xFF) >> 2);
        this.activeTime = new Duration(minutes * 60 *1000);

        int highInsulinBits = (encodedData[2] & 0x0F) << 9;
        int middleInsulinBits = ((int)encodedData[3] & 0xFF) << 1;
        int lowInsulinBits = (encodedData[4] & 0xFF) >> 7;
        this.insulin = Constants.PodPulseSize * (highInsulinBits | middleInsulinBits | lowInsulinBits);
        this.podMessageCounter = (byte) (((encodedData[4] & 0xFF) >> 3) & 0x0F);

        this.insulinNotDelivered = Constants.PodPulseSize * ((encodedData[4] & 0x03) << 8) + ((int)encodedData[5] & 0xFF);
        this.alarms = new PodAlarm((byte) (((encodedData[6] & 0x7f) << 1) | ((encodedData[7] & 0xFF) >> 7)));

        int resHighBits = ((encodedData[8] & 0x03) << 6);
        int resLowBits = ((encodedData[9] & 0xFF) >> 2);

        this.reservoirLevel = Math.round((double)((resHighBits + resLowBits)) * 50 / 255);

        this.encodedData = ByteUtil.substring(encodedData, 1, 9);

    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.StatusResponse;
    }
}
