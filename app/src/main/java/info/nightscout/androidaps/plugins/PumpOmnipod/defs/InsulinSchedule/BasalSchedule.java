package info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

public class BasalSchedule {
    public final BasalScheduleEntry[] entries;

    public BasalSchedule(BasalScheduleEntry[] entries) {
        this.entries = entries;
    }

    public double rateAt(Duration timeOffset) {
        return 0;
    }
}
