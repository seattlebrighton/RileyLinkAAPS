package info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.IRawRepresentable;

public class Bolus extends DeliverySchedule implements IRawRepresentable {

    private final double units;
    private final int multiplier;

    public Bolus(double units, int multiplier) {

        this.units = units;
        this.multiplier = multiplier;
    }


    @Override
    public byte[] getRawData() {
        byte[] rawData = new byte[5];

        int pulseCount = (int) (units / Constants.PodPulseSize);
        int fieldA = pulseCount * multiplier;
        rawData[0] = 1;
        byte[] buffer = ByteUtil.getBytesFromInt(pulseCount);
        rawData[1] = buffer[2];
        rawData[2] = buffer[3];
        rawData[3] = buffer[2];
        rawData[4] = buffer[3];
        return rawData;

    }

    @Override
    public InsulinScheduleType getType() {
        return InsulinScheduleType.Bolus;
    }

    @Override
    public int checksum() {
        int checksum = 0;
        for(byte b : getRawData()) {
            checksum += (0xFF & b);
        }
        return checksum;
    }
}
