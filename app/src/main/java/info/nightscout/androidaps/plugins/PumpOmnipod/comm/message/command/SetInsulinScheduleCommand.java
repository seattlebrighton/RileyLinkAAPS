package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule.Bolus;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule.DeliverySchedule;

public class SetInsulinScheduleCommand extends MessageBlock {

    private int nonce;
    private DeliverySchedule schedule;

    public SetInsulinScheduleCommand(int nonce, DeliverySchedule schedule) {
        super(null);

        this.nonce = nonce;
        this.schedule = schedule;
        encode();
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        encodedData = ByteUtil.concat(encodedData, schedule.getType().getValue());
        encodedData = ByteUtil.concat(encodedData,
                ByteUtil.substring(
                        ByteUtil.getBytesFromInt(schedule.checksum()), 2,2));
        encodedData = ByteUtil.concat(encodedData, schedule.getRawData());

    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.SetInsulinSchedule;
    }
}
