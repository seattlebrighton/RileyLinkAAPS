package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ConfigResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ErrorResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.StatusError;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.StatusResponse;


public enum MessageBlockType {
    ConfigResponse(0x01),
    StatusError(0x02),
    ConfirmPairing(0x03),
    ErrorResponse(0x06),
    AssignAddress(0x07),
    GetStatus(0x0e),
    BasalScheduleExtra(0x13),
    TempBasalExtra(0x16),
    BolusExtra(0x17),
    ConfigureAlerts(0x19),
    SetInsulinSchedule(0x1a),
    DeactivatePod(0x1c),
    StatusResponse(0x1d),
    CancelDelivery(0x1f);

    byte value;

    MessageBlockType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static MessageBlockType fromByte(byte input) {
        for (MessageBlockType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }

    public MessageBlock Decode(byte[] rawData) {
        switch (this) {
            case ConfigResponse:
                return new ConfigResponse(rawData);
            case ErrorResponse:
                return new ErrorResponse(rawData);
            case StatusError:
                return new StatusError(rawData);
            case StatusResponse:
                return new StatusResponse(rawData);
//            case ConfirmPairing:
//                break;
//            case AssignAddress:
//                break;
//            case GetStatus:
//                break;
//            case BasalScheduleExtra:
//                break;
//            case TempBasalExtra:
//                break;
//            case BolusExtra:
//                break;
//            case ConfigureAlerts:
//                break;
//            case SetInsulinSchedule:
//                break;
//            case DeactivatePod:
//                break;
//            case CancelDelivery:
//                break;
            default:
                throw new NotImplementedException(this.toString());
        }
    }
}
