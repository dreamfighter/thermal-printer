package id.dreamfighter.android.thermalprinter.utils;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import id.dreamfighter.android.thermalprinter.enums.TableColumn;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

public class Epp200PrintBuilder {
    private StringBuffer sb = new StringBuffer();
    private final int LENGTH = 33;
    private PrinterUtils printerUtils;
    private OutputStream outputStream;
    private ConnectableObservable<OutputStream> observable;
    public static byte[] NORMAL = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
    public static byte[] LARGE_BOLD = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
    public static byte[] BOLD = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
    public static byte[] BOLD_MEDIUM = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text

    public static Epp200PrintBuilder Build() {
        return new Epp200PrintBuilder();
    }

    public static Epp200PrintBuilder Print(Activity activity) {
        Epp200PrintBuilder builder = new Epp200PrintBuilder();
        builder.printerUtils = PrinterUtils.getInstance(activity);
        //builder.outputStream = builder.printerUtils.ini
        builder.observable = builder.printerUtils.publish();

        return builder;
    }

    public String format(){
        return sb.toString();
    }

    public Epp200PrintBuilder append(final String line) {
        sb.append(line);
        observable.subscribe(bluetoothSocket -> {
            bluetoothSocket.write(line.getBytes());
        });
        return this;
    }

    public Epp200PrintBuilder resetPrint() {
        observable.subscribe(outputStream -> {
            outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(PrinterCommands.FS_FONT_ALIGN);
            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
            outputStream.write(PrinterCommands.LF);
        });
        return this;
    }

    public Epp200PrintBuilder text(final String line,byte[] style) {
        sb.append(line);
        observable.subscribe(bluetoothSocket -> {
            bluetoothSocket.write(style);
            bluetoothSocket.write(line.getBytes());
            bluetoothSocket.write(NORMAL);
        });
        return this;
    }

    public Epp200PrintBuilder newline() {
        sb.append("\n");

        observable.subscribe(bluetoothSocket -> {
            bluetoothSocket.write(PrinterCommands.FEED_LINE);
        });
        return this;
    }

    //print photo
    public Epp200PrintBuilder image(String str,int size) {
        try {

            //byte [] encodeByte = Base64.decode(str,Base64.DEFAULT);
            //Bitmap bmp = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            //if(bmp!=null){

                observable.observeOn(Schedulers.io())
                        .map(outputStream1 -> {
                            Rect r = new Rect(0,0,200,200);
                            Bitmap bmp = Bitmap.createBitmap( (int)200, (int)200, Bitmap.Config.ARGB_8888 );
                            Canvas canvas = new Canvas(bmp);
                            Paint paint = new Paint();
                            paint.setColor(Color.WHITE);
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawPaint(paint);

                            paint.setColor(Color.BLACK);
                            paint.setAntiAlias(true);
                            paint.setTextSize(size);
                            paint.setTextAlign(Paint.Align.LEFT);
                            canvas.getClipBounds(r);
                            int cHeight = r.height();
                            int cWidth = r.width();
                            paint.getTextBounds(str, 0, str.length(), r);
                            float x = cWidth / 2f - r.width() / 2f - r.left;
                            float y = cHeight / 2f + r.height() / 2f - r.bottom;
                            canvas.drawText(str, x, y, paint);
                            byte[] command = PrintUtils.decodeBitmap(bmp);
                            outputStream1.write(PrinterCommands.ESC_ALIGN_CENTER);
                            outputStream1.write(command);
                            outputStream1.write(PrinterCommands.FEED_LINE);
                            outputStream1.write(PrinterCommands.ESC_ALIGN_LEFT);
                    return outputStream1;
                }).subscribe(bluetoothSocket -> {
                });
            //}else{
             //   Log.e("Print Photo error", "the file isn't exists");
            //}
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "unable to convert string to bitmap");
        }
        return this;
    }

    //print photo
    public Epp200PrintBuilder image(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(printerUtils.getContext().getResources(),
                    img);
            if(bmp!=null){
                byte[] command = PrintUtils.decodeBitmap(bmp);
                observable.subscribe(bluetoothSocket -> {
                    bluetoothSocket.write(PrinterCommands.ESC_ALIGN_CENTER);
                    bluetoothSocket.write(command);
                    bluetoothSocket.write(PrinterCommands.FEED_LINE);
                    bluetoothSocket.write(PrinterCommands.ESC_ALIGN_LEFT);
                });
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
        return this;
    }

    public Epp200PrintBuilder right(String line){
        sb.append(right(line,LENGTH));

        return this;
    }

    private String right(String line,int length){
        StringBuffer temp = new StringBuffer();
        if(line.length()<length) {
            String r = String.format("%1$" + (length-1) + "s", line);
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(r.getBytes());
            });
            return r;
        }else{
            String l = line.substring(0,length-1);
            temp.append(l).append("\n");
            String r = temp.append(right(line.substring(length-1),length)).toString();
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(r.getBytes());
            });
            return r;
        }
    }

    public Epp200PrintBuilder left(String line){
        sb.append(left(line,LENGTH));
        return this;
    }

    private String left(String line,int length){
        StringBuffer temp = new StringBuffer();
        if(line.trim().length()<length) {
            String r =  String.format("%-" + (length-1) + "s", line.trim());
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(r.getBytes());
            });
            return r;
        }else{
            String l = line.substring(0,length-1);
            temp.append(l).append("\n");
            String r = temp.append(left(line.substring(length-1),length)).toString();
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(r.getBytes());
            });
            return r;
        }
    }

    public Epp200PrintBuilder center(String line){
        sb.append(center(line,LENGTH));
        return this;
    }

    private String center(String line, int length){
        StringBuffer temp = new StringBuffer();
        if(line.length()<length-1) {

            int div = line.length() / 2;
            String start = line.substring(0,div-1);

            temp.append(String.format("%1$" + ((length/2)-1) + "s", start));
            String end = line.substring(div-1);

            temp.append(String.format("%-" + ((length/2)+1) + "s", end));
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(temp.toString().getBytes());
            });
            return temp.toString();
        }else if(line.length()<length){
            String r = String.format("%1$" + (length-1) + "s", line);
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(r.getBytes());
            });
            return r;
        }else{
            String l = line.substring(0,length-1);
            temp.append(l).append("\n");
            String r = temp.append(center(line.substring(length-1),length)).toString();
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write(r.getBytes());
            });
            return r;
        }
    }
    public Epp200PrintBuilder table(List<? extends Object> entities){
        List<Map<String,String>> rows = new ArrayList<>();
        List<Map<String,String>> cols = new ArrayList<>();
        int i = 0;

        for(Object o:entities) {
            Method[] m = o.getClass().getDeclaredMethods();

            Map<String,String> row = new HashMap<String,String>();
            for(Method method:m) {
                TableColumn column = method
                        .getAnnotation(TableColumn.class);
                if(column!=null){
                    if(i==0){
                        Map<String,String> col = new HashMap<String,String>();
                        col.put("key",column.key());
                        col.put("name",column.name());
                        col.put("align",column.align());
                        col.put("index",String.valueOf(column.index()));
                        cols.add(col);
                    }
                    try {
                        row.put(column.key(),String.valueOf(method.invoke(o)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            rows.add(row);
            i++;
        }
        Collections.sort(cols, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> stringStringMap, Map<String, String> t1) {
                return stringStringMap.get("index").compareTo(t1.get("index"));
            }
        });
        return table(rows,cols);
    }

    public Epp200PrintBuilder table(List<Map<String,String>> list,List<Map<String,String>> columnNames){
        Map<String,Integer> colLength = new HashMap<String,Integer>();
        Map<String,Integer> colPecentage = new HashMap<String,Integer>();

        for(Map<String,String> m:list){
            for(String key:m.keySet()) {
                String val = m.get(key);
                Integer l = colLength.get(key);
                if(l==null){
                    l = 0;
                    colLength.put(key,l);
                }

                if(l<val.length()){
                    colLength.put(key,val.length());
                }
            }
        }

        if(columnNames.size()>3){
            sb.append("TO MANY COLUMN");
            observable.subscribe(bluetoothSocket -> {
                bluetoothSocket.write("TO MANY COLUMN".getBytes());
            });
        }else{
            int sum = 0;
            //int cols = LENGTH / colLength.size();
            for(String key:colLength.keySet()){
                sum +=colLength.get(key);
            }
            for(String key:colLength.keySet()){
                int size = (int) (LENGTH * (1.0 * colLength.get(key)/sum));
                colPecentage.put(key, size);
            }

            //row(m,new HashMap<>(),colPecentage,columnNames);
            for(Map<String,String> m:list) {
                row(m,new HashMap<>(),colPecentage,columnNames);
            }
        }

        return this;
    }

    private String row(Map<String,String> line,
                       Map<String,String> format,
                       Map<String,Integer> percentage,
                       List<Map<String,String>> columnNames){
        boolean cntnew = false;
        StringBuffer tmpSb = new StringBuffer();

        Map<String,String> tmpLine = new HashMap<String,String>();

        for(Map<String,String> map:columnNames){
            String key = map.get("key");
            String l = line.get(key);
            int length = percentage.get(key);

//            if(length>2){
//                length = length + 2;
//            }
            if(l.length()<length) {
                String align = map.get("align");
                if("center".equals(align)) {
                    StringBuffer tmp = new StringBuffer();
                    int div = l.length() / 2;
                    String start = l.substring(0,div-1);

                    tmp.append(String.format("%1$" + ((length/2)-1) + "s", start));
                    String end = l.substring(div-1);

                    tmp.append(String.format("%-" + ((length/2)+1) + "s", end));
                    sb.append(tmp.toString());
                    tmpSb.append(tmp.toString());
                }else if("right".equals(align)){
                    sb.append(String.format("%1$" + length + "s", l));
                    tmpSb.append(String.format("%1$" + length + "s", l));
                }else{
                    sb.append(String.format("%-" + length + "s", l));
                    tmpSb.append(String.format("%-" + length + "s", l));
                }
                tmpLine.put(key,"");
            }else{
                String ll = l.substring(0,length);
                sb.append(ll);
                tmpSb.append(ll);
                String sRest = l.substring(length);
                tmpLine.put(key,sRest);
            }
        }

        for(String key:tmpLine.keySet()){
            String l = tmpLine.get(key);
            if(!l.equals("")){
                cntnew = true;
            }
        }

        sb.append("\n");
        tmpSb.append("\n");
        observable.subscribe(bluetoothSocket -> {
            Log.d("PRINT","TABLE="+tmpSb.toString());
            bluetoothSocket.write(tmpSb.toString().getBytes());
        });
        if(cntnew){
            return row(tmpLine,format,percentage,columnNames);
        }
        return "";
    }

    public boolean print(){
        observable.subscribe(outputStream1 -> {
            Log.d("SUBSCRIBE","FINISH");
            outputStream1.flush();
            //printerUtils.finish();
        });
        observable.connect();
        printerUtils.initPrinter();
        return true;
    }
}
