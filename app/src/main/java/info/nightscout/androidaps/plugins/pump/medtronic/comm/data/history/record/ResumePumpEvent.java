package info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.record;

import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history.TimeStampedRecord;

public class ResumePumpEvent extends TimeStampedRecord {
    public ResumePumpEvent() {
    }

    @Override
    public String getShortTypeName() {
        return "Resume";
    }

    @Override
    public boolean isAAPSRelevant() {
        return false;
    }
}
