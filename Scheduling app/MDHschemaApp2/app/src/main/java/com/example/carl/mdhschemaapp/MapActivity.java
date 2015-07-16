package com.example.carl.mdhschemaapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MapActivity extends Activity {
    //private ImageView imageView;
    private TouchImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //imageView = (ImageView) findViewById(R.id.map);
        imageView = (TouchImageView) findViewById(R.id.map);
       // imageView.setOnTouchListener(imageViewTouch);

        Bundle b = getIntent().getExtras();
        String room = b.getString("room");
        SetLocation(room);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class Room {
        public final int drawable;
        public final int x;
        public final int y;

        Room(int drawable, int x, int y) {
            this.drawable = drawable;
            this.x = x;
            this.y = y;
        }
    }

    private Room GetRoom(String room) {
        try {
            InputStream iS = getAssets().open("rooms.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] info = line.split("\\s+");
                if (info[1].equals(room.toLowerCase())) {
                    reader.close();
                    iS.close();
                    return new Room(getResources().getIdentifier(info[0], "drawable", getPackageName()), Integer.parseInt(info[2]), Integer.parseInt(info[3]));
                }
            }
            reader.close();
            iS.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void SetLocation(String room) {
        Room loc = GetRoom(room.split("\\s")[0]);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        if (loc != null) {
            Bitmap bm = ImageLoader.getInstance().loadImageSync("drawable://" + loc.drawable);

            Bitmap bmOverlay = bm.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bmOverlay);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            canvas.drawCircle(loc.x, loc.y, 20, paint);
            imageView.setImageBitmap(bmOverlay);
        }
        else if (Character.isLetter(room.charAt(0)) && Character.isDigit(room.charAt(1))) {
            ImageLoader.getInstance().displayImage("drawable://" + getResources().getIdentifier(room.toLowerCase(), "drawable", getPackageName()), imageView);
        }
        else {
            ImageLoader.getInstance().displayImage("drawable://" + R.drawable.u1, imageView);
        }
    }
}
