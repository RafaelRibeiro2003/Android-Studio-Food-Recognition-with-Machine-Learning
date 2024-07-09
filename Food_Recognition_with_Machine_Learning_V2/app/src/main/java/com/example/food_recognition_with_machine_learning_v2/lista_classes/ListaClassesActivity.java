package com.example.food_recognition_with_machine_learning_v2.lista_classes;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_recognition_with_machine_learning_v2.BaseActivity;
import com.example.food_recognition_with_machine_learning_v2.R;
import com.example.food_recognition_with_machine_learning_v2.Database;

import java.util.ArrayList;
import java.util.List;

public class ListaClassesActivity extends BaseActivity {
    private ClassesAdapter classesAdapter;
    private Database database;
    private String selectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_classes_activity);

        //Verificar se tem internet
        isNetworkAvailable();

        //Titulo
        ((TextView) findViewById(R.id.texto_titulo_lista)).setText("Lista de todas as Classes");

        database = new Database(this);
        List<ClassItem> classList = getAllClasses();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedLanguage = getIntent().getStringExtra("selectedLanguage");

        classesAdapter = new ClassesAdapter(this, classList, selectedLanguage);
        recyclerView.setAdapter(classesAdapter);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                classesAdapter.filter(newText);
                return false;
            }
        });

        // Traduzir o texto de todos os TextView
        ViewGroup rootView = findViewById(android.R.id.content);
        getAllTextViews(rootView);
        translateAllTextViews(selectedLanguage);
    }

    private List<ClassItem> getAllClasses() {
        List<ClassItem> classList = new ArrayList<>();
        SQLiteDatabase db = database.getReadableDatabase();
        String[] projection = {
                "_id",
                "identificador",
                "nome_classe",
                "is_marcado",
                "texto_resumo"
        };

        Cursor cursor = db.query(
                "classes",
                projection,
                null,
                null,
                null,
                null,
                "nome_classe ASC"
        );

        while (cursor.moveToNext()) {
            String identificador = cursor.getString(cursor.getColumnIndexOrThrow("identificador"));
            String nomeClasse = cursor.getString(cursor.getColumnIndexOrThrow("nome_classe"));
            boolean isMarcado = cursor.getInt(cursor.getColumnIndexOrThrow("is_marcado")) > 0;
            String textoResumo = cursor.getString(cursor.getColumnIndexOrThrow("texto_resumo"));

            classList.add(new ClassItem(identificador, nomeClasse, isMarcado, textoResumo));
        }
        cursor.close();
        return classList;
    }

    private void translateAllTextViews(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
        translateAllTextViews();
    }
}