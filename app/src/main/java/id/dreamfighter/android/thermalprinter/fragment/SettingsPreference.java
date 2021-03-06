package id.dreamfighter.android.thermalprinter.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import id.dreamfighter.android.thermalprinter.R;

public class SettingsPreference extends PreferenceFragmentCompat {
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_main, rootKey);

        CharSequence[] devices = new CharSequence[0];

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0)
            {
                devices = new CharSequence[pairedDevices.size()];
                int i = 0;
                for(BluetoothDevice device : pairedDevices)
                {
                    devices[i] = device.getName();
                    i++;
                }
            }
            else
            {
                Toast.makeText(getContext(), R.string.message_device_notfound, Toast.LENGTH_LONG).show();
                return;
            }
        }
        catch(Exception ex)
        {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }


        final SharedPreferences sh = getPreferenceManager().getSharedPreferences() ;


        //Pref1

        /*
        final Preference stylePref = findPreference(getString(R.string.pref_api_url));
        stylePref.setSummary(sh.getString(getString(R.string.pref_api_url), getString(R.string.default_api_url)));
        stylePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    stylePref.setSummary(String.valueOf(newValue));
                    return true;
                }
        );

        final Preference keyPref = findPreference(getString(R.string.pref_api_key));
        keyPref.setSummary(sh.getString(getString(R.string.pref_api_key), getString(R.string.default_api_key)));
        keyPref.setOnPreferenceChangeListener((preference, newValue) -> {
            keyPref.setSummary(String.valueOf(newValue));
                    return true;
                }
        );

        final Preference secretPref = findPreference(getString(R.string.pref_api_secret));
        secretPref.setSummary(sh.getString(getString(R.string.pref_api_secret), getString(R.string.default_api_secret)));
        secretPref.setOnPreferenceChangeListener((preference, newValue) -> {
            secretPref.setSummary(String.valueOf(newValue));
                    return true;
                }
        );
        */

        final ListPreference basePref = findPreference(getString(R.string.pref_printer_name));
        basePref.setSummary(sh.getString(getString(R.string.pref_printer_name), getString(R.string.default_printer_name)));

        basePref.setEntries(devices);
        basePref.setEntryValues(devices);

        basePref.setOnPreferenceChangeListener((preference, newValue) -> {
            basePref.setSummary(String.valueOf(newValue));
                    return true;
                }
        );
    }


}
