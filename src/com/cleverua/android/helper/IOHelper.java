package com.cleverua.android.helper;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;

import android.content.Context;
import android.util.Log;

public class IOHelper {
	
    private static final String TAG = IOHelper.class.getSimpleName();
    
    private static final char SEP = File.separatorChar;
    
    public static File getImageFile(Context context) {
        return new File(context.getFilesDir().getPath() + SEP + "image.jpg");
    }
    
    public static boolean isImagePresent(Context context) {
        return getImageFile(context).exists();
    }
    
	public static void createImage(InputStream is, Context context) throws IOException {
	    FileOutputStream fos = null;
	    final File imageFile = getImageFile(context);
        try {
            try {
                fos = new FileOutputStream(imageFile);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "createImage: '" + imageFile + "' cannot be opened for writing: " + e);
            }
            
            byte[] buf = new byte[1024];
            int len;
            try {
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
            } catch (IOException e) {
                Log.e(TAG, "createImage: failed to save '" + imageFile + '\'', e);
                throw e;
            }
        } finally {
            safelyCloseCloseable(fos);
            safelyCloseCloseable(is);
        }
	}
	
	public static long getImageFileSize(Context context) {
	    return getImageFile(context).length();
	}
	
	public static void safelyCloseCloseable(Closeable closeable) {
		if (closeable != null) {
			try { 
				closeable.close();
				closeable = null;
			} catch (IOException e) { /* that's ok */ }
		}
	}
	
    public static void safelyCloseHttpClient(HttpClient httpClient) {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
            httpClient = null;
        }
    }
}