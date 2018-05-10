package com.gxwtech.roundtrip2.RoundtripService.medtronic;

import android.provider.ContactsContract;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.BasalProfile;
import com.gxwtech.roundtrip2.util.MedtronicUtil;

import org.joda.time.LocalTime;

/**
 * Created by andy on 5/9/18.
 */

public class MedtronicConverter {


    protected BasalProfile decodeProfile(byte[] rep)
    {
        //byte rep[] = minimedReply.getRawData();

        // String profile = getProfileName(minimedReply);

        BasalProfile basalProfile = new BasalProfile();

        // 0x12 0x00 0x00 0x16 0x00 0x11 0x00

        if ((rep.length >= 3) && (rep[2] == 0x3F))
        {
            //String i18value = i18nControl.getMessage("NOT_SET");
            //writeSetting(key, i18value, i18value, PumpConfigurationGroup.Basal);
            return null;
        }

        int time_x;
        double vald;

        for (int i = 0; i < rep.length; i += 3)
        {


            vald = MedtronicUtil.decodeBasalInsulin(rep[i + 1], rep[i]);

            time_x = rep[i + 2];

            LocalTime atd = MedtronicUtil.getTimeFrom30MinInterval(time_x);

            if ((i != 0) && (time_x == 0))
            {
                break;
            }

            //String value = i18nControl.getMessage("CFG_BASE_FROM") + "=" + atd.getTimeString() + ", "
            //        + i18nControl.getMessage("CFG_BASE_AMOUNT") + "=" + vald;

            //writeSetting(key, value, value, PumpConfigurationGroup.Basal);


        }

        return basalProfile;
    }


    private Integer decodeBatteryStatus(byte[] rawData)
    {


        if (rawData.length<=2) {

            int status = rawData[0];

            if (status == 0) {
                return 75; // NORMAL
            } else if (status == 1) {
                return 20; // LOW
            } else if (status == 2) {

            }

            return null;
        }
        else
        {
            // if response in 3 bytes then we add additional information
            double d = MedtronicUtil.makeUnsignedShort(rawData[2], rawData[1]) / 100.0d;


            // LOG.warn("Unknown status: " + status + " Resolved to: " +
            // status_s);
            // LOG.warn("Full result: " + minimedReply.getRawDataForDebug());

            return (int)d;
        }

    }


    protected void decodeRemainingInsulin(byte[] rawData)
    {
        double value = MedtronicUtil.makeUnsignedShort(rawData[1], rawData[0]) / 10.0f;
    }


}
