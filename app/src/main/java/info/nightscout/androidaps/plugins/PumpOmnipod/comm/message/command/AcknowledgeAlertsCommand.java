package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.AlertType;

public class AcknowledgeAlertsCommand extends MessageBlock {

    private int nonce;
    private Iterable<AlertType> alertTypes;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.AcknowledgeAlerts;
    }

    public AcknowledgeAlertsCommand(
            int nonce,
            Iterable<AlertType> alertTypes)
    {
        super(null);
        this.nonce = nonce;
        this.alertTypes = alertTypes;
        encode();
    }

    public AcknowledgeAlertsCommand(
            int nonce,
            AlertType alertType)
    {
        this(nonce, Arrays.asList(alertType));
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        byte alertTypeBits = 0;
        for (AlertType alertType:alertTypes) {
            alertTypeBits |= (0x01 << alertType.getValue());
        }
        encodedData = ByteUtil.concat(encodedData, alertTypeBits);
    }
}
