package com.example_files.files;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /*
        1.
            internal storage : /data/data/package name/files etc.....
            external storage : /storage/emulated/0/etc ....
            we can use write and read in internal storage with use external storage codes (just change direction ==>
            internal storage direction is : getFilesDir()
            external storage direction is : Environment.getExternalStorageDirectory();)
            but cant do it in reverse
        2.
            if this code does not work because of runtime permissions
     */
    EditText edt_file_name, edt_file_content;
    Button btn_write, btn_read;
    TextView txt_tv;
    File internal_directory_path, external_public_directory_path, sd_card_path,  external_directory;
    String file_name, file_content;
    CheckBox cb_external_storage, cb_sd_card;
    boolean work_with_external_public_storage, work_with_sd_card;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        btn_write.setOnClickListener(MainActivity.this);
        btn_read.setOnClickListener(MainActivity.this);
        getInternalPath();
        getExternalPath();
        getNameAndContentAndDirection();
        cb_external_storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cb_external_storage.isChecked())
                    cb_sd_card.setChecked(false);
            }
        });
        cb_sd_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cb_sd_card.isChecked())
                    cb_external_storage.setChecked(false);
            }
        });
    }

    private void getNameAndContentAndDirection() {
        file_name = edt_file_name.getText().toString().trim();
        file_content = edt_file_content.getText().toString().trim();
        if (cb_sd_card.isChecked()){
            external_directory = new File(sd_card_path, "my_directory");//into default external directory make file with my_directory name
        } else if (cb_external_storage.isChecked()){
            external_directory = new File(external_public_directory_path, "my_directory");//into default external directory make file with my_directory name
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getExternalPath() {
        StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        List<StorageVolume> storageVolumeList = storageManager.getStorageVolumes();
        StorageVolume storageVolume = storageVolumeList.get(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sd_card_path = new File(storageVolume.getDirectory().getPath() + "/Documents");
        }
        if (sd_card_path.exists())
            txt_tv.append("2.sd card path : "+ sd_card_path.toString()+"\n\n\n");
        external_public_directory_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (external_public_directory_path.exists())
            txt_tv.append("3.internal public storage(android call it ExternalStoragePublicDirectory) path : "+ external_public_directory_path.toString());
    }

    private void getInternalPath() {
        internal_directory_path = getFilesDir();
        txt_tv.setText("1.if you do not choose any of the options external storage or sd card" +
                " the files will be created but they can only be accessed in rooted device or " +
                "through this app. internal private storage path is : "+internal_directory_path.getAbsolutePath()+"\n\n\n");
    }

    private void setupViews() {
        edt_file_name = (EditText) findViewById(R.id.edt_file_name);
        edt_file_content = (EditText) findViewById(R.id.edt_file_content);
        btn_write = (Button) findViewById(R.id.btn_write_file);
        btn_read = (Button) findViewById(R.id.btn_read_file);
        txt_tv = (TextView) findViewById(R.id.txt_tv);
        cb_external_storage = (CheckBox) findViewById(R.id.cb_external_storage);
        cb_sd_card = (CheckBox) findViewById(R.id.cb_sd_card);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onClick(View view) {
        int ID = view.getId();
        work_with_external_public_storage = cb_external_storage.isChecked();
        work_with_sd_card = cb_sd_card.isChecked();
        switch (ID){
            case R.id.btn_write_file:
                boolean write = checkInputEmptyWrite();
                if(write){
                    if(work_with_external_public_storage || work_with_sd_card) {
                        getNameAndContentAndDirection();
                        try {
                            createPublicExternalOrSdCardFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        getNameAndContentAndDirection();
                        createPrivateInternalFile();
                    }
                }
                break;
            case R.id.btn_read_file:
                boolean read = checkInputEmptyRead();
                if(read){
                    if(work_with_external_public_storage || work_with_sd_card){
                        getNameAndContentAndDirection();
                        String readExternalStorage = readFromPublicExternalOrSdCardDirectory();
                        edt_file_content.setText(readExternalStorage);
                    }else {
                        getNameAndContentAndDirection();
                        String content = readPrivateInternalFile();
                        edt_file_content.setText(content);
                    }
                }
                break;
        }
    }

    private boolean checkInputEmptyRead() {
        if(edt_file_name.getText().toString().trim().isEmpty()){
            edt_file_name.setError("EMPTY FILE NAME");
            edt_file_name.requestFocus();
            return false;
        }
        return true;
    }

    private boolean checkInputEmptyWrite() {
        if(edt_file_name.getText().toString().trim().isEmpty()){
            edt_file_name.setError("EMPTY FILE NAME");
            edt_file_name.requestFocus();
            return false;
        }
        if(edt_file_content.getText().toString().trim().isEmpty()){
            edt_file_content.setError("EMPTY FILE CONTENT");
            edt_file_name.requestFocus();
            return false;
        }
        return true;
    }

    //external storage---------------------------------------------------------------external storage
    private String readFromPublicExternalOrSdCardDirectory() {
        file_name = file_name.replace(' ', '_');
        file_name += ".txt";
        File file = new File(external_directory, file_name);
        if(! file.exists()){
            Toast.makeText(MainActivity.this, file_name + " not found!", Toast.LENGTH_LONG).show();
            return "";
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            StringBuilder stringBuilder = new StringBuilder();
            /*
                stringBuilder ==> use for text files
                we should use byte[] bytes for other file types to read them because BufferInputStream read bytes
             */
            while (bufferedInputStream.available() != 0){
                stringBuilder.append((char) bufferedInputStream.read());
            }
            bufferedInputStream.close();
            fileInputStream.close();
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void createPublicExternalOrSdCardFile() throws IOException {
        getExternalWorkRunTimePermission();
        if(! external_directory.exists()){
            external_directory.mkdirs();//not work
            Toast.makeText(this, external_directory.toString(), Toast.LENGTH_SHORT).show();
        }
        if(external_directory.exists()){
            file_name = file_name.replace(' ', '_');
            file_name += ".txt";
            File file = new File(external_directory, file_name);
            Toast.makeText(this, file.toString(), Toast.LENGTH_SHORT).show();
            file.createNewFile();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(file_content.getBytes());
                fileOutputStream.close();
                Toast.makeText(MainActivity.this, "file created in \n"+file.getPath(), Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this, "directory does not build", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getExternalWorkRunTimePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            if(checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},1);
            }
            if (!Environment.isExternalStorageManager()){
                Intent get_permission = new Intent();
                get_permission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(get_permission);
            }
        }
    }
//external storage---------------------------------------------------------------external storage
//internal storage---------------------------------------------------------------internal storage
    private String readPrivateInternalFile() {
        try {
            FileInputStream fileInputStream = openFileInput(file_name);//openFileInput ==> use for internal storage
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";//use to read line by line
            while((line = bufferedReader.readLine()) != null){//when lines finished readLine return null
                stringBuilder.append(line);//if want add to file use append (maybe file exist from past)
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Exception : "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void createPrivateInternalFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(file_name,MODE_PRIVATE);//openFileOutput ==> use for internal storage
            fileOutputStream.write(file_content.getBytes());
            fileOutputStream.close();
            Toast.makeText(MainActivity.this, "file created in internal private storage", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//internal storage---------------------------------------------------------------internal storage
}