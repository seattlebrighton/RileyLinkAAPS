package info.nightscout.androidaps.plugins.pump.common.hw.rileylink;

/**
 * Created by andy on 16/05/2018.
 */

public class RileyLinkConst {

    static final String Prefix = "AAPS.RileyLink.";

    public class Intents {

        public static final String RileyLinkReady = Prefix + "RileyLink_Ready";
        public static final String RileyLinkGattFailed = Prefix + "RileyLink_Gatt_Failed";
        //public static final String RileyLinkError = Prefix + "RileyLink_Ready";

        public static final String BluetoothConnected = Prefix + "Bluetooth_Connected";
        public static final String BluetoothReconnected = Prefix + "Bluetooth_Reconnected";
        public static final String BluetoothDisconnected = Prefix + "Bluetooth_Disconnected";
        public static final String RileyLinkDisconnected = Prefix + "RileyLink_Disconnected";

    }

    public class Prefs {

        public static final String PrefPrefix = "pref_rileylink_";
        public static final String RileyLinkAddress = PrefPrefix + "mac_address";

        public static final String LastGoodDeviceCommunicationTime = Prefix + "lastGoodDeviceCommunicationTime";
        public static final String LastGoodDeviceFrequency = Prefix + "LastGoodDeviceFrequency";
    }


}
