package com.gxwtech.roundtrip2.RoundtripService.medtronic.Messages;

import com.gxwtech.roundtrip2.util.MedtronicUtil;

/**
 * Created by geoff on 5/29/16.
 */
public class BolusMessageBody extends CarelinkShortMessageBody {

    public BolusMessageBody() { init(new byte[] {0}); }

    public BolusMessageBody(double bolusAmount)
    {
        init(MedtronicUtil.getBolusStrokes(bolusAmount));
    }
}
