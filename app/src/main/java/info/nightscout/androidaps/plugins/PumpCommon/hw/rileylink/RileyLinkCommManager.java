package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFTools;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.FrequencyScanResults;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.FrequencyTrial;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpMessage;
import com.gxwtech.roundtrip2.util.ByteUtil;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioResponse;


/**
 * Created by geoff on 5/30/16.
 */
public abstract class RileyLinkCommManager {

    private static final Logger LOG = LoggerFactory.getLogger(RileyLinkCommManager.class);


    protected final RFSpy rfspy;
    protected final Context context;

    
    //private static final String TAG = "MedtronicCommManager";
    private double[] scanFrequencies; // = {916.45, 916.50, 916.55, 916.60, 916.65, 916.70, 916.75, 916.80};

    protected int pumpAwakeForMinutes = 6;

    protected byte[] pumpID;
    //public boolean DEBUG_PUMPMANAGER = true;

    protected SharedPreferences prefs;
    protected Instant lastGoodPumpCommunicationTime = new Instant(0);
    protected PumpStatus pumpStatus = new PumpStatus();


    public RileyLinkCommManager(Context context, RFSpy rfspy, double[] scanFrequencies) {
        this.context = context;
        this.rfspy = rfspy;
        this.scanFrequencies = scanFrequencies;
    }



    public PumpMessage sendAndListen(RLMessage msg) {
        return sendAndListen(msg,2000);
    }

    // All pump communications go through this function.
    public PumpMessage sendAndListen(RLMessage msg, int timeout_ms) {
        boolean showPumpMessages = true;
        if (showPumpMessages) {
            LOG.info("Sent:"+ByteUtil.shortHexString(msg.getTxData()));
        }
        RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(msg.getTxData()),timeout_ms);
        PumpMessage rval = new PumpMessage(resp.getRadioResponse().getPayload());
        if (rval.isValid()) {
            // Mark this as the last time we heard from the pump.
            rememberLastGoodPumpCommunicationTime();
        }
        if (showPumpMessages) {
            LOG.info("Received:"+ByteUtil.shortHexString(resp.getRadioResponse().getPayload()));
        }
        return rval;
    }


    //protected abstract PumpMessage makePumpMessage(MessageType messageType, MessageBody messageBody);

    //protected abstract PumpMessage makePumpMessage(byte msgType, MessageBody body);

    //protected abstract PumpMessage makePumpMessage(byte[] typeAndBody);





    public void tryoutPacket(byte[] pkt) {
        sendAndListen(makeRLMessage(pkt));
    }

    // TODO we might need to fix this. Maybe make pump awake for shorter time (battery factor for pump)
    public void wakeup(int duration_minutes) {
        // If it has been longer than n minutes, do wakeup.  Otherwise assume pump is still awake.
        // **** FIXME: this wakeup doesn't seem to work well... must revisit
        pumpAwakeForMinutes = duration_minutes;
        Instant lastGood = getLastGoodPumpCommunicationTime();
//        Instant lastGoodPlus = lastGood.plus(new Duration(pumpAwakeForMinutes * 60 * 1000));
        Instant lastGoodPlus = lastGood.plus(new Duration(1 * 60 * 1000));
        Instant now = Instant.now();
        if (now.compareTo(lastGoodPlus) > 0) {
            LOG.info("Waking pump...");
            RLMessage msg = makeRLMessage(RLMessageType.PowerOn, new byte[]{(byte) duration_minutes});
            RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(msg.getTxData()), (byte) 0, (byte) 200, (byte) 0, (byte) 0, 15000, (byte) 0);
            LOG.info( "wakeup: raw response is " + ByteUtil.shortHexString(resp.getRaw()));
        } else {
            LOG.debug("Last pump communication was recent, not waking pump.");
        }
    }

    public void setRadioFrequencyForPump(double freqMHz) {
        rfspy.setBaseFrequency(freqMHz);
    }

    public double tuneForPump() {
        return scanForPump(scanFrequencies);
    }

    public double scanForPump(double[] frequencies) {
        LOG.info("Scanning for pump ID " + pumpID);
        wakeup(pumpAwakeForMinutes);
        FrequencyScanResults results = new FrequencyScanResults();

        for (int i=0; i<frequencies.length; i++) {
            int tries = 3;
            FrequencyTrial trial = new FrequencyTrial();
            trial.frequencyMHz = frequencies[i];
            rfspy.setBaseFrequency(frequencies[i]);
            int sumRSSI = 0;
            for (int j = 0; j<tries; j++) {
                RLMessage msg = makeRLMessage(RLMessageType.ReadSimpleData);
                RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(msg.getTxData()),(byte) 0, (byte) 0, (byte) 0, (byte) 0, rfspy.EXPECTED_MAX_BLUETOOTH_LATENCY_MS, (byte) 0);
                if (resp.wasTimeout()) {
                    LOG.error( String.format("scanForPump: Failed to find pump at frequency %.2f", frequencies[i]));
                } else if (resp.looksLikeRadioPacket()) {
                    RadioResponse radioResponse = new RadioResponse(resp.getRaw());
                    if (radioResponse.isValid()) {
                        sumRSSI += radioResponse.rssi;
                        trial.successes++;
                    } else {
                        LOG.warn("Failed to parse radio response: " + ByteUtil.shortHexString(resp.getRaw()));
                    }
                } else {
                    LOG.error( "scanForPump: raw response is " + ByteUtil.shortHexString(resp.getRaw()));
                }
                trial.tries++;
            }
            sumRSSI += -99.0 * (trial.tries - trial.successes);
            trial.averageRSSI = (double)(sumRSSI) / (double)(trial.tries);
            results.trials.add(trial);
        }
        results.sort(); // sorts in ascending order
        LOG.debug("Sorted scan results:");
        for (int k=0; k<results.trials.size(); k++) {
            FrequencyTrial one = results.trials.get(k);
            LOG.debug(String.format("Scan Result[%d]: Freq=%.2f, avg RSSI = %f",k,one.frequencyMHz, one.averageRSSI));
        }
        FrequencyTrial bestTrial = results.trials.get(results.trials.size()-1);
        results.bestFrequencyMHz = bestTrial.frequencyMHz;
        if (bestTrial.successes > 0) {
            rfspy.setBaseFrequency(results.bestFrequencyMHz);
            return results.bestFrequencyMHz;
        } else {
            LOG.error("No pump response during scan.");
            return 0.0;
        }
    }

    public RLMessage makeRLMessage(RLMessageType type)
    {
        return makeRLMessage(type, null);
    }

    public abstract RLMessage makeRLMessage(RLMessageType type, byte[] data);

    public abstract RLMessage makeRLMessage(byte[] data);



    private int tune_tryFrequency(double freqMHz) {
        rfspy.setBaseFrequency(freqMHz);
        RLMessage msg = makeRLMessage(RLMessageType.ReadSimpleData);
        RadioPacket pkt = new RadioPacket(msg.getTxData());
        RFSpyResponse resp = rfspy.transmitThenReceive(pkt,(byte)0,(byte)0,(byte)0,(byte)0,rfspy.EXPECTED_MAX_BLUETOOTH_LATENCY_MS,(byte)0);
        if (resp.wasTimeout()) {
            LOG.warn(String.format("tune_tryFrequency: no pump response at frequency %.2f",freqMHz));
        } else if (resp.looksLikeRadioPacket()) {
            RadioResponse radioResponse = new RadioResponse(resp.getRaw());
            if (radioResponse.isValid()) {
                LOG.warn(String.format("tune_tryFrequency: saw response level %d at frequency %.2f",radioResponse.rssi,freqMHz));
                return radioResponse.rssi;
            } else {
                LOG.warn("tune_tryFrequency: invalid radio response:"+ByteUtil.shortHexString(radioResponse.getPayload()));
            }
        }
        return 0;
    }

    public double quickTuneForPump(double startFrequencyMHz) {
        double betterFrequency = startFrequencyMHz;
        double stepsize = 0.05;
        for (int tries = 0; tries < 4; tries++) {
            double evenBetterFrequency = quickTunePumpStep(betterFrequency, stepsize);
            if (evenBetterFrequency == 0.0) {
                // could not see the pump at all.
                // Try again at larger step size
                stepsize += 0.05;
            } else {
                if ((int)(evenBetterFrequency * 100) == (int)(betterFrequency * 100)) {
                    // value did not change, so we're done.
                    break;
                }
                betterFrequency = evenBetterFrequency; // and go again.
            }
        }
        if (betterFrequency == 0.0) {
            // we've failed... caller should try a full scan for pump
            LOG.error("quickTuneForPump: failed to find pump");
        } else {
            rfspy.setBaseFrequency(betterFrequency);
            if (betterFrequency != startFrequencyMHz) {
                LOG.info( String.format("quickTuneForPump: new frequency is %.2fMHz", betterFrequency));
            } else {
                LOG.info( String.format("quickTuneForPump: pump frequency is the same: %.2fMHz", startFrequencyMHz));
            }
        }
        return betterFrequency;
    }

    private double quickTunePumpStep(double startFrequencyMHz, double stepSizeMHz) {
        LOG.info("Doing quick radio tune for pump ID " + pumpID);
        wakeup(pumpAwakeForMinutes);
        int startRssi = tune_tryFrequency(startFrequencyMHz);
        double lowerFrequency = startFrequencyMHz - stepSizeMHz;
        int lowerRssi = tune_tryFrequency(lowerFrequency);
        double higherFrequency = startFrequencyMHz + stepSizeMHz;
        int higherRssi = tune_tryFrequency(higherFrequency);
        if ((higherRssi == 0.0) && (lowerRssi == 0.0) && (startRssi == 0.0)) {
            // we can't see the pump at all...
            return 0.0;
        }
        if (higherRssi > startRssi) {
            // need to move higher
            return higherFrequency;
        } else if (lowerRssi > startRssi) {
            // need to move lower.
            return lowerFrequency;
        }
        return startFrequencyMHz;
    }



    private void rememberLastGoodPumpCommunicationTime() {
        lastGoodPumpCommunicationTime = Instant.now();
        SharedPreferences.Editor ed = prefs.edit();
        ed.putLong("lastGoodPumpCommunicationTime",lastGoodPumpCommunicationTime.getMillis());
        ed.commit();
    }

    private Instant getLastGoodPumpCommunicationTime() {
        // If we have a value of zero, we need to load from prefs.
        if (lastGoodPumpCommunicationTime.getMillis() == new Instant(0).getMillis()) {
            lastGoodPumpCommunicationTime = new Instant(prefs.getLong("lastGoodPumpCommunicationTime",0));
            // Might still be zero, but that's fine.
        }
        double minutesAgo = (Instant.now().getMillis() - lastGoodPumpCommunicationTime.getMillis()) / (1000.0 * 60.0);
        LOG.debug("Last good pump communication was " + minutesAgo + " minutes ago.");
        return lastGoodPumpCommunicationTime;
    }




    public PumpStatus getPumpStatus() { return pumpStatus; }



}
