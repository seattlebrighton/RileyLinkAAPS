package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.AlertConfiguration;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class ConfigureAlertsCommand extends MessageBlock {


    private int nonce;
    private AlertConfiguration[] configurations;

    public ConfigureAlertsCommand(byte[] rawData) {
        super(rawData);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ConfigureAlerts;
    }

    public ConfigureAlertsCommand(
            int nonce,
            AlertConfiguration[] configurations
    ) {
        super(null);
        this.nonce = nonce;
        this.configurations = configurations;
        encode();
    }
    private void encode() {
        rawData = ByteUtil.getBytesFromInt(nonce);
        for (AlertConfiguration config:configurations
             ) {
            rawData = ByteUtil.concat(rawData, config.getRawData());
        }
    }
}
