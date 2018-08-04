package info.nightscout.androidaps.plugins.PumpOmnipod.defs;

import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;

public class PodState {
    public int Address;
    public DateTime ActivatedAt;
    public String PiVersion;
    public String PmVersion;
    public int Lot;
    public int Tid;
    public int CurrentNonce;


    public void ResyncNonce(int nonceSearchKey, int currentNonce, int sequenceNumber) {
        throw new NotImplementedException("ResyncNonce");

    }
}
