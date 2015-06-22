package madvillain.blurpixelatephotos;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {
    ImageView currentImage;
    String mCurrentPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentImage = (ImageView)findViewById(R.id.currentImage);
        findViewById(R.id.SaveButton).setClickable(false);

        findViewById(R.id.CameraButton).setOnClickListener(CameraClick);
        findViewById(R.id.BlurButton).setOnClickListener(BlurClick);
        findViewById(R.id.PixelateButton).setOnClickListener(PixelateClick);
        findViewById(R.id.SaveButton).setOnClickListener(null);
    }

    View.OnClickListener CameraClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent snapPicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            findViewById(R.id.SaveButton).setOnClickListener(SaveClick);
            if (!mCurrentPhotoPath.isEmpty())
                (new File(mCurrentPhotoPath)).delete();
            if (snapPicIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.d("takepictureintent", ex.getMessage());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    snapPicIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(snapPicIntent, 1);
                }
            }
        }
    };

    View.OnClickListener BlurClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (!mCurrentPhotoPath.isEmpty()) {
                currentImage.setImageURI(Uri.parse(mCurrentPhotoPath));
                Bitmap b = blurImage(25);
                currentImage.setImageBitmap(b);
            }
        }
    };

    View.OnClickListener PixelateClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (!mCurrentPhotoPath.isEmpty()) {
                currentImage.setImageURI(Uri.parse(mCurrentPhotoPath));
                Bitmap b = pixelateImage();
                currentImage.setImageBitmap(b);
            }
        }
    };

    View.OnClickListener SaveClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(mCurrentPhotoPath.substring(5));
                ((BitmapDrawable)currentImage.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace(); // prints the exception to system.err
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            addToGallery();
            findViewById(R.id.SaveButton).setOnClickListener(null);
            Toast.makeText(getApplicationContext(), "Image saved!",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            currentImage.setImageURI(Uri.parse(mCurrentPhotoPath));
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_.jpg";

        File storageDir;

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (file.exists()) {
            storageDir = file;
        } else {
            storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/");
        }

        storageDir.mkdirs(); // Creates the directory named by this file, assuming its parents exist.

        File image = new File(storageDir.getAbsolutePath(), imageFileName);
        if (image.createNewFile())
            Log.d("Created file!", image.getAbsolutePath());
        else
            Log.d("rip file", "");

        //File image = File.createTempFile(
        //        imageFileName,  /* prefix */
        //        ".jpg",         /* suffix */
        //        storageDir      /* directory */
        //);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private Bitmap blurImage(int radius) {
        // We need an odd number
        if (radius % 2 == 0)
            return (null);

        // Create new temp bitmap so we do not overwrite the old image before user saves
        Bitmap current = ((BitmapDrawable)currentImage.getDrawable()).getBitmap();
        Bitmap temp = current.copy(current.getConfig(), true);

        RenderScript rs = RenderScript.create(this);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(rs, current, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(radius);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(temp);

        return temp;
    }

    public Bitmap pixelateImage() {
        int pixelationAmount = 30; // hur mycket man vill pixelera
        Bitmap OriginalBitmap = ((BitmapDrawable) currentImage.getDrawable()).getBitmap();
        Bitmap bmOut = Bitmap.createBitmap(OriginalBitmap.getWidth(), OriginalBitmap.getHeight(), OriginalBitmap.getConfig());
        int width = OriginalBitmap.getWidth();
        int height = OriginalBitmap.getHeight();
        int avR,avB,avG; // Medelvärdet av rgb färger
        int pixel, bx, by;

        for(int x = 0; x < width; x += pixelationAmount) { // Genom hela bilden
            for(int y = 0; y < height; y += pixelationAmount) {
                avR = 0; avG = 0; avB =0;

                bx = x + pixelationAmount;
                by = y + pixelationAmount;
                if(by >= height) by = height;
                if(bx >= width)bx = width;
                for(int xx = x; xx < bx; xx++){
                    for(int yy = y; yy < by; yy++){
                        pixel = OriginalBitmap.getPixel(xx, yy);
                        avR += Color.red(pixel);
                        avG += Color.green(pixel);
                        avB += Color.blue(pixel);
                    }
                }

                // Dividera alla med samples tagna för att få ett medel
                avR /= pixelationAmount * pixelationAmount;
                avG /= pixelationAmount * pixelationAmount;
                avB /= pixelationAmount * pixelationAmount;

                for(int xx =x; xx < bx; xx++){
                    for(int yy= y; yy < by; yy++){ // Går över ett block
                        bmOut.setPixel(xx, yy, Color.argb(255, avR, avG,avB)); // Average color för blocksen
                    }
                }
            }
        }
        return bmOut;
    }

    private void addToGallery() {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, mCurrentPhotoPath.substring(5));

        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (!mCurrentPhotoPath.isEmpty())
            (new File(mCurrentPhotoPath)).delete();
    }
}
