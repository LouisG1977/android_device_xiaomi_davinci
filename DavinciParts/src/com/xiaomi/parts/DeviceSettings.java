/*
 * Copyright (C) 2018 The Asus-SDM660 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.xiaomi.parts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SELinux;
import android.os.Handler;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.SwitchPreference;
import android.os.SystemProperties;
import android.util.Log;

import com.xiaomi.parts.kcal.KCalSettingsActivity;
import com.xiaomi.parts.ambient.AmbientGesturePreferenceActivity;
import com.xiaomi.parts.speaker.ClearSpeakerActivity;
import com.xiaomi.parts.preferences.VibratorStrengthPreference;
import com.xiaomi.parts.preferences.VibratorCallStrengthPreference;
import com.xiaomi.parts.preferences.VibratorNotifStrengthPreference;
import com.xiaomi.parts.preferences.CustomSeekBarPreference;
import com.xiaomi.parts.preferences.SecureSettingListPreference;
import com.xiaomi.parts.preferences.SecureSettingSwitchPreference;
import com.xiaomi.parts.SuShell;
import com.xiaomi.parts.SuTask;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "DavinciParts";

    public static final String KEY_VIBSTRENGTH = "vib_strength";    
    public static final String KEY_CALL_VIBSTRENGTH = "vib_call_strength";
    public static final String KEY_NOTIF_VIBSTRENGTH = "vib_notif_strength";
    public static final String CATEGORY_DISPLAY = "display";
    public static final String PREF_DEVICE_KCAL = "device_kcal";

    public static final String PERF_MSM_THERMAL = "msmthermal";
    public static final String MSM_THERMAL_PATH = "/sys/module/msm_thermal/parameters/enabled";
    public static final String PERF_CORE_CONTROL = "corecontrol";
    public static final String CORE_CONTROL_PATH = "/sys/module/msm_thermal/core_control/enabled";
    public static final String PERF_VDD_RESTRICTION = "vddrestrict";
    public static final String VDD_RESTRICTION_PATH = "/sys/module/msm_thermal/vdd_restriction/enabled";
    public static final String PREF_CPUCORE = "cpucore";
    public static final String CPUCORE_SYSTEM_PROPERTY = "persist.cpucore.profile";
    public static final String PREF_LKM = "lkmprofile";
    public static final String LKM_SYSTEM_PROPERTY = "persist.lkm.profile";
    public static final String PREF_TCP = "tcpcongestion";
    public static final String TCP_SYSTEM_PROPERTY = "persist.tcp.profile";

    public static final String PREF_USB_FASTCHARGE = "fastcharge";
    public static final String USB_FASTCHARGE_PATH = "/sys/kernel/fast_charge/force_fast_charge";
    public static final String PREF_ENABLE_DIRAC = "dirac_enabled";
    public static final String PREF_HEADSET = "dirac_headset_pref";
    public static final String PREF_PRESET = "dirac_preset_pref";
    public static final String PREF_KEY_FPS_INFO = "fps_info";

    public static final String PREF_MSM_TOUCHBOOST = "touchboost";
    public static final String MSM_TOUCHBOOST_PATH = "/sys/module/msm_performance/parameters/touchboost";
    
    public static final String PREF_GPUBOOST = "gpuboost";
    public static final String GPUBOOST_SYSTEM_PROPERTY = "persist.gpuboost.profile";
    public static final String PREF_CPUBOOST = "cpuboost";
    public static final String CPUBOOST_SYSTEM_PROPERTY = "persist.cpuboost.profile";

    private static final String SELINUX_CATEGORY = "selinux";
    private static final String PREF_SELINUX_MODE = "selinux_mode";
    private static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";

    private static final String PREF_CLEAR_SPEAKER = "clear_speaker_settings";
 
    private Preference mKcal;
    private Preference mClearSpeakerPref;
    private SecureSettingSwitchPreference mMsmThermal;
    private SecureSettingSwitchPreference mCoreControl;
    private SecureSettingSwitchPreference mVddRestrict;
    private SecureSettingListPreference mCPUCORE;
    private SecureSettingListPreference mLKM;
    private SecureSettingListPreference mTCP;
    private SecureSettingSwitchPreference mTouchboost;
    private SecureSettingListPreference mGPUBOOST;
    private SecureSettingListPreference mCPUBOOST;
    private VibratorStrengthPreference mVibratorStrength;
    private VibratorCallStrengthPreference mVibratorCallStrength;
    private VibratorNotifStrengthPreference mVibratorNotifStrength;
    private Preference mAmbientPref;
    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;
    private SecureSettingSwitchPreference mEnableDirac;
    private SecureSettingListPreference mHeadsetType;
    private SecureSettingListPreference mPreset;

    private static Context mContext;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_davinci_Parts, rootKey);
        mContext = this.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        String device = FileUtils.getStringProp("ro.build.product", "unknown");

        boolean enhancerEnabled;
        try {
            enhancerEnabled = DiracService.sDiracUtils.isDiracEnabled();
        } catch (java.lang.NullPointerException e) {
            getContext().startService(new Intent(getContext(), DiracService.class));
            try {
                enhancerEnabled = DiracService.sDiracUtils.isDiracEnabled();
            } catch (NullPointerException ne) {
                // Avoid crash
                ne.printStackTrace();
                enhancerEnabled = false;
            }
        }

        mEnableDirac = (SecureSettingSwitchPreference) findPreference(PREF_ENABLE_DIRAC);
        mEnableDirac.setOnPreferenceChangeListener(this);
        mEnableDirac.setChecked(enhancerEnabled);

        mHeadsetType = (SecureSettingListPreference) findPreference(PREF_HEADSET);
        mHeadsetType.setOnPreferenceChangeListener(this);

        mPreset = (SecureSettingListPreference) findPreference(PREF_PRESET);
        mPreset.setOnPreferenceChangeListener(this);

        PreferenceCategory displayCategory = (PreferenceCategory) findPreference(CATEGORY_DISPLAY);

        SwitchPreference usbfastCharger = (SwitchPreference) findPreference(PREF_USB_FASTCHARGE);
        usbfastCharger.setEnabled(FileUtils.fileWritable(USB_FASTCHARGE_PATH));
        usbfastCharger.setChecked(FileUtils.getFileValueAsBoolean(USB_FASTCHARGE_PATH, true));
        usbfastCharger.setOnPreferenceChangeListener(this);

        mKcal = findPreference(PREF_DEVICE_KCAL);

        mKcal.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), KCalSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        mAmbientPref = findPreference("ambient_display_gestures");
        mAmbientPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), AmbientGesturePreferenceActivity.class);
                startActivity(intent);
                return true;
            }
        });

        mClearSpeakerPref = (Preference) findPreference(PREF_CLEAR_SPEAKER);
        mClearSpeakerPref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), ClearSpeakerActivity.class);
            startActivity(intent);
            return true;
        });

      mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null)
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        mVibratorCallStrength = (VibratorCallStrengthPreference) findPreference(KEY_CALL_VIBSTRENGTH);
        if (mVibratorCallStrength != null)
            mVibratorCallStrength.setEnabled(VibratorCallStrengthPreference.isSupported());
        mVibratorNotifStrength = (VibratorNotifStrengthPreference) findPreference(KEY_NOTIF_VIBSTRENGTH);
        if (mVibratorNotifStrength != null)
            mVibratorNotifStrength.setEnabled(VibratorNotifStrengthPreference.isSupported());

        if (FileUtils.fileWritable(MSM_THERMAL_PATH)) {
            mMsmThermal = (SecureSettingSwitchPreference) findPreference(PERF_MSM_THERMAL);
            mMsmThermal.setChecked(FileUtils.getFilesValueAsBoolean(MSM_THERMAL_PATH, true));
            mMsmThermal.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PERF_MSM_THERMAL));
        }

        if (FileUtils.fileWritable(CORE_CONTROL_PATH)) {
            mCoreControl = (SecureSettingSwitchPreference) findPreference(PERF_CORE_CONTROL);
            mCoreControl.setChecked(FileUtils.getFileValueAsBoolean(CORE_CONTROL_PATH, true));
            mCoreControl.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PERF_CORE_CONTROL));
        }

        if (FileUtils.fileWritable(VDD_RESTRICTION_PATH)) {
            mVddRestrict = (SecureSettingSwitchPreference) findPreference(PERF_VDD_RESTRICTION);
            mVddRestrict.setChecked(FileUtils.getFileValueAsBoolean(VDD_RESTRICTION_PATH, true));
            mVddRestrict.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PERF_VDD_RESTRICTION));
        }

        mCPUCORE = (SecureSettingListPreference) findPreference(PREF_CPUCORE);
        mCPUCORE.setValue(FileUtils.getStringProp(CPUCORE_SYSTEM_PROPERTY, "0"));
        mCPUCORE.setSummary(mCPUCORE.getEntry());
        mCPUCORE.setOnPreferenceChangeListener(this);
         
        mLKM = (SecureSettingListPreference) findPreference(PREF_LKM);
        mLKM.setValue(FileUtils.getStringProp(LKM_SYSTEM_PROPERTY, "0"));
        mLKM.setSummary(mLKM.getEntry());
        mLKM.setOnPreferenceChangeListener(this);

        mTCP = (SecureSettingListPreference) findPreference(PREF_TCP);
        mTCP.setValue(FileUtils.getStringProp(TCP_SYSTEM_PROPERTY, "0"));
        mTCP.setSummary(mTCP.getEntry());
        mTCP.setOnPreferenceChangeListener(this);

        if (FileUtils.fileWritable(MSM_TOUCHBOOST_PATH)) {
            mTouchboost = (SecureSettingSwitchPreference) findPreference(PREF_MSM_TOUCHBOOST);
            mTouchboost.setChecked(FileUtils.getFileValueAsBoolean(MSM_TOUCHBOOST_PATH, true));
            mTouchboost.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PREF_MSM_TOUCHBOOST));
        }

        mGPUBOOST = (SecureSettingListPreference) findPreference(PREF_GPUBOOST);
        mGPUBOOST.setValue(FileUtils.getStringProp(GPUBOOST_SYSTEM_PROPERTY, "0"));
        mGPUBOOST.setSummary(mGPUBOOST.getEntry());
        mGPUBOOST.setOnPreferenceChangeListener(this);

        mCPUBOOST = (SecureSettingListPreference) findPreference(PREF_CPUBOOST);
        mCPUBOOST.setValue(FileUtils.getStringProp(CPUBOOST_SYSTEM_PROPERTY, "0"));
        mCPUBOOST.setSummary(mCPUBOOST.getEntry());
        mCPUBOOST.setOnPreferenceChangeListener(this);

        // Vibrator Strength
        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }

        SwitchPreference fpsinfo = (SwitchPreference) findPreference(PREF_KEY_FPS_INFO);
        fpsinfo.setChecked(prefs.getBoolean(PREF_KEY_FPS_INFO, false));
        fpsinfo.setOnPreferenceChangeListener(this);

        // SELinux
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);

        mSelinuxPersistence =
        (SwitchPreference) findPreference(PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
        .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
        .contains(PREF_SELINUX_MODE));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_SELINUX_MODE:
                  if (preference == mSelinuxMode) {
                  boolean enabled = (Boolean) value;
                  new SwitchSelinuxTask(getActivity()).execute(enabled);
                  setSelinuxEnabled(enabled, mSelinuxPersistence.isChecked());
                  return true;
                } else if (preference == mSelinuxPersistence) {
                  setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) value);
                  return true;
                }
                break;

	    case PREF_ENABLE_DIRAC:
                try {
                    DiracService.sDiracUtils.setEnabled((boolean) value);
                } catch (java.lang.NullPointerException e) {
                    getContext().startService(new Intent(getContext(), DiracService.class));
                    DiracService.sDiracUtils.setEnabled((boolean) value);
                }
                break;

            case PREF_HEADSET:
                try {
                    DiracService.sDiracUtils.setHeadsetType(Integer.parseInt(value.toString()));
                } catch (java.lang.NullPointerException e) {
                    getContext().startService(new Intent(getContext(), DiracService.class));
                    DiracService.sDiracUtils.setHeadsetType(Integer.parseInt(value.toString()));
                }
                break;

            case PREF_PRESET:
                try {
                    DiracService.sDiracUtils.setLevel(String.valueOf(value));
                } catch (java.lang.NullPointerException e) {
                    getContext().startService(new Intent(getContext(), DiracService.class));
                    DiracService.sDiracUtils.setLevel(String.valueOf(value));
                }
                break;
                
            case PREF_USB_FASTCHARGE:
                FileUtils.setValue(USB_FASTCHARGE_PATH, (boolean) value);
                break;

            case PERF_MSM_THERMAL:
                FileUtils.setValue(MSM_THERMAL_PATH, (boolean) value);
                break;

            case PERF_CORE_CONTROL:
                FileUtils.setValue(CORE_CONTROL_PATH, (boolean) value);
                break;

            case PERF_VDD_RESTRICTION:
                FileUtils.setValue(VDD_RESTRICTION_PATH, (boolean) value);
                break;

            case PREF_CPUCORE:
                mCPUCORE.setValue((String) value);
                mCPUCORE.setSummary(mCPUCORE.getEntry());
                FileUtils.setStringProp(CPUCORE_SYSTEM_PROPERTY, (String) value);
                break;
           
            case PREF_LKM:
                mLKM.setValue((String) value);
                mLKM.setSummary(mLKM.getEntry());
                FileUtils.setStringProp(LKM_SYSTEM_PROPERTY, (String) value);
                break;

             case PREF_TCP:
                mTCP.setValue((String) value);
                mTCP.setSummary(mTCP.getEntry());
                FileUtils.setStringProp(TCP_SYSTEM_PROPERTY, (String) value);
                break;
       
            case PREF_GPUBOOST:
                mGPUBOOST.setValue((String) value);
                mGPUBOOST.setSummary(mGPUBOOST.getEntry());
                FileUtils.setStringProp(GPUBOOST_SYSTEM_PROPERTY, (String) value);
                break;

            case PREF_CPUBOOST:
                mCPUBOOST.setValue((String) value);
                mCPUBOOST.setSummary(mCPUBOOST.getEntry());
                FileUtils.setStringProp(CPUBOOST_SYSTEM_PROPERTY, (String) value);
                break;

            case PREF_KEY_FPS_INFO:
                boolean enabled = (Boolean) value;
                Intent fpsinfo = new Intent(this.getContext(), FPSInfoService.class);
                if (enabled) {
                    this.getContext().startService(fpsinfo);
                } else {
                    this.getContext().stopService(fpsinfo);
                }
                break;
            default:
                break;
        }
        return true;
    }

        private void setSelinuxEnabled(boolean status, boolean persistent) {
          SharedPreferences.Editor editor = getContext()
              .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
          if (persistent) {
            editor.putBoolean(PREF_SELINUX_MODE, status);
          } else {
            editor.remove(PREF_SELINUX_MODE);
          }
          editor.apply();
          mSelinuxMode.setChecked(status);
        }

        private class SwitchSelinuxTask extends SuTask<Boolean> {
          public SwitchSelinuxTask(Context context) {
            super(context);
          }
          @Override
          protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
            if (params.length != 1) {
              Log.e(TAG, "SwitchSelinuxTask: invalid params count");
              return;
            }
            if (params[0]) {
              SuShell.runWithSuCheck("setenforce 1");
            } else {
              SuShell.runWithSuCheck("setenforce 0");
            }
          }

          @Override
          protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
              // Did not work, so restore actual value
              setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
            }
          }
        }

    private boolean isAppNotInstalled(String uri) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}
