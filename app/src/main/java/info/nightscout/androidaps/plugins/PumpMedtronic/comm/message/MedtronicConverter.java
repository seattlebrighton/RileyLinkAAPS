package info.nightscout.androidaps.plugins.PumpMedtronic.comm.message;

import com.gxwtech.roundtrip2.util.StringUtil;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.HexDump;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.BasalProfile;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.BatteryType;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.MedtronicCommandType;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.MedtronicDeviceType;
import info.nightscout.androidaps.plugins.PumpMedtronic.util.MedtronicUtil;

/**
 * Created by andy on 5/9/18.
 */

public class MedtronicConverter {

    private static final Logger LOG = LoggerFactory.getLogger(MedtronicConverter.class);


    public Object convertResponse(MedtronicCommandType commandType, byte[] rawContent) {

        LOG.debug("Raw response before convert: " + HexDump.toHexStringDisplayable(rawContent));

        switch (commandType) {

            case PumpModel: {
                return MedtronicDeviceType.getByDescription(StringUtil.fromBytes(ByteUtil.substring(rawContent, 1, 3)));
            }

            case RealTimeClock: {
                return decodeTime(rawContent);
            }

            case GetRemainingInsulin: {
                return decodeRemainingInsulin(rawContent);
            }

            case GetBatteryStatus: {
                return decodeBatteryStatus(rawContent);
            }

            case GetBasalProfileSTD: {
                return new BasalProfile(rawContent);
            }


            default: {
                throw new RuntimeException("Unsupported command Type: " + commandType);
            }

        }


    }


    protected BasalProfile decodeProfile2(byte[] rep) {
        //byte rep[] = minimedReply.getRawData();

        // String profile = getProfileName(minimedReply);

        BasalProfile basalProfile = new BasalProfile();

        // 0x12 0x00 0x00 0x16 0x00 0x11 0x00

        if ((rep.length >= 3) && (rep[2] == 0x3F)) {
            //String i18value = i18nControl.getMessage("NOT_SET");
            //writeSetting(key, i18value, i18value, PumpConfigurationGroup.Basal);
            return null;
        }

        int time_x;
        double vald;

        for(int i = 0; i < rep.length; i += 3) {


            vald = MedtronicUtil.decodeBasalInsulin(rep[i + 1], rep[i]);

            time_x = rep[i + 2];

            LocalTime atd = MedtronicUtil.getTimeFrom30MinInterval(time_x);

            if ((i != 0) && (time_x == 0)) {
                break;
            }

            //String value = i18nControl.getMessage("CFG_BASE_FROM") + "=" + atd.getTimeString() + ", "
            //        + i18nControl.getMessage("CFG_BASE_AMOUNT") + "=" + vald;

            //writeSetting(key, value, value, PumpConfigurationGroup.Basal);


        }

        return basalProfile;
    }


    private Integer decodeBatteryStatus(byte[] rawData) {
        //00 00 7C 00 00

        if (rawData.length <= 2) {

            int status = rawData[0];

            if (status == 0) {
                return 75; // NORMAL
            } else if (status == 1) {
                return 20; // LOW
            } else if (status == 2) {

            }

            return null;
        } else {
            // if response in 3 bytes then we add additional information
            //double d = MedtronicUtil.makeUnsignedShort(rawData[2], rawData[1]) / 100.0d;

            double d = ByteUtil.toInt(rawData[1], rawData[2]) / 100.0d;

            double perc = (d - BatteryType.Alkaline.lowVoltage) / (BatteryType.Alkaline.highVoltage - BatteryType.Alkaline.lowVoltage);

            LOG.warn("Percent status: " + perc);
            LOG.warn("Unknown status: " + rawData[0]);
            LOG.warn("Full result: " + d);

            return (int) d;
        }

    }


    protected Float decodeRemainingInsulin(byte[] rawData) {
        //float value = MedtronicUtil.makeUnsignedShort(rawData[0], rawData[1]) / 10.0f;

        float value = ByteUtil.toInt(rawData[0], rawData[1]) / 10.0f;

        System.out.println("Remaing insulin: " + value);
        return value;
    }


    private LocalDateTime decodeTime(byte[] rawContent) {
        //LocalDateTime pumpTime = parsePumpRTCBytes(ByteUtil.substring(receivedData, 2, 7));


        int hours = ByteUtil.asUINT8(rawContent[0]);
        int minutes = ByteUtil.asUINT8(rawContent[1]);
        int seconds = ByteUtil.asUINT8(rawContent[2]);
        int year = (ByteUtil.asUINT8(rawContent[4]) & 0x3f) + 1984;
        int month = ByteUtil.asUINT8(rawContent[5]);
        int day = ByteUtil.asUINT8(rawContent[6]);
        try {
            LocalDateTime pumpTime = new LocalDateTime(year, month, day, hours, minutes, seconds);
            return pumpTime;
        } catch (IllegalFieldValueException e) {
            LOG.error("parsePumpRTCBytes: Failed to parse pump time value: year=%d, month=%d, hours=%d, minutes=%d, seconds=%d", year, month, day, hours, minutes, seconds);
            return null;
        }

    }
}
