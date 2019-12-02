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
        initPrinterSettings();

    }

    public void initPrinterSettings(){
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

        final ListPreference basePref = findPreference(getString(R.string.pref_printer_name));

        if(devices.length==0) {
            devices = new CharSequence[1];
            devices[0] = "No Printer Device";
            basePref.setSummary("No Printer Device");
        }else{
            basePref.setSummary(sh.getString(getString(R.string.pref_printer_name), getString(R.string.default_printer_name)));
        }

        basePref.setEntries(devices);
        basePref.setEntryValues(devices);

        basePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    basePref.setSummary(String.valueOf(newValue));
                    return true;
                }
        );
    }


}
