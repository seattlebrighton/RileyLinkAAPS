package info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.record;

import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.TimeStampedRecord;

public class JournalEntryPumpLowReservoirPumpEvent extends TimeStampedRecord {
    public JournalEntryPumpLowReservoirPumpEvent() {
    }

    @Override
    public String getShortTypeName() {
        return "Low Reservoir";
    }

    @Override
    public boolean isAAPSRelevant() {
        return false;
    }
}
