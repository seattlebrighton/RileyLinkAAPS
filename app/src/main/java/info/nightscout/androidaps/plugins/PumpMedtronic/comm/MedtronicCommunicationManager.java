package info.nightscout.androidaps.plugins.PumpMedtronic.comm;

import android.content.Context;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.ISFTable;

import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.HexDump;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.BasalProfile;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.Page;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.RawHistoryPage;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.TempBasalPair;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.history.Record;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.ButtonPressCarelinkMessageBody;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.CarelinkShortMessageBody;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.GetHistoryPageCarelinkMessageBody;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.GetPumpModelCarelinkMessageBody;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.MedtronicConverter;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.MessageBody;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.MessageType;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.PacketType;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.PumpAckMessageBody;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.PumpMessage;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.MedtronicCommandType;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.MedtronicDeviceType;
import info.nightscout.androidaps.plugins.PumpMedtronic.service.RileyLinkMedtronicService;
import info.nightscout.androidaps.plugins.PumpMedtronic.util.MedtronicUtil;


/**
 * Created by geoff on 5/30/16.
 * <p>
 * Split into 2 implementations, so that we can split it by target device. - Andy
 */
public class MedtronicCommunicationManager extends RileyLinkCommunicationManager {

    private static final Logger LOG = LoggerFactory.getLogger(MedtronicCommunicationManager.class);

    // If this changes, you need to change RFSpy configureForRegion
    private static double[] scanFrequenciesUS = {916.45, 916.50, 916.55, 916.60, 916.65, 916.70, 916.75, 916.80};
    private static double[] scanFrequenciesWorldwide = {868.25, 868.30, 868.35, 868.40, 868.45, 868.50, 868.55, 868.60, 868.65};
    static MedtronicCommunicationManager medtronicCommunicationManager;
    private MedtronicConverter medtronicConverter;

    String errorMessage;


    public MedtronicCommunicationManager(Context context, RFSpy rfspy, boolean hasUSfrequency) {
        super(context, rfspy, hasUSfrequency ? scanFrequenciesUS : scanFrequenciesWorldwide);
        //this.pumpID = pumpID;
        //refs = context.getSharedPreferences(RT2Const.serviceLocal.sharedPreferencesKey, Context.MODE_PRIVATE);
        medtronicCommunicationManager = this;
        this.medtronicConverter = new MedtronicConverter();
    }


    @Override
    protected void configurePumpSpecificSettings() {
        pumpStatus = RileyLinkUtil.getMedtronicPumpStatus();
    }


    @Override
    public boolean tryToConnectToDevice() {

        MedtronicDeviceType pumpModel = getPumpModel();

        return pumpModel != MedtronicDeviceType.Unknown_Device;
    }


    public static MedtronicCommunicationManager getInstance() {
        return medtronicCommunicationManager;
    }


    private PumpMessage runCommandWithArgs(PumpMessage msg) {
        PumpMessage rval;
        PumpMessage shortMessage = makePumpMessage(msg.commandType, new CarelinkShortMessageBody(new byte[]{0}));
        // look for ack from short message
        PumpMessage shortResponse = sendAndListen(shortMessage);
        if (shortResponse.commandType == MedtronicCommandType.CommandAck) {
            rval = sendAndListen(msg);
            return rval;
        } else {
            LOG.error("runCommandWithArgs: Pump did not ack Attention packet");
        }
        return new PumpMessage();
    }


    public Page getPumpHistoryPage(int pageNumber) {
        RawHistoryPage rval = new RawHistoryPage();
        wakeup(receiverDeviceAwakeForMinutes);
        PumpMessage getHistoryMsg = makePumpMessage(MedtronicCommandType.GetHistoryData, new GetHistoryPageCarelinkMessageBody(pageNumber));
        //LOG.info("getPumpHistoryPage("+pageNumber+"): "+ByteUtil.shortHexString(getHistoryMsg.getTxData()));
        // Ask the pump to transfer history (we get first frame?)
        PumpMessage firstResponse = runCommandWithArgs(getHistoryMsg);
        //LOG.info("getPumpHistoryPage("+pageNumber+"): " + ByteUtil.shortHexString(firstResponse.getContents()));

        PumpMessage ackMsg = makePumpMessage(MedtronicCommandType.CommandAck, new PumpAckMessageBody());
        GetHistoryPageCarelinkMessageBody currentResponse = new GetHistoryPageCarelinkMessageBody(firstResponse.getMessageBody().getTxData());
        int expectedFrameNum = 1;
        boolean done = false;
        //while (expectedFrameNum == currentResponse.getFrameNumber()) {
        int failures = 0;
        while (!done) {
            // examine current response for problems.
            byte[] frameData = currentResponse.getFrameData();
            if ((frameData != null) && (frameData.length > 0) && currentResponse.getFrameNumber() == expectedFrameNum) {
                // success! got a frame.
                if (frameData.length != 64) {
                    LOG.warn("Expected frame of length 64, got frame of length " + frameData.length);
                    // but append it anyway?
                }
                // handle successful frame data
                rval.appendData(currentResponse.getFrameData());
                RileyLinkMedtronicService.getInstance().announceProgress(((100 / 16) * currentResponse.getFrameNumber() + 1));
                LOG.info("getPumpHistoryPage: Got frame " + currentResponse.getFrameNumber());
                // Do we need to ask for the next frame?
                if (expectedFrameNum < 16) { // This number may not be correct for pumps other than 522/722
                    expectedFrameNum++;
                } else {
                    done = true; // successful completion
                }
            } else {
                if (frameData == null) {
                    LOG.error("null frame data, retrying");
                } else if (currentResponse.getFrameNumber() != expectedFrameNum) {
                    LOG.warn("Expected frame number {}, received {} (retrying)", expectedFrameNum, currentResponse.getFrameNumber());
                } else if (frameData.length == 0) {
                    LOG.warn("Frame has zero length, retrying");
                }
                failures++;
                if (failures == 6) {
                    LOG.error("6 failures in attempting to download frame {} of page {}, giving up.", expectedFrameNum, pageNumber);
                    done = true; // failure completion.
                }
            }
            if (!done) {
                // ask for next frame
                PumpMessage nextMsg = sendAndListen(ackMsg);
                currentResponse = new GetHistoryPageCarelinkMessageBody(nextMsg.getMessageBody().getTxData());
            }
        }
        if (rval.getLength() != 1024) {
            LOG.warn("getPumpHistoryPage: short page.  Expected length of 1024, found length of " + rval.getLength());
        }
        if (!rval.isChecksumOK()) {
            LOG.error("getPumpHistoryPage: checksum is wrong");
        }

        rval.dumpToDebug();

        Page page = new Page();
        //page.parseFrom(rval.getData(),PumpModel.MM522);
        // FIXME
        page.parseFrom(rval.getData(), MedtronicDeviceType.Medtronic_522);

        return page;
    }


    public ArrayList<Page> getAllHistoryPages() {
        ArrayList<Page> pages = new ArrayList<>();

        for(int pageNum = 0; pageNum < 16; pageNum++) {
            pages.add(getPumpHistoryPage(pageNum));
        }

        return pages;
    }


    public ArrayList<Page> getHistoryEventsSinceDate(Instant when) {
        ArrayList<Page> pages = new ArrayList<>();
        for(int pageNum = 0; pageNum < 16; pageNum++) {
            pages.add(getPumpHistoryPage(pageNum));
            for(Page page : pages) {
                for(Record r : page.mRecordList) {
                    LocalDateTime timestamp = r.getTimestamp().getLocalDateTime();
                    LOG.info("Found record: (" + r.getClass().getSimpleName() + ") " + timestamp.toString());
                }
            }
        }
        return pages;
    }


    public String getErrorResponse() {
        return this.errorMessage;
    }


    // See ButtonPressCarelinkMessageBody
    public void pressButton(int which) {
        wakeup(receiverDeviceAwakeForMinutes);
        PumpMessage pressButtonMessage = makePumpMessage(MedtronicCommandType.PushButton, new ButtonPressCarelinkMessageBody(which));
        PumpMessage resp = sendAndListen(pressButtonMessage);
        if (resp.commandType != MedtronicCommandType.CommandAck) {
            LOG.error("Pump did not ack button press.");
        }
    }


    // FIXME
    //@Override
    public RLMessage makeRLMessage(RLMessageType type, byte[] data) {
        switch (type) {
            case PowerOn:
                return makePumpMessage(MedtronicCommandType.RFPowerOn, new CarelinkShortMessageBody(data));

            case ReadSimpleData:
                return makePumpMessage(MedtronicCommandType.PumpModel, new GetPumpModelCarelinkMessageBody());

        }
        return null;
    }


    @Override
    public RLMessage makeRLMessage(byte[] data) {
        return makePumpMessage(data);
    }


    @Override
    public byte[] createPumpMessageContent(RLMessageType type) {
        switch (type) {
            case PowerOn:
                return MedtronicUtil.buildCommandPayload(MessageType.PowerOn, //
                        new byte[]{1, (byte) receiverDeviceAwakeForMinutes});

            case ReadSimpleData:
                return MedtronicUtil.buildCommandPayload(MessageType.GetPumpModel, null);
        }
        return new byte[0];
    }


    //    @Deprecated
    //    protected PumpMessage makePumpMessage(MessageType messageType, MessageBody messageBody) {
    //        PumpMessage msg = new PumpMessage();
    //        msg.init(PacketType.Carelink, rileyLinkServiceData.pumpIDBytes, messageType, messageBody);
    //        return msg;
    //    }


    //    protected PumpMessage makePumpMessage(byte msgType, MessageBody body) {
    //        return makePumpMessage(MessageType.getByValue(msgType), body);
    //    }


    //    @Deprecated
    //    protected PumpMessage makePumpMessage(MessageType messageType, byte[] body) {
    //        return makePumpMessage(messageType, body == null ? new CarelinkShortMessageBody() : new CarelinkShortMessageBody(body));
    //    }


    protected PumpMessage makePumpMessage(MedtronicCommandType messageType, byte[] body) {
        return makePumpMessage(messageType, body == null ? new CarelinkShortMessageBody() : new CarelinkShortMessageBody(body));
    }


    //    @Deprecated
    //    protected PumpMessage makePumpMessage(MessageType messageType) {
    //        return makePumpMessage(messageType, (byte[]) null);
    //    }


    protected PumpMessage makePumpMessage(MedtronicCommandType messageType) {
        return makePumpMessage(messageType, (byte[]) null);
    }


    protected PumpMessage makePumpMessage(MedtronicCommandType messageType, MessageBody messageBody) {
        PumpMessage msg = new PumpMessage();
        msg.init(PacketType.Carelink, rileyLinkServiceData.pumpIDBytes, messageType, messageBody);
        return msg;
    }


    protected PumpMessage makePumpMessage(byte[] typeAndBody) {
        PumpMessage msg = new PumpMessage();
        msg.init(ByteUtil.concat(ByteUtil.concat(new byte[]{PacketType.Carelink.getValue()}, rileyLinkServiceData.pumpIDBytes), typeAndBody));
        return msg;
    }


    //    private PumpMessage sendAndGetResponse(MessageType messageType) {
    //        return sendAndGetResponse(messageType, null);
    //    }


    //    @Deprecated
    //    private PumpMessage sendAndGetResponse(MessageType messageType, byte[] bodyData) {
    //        // wakeUp
    //        wakeup(receiverDeviceAwakeForMinutes);
    //
    //        // create message
    //        PumpMessage msg;
    //
    //        if (bodyData == null)
    //            msg = makePumpMessage(messageType);
    //        else
    //            msg = makePumpMessage(messageType, bodyData);
    //
    //        // send and wait for response
    //        PumpMessage response = sendAndListen(msg);
    //        return response;
    //    }


    private PumpMessage sendAndGetResponse(MedtronicCommandType commandType) {

        return sendAndGetResponse(commandType, null);
    }


    private PumpMessage sendAndGetResponse(MedtronicCommandType commandType, byte[] bodyData) {
        // wakeUp
        wakeup(receiverDeviceAwakeForMinutes);

        // create message
        PumpMessage msg;

        if (bodyData == null)
            msg = makePumpMessage(commandType);
        else
            msg = makePumpMessage(commandType, bodyData);

        // send and wait for response
        PumpMessage response = sendAndListen(msg);
        return response;
    }


    private Object sendAndGetResponseWithCheck(MedtronicCommandType commandType) {

        PumpMessage response = sendAndGetResponse(commandType);

        String check = checkResponseContent(response, commandType.commandDescription, commandType.expectedLength);

        if (check == null) {

            Object dataResponse = medtronicConverter.convertResponse(commandType, response.getRawContent());

            LOG.debug("Converted response for {} is {}.", commandType.name(), dataResponse);

            return dataResponse;
        } else {
            this.errorMessage = check;
            return null;
        }
    }


    private Object sendAndGetResponseWithCheck(MedtronicCommandType commandType, byte[] bodyData) {

        PumpMessage response = sendAndGetResponse(commandType, bodyData);

        String check = checkResponseContent(response, commandType.commandDescription, commandType.expectedLength);

        if (check == null) {

            Object dataResponse = medtronicConverter.convertResponse(commandType, response.getRawContent());

            LOG.debug("Converted response for {} is {}.", commandType.name(), dataResponse);

            return dataResponse;
        } else {
            this.errorMessage = check;
            return null;
        }
    }


    private String checkResponseContent(PumpMessage response, String method, int expectedLength) {
        byte[] contents = response.getRawContent();

        if (contents != null) {
            if (contents.length >= expectedLength) {
                LOG.trace("{}: Content: {}", method, HexDump.toHexStringDisplayable(contents));
                return null;

            } else {
                String responseData = String.format("%s: Cannot return data. Data is too short [expected=%s, received=%s].", method, "" + expectedLength, "" + contents.length);

                LOG.warn(responseData);
                return responseData;
            }
        } else {
            String responseData = String.format("%s: Cannot return data. Null response.", method);
            LOG.warn(responseData);
            return responseData;
        }
    }


    // PUMP SPECIFIC COMMANDS


    public Float getRemainingInsulin() {

        Object responseObject = sendAndGetResponseWithCheck(MedtronicCommandType.GetRemainingInsulin);

        return responseObject == null ? null : (Float) responseObject;
    }


    public MedtronicDeviceType getPumpModel() {

        Object responseObject = sendAndGetResponseWithCheck(MedtronicCommandType.PumpModel);

        return responseObject == null ? null : (MedtronicDeviceType) responseObject;
    }


    public BasalProfile getBasalProfile() {

        Object responseObject = sendAndGetResponseWithCheck(MedtronicCommandType.GetBasalProfileSTD);

        return responseObject == null ? null : (BasalProfile) responseObject;
    }


    public LocalDateTime getPumpTime() {

        Object responseObject = sendAndGetResponseWithCheck(MedtronicCommandType.RealTimeClock);

        return responseObject == null ? null : (LocalDateTime) responseObject;
    }


    //    // Get Medtronic specific data
    //    private LocalDateTime parsePumpRTCBytes(byte[] bytes) {
    //        if (bytes == null)
    //            return null;
    //        if (bytes.length < 7)
    //            return null;
    //        int hours = ByteUtil.asUINT8(bytes[0]);
    //        int minutes = ByteUtil.asUINT8(bytes[1]);
    //        int seconds = ByteUtil.asUINT8(bytes[2]);
    //        int year = (ByteUtil.asUINT8(bytes[4]) & 0x3f) + 1984;
    //        int month = ByteUtil.asUINT8(bytes[5]);
    //        int day = ByteUtil.asUINT8(bytes[6]);
    //        try {
    //            LocalDateTime pumpTime = new LocalDateTime(year, month, day, hours, minutes, seconds);
    //            return pumpTime;
    //        } catch (IllegalFieldValueException e) {
    //            LOG.error("parsePumpRTCBytes: Failed to parse pump time value: year=%d, month=%d, hours=%d, minutes=%d, seconds=%d", year, month, day, hours, minutes, seconds);
    //            return null;
    //        }
    //    }
    //
    //
    //    @Deprecated
    //    public LocalDateTime getPumpRTC() {
    //        //ReadPumpClockResult rval = new ReadPumpClockResult();
    //        wakeup(receiverDeviceAwakeForMinutes);
    //        PumpMessage getRTCMsg = makePumpMessage(MedtronicCommandType.RealTimeClock, new byte[]{0});
    //        LOG.info("getPumpRTC: " + ByteUtil.shortHexString(getRTCMsg.getTxData()));
    //        PumpMessage response = sendAndListen(getRTCMsg);
    //        if (response.isValid()) {
    //            byte[] receivedData = response.getContents();
    //            if (receivedData != null) {
    //                if (receivedData.length >= 9) {
    //                    LocalDateTime pumpTime = parsePumpRTCBytes(ByteUtil.substring(receivedData, 2, 7));
    //                }
    //            }
    //        } else {
    //            LOG.error("Invalid response: {}", ByteUtil.showPrintable(response.getContents()));
    //        }
    //
    //        return null;
    //        //return rval;
    //    }


    // TODO remove for AAPS
    public ISFTable getPumpISFProfile() {

        PumpMessage response = sendAndGetResponse(MedtronicCommandType.ReadInsulinSensitivities);

        ISFTable table = new ISFTable();
        table.parseFrom(response.getContents());
        return table;
    }


    // TODO remove for AAPS
    public PumpMessage getBolusWizardCarbProfile() {
        PumpMessage response = sendAndGetResponse(MedtronicCommandType.GetCarbohydrateRatios);

        return response;
    }


    // TODO check
    public Integer getRemainingBattery() {
        //PumpMessage response = sendAndGetResponse(MedtronicCommandType.GetBatteryStatus);

        // TODO decode here

        //String check = checkResponseContent(response, "getRemainingBattery", 3);

        Object responseObject = sendAndGetResponseWithCheck(MedtronicCommandType.GetBatteryStatus);

        return responseObject == null ? null : (Integer) responseObject;

        //        if (response.isValid()) {
        //            byte[] remainingBatteryBytes = response.getContents();
        //            if (remainingBatteryBytes != null) {
        //                if (remainingBatteryBytes.length == 5) {
        //                    /**
        //                     * 0x72 0x03, 0x00, 0x00, 0x82
        //                     * meaning what ????
        //                     */
        //
        //                    // FIXME use RawData and decoding is not correct
        //                    // TODO review this !!! Andy
        //
        //                    return ByteUtil.asUINT8(remainingBatteryBytes[5]);
        //                }
        //            }
        //        }


        //return null;
    }


    // FIXME check
    public TempBasalPair getStatus_CurrentTBR() {
        PumpMessage response = sendAndGetResponse(MedtronicCommandType.ReadTemporaryBasal);

        byte[] data = response.getRawContent();

        LOG.debug("Current Basal Rate: {}", HexDump.toHexStringDisplayable(data));


        TempBasalPair tbr = new TempBasalPair(data);

        return tbr;
    }


    // TODO test


    // TODO generateRawData (check if it works correctly) and test
    public PumpMessage setBasalProfile(BasalProfile basalProfile) {

        basalProfile.generateRawData();

        PumpMessage response = sendAndGetResponse(MedtronicCommandType.SetBasalProfileSTD, basalProfile.getRawData());

        // what kind of response are we expecting when set it sent
        return response;
    }


    // TODO test
    public PumpMessage setTBR(TempBasalPair tbr) {

        // TODO check getAs Raw Data is correct data
        // .ChangeTempBasal
        PumpMessage response = sendAndGetResponse(MedtronicCommandType.SetTemporaryBasal, tbr.getAsRawData());

        return response;

    }


    // TODO test
    public PumpMessage cancelTBR() {
        return setTBR(new TempBasalPair(0.0d, false, 0));
    }


    // TODO test ??? this might work correctly, check low value and some high value 25 (25 is max bolus)
    public PumpMessage setBolus(double units) {
        PumpMessage response = sendAndGetResponse(MedtronicCommandType.SetBolus, MedtronicUtil.getBolusStrokes(units));

        return response;
    }


    public PumpMessage cancelBolus() {
        //? maybe suspend and resume
        return null;
    }


    public PumpMessage setExtendedBolus(double units, int duration) {
        // FIXME see decocare
        PumpMessage response = sendAndGetResponse(MedtronicCommandType.SetBolus, MedtronicUtil.getBolusStrokes(units));

        return response;
    }


    public PumpMessage cancelExtendedBolus() {
        // set cancelBolus
        return null;
    }

    // Set TBR
    // Cancel TBR (set TBR 100%)
    // Get Status                  (40%)

    // Set Bolus                            20%
    // Set Extended Bolus                   20%
    // Cancel Bolus                         0% ?
    // Cancel Extended Bolus                0% ?

    // Get Basal Profile (0x92) Read STD    20%
    // Set Basal Profile                    20%
    // Read History                         60%
    // Load TDD                             ?


    public void updatePumpManagerStatus() {
        Integer resp = getRemainingBattery();
        pumpStatus.batteryRemaining = resp == null ? -1 : resp;

        pumpStatus.remainUnits = getRemainingInsulin();

        /* current basal */
        //TempBasalPair basalRate = getCurrentBasalRate();

        // FIXME
        //        byte[] basalRateBytes = resp.getContents();
        //        if (basalRateBytes != null) {
        //            if (basalRateBytes.length == 2) {
        //                /**
        //                 * 0x98 0x06
        //                 * 0x98 is "basal rate"
        //                 * 0x06 is what? Not currently running a temp basal, current basal is "standard" at 0
        //                 */
        //                double basalRate = ByteUtil.asUINT8(basalRateBytes[1]);
        //                pumpStatus.currentBasal = basalRate;
        //            }
        //        }
        // get last bolus amount
        // get last bolus time
        // get tempBasalInProgress
        // get tempBasalRatio
        // get tempBasalRemainMin
        // get tempBasalStart
        // get pump time
        LocalDateTime clockResult = getPumpTime();
        if (clockResult != null) {
            pumpStatus.time = clockResult.toDate();
        }
        // get last sync time

    }


}
