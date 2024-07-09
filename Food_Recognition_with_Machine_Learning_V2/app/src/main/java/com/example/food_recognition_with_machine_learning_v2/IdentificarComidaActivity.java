package com.example.food_recognition_with_machine_learning_v2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.food_recognition_with_machine_learning_v2.ml.Model;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class IdentificarComidaActivity extends MainActivity {

    int imageSize = 224;
    Database database = new Database(this);
    public static final String CLASSE = "";
    private String selectedLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identificarcomidaactivity);

        //Verificar se tem internet
        isNetworkAvailable();

        ImageView imageView = findViewById(R.id.image_view);
        Button botaoVoltar = findViewById(R.id.botao_voltar);
        Button botao_pra_receita = findViewById(R.id.botao_para_receita);

        //Buscar a foto
        String photoPath = getIntent().getStringExtra("photoPath");
        File photoFile = new File(photoPath);
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        imageView.setImageBitmap(bitmap);

        //Classificar a foto usando modelo de machine learning
        String classe_identificado = classifyImage(bitmap);

        //Traduzir tudo que seja texto
        selectedLanguage = getIntent().getStringExtra("selectedLanguage");
        ViewGroup rootView = findViewById(android.R.id.content);
        getAllTextViews(rootView);
        translateAllTextViews(selectedLanguage);

        //Botao para voltar á pagina principal
        botaoVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(IdentificarComidaActivity.this, MainActivity.class);
            startActivity(intent);
        });

        //Botao para ver receita
        botao_pra_receita.setOnClickListener(v -> {
            Intent intent = new Intent(IdentificarComidaActivity.this, ReceitasActivity.class);
            intent.putExtra("selectedLanguage", selectedLanguage);
            intent.putExtra(CLASSE, classe_identificado);
            startActivity(intent);
        });
    }

    //funcao que coloca o nome da classe correto
    public String setNomeClasse(String nome_classe) {
        SQLiteDatabase db = database.getReadableDatabase();

        String[] projection = {"identificador", "nome_classe"};
        // Define a condição da consulta
        String selection = "identificador = ?";
        String[] selectionArgs = {nome_classe};

        // Executa a consulta
        Cursor cursor = db.query(
                "classes",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String nome_correto = cursor.getString(cursor.getColumnIndexOrThrow("nome_classe"));
            cursor.close();
            return nome_correto;
        } else {
            // No rows returned.
            cursor.close();
            return null;
        }
    }

    public String classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Redimensionar a imagem
            Bitmap resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);

            // Cria entradas para referência
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            resizedImage.getPixels(intValues, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());
            int pixel = 0;

            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] receitas =
                    {
                            "alheira", "ameijoas", "arroz_de_cabidela", "arroz_de_feijao", "arroz_de_pato", "arroz_de_polvo", "arroz_de_tomate", "arroz_doce", "azeitonas",
                            "azevias", "bacalhau_a_bras", "bacalhau_a_lagareiro", "bacalhau_com_broa", "bacalhau_com_natas", "bacalhau_com_todos", "bifana", "bitoque",
                            "bola_de_berlim", "bolinhas_de_alheira", "bolo_de_arroz", "bolo_de_bolacha", "bolo_de_cenoura", "bolo_de_chocolate", "bolo_de_mel_da_madeira",
                            "bolo_rainha", "bolo_rei", "brisas_do_liz", "cabrito_assado", "caldeirada_de_peixe", "canja", "caracois", "cataplana_de_marisco ", "cavacas",
                            "chanfana", "chocos_de_setubal", "chocos_grelhados", "cogumelos_salteados", "coscoroes", "cozido_a_portuguesa", "crepes", "enguias_fritas",
                            "ensopado_de_borrego", "entrecosto_grelhado", "esparguete_a_bolonhesa", "espetadas", "farinheira_com_ovos", "farturas", "feijoada",
                            "filetes_de_pesacada ", "fios_de_ovos", "folar_da_pascoa", "folhados_de_salsicha", "francesinha", "frango_assado", "grelhada_mista",
                            "lampreia_de_ovos", "lasanha", "leitao", "leite_creme", "maranhos", "melao_com_presunto", "mexilhao", "migas", "mil_folhas",
                            "moelas_estufadas", "mousse_de_chocolate", "omelete", "ovos_moles", "pampilhos", "panados", "pao_de_lo", "pastel_de_bacalhau",
                            "pastel_de_nata", "pastel_de_tentugal", "pataniscas", "pate", "peixe_dourada", "pimentos_recheados", "polvo_a_lagareiro", "pudim",
                            "queijadas_de_sintra", "regueifa_doce", "rojoes", "salada", "salada_de_frutas", "salada_de_polvo", "salame_de_chocolate", "salmao",
                            "sardinhas_assadas", "sericaia", "serradura", "sopa_alentejana", "sopa_de_caldo_verde", "sopa_de_tomate", "suspiros", "tarte_de_amendoa",
                            "toucinho_do_ceu", "travesseiro_de_noiva", "tronco_de_natal", "waffles"
                    };

            DecimalFormat decimalFormat = new DecimalFormat("0.0%");
            if (maxConfidence < 0.8) {
                ((TextView) findViewById(R.id.texto_classe_identificado)).setText("Prato não reconhecido!");

            } else {
                String porcentagemFormatada = decimalFormat.format(maxConfidence);
                String classe_identificado = receitas[maxPos];
                ((TextView) findViewById(R.id.resultado_percentagem)).setText(porcentagemFormatada);
                ((TextView) findViewById(R.id.texto_classe_identificado)).setText(classe_identificado);
            }


            // Releases model resources if no longer used.
            model.close();
            return receitas[maxPos];
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return "";
    }

    private void translateAllTextViews(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
        translateAllTextViews();
    }
}
