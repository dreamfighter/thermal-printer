package id.dreamfighter.android.thermalprinter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import id.dreamfighter.android.thermalprinter.fragment.SettingsPreference;
import id.dreamfighter.android.thermalprinter.utils.PrinterUtils;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton mainBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainBtn = findViewById(R.id.fab);
        mainBtn.setOnClickListener(view -> {
            PrinterUtils.getInstance(this).print("\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa This is me testing this app\n Hello World, it's John Oke?\n Testing Bluetooth Printing\n");
            //IntentPrint("\nThis is me testing this app\n Hello World, it's John Oke\n Testing Bluetooth Printing\n");
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_printer_settings,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:{
                getSupportFragmentManager().beginTransaction().replace(R.id.frame,new SettingsPreference()).commit();
                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
