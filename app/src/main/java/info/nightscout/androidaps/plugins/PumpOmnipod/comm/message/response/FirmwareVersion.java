package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

public class FirmwareVersion {
    private int major;
    private int minor;
    private int patch;

    public FirmwareVersion(int major, int minor, int patch) {

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

}
