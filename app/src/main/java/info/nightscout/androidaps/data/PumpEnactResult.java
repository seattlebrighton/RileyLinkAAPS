package info.nightscout.androidaps.data;


import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.R;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PumpEnactResult {
    private static Logger log = LoggerFactory.getLogger(PumpEnactResult.class);

    public boolean success = false;    // request was processed successfully (but possible no change was needed)
    public boolean enacted = false;    // request was processed successfully and change has been made
    public String comment = "";

    // Result of basal change
    public int duration = -1;      // duration set [minutes]
    public double absolute = -1d;      // absolute rate [U/h] , isPercent = false
    public int percent = -1;       // percent of current basal [%] (100% = current basal), isPercent = true
    public boolean isPercent = false;  // if true percent is used, otherwise absolute
    public boolean isTempCancel = false; // if true we are caceling temp basal
    // Result of treatment delivery
    public double bolusDelivered = 0d; // real value of delivered insulin
    public double carbsDelivered = 0d; // real value of delivered carbs

    public boolean queued = false;


    public PumpEnactResult success(boolean success) {
        this.success = success;
        return this;
    }


    public PumpEnactResult enacted(boolean enacted) {
        this.enacted = enacted;
        return this;
    }


    public PumpEnactResult comment(String comment) {
        this.comment = comment;
        return this;
    }


    public PumpEnactResult duration(int duration) {
        this.duration = duration;
        return this;
    }


    public PumpEnactResult absolute(double absolute) {
        this.absolute = absolute;
        return this;
    }


    public PumpEnactResult percent(int percent) {
        this.percent = percent;
        return this;
    }


    public PumpEnactResult isPercent(boolean isPercent) {
        this.isPercent = isPercent;
        return this;
    }


    public PumpEnactResult isTempCancel(boolean isTempCancel) {
        this.isTempCancel = isTempCancel;
        return this;
    }


    public PumpEnactResult bolusDelivered(double bolusDelivered) {
        this.bolusDelivered = bolusDelivered;
        return this;
    }


    public PumpEnactResult carbsDelivered(double carbsDelivered) {
        this.carbsDelivered = carbsDelivered;
        return this;
    }


    public PumpEnactResult queued(boolean queued) {
        this.queued = queued;
        return this;
    }


    public String log() {
        return "Success: " + success + " Enacted: " + enacted + " Comment: " + comment + " Duration: " + duration + " Absolute: " + absolute + " Percent: " + percent + " IsPercent: " + isPercent + " IsTempCancel: " + isTempCancel + " bolusDelivered: " + bolusDelivered + " carbsDelivered: " + carbsDelivered + " Queued: " + queued;
    }


    public String toString() {

        String ret = MainApp.gs(R.string.success) + ": " + success;
        if (enacted) {
            if (bolusDelivered > 0) {
                ret += "\n" + MainApp.gs(R.string.enacted) + ": " + enacted;
                ret += "\n" + MainApp.gs(R.string.comment) + ": " + comment;
                ret += "\n" + MainApp.gs(R.string.smb_shortname) + ": " + bolusDelivered + " " + MainApp.gs(R.string.insulin_unit_shortname);
            } else if (isTempCancel) {
                ret += "\n" + MainApp.gs(R.string.enacted) + ": " + enacted;
                if (!comment.isEmpty()) ret += "\n" + MainApp.gs(R.string.comment) + ": " + comment;
                ret += "\n" + MainApp.gs(R.string.canceltemp);
            } else if (isPercent) {
                ret += "\n" + MainApp.gs(R.string.enacted) + ": " + enacted;
                if (!comment.isEmpty()) ret += "\n" + MainApp.gs(R.string.comment) + ": " + comment;
                ret += "\n" + MainApp.gs(R.string.duration) + ": " + duration + " min";
                ret += "\n" + MainApp.gs(R.string.percent) + ": " + percent + "%";
            } else {
                ret += "\n" + MainApp.gs(R.string.enacted) + ": " + enacted;
                if (!comment.isEmpty()) ret += "\n" + MainApp.gs(R.string.comment) + ": " + comment;
                ret += "\n" + MainApp.gs(R.string.duration) + ": " + duration + " min";
                ret += "\n" + MainApp.gs(R.string.absolute) + ": " + absolute + " U/h";
            }
        } else {
            ret += "\n" + MainApp.gs(R.string.comment) + ": " + comment;
        }
        return ret;
    }


    public String toHtml() {
        return null;
    }


    public JSONObject json(Profile profile) {
        JSONObject result = new JSONObject();

        return result;
    }
}
