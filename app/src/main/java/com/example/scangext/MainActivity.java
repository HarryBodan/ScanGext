package com.example.scangext;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;

import android.widget.PopupMenu;



public class MainActivity extends AppCompatActivity {

    private Button recognize_text, btn_change_Image;
    private ImageView image;
    private EditText recognized_textET;
    private Uri uri = null;
    private ProgressDialog progressDialog;
    private TextRecognizer textRecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recognize_text = findViewById(R.id.recognize_text);
        image = findViewById(R.id.imagem);
        recognized_textET = findViewById(R.id.recognized_textET);
        btn_change_Image = findViewById(R.id.btn_change_image);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognize_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri==null){
                    Toast.makeText(MainActivity.this, "Por favor seleccione una imagen", Toast.LENGTH_SHORT).show();
                }else{
                    recognizeImageText();
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        btn_change_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
    }

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryARL.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>(){
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        uri = data.getData();
                        image.setImageURI(uri);
                        recognized_textET.setText("");
                        recognize_text.setEnabled(true);
                        btn_change_Image.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(MainActivity.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            );

    private void openCamera(){
        ContentValues values =  new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Titulo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion");

        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        cameraARL.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                       image.setImageURI(uri);
                        recognized_textET.setText("");
                        recognize_text.setEnabled(true);
                        btn_change_Image.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(MainActivity.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void recognizeImageText(){
        progressDialog.setMessage("Preparando imagen");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(this, uri);
            progressDialog.setMessage("Reconociendo texto");
            Task<Text> textTask = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            progressDialog.dismiss();
                            String newText = text.getText();
                            recognized_textET.setText(newText);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "No se pudo reconocer el texto. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error al preparar la imagen. Erro: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_open_gallery){
            openGallery();
           // Toast.makeText(this, "Abrir galería", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId() == R.id.menu_open_camera){
            openCamera();
            //Toast.makeText(this, "Abrir cámara", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.my_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_open_gallery) {
                    openGallery();
                    return true;
                } else if (itemId == R.id.menu_open_camera) {
                    openCamera();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }


}