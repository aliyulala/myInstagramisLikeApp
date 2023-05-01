package com.example.myapp1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.myapp1.databinding.ActivityDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.SQLData;

import javax.xml.transform.Source;

public class detailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();
        database = this.openOrCreateDatabase("Page",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){
            binding.textPictureName.setText("");
            binding.textCommentName.setText("");
            binding.imageView.setImageResource(R.drawable.selectimage);
            binding.button.setVisibility(View.VISIBLE);

        }else{
            int pictureId = intent.getIntExtra("pictureId",1);
            binding.button.setVisibility(View.INVISIBLE);

            try{
                Cursor cursor = database.rawQuery("SELECT * FROM page WHERE id =?",new String[]{String.valueOf(pictureId)});
                int pictureNameIx = cursor.getColumnIndex("picturename");
                int commentNameIx = cursor.getColumnIndex("comment");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.textPictureName.setText(cursor.getString(pictureNameIx));
                    binding.textCommentName.setText(cursor.getString(commentNameIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                }

                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    public void save(View view){

        String name = binding.textPictureName.getText().toString();
        String comment = binding.textCommentName.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{


            database.execSQL("CREATE TABLE IF NOT EXISTS page(id INTEGER PRIMARY KEY, picturename VARCHAR,comment VARCHAR,image BLOB)");

            String sqlSrtring = "INSERT INTO arts (picturename,comment,image) VALUES(?,?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlSrtring);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,comment);
            sqLiteStatement.bindBlob(3,byteArray);
            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();

        }

        Intent intent = new Intent(detailsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        Uri imageData = intentFromResult.getData();
                        //binding.imageView.setImageURI(imageData);
                        try {
                            ImageDecoder.Source source = ImageDecoder.createSource(detailsActivity.this.getContentResolver(), imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //kabul edildi
                    Intent intenttogallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intenttogallery);
                } else {
                    Toast.makeText(detailsActivity.this, "permission nedded", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void selectImage(View view){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"permission needen for gallery",Snackbar.LENGTH_INDEFINITE).setAction("give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }

            }else{
                Intent intenttogallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intenttogallery);
            }

        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"permission needen for gallery",Snackbar.LENGTH_INDEFINITE).setAction("give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

            }else{
                Intent intenttogallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intenttogallery);
            }

        }

    }

    public Bitmap makeSmallerImage(Bitmap image,int maxsize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width/(float) height;
        if(bitmapRatio > 1){
            width = maxsize;
            height = (int)(width/bitmapRatio);
        }else{
            height = maxsize;
            width= (int) (height * bitmapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);
    }
}