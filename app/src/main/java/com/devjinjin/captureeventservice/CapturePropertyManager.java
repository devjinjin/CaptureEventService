package com.devjinjin.captureeventservice;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * ISL_KOREA
 * Created by jylee on 2018-01-02.
 */

public class CapturePropertyManager {

    private static final String PREF_NAME = "mypref";
    private final SharedPreferences mPrefs;

    public CapturePropertyManager(Context pContext) {
        mPrefs = pContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static final String FIELD_SAVE_CAPTURE_LIST = "capture_list";

    public Set<String> getSaveCaptureList() {

        Set<String> saveCaptureList = mPrefs.getStringSet(FIELD_SAVE_CAPTURE_LIST, null);

        return saveCaptureList;
    }

    public void clearSaveCaptureList() {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        Set<String> saveCaptureList = new HashSet<>();
        saveCaptureList.clear();
        mEditor.putStringSet(FIELD_SAVE_CAPTURE_LIST, saveCaptureList);
    }

    public void setSaveCaptureList(String path) {
        if (path != null) {
            SharedPreferences.Editor mEditor = mPrefs.edit();

            Set<String> saveCaptureList = mPrefs.getStringSet(FIELD_SAVE_CAPTURE_LIST, null);
            if (saveCaptureList == null) {
                Set<String> pathSet = new HashSet<>();
                pathSet.add(path);
                mEditor.putStringSet(FIELD_SAVE_CAPTURE_LIST, pathSet);
            } else {
                saveCaptureList.add(path);
                mEditor.putStringSet(FIELD_SAVE_CAPTURE_LIST, saveCaptureList);
            }
            mEditor.apply();
        }
    }
}
