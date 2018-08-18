package info.nightscout.androidaps.plugins.PumpOmnipod.defs;

import org.apache.commons.lang3.NotImplementedException;

public class NonceState {
    private int[] table;
    private int index;

    public NonceState(int lot, int tid) {
        InitializeTable(lot, tid, (byte)0);

    }

    public NonceState(int lot, int tid, byte seed) {

        InitializeTable(lot, tid, seed);
    }

    private void InitializeTable(int lot, int tid, byte seed) {
        table = new int[21];
        table[0] = (lot & 0xFFFF) + 0x55543DC3 + (lot >> 16);
        table[0] = table[0] & 0xFFFFFFFF;
        table[1] = (tid & 0xFFFF) + 0xAAAAE44E + (tid >> 16);
        table[1] = table[1] & 0xFFFFFFFF;
        index = 0;
        table[0] += seed;
        for(int i = 0; i< 16;i++) {
            table[2+i] = generateEntry();
        }
        index = (table[0] + table[1]) & 0X0F;


    }

    private int generateEntry() {
        table[0] = (((table[0] >> 16) + (table[0] & 0xFFFF) * 0x5D7F) & 0xFFFFFFFF);
        table[1] = (((table[1] >> 16) + (table[1] & 0xFFFF) * 0x8CA0) & 0xFFFFFFFF);
        return (int)(((long)(table[1]) + ((long)(table[0]) << 16)) & 0xFFFFFFFF);

    }
    public int getCurrentNonce() {
        return table[(2 + index)];

    }

    public void AdvanceToNextNonce() {
        int nonce = getCurrentNonce();
        table[(2 + index)] = generateEntry();
        index = (nonce & 0x0F);
    }
}
