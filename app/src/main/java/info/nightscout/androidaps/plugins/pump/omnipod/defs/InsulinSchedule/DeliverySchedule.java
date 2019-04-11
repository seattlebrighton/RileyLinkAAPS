package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

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
