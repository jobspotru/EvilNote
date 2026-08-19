package com.evilnote.filesaver;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

@CapacitorPlugin(name = "FileSaver")
public class FileSaverPlugin extends Plugin {

    @PluginMethod
    public void saveToDownloads(PluginCall call) {
        String name = call.getString("name");
        String content = call.getString("content");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                Uri uri = getContext().getContentResolver().insert(Uri.parse("content://media/external/downloads"), values);
                OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                os.write(content.getBytes("UTF-8"));
                os.close();
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File f = new File(dir, name);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(content.getBytes("UTF-8"));
                fos.close();
            }
            JSObject ret = new JSObject();
            ret.put("path", name);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void listDownloads(PluginCall call) {
        try {
            JSONArray arr = new JSONArray();
            Cursor c = getContext().getContentResolver().query(
                Uri.parse("content://media/external/downloads"),
                new String[] { "_id", "_display_name" },
                null, null, "date_added DESC");
            if (c != null) {
                while (c.moveToNext()) {
                    String name = c.getString(1);
                    if (name != null && name.endsWith(".en")) {
                        long id = c.getLong(0);
                        JSONObject o = new JSONObject();
                        o.put("name", name);
                        o.put("uri", "content://media/external/downloads/" + id);
                        arr.put(o);
                    }
                }
                c.close();
            }
            JSObject ret = new JSObject();
            ret.put("files", arr);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void readUri(PluginCall call) {
        String uri = call.getString("uri");
        try {
            InputStream is = getContext().getContentResolver().openInputStream(Uri.parse(uri));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) bos.write(buf, 0, r);
            is.close();
            JSObject ret = new JSObject();
            ret.put("content", bos.toString("UTF-8"));
            call.resolve(ret);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }
}
