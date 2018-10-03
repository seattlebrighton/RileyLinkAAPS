package info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.IRawRepresentable;

public abstract class DeliverySchedule implements IRawRepresentable {

    public abstract InsulinScheduleType getType();

    public int checksum() {
        int checksum = 0;
        for(byte b : getRawData()) {
            checksum += (0xFF & b);
        }
        return checksum;
    }
}
