package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble;

import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.operations.BLECommOperationResult;

public interface IRileyLinkBLE {
    void debugService(BluetoothGattService service, int indentCount);

    void registerRadioResponseCountNotification(Runnable notifier);

    boolean isConnected();

    boolean discoverServices();

    boolean enableNotifications();

    void findRileyLink(String RileyLinkAddress);

    // This function must be run on UI thread.
    void connectGatt();

    void disconnect();

    void close();

    BLECommOperationResult setNotification_blocking(UUID serviceUUID, UUID charaUUID);

    // call from main
    BLECommOperationResult writeCharacteristic_blocking(UUID serviceUUID, UUID charaUUID, byte[] value);

    BLECommOperationResult readCharacteristic_blocking(UUID serviceUUID, UUID charaUUID);
}
