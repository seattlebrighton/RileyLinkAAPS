package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.operations;

import android.bluetooth.BluetoothGatt;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RileyLinkBLE;

import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * Created by geoff on 5/26/16.
 */
public abstract class BLECommOperation {
    public boolean timedOut = false;
    public boolean interrupted = false;
    protected byte[] value;
    protected BluetoothGatt gatt;

    protected Semaphore operationComplete = new Semaphore(0,true);

    // This is to be run on the main thread
    public abstract void execute(RileyLinkBLE comm);
    public void gattOperationCompletionCallback(UUID uuid, byte[] value) {}
    public int getGattOperationTimeout_ms() { return 22000;}

    public byte[] getValue() { return value; }
}
