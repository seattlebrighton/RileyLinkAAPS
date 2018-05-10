package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.RoundtripService.RileyLinkServiceMedtronic;

/**
 * Created by geoff on 7/9/16.
 */
public class DiscoverGattServicesTask extends ServiceTask {
    public DiscoverGattServicesTask() {}

    @Override
    public void run() {
        RileyLinkServiceMedtronic.getInstance().rileyLinkBLE.discoverServices();
    }
}
