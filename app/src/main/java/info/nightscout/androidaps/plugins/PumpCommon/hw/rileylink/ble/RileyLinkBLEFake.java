package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble;

import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.operations.BLECommOperationResult;

public class RileyLinkBLEFake implements IRileyLinkBLE {
    @Override
    public void debugService(BluetoothGattService service, int indentCount) {

    }

    @Override
    public void registerRadioResponseCountNotification(Runnable notifier) {

    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean discoverServices() {
        return true;
    }

    @Override
    public boolean enableNotifications() {
        return true;
    }

    @Override
    public void findRileyLink(String RileyLinkAddress) {

    }

    @Override
    public void connectGatt() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void close() {

    }

    @Override
    public BLECommOperationResult setNotification_blocking(UUID serviceUUID, UUID charaUUID) {
        return new BLECommOperationResult();
    }

    @Override
    public BLECommOperationResult writeCharacteristic_blocking(UUID serviceUUID, UUID charaUUID, byte[] value) {
        return new BLECommOperationResult();
    }

    @Override
    public BLECommOperationResult readCharacteristic_blocking(UUID serviceUUID, UUID charaUUID) {
        return new BLECommOperationResult();
    }
}
