package com.neelesh.meesho;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
//            ArrayList<String> paths = extras.getStringArrayList("paths");
            ArrayList<String> paths = extras.getStringArrayList("paths");
            Log.wtf("paths", paths.toString());
            int size = paths.size();

            ImageView result = (ImageView) findViewById(R.id.resultIV);

            int max_w = 0;
            int max_h = 0;
            int total_w = 0;
            for (int i=0; i<paths.size(); i++) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(paths.get(i),options);
                max_w = Math.max(options.outWidth,max_w);
                max_h = Math.max(options.outHeight,max_h);
                total_w += options.outWidth;
            }
            max_h = max_h*max_w/total_w;
            Bitmap new_bitmap = processingBitmap(paths, max_w, max_h, total_w);
            result.setImageBitmap(new_bitmap);
            /********************************************************************************/
            // to save the image
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
//                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            final String loc = mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png";

            File mediaFile = new File(loc);
            OutputStream os = null;
            try {
                mediaFile.createNewFile();
                os = new FileOutputStream(mediaFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                new_bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                os.write(bytes.toByteArray());
            } catch(IOException e) {
                Log.e("saveImages", "problem saving images", e);
            }
            Log.wtf("res path", loc);
//            new_bitmap.recycle();

            Button share_button = (Button) findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(loc));

                    share.setType("image/*");
                    startActivity(share);
                    startActivity(Intent.createChooser(share, "Share Image"));
                }
            });
        }
    }

    private Bitmap processingBitmap(ArrayList<String>paths, int max_w, int max_h, int total_w){
        Bitmap res = null;
        Log.wtf("max_w", String.valueOf(max_w));
        Log.wtf("max_h", String.valueOf(max_h));
        res = Bitmap.createBitmap(max_w,max_h, Bitmap.Config.RGB_565);

        Canvas cs = new Canvas(res);

        int start = 0;
        for (int i=0; i<paths.size(); ++i) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(paths.get(i),options);

            options.inSampleSize = calculateInSampleSize(options.outWidth, max_w, total_w);
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeFile(paths.get(i), options);
            cs.drawBitmap(bm,start,0,null);

            start +=options.outWidth;
            bm.recycle();
        }

        return res;
    }

    public static int calculateInSampleSize( int cur, int max_w, int total_w
            ) {

        Log.wtf("insamplesize", String.valueOf(max_w*cur/total_w));
        return total_w/cur;
    }

}
