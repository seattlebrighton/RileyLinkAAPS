package info.nightscout.androidaps.plugins.PumpMedtronic.data.dto;

import android.util.Log;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by andy on 7/26/18.
 */
public class BasalProfileUTest {

    private static final String TAG = "BasalProfileUTest";


    @Test
    public void getBasalProfileAsString() throws Exception {

        byte[] testData = new byte[]{0x23, 0x00, 0x00, 0x2E, 0x00, 0x0A, 0x23, 0x00, 0x14, 0x2E, 0x00, 0x1C, 0x23, 0x00, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        BasalProfile profile = new BasalProfile();
        profile.setRawData(testData);

        Log.d(TAG, profile.getBasalProfileAsString());
    }

    //A7 93 53 69 92 01 03 00 00 04 00 02 03 00 04 03 00 06 03 00 08 03 00 0A 03 00 0C 03 00 0E 03 00 10 03 00 12 03 00 14 03 00 16 02 00 18 01 00 1A 02 00 1C 03 00 1E 03 00 20 04 00 22 04 00 24 04 00 26 05 00 28 06


    @Test
    public void getEntries2() {
        byte[] testData = new byte[]{0x03, 0x00, 0x00, 0x04, 0x00, 0x02, 0x03, 0x00, 0x04, 0x03, 0x00, 0x06, 0x03, 0x00, 0x08, 0x03, 0x00, 0x0A, 0x03, 0x00, 0x0C, 0x03, 0x00, 0x0E, 0x03, 0x00, 0x10, 0x03, 0x00, 0x12, 0x03, 0x00, 0x14, 0x03, 0x00, 0x16, 0x02, 0x00, 0x18, 0x01, 0x00, 0x1A, 0x02, 0x00, 0x1C, 0x03, 0x00, 0x1E, 0x03, 0x00, 0x20, 0x04, 0x00, 0x22, 0x04, 0x00, 0x24, 0x04, 0x00, 0x26, 0x05, 0x00, 0x28, 0x06};
  /* from decocare:
  _test_schedule = {'total': 22.50, 'schedule': [
    { 'start': '12:00A', 'rate': 0.80 },
    { 'start': '6:30A', 'rate': 0.95 },
    { 'start': '9:30A', 'rate': 1.10 },
    { 'start': '2:00P', 'rate': 0.95 },
  ]}
  */
        BasalProfile profile = new BasalProfile();
        profile.setRawData(testData);
        List<BasalProfileEntry> entries = profile.getEntries();

        if (entries.isEmpty()) {
            //LOG.error("testParser: failed");
            Assert.fail();
        } else {
            for(int i = 0; i < entries.size(); i++) {
                BasalProfileEntry e = entries.get(i);
                Log.d(TAG, String.format("testParser entry #%d: rate: %.3f, start %d:%d", i, e.rate, e.startTime.getHourOfDay(), e.startTime.getMinuteOfHour()));
            }

            Assert.assertEquals(21, entries.size());
            return;
        }
    }


    @Test
    public void getEntries() {
        byte[] testData = new byte[]{32, 0, 0, 38, 0, 13, 44, 0, 19, 38, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  /* from decocare:
  _test_schedule = {'total': 22.50, 'schedule': [
    { 'start': '12:00A', 'rate': 0.80 },
    { 'start': '6:30A', 'rate': 0.95 },
    { 'start': '9:30A', 'rate': 1.10 },
    { 'start': '2:00P', 'rate': 0.95 },
  ]}
  */
        BasalProfile profile = new BasalProfile();
        profile.setRawData(testData);
        List<BasalProfileEntry> entries = profile.getEntries();

        if (entries.isEmpty()) {
            //LOG.error("testParser: failed");
            Assert.fail();
        } else {
            for(int i = 0; i < entries.size(); i++) {
                BasalProfileEntry e = entries.get(i);
                Log.d(TAG, String.format("testParser entry #%d: rate: %.2f, start %d:%d", i, e.rate, e.startTime.getHourOfDay(), e.startTime.getMinuteOfHour()));
            }

            Assert.assertEquals(4, entries.size());
            return;
        }
    }


}
