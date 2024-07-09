package com.example.food_recognition_with_machine_learning_v2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;

import com.example.food_recognition_with_machine_learning_v2.lista_classes.ListaClassesActivity;
import com.example.food_recognition_with_machine_learning_v2.lista_classes.ListaClassesMarcadasActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends BaseActivity {

    Button botaoTirarFoto, botaoParaGaleria, botaoParaVerClasses, botaoParaVerClassesGuardadas;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final String TEMP_IMAGE_FILE_NAME = "temp_image.jpg";
    private File tempImageFile;
    Spinner opcaoLingua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Verificar se tem internet
        isNetworkAvailable();

        //Botões
        botaoTirarFoto = findViewById(R.id.botao_tirar_foto);
        botaoParaGaleria = findViewById(R.id.botao_para_galeria);
        botaoParaVerClasses = findViewById(R.id.botao_para_ver_classes);
        botaoParaVerClassesGuardadas = findViewById(R.id.botao_para_ver_classes_guardadas);

        //Opçoes de Linguas
        opcaoLingua = findViewById(R.id.idioma_spinner);

        // Configura o spinner de linguas
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opcaoLingua.setAdapter(adapter);

        opcaoLingua.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String idioma_selecionado = parent.getItemAtPosition(position).toString();

                ViewGroup rootView = findViewById(android.R.id.content);
                getAllTextViews(rootView);

                idioma_selecionado = idioma_selecionado.toLowerCase();
                translateAllTextViews(idioma_selecionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada selecionado
            }
        });

        //Botao para tirar foto
        botaoTirarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tiraFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(tiraFotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        //Botao para buscar foto na galeria
        botaoParaGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buscarFotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(buscarFotoIntent, REQUEST_IMAGE_PICK);            }
        });

        //Botao para ver classes
        botaoParaVerClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListaClassesActivity.class);
                intent.putExtra("selectedLanguage", selectedLanguage);
                startActivity(intent);
            }
        });

        //Botao para ver classes marcadas
        botaoParaVerClassesGuardadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListaClassesMarcadasActivity.class);
                intent.putExtra("selectedLanguage", selectedLanguage);
                startActivity(intent);            }
        });

        tempImageFile = new File(getExternalCacheDir(), TEMP_IMAGE_FILE_NAME);
    }

    //Ação depois de tirar foto ou buscar foto na galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {     // if do depois tirar foto
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                salvarImagemParaFile(imageBitmap);
                encaminharparaIdentificarComidaActivity();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) { // if do depois buscar foto na galeria
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    salvarImagemParaFile(imageBitmap);
                    encaminharparaIdentificarComidaActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Funcao para guardar imagem temporaria
    private void salvarImagemParaFile(Bitmap bitmap) {
        try {
            FileOutputStream fos = new FileOutputStream(tempImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void encaminharparaIdentificarComidaActivity() {
        Intent intent = new Intent(this, IdentificarComidaActivity.class);
        intent.putExtra("photoPath", tempImageFile.getAbsolutePath());
        intent.putExtra("selectedLanguage", selectedLanguage);
        startActivity(intent);
    }

    private void translateAllTextViews(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
        translateAllTextViews();
    }
}