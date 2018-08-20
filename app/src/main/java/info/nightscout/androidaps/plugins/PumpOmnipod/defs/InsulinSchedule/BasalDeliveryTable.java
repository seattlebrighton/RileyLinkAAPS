package info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.IRawRepresentable;

import static java.lang.Math.floor;

public class BasalDeliveryTable implements IRawRepresentable {

    private BasalTableEntry[] entries = new BasalTableEntry[0];

    public BasalDeliveryTable(BasalSchedule schedule) {
        ArrayList<BasalTableEntry> entries = new ArrayList<>();
        for(BasalScheduleEntry e: schedule.entries) {
            double pulsesPerSegment = e.rate * 0.5 / Constants.PodPulseSize; //0.5 hours per segment
            boolean alternateSegmentPulse = pulsesPerSegment - floor(pulsesPerSegment) > 0;
            int remainingSegments = (int) (e.duration.getStandardMinutes() / 30); //30 minutes for each segment
            while(remainingSegments > 0) {
                int segments = Math.min(remainingSegments, 16);
                BasalTableEntry tableEntry = new BasalTableEntry(segments, (int) floor(pulsesPerSegment), alternateSegmentPulse);
                entries.add(tableEntry);
                remainingSegments -= segments;
            }
        }
        this.entries = entries.toArray(this.entries);
    }

    @Override
    public byte[] getRawData() {
        return new byte[0];

    }
}

