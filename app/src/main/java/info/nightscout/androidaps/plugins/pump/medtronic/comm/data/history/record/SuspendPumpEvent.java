package info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.record;

import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.TimeStampedRecord;

public class SuspendPumpEvent extends TimeStampedRecord {
    public SuspendPumpEvent() {
    }

    @Override
    public String getShortTypeName() {
        return "Suspend";
    }

    @Override
    public boolean isAAPSRelevant() {
        return true;
    }
}
