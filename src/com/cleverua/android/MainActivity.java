package com.cleverua.android;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.cleverua.android.helper.IOHelper;
import com.cleverua.android.helper.ToastHelper;
import com.cleverua.android.multipart.FilePart;
import com.cleverua.android.multipart.MultipartEntity;
import com.cleverua.android.multipart.StringPart;

public class MainActivity extends BaseActivity {
    
    private EditText name, value, charset;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setPersistent(true); // a hack to prevent OS from killing the activity 
        
        name    = (EditText) findViewById(R.id.name);
        value   = (EditText) findViewById(R.id.value);
        charset = (EditText) findViewById(R.id.charset);
        
        final Button postBtn = (Button) findViewById(R.id.post_btn);
        postBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                log("postBtn: clicked");
                new PostTask().execute();
            }
        });
        
        if (isCleanStart) {
            // put file on internal memory from app assets unless it already exists
            if (!IOHelper.isImagePresent(MainActivity.this)) {
                final AssetManager assetManager = getResources().getAssets();
                try {
                    IOHelper.createImage(assetManager.open("image.jpg"), MainActivity.this);
                } catch (Exception e) {
                    log("onCreate: failed to create image file", e);
                    postBtn.setEnabled(false);
                    ToastHelper.showToast(MainActivity.this, "Failed to create image file: " + e);
                }
            }
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        log("onConfigurationChanged");
        // this prevents the Activity recreation on Configuration changes 
        // (device orientation changes or hardware keyboard open/close).
        // just do nothing on these changes:
        super.onConfigurationChanged(null);
    }
    
    private class PostTask extends AsyncTask<Void, Void, Void> {

        private String tag;
        private ProgressDialog progress;
        private Exception error;
        
        @Override
        protected void onPreExecute() {
            log("onPreExecute");
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Posting..");
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            log("doInBackground");
            
            HttpClient httpclient = null;
            try {
                httpclient = new DefaultHttpClient();
                
                final HttpPost httppost = new HttpPost("http://www.myhost.com");
                
                final File imageFile = IOHelper.getImageFile(MainActivity.this);
                
                final MultipartEntity entity = new MultipartEntity();
                
                String n = name.getText().toString();
                String v = value.getText().toString();
                String c = charset.getText().toString();
                
                StringPart strPart;
                if (TextUtils.isEmpty(c)) {
                    strPart = new StringPart(n, v);
                } else {
                    strPart = new StringPart(n, v, c);
                }
                
                entity.addPart(strPart);
                entity.addPart(new FilePart("picture", imageFile, null, "image/jpeg"));
                
                httppost.setEntity(entity);
                
                final HttpResponse httpResponse = httpclient.execute(httppost);

                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw new Exception("Got HTTP " + statusCode 
                            + " (" + httpResponse.getStatusLine().getReasonPhrase() + ')');
                }
                
                String response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                log("doInBackground: got response:\n" + response);
                
            } catch (Exception e) {
                log("doInBackground: got error", e);
                error = e;
            } finally {
                IOHelper.safelyCloseHttpClient(httpclient);
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            log("onPostExecute");
            progress.dismiss();
            String msg = (error != null) ? "Failed: " + error : "Posted OK";
            ToastHelper.showToast(MainActivity.this, msg);
        }
        
        private String getTag() {
            if (tag == null) {
                tag = getString(R.string.log_tag) + ": " + PostTask.class.getSimpleName();
            }
            return tag;
        }
        
        private void log(String msg) {
            Log.d(getTag(), msg);
        }
        
        private void log(String msg, Throwable e) {
            Log.e(getTag(), msg, e);
        }
    }
}