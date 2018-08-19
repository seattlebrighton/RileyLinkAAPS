package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import org.joda.time.Interval;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.ExpirationAdvisory;

public class AlertConfiguration{
    private AlertType alertType;
    private boolean audible;
    private boolean autoOffModifier;
    private int duration;
    private ExpirationAdvisory expirationType;
    private int beepType;

    public AlertConfiguration(
            AlertType alertType,
            boolean audible,
            boolean autoOffModifier,
            int duration,
            ExpirationAdvisory expirationType,
            int beepType
    ) {
        this.alertType = alertType;
        this.audible = audible;
        this.autoOffModifier = autoOffModifier;
        this.duration = duration;
        this.expirationType = expirationType;
        this.beepType = beepType;
    }

    public byte[] getRawData() {
        byte[] rawData = new byte[6];

        byte firstByte = (byte) (alertType.getValue() << 4);
        firstByte |= expirationType.expirationType.getValue();
        byte[] valueBuffer = new byte[0];
        valueBuffer = ByteUtil.getBytesFromInt(duration);

        byte durationHigh = (byte) (valueBuffer[2] & (byte)1);
        firstByte |= durationHigh;

        rawData[0] = firstByte;
        rawData[1] = valueBuffer[3];

        switch (expirationType.expirationType) {
            case Reservoir:
                int ticks = (int)(expirationType.reservoirLevel / Constants.PodPulseSize / 2);
                valueBuffer = ByteUtil.getBytesFromInt(ticks);
                break;
            case Timer:
                int duration = (int)expirationType.timeToExpire.getStandardMinutes();
                valueBuffer = ByteUtil.getBytesFromInt(duration);
                break;
        }
        rawData[2] = valueBuffer[2];
        rawData[3] = valueBuffer[3];
        valueBuffer = ByteUtil.getBytesFromInt(beepType);
        rawData[4] = valueBuffer[2];
        rawData[5] = valueBuffer[3];
        return rawData;
    }


}
