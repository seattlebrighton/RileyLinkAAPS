package info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.IRawRepresentable;

public class RateEntry  implements IRawRepresentable {
    @Override
    public byte[] getRawData() {
        throw new NotImplementedException("RateEntry.getRawData");
        //return new byte[0];
    }
}
