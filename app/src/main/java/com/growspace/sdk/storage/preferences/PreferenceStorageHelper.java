package com.growspace.sdk.storage.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceStorageHelper {
    private static final int DEFAULT_DISTANCEALERTCLOSERANGETHRESHOLD = 100;
    private static final int DEFAULT_DISTANCEALERTFARRANGETHRESHOLD = 200;
    private static final boolean DEFAULT_LOGSENABLED = false;
    private static final int DEFAULT_POINTANDTRIGGERANGLEDEVIATION = 20;
    private static final int DEFAULT_POINTANDTRIGGERMAXIMUMDISTANCE = 500;
    private static final boolean DEFAULT_PREF_SHOWSUGGESTUPDATEOS = true;
    private static final boolean DEFAULT_SHOWELEVATIONNOTSUPPORTED = true;
    private static final boolean DEFAULT_SHOWPAIRINGINFO = true;
    private static final int DEFAULT_UWBCHANNEL = 9;
    private static final int DEFAULT_UWBCONFIGTYPE = 1;
    private static final int DEFAULT_UWBPREAMBLEINDEX = 10;
    private static final String DEFAULT_UWBROLE = "Controlee";
    private static final String PREFS_NAME_APP = "UWB_CONNECT_APP_SHARED_PREFERENCES";
    private static final String PREFS_NAME_DISTANCEALERT = "UWB_CONNECT_DISTANCEALERT_SHARED_PREFERENCES";
    private static final String PREFS_NAME_POINTANDTRIGGER = "UWB_CONNECT_POINTANDTRIGGER_SHARED_PREFERENCES";
    private static final String PREFS_NAME_UWB = "UWB_CONNECT_UWB_SHARED_PREFERENCES";
    private static final String PREF_DISTANCEALERTCLOSERANGETHRESHOLD_KEY = "DISTANCEALERTCLOSERANGETHRESHOLD_KEY";
    private static final String PREF_DISTANCEALERTFARRANGETHRESHOLD_KEY = "DISTANCEALERTFARRANGETHRESHOLD_KEY";
    private static final String PREF_LOGSENABLED_KEY = "LOGSENABLED_KEY";
    private static final String PREF_POINTANDTRIGGERANGLEDEVIATION_KEY = "POINTANDTRIGGERANGLEDEVIATION_KEY";
    private static final String PREF_POINTANDTRIGGERMAXIMUMDISTANCE_KEY = "POINTANDTRIGGERMAXIMUMDISTANCE_KEY";
    private static final String PREF_SHOWELEVATIONNOTSUPPORTED_KEY = "SHOWELEVATIONNOTSUPPORTED_KEY";
    private static final String PREF_SHOWPAIRINGINFO_KEY = "SHOWPAIRINGINFO_KEY";
    private static final String PREF_SHOWSUGGESTUPDATEOS_KEY = "PREF_SHOWSUGGESTUPDATEOS_KEY";
    private static final String PREF_UWBCHANNEL_KEY = "UWBCHANNEL_KEY";
    private static final String PREF_UWBCONFIGTYPE_KEY = "UWBCONFIGTYPE_KEY";
    private static final String PREF_UWBPREAMBLEINDEX_KEY = "UWBPREAMBLEINDEX_KEY";
    private static final String PREF_UWBROLE_KEY = "UWBROLE_KEY";
    private final Context mContext;

    public PreferenceStorageHelper(Context context) {
        this.mContext = context;
    }

    public void setShowPairingInfo(boolean z) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_APP, 0).edit();
        edit.putBoolean(PREF_SHOWPAIRINGINFO_KEY, z);
        edit.apply();
    }

    public boolean getShowPairingInfo() {
        return this.mContext.getSharedPreferences(PREFS_NAME_APP, 0).getBoolean(PREF_SHOWPAIRINGINFO_KEY, true);
    }

    public void setShowElevationNotSupported(boolean z) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_APP, 0).edit();
        edit.putBoolean(PREF_SHOWELEVATIONNOTSUPPORTED_KEY, z);
        edit.apply();
    }

    public boolean getShowElevationNotSupported() {
        return this.mContext.getSharedPreferences(PREFS_NAME_APP, 0).getBoolean(PREF_SHOWELEVATIONNOTSUPPORTED_KEY, true);
    }

    public void setShowSuggestUpdateOS(boolean z) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_APP, 0).edit();
        edit.putBoolean(PREF_SHOWSUGGESTUPDATEOS_KEY, z);
        edit.apply();
    }

    public boolean getShowSuggestUpdateOS() {
        return this.mContext.getSharedPreferences(PREFS_NAME_APP, 0).getBoolean(PREF_SHOWSUGGESTUPDATEOS_KEY, true);
    }

    public void setLogsEnabled(boolean z) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).edit();
        edit.putBoolean(PREF_LOGSENABLED_KEY, z);
        edit.apply();
    }

    public boolean getLogsEnabled() {
        return this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).getBoolean(PREF_LOGSENABLED_KEY, false);
    }

    public void setUwbChannel(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).edit();
        edit.putInt(PREF_UWBCHANNEL_KEY, i);
        edit.apply();
    }

    public int getUwbChannel() {
        return this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).getInt(PREF_UWBCHANNEL_KEY, 9);
    }

    public void setUwbPreambleIndex(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).edit();
        edit.putInt(PREF_UWBPREAMBLEINDEX_KEY, i);
        edit.apply();
    }

    public int getUwbPreambleIndex() {
        return this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).getInt(PREF_UWBPREAMBLEINDEX_KEY, 10);
    }

    public void setUwbRole(String str) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).edit();
        edit.putString(PREF_UWBROLE_KEY, str);
        edit.apply();
    }

    public String getUwbRole() {
        return this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).getString(PREF_UWBROLE_KEY, DEFAULT_UWBROLE);
    }

    public void setUwbConfigType(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).edit();
        edit.putInt(PREF_UWBCONFIGTYPE_KEY, i);
        edit.apply();
    }

    public int getUwbConfigType() {
        return this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).getInt(PREF_UWBCONFIGTYPE_KEY, 1);
    }

    public void clearUwbSettings() {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_UWB, 0).edit();
        edit.clear();
        edit.apply();
    }

    public int getDistanceAlertCloseRangeThreshold() {
        return this.mContext.getSharedPreferences(PREFS_NAME_DISTANCEALERT, 0).getInt(PREF_DISTANCEALERTCLOSERANGETHRESHOLD_KEY, 100);
    }

    public void setDistanceAlertCloseRangeThreshold(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_DISTANCEALERT, 0).edit();
        edit.putInt(PREF_DISTANCEALERTCLOSERANGETHRESHOLD_KEY, i);
        edit.apply();
    }

    public int getDistanceAlertFarRangeThreshold() {
        return this.mContext.getSharedPreferences(PREFS_NAME_DISTANCEALERT, 0).getInt(PREF_DISTANCEALERTFARRANGETHRESHOLD_KEY, 200);
    }

    public void setDistanceAlertFarRangeThreshold(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_DISTANCEALERT, 0).edit();
        edit.putInt(PREF_DISTANCEALERTFARRANGETHRESHOLD_KEY, i);
        edit.apply();
    }

    public int getPointAndTriggerMaximumDistance() {
        return this.mContext.getSharedPreferences(PREFS_NAME_POINTANDTRIGGER, 0).getInt(PREF_POINTANDTRIGGERMAXIMUMDISTANCE_KEY, DEFAULT_POINTANDTRIGGERMAXIMUMDISTANCE);
    }

    public void setPointAndTriggerMaximumDistance(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_POINTANDTRIGGER, 0).edit();
        edit.putInt(PREF_POINTANDTRIGGERMAXIMUMDISTANCE_KEY, i);
        edit.apply();
    }

    public int getPointAndTriggerAngleDeviation() {
        return this.mContext.getSharedPreferences(PREFS_NAME_POINTANDTRIGGER, 0).getInt(PREF_POINTANDTRIGGERANGLEDEVIATION_KEY, 20);
    }

    public void setPointAndTriggerAngleDeviation(int i) {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREFS_NAME_POINTANDTRIGGER, 0).edit();
        edit.putInt(PREF_POINTANDTRIGGERANGLEDEVIATION_KEY, i);
        edit.apply();
    }
}
