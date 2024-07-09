package com.example.food_recognition_with_machine_learning_v2;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ReceitasActivity extends BaseActivity{
    Database database = new Database(this);
    private String selectedLanguage;
    Button mudarEstadoBotao;
    boolean estadoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receitas_activity);

        //Verificar se tem internet
        isNetworkAvailable();

        String classe = getIntent().getStringExtra(IdentificarComidaActivity.CLASSE);
        mudarEstadoBotao = findViewById(R.id.mudarEstadoBotao);

        //Mostar as informação do database
        setInfoClasse(classe);
        mostrareceita(classe);

        // Traduzir o texto de todos os TextView
        selectedLanguage = getIntent().getStringExtra("selectedLanguage");
        ViewGroup rootView = findViewById(android.R.id.content);
        getAllTextViews(rootView);
        if(!(selectedLanguage.equals("pt"))) {
            translateAllTextViews(selectedLanguage);
        }
    }

    //Colocar as Informaçoes da Classe
    public void setInfoClasse(String nome_classe) {
        SQLiteDatabase db = database.getReadableDatabase();

        String[] projection = {"identificador", "nome_classe", "link_img", "is_marcado", "texto_resumo"};
        String selection = "identificador = ?";
        String[] selectionArgs = {nome_classe};

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
            String texto_resumo = cursor.getString(cursor.getColumnIndexOrThrow("texto_resumo"));
            String link_img = cursor.getString(cursor.getColumnIndexOrThrow("link_img"));
            estadoAtual = cursor.getInt(cursor.getColumnIndexOrThrow("is_marcado")) == 1;

            ((TextView) findViewById(R.id.resumo_texto_classe)).setText(texto_resumo);
            ((TextView) findViewById(R.id.texto_titulo_classe)).setText(nome_correto);
            ImageView imageView = findViewById(R.id.classe_imagem_view);
            Picasso.get().load(link_img).into(imageView);

            //Botão de Marcar ou Desmarcar a receita
            mudarEstadoBotao.setText(estadoAtual ? "Desmarcar" : "Marcar");
            mudarEstadoBotao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    atualizarEstado(nome_classe, !estadoAtual);
                }
            });

            cursor.close();
        } else {
            cursor.close();
        }
    }

    //Funcao para mostrar as três receitas da classe
    public void mostrareceita(String nome_classe) {
        SQLiteDatabase db = database.getReadableDatabase();

        String[] projection = {"titulo", "site", "tempo", "pessoas", "link"};
        String selection = "identificador = ?";
        String[] selectionArgs = {nome_classe};

        Cursor cursor = db.query(
                "receitas",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int i = 1;
        while (cursor.moveToNext() && i <= 3) {
            String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
            String site = cursor.getString(cursor.getColumnIndexOrThrow("site"));
            String tempo = cursor.getString(cursor.getColumnIndexOrThrow("tempo"));
            String pessoas = cursor.getString(cursor.getColumnIndexOrThrow("pessoas"));
            String link = cursor.getString(cursor.getColumnIndexOrThrow("link"));

            int id_titulo = getResources().getIdentifier("receita_titulo" + i, "id", getPackageName());
            ((TextView) findViewById(id_titulo)).setText(titulo);

            int id_site = getResources().getIdentifier("receita_site" + i, "id", getPackageName());
            ((TextView) findViewById(id_site)).setText(site);

            int id_tempo = getResources().getIdentifier("receita_tempo" + i, "id", getPackageName());
            ((TextView) findViewById(id_tempo)).setText(tempo);

            int id_pessoas = getResources().getIdentifier("receita_pessoas" + i, "id", getPackageName());
            ((TextView) findViewById(id_pessoas)).setText(pessoas);

            int id_link = getResources().getIdentifier("receita" + i, "id", getPackageName());
            setLink(id_link, link);

            i++;
        }
        cursor.close();
    }

    //Coloca o link de cada receita
    public void setLink(int id, String link) {
        ((LinearLayout) findViewById(id)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse(link);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                try {
                    startActivity(webIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Nenhum navegador da web encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Atualiza o Estado na Base de dados
    private void atualizarEstado(String classeId, boolean novoEstado) {
        SQLiteDatabase db = database.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("is_marcado", novoEstado ? 1 : 0);

        String selection = "identificador = ?";
        String[] selectionArgs = {classeId};

        int count = db.update(
                "classes",
                values,
                selection,
                selectionArgs
        );

        if (count > 0) {
            Toast.makeText(this, "Receita Marcado", Toast.LENGTH_SHORT).show();
            estadoAtual = novoEstado;
            mudarEstadoBotao.setText(novoEstado ? "Desmarcar" : "Marcar");
        } else {
            Toast.makeText(this, "Erro de marcar a Receita", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void translateAllTextViews(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
        translateAllTextViews();
    }
}