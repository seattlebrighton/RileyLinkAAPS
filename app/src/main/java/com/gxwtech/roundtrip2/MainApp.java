package com.gxwtech.roundtrip2;

import android.app.Application;
import android.content.res.Resources;

import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicConst;
import info.nightscout.utils.SP;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Tim on 15/06/2016.
 */
public class MainApp extends Application {

    private static MainApp sInstance;
    private static ServiceClientConnection serviceClientConnection;
    public static Resources sResources;


    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        serviceClientConnection = new ServiceClientConnection();

        //initialize Realm
        Realm.init(instance());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name("rt2.realm").schemaVersion(0).deleteRealmIfMigrationNeeded() // TODO: 03/08/2016 @TIM remove
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        sResources = getResources();

        // you need to set frequency here (US = R.string.medtronic_pump_frequency_us, WW = R.string.medtronic_pump_frequency_worldwide)
        SP.putString(MedtronicConst.Prefs.PumpFrequency, gs(R.string.medtronic_pump_frequency_us));

        //SP.putString(MedtronicConst.Prefs.RileyLinkAddress, "CD:72:E1:4C:D5:9D");
    }


    public static MainApp instance() {
        return sInstance;
    }


    public static ServiceClientConnection getServiceClientConnection() {
        if (serviceClientConnection == null) {
            serviceClientConnection = new ServiceClientConnection();
        }
        return serviceClientConnection;
    }

    // TODO: 09/07/2016 @TIM uncomment ServiceClientConnection once class is added


    public static String gs(int id) {
        return sResources.getString(id);
    }


}