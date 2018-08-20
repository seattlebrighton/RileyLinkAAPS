package info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.IRawRepresentable;

public class BasalScheduleCommandPart extends DeliverySchedule implements IRawRepresentable {

    private final byte currentSegment;
    private final int secondsRemaining;
    private final int pulsesRemaining;
    private final BasalDeliveryTable basalTable;

    public BasalScheduleCommandPart(
            byte currentSegment,
            int secondsRemaining,
            int pulsesRemaining,
            BasalDeliveryTable basalTable
    ) {

        this.currentSegment = currentSegment;
        this.secondsRemaining = secondsRemaining;
        this.pulsesRemaining = pulsesRemaining;
        this.basalTable = basalTable;
    }

    @Override
    public byte[] getRawData() {
        byte[] rawData = new byte[0];
        throw new NotImplementedException("BasalSchedule.getRawData");
    }

    @Override
    public InsulinScheduleType getType() {
        return InsulinScheduleType.BasalSchedule;
    }

    @Override
    public int checksum() {
        throw new NotImplementedException("BasalScheduleCommandPart.checksum");
        //return 0;
    }
}
