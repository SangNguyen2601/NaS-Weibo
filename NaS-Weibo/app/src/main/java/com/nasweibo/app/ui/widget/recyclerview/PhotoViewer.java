package com.nasweibo.app.ui.widget.recyclerview;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.nasweibo.app.R;
import com.nasweibo.app.util.ImageUtils;


public class PhotoViewer extends AppCompatActivity {
    ImageView photoViewer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        photoViewer = findViewById(R.id.img_photo_viewer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String url = bundle.getString("url");
            ImageUtils.displayImageFromUrl(this, url, photoViewer, null);
        }else {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }
}
