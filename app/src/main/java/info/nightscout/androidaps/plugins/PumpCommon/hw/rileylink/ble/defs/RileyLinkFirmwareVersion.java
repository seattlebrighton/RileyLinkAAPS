package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs;

import org.apache.commons.lang3.NotImplementedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RileyLinkFirmwareVersion {
    private static final String FIRMWARE_IDENTIFICATION_PREFIX = "ubg_rfspy ";
    private static final Pattern _version_pattern = Pattern.compile(FIRMWARE_IDENTIFICATION_PREFIX +"([0-9]+)\\.([0-9]+)");

    private int major;
    private int minor;

    public RileyLinkFirmwareVersion(String versionString) {
        if (versionString != null) {
            Matcher m = _version_pattern.matcher(versionString);
            if (m.find()) {
                major = Integer.parseInt(m.group(1));
                minor = Integer.parseInt(m.group(2));
                if (!isVersionSupported(major, minor))
                    throw new NotImplementedException(String.format("RileyLink firmware version %d.%dnot supported", major, minor));
            }
        }

    }

    public int getCombinedVersion() {
        return major * 10000 + minor * 100; //We reserve place for revisions of minor version
    }

    public static Boolean isVersionSupported(int major, int minor) {
        switch(major) {
            case 0:
                switch(minor) {
                    case 9:
                        return true;
                }

            case 1:
                switch(minor) {
                    case 0:
                        return true;
                }
            case 2:
                switch(minor) {
                    case 0:
                        return true;
                    case 2:
                        return true;
                }
        }
        return false;
    }

    @Override
    public String toString() {
        return FIRMWARE_IDENTIFICATION_PREFIX + major + "." + minor;
    }
}
