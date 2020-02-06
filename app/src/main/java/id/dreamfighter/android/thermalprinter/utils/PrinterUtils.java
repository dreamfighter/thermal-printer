package id.dreamfighter.android.thermalprinter.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import id.dreamfighter.android.thermalprinter.R;
import id.dreamfighter.android.thermalprinter.activity.DeviceList;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;

public class PrinterUtils extends Observable<OutputStream> {

    private static PrinterUtils INSTANCE;
    private Activity context;
    private BluetoothSocket socket;
    private BluetoothDevice bluetoothDevice;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private Observer<? super OutputStream> observer;

    private static BluetoothSocket btsocket;

    public static PrinterUtils getInstance(Activity context){
        if(INSTANCE==null){
            INSTANCE = new PrinterUtils();
            INSTANCE.context = context;
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(context,
                            new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                            2);


                }

                btsocket = DeviceList.getSocket();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return INSTANCE;
    }

    @Override
    protected void subscribeActual(Observer<? super OutputStream> observer) {
        //System.out.println("subscribeActual");
        //observer.onNext("test");
        this.observer = observer;
    }

    private String feeding(){
        return "\n\n\n";
    }

    public String epposEpp200(String line){
        int length = 33;
        return line;
    }


    public void print(String txtvalue)
    {
        String finalContent = txtvalue + feeding();
        byte[] buffer = finalContent.getBytes();
        byte[] PrintHeader = { (byte) 0xAA, 0x55,2,0 };
        PrintHeader[3]=(byte) buffer.length;
        InitPrinter();
        if(PrintHeader.length>128){
            Log.d("PRINTER","Value is more than 128 size");
            Toast.makeText(context, "Value is more than 128 size", Toast.LENGTH_LONG).show();
        }else {
            try {

                outputStream.write(finalContent.getBytes());
                outputStream.close();
                socket.close();
            } catch(Exception ex) {
                ex.printStackTrace();
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void printBitmap(Bitmap bmp) {
        if(bmp!=null){

            byte[] buffer = PrintBitmapUtils.decodeBitmap(bmp);
            byte[] PrintHeader = { (byte) 0xAA, 0x55,2,0 };
            PrintHeader[3]=(byte) buffer.length;
            InitPrinter();
            if(PrintHeader.length>128){
                Log.d("PRINTER","Value is more than 128 size");
                Toast.makeText(context, "Value is more than 128 size", Toast.LENGTH_LONG).show();
            }else {
                try {
                    //outputStream.write(ESC_ALIGN_CENTER);
                    outputStream.write(buffer);
                    outputStream.close();
                    socket.close();
                } catch(Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }else{
            Log.e("Print Photo error", "the bitmap isn't exists");
        }
    }

    private void InitPrinter() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            if(!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for(BluetoothDevice device : pairedDevices)
                {

                    if(device.getName().equals(PreferenceManager
                            .getDefaultSharedPreferences(context)
                            .getString(context.getString(R.string.pref_printer_name), context.getString(R.string.default_printer_name)
                    ))) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        break;
                    }
                }

                Thread connectThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            boolean gotuuid = bluetoothDevice
                                    .fetchUuidsWithSdp();
                            UUID uuid = bluetoothDevice.getUuids()[0]
                                    .getUuid();
                            socket = bluetoothDevice
                                    .createRfcommSocketToServiceRecord(uuid);

                            socket.connect();
                            observer.onNext(socket.getOutputStream());
                            //emitter.onNext("Test");
                        } catch (IOException ex) {
                            //runOnUiThread(socketErrorRunnable);
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            socket = null;
                        }
                    }
                });

                connectThread.start();

                /*
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                beginListenForData();
                */
            } else {
                Log.d("PRINTER",context.getString(R.string.message_device_notfound));
                Toast.makeText(context, R.string.message_device_notfound, Toast.LENGTH_LONG).show();
                return;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finish(){
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
