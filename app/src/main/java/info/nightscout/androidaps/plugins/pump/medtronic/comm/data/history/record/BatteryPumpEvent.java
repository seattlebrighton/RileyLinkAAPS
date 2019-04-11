package info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.record;

import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.TimeStampedRecord;

public class BatteryPumpEvent extends TimeStampedRecord {
    public BatteryPumpEvent() {
    }

    @Override
    public String getShortTypeName() {
        return "Battery";
    }

    @Override
    public boolean isAAPSRelevant() {
        return false;
    }
}
