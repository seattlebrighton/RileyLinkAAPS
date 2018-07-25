package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs;

/**
 * Created by andy on 5/6/18.
 */
public abstract class RLMessage {

    protected RLSoftwareEncodingType encoding;

    public abstract RLSoftwareEncodingType getEncoding();

    public abstract byte[] getTxData();

    public abstract boolean isValid();

}
