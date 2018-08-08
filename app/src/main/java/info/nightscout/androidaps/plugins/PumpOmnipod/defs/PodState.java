package info.nightscout.androidaps.plugins.PumpOmnipod.defs;

import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.PumpCommon.utils.CRC;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.FirmwareVersion;

public class PodState {

    public int Address;
    public DateTime ActivatedAt;
    public FirmwareVersion PiVersion;
    public FirmwareVersion PmVersion;
    public int Lot;
    public int Tid;
    private NonceState nonceState;

    public PodState(int address, DateTime activatedAt,FirmwareVersion piVersion, FirmwareVersion pmVersion, int lot, int tid) {

        this.Address = address;
        this.ActivatedAt = activatedAt;
        this.PiVersion = piVersion;
        this.PmVersion = pmVersion;
        this.Lot = lot;
        this.Tid = tid;
        this.nonceState = new NonceState(lot, tid);
    }

    public void ResyncNonce(int syncWord, int sentNonce, int sequenceNumber) {
        int sum = (sentNonce & 0xFFFF)
                + CRC.crc16lookup[sequenceNumber]
                + this.Lot & 0xFFFF
                + this.Tid & 0xFFFF;
        int seed = sum &0xFFFF ^ syncWord;
        this.nonceState = new NonceState(Lot, Tid, (byte)(seed & 0xFF));

    }

    public int getCurrentNonce() {
        return nonceState.getCurrentNonce();
    }

    public void AdvanceToNextNonce() {
        nonceState.AdvanceToNextNonce();
    }
}
