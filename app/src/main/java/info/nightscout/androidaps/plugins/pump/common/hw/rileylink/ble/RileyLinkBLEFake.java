package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble;

import android.bluetooth.BluetoothGattService;

import com.gxwtech.roundtrip2.RT2Const;

import java.util.UUID;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.operations.BLECommOperationResult;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceNotification;

public class RileyLinkBLEFake implements IRileyLinkBLE {

    public RileyLinkBLEFake() {
        RileyLinkUtil.sendBroadcastMessage(RileyLinkConst.Intents.RileyLinkReady);
        RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_BLE_RileyLinkReady), null);

    }

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
