package com.example.food_recognition_with_machine_learning_v2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseActivity extends AppCompatActivity {

    protected TranslatorService translatorService;
    protected List<View> textViewList = new ArrayList<>();
    protected String selectedLanguage = "pt"; // Default

    //funcao para verificar se tem internet
    public void isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            Intent intent = new Intent(this, SemInternetActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configura Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cognitive.microsofttranslator.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        translatorService = retrofit.create(TranslatorService.class);

        // Receber o idioma selecionado
        if (getIntent().getStringExtra("selectedLanguage") != null) {
            selectedLanguage = getIntent().getStringExtra("selectedLanguage");
        }
    }

    // Funcao que busca todos TextView e Button para traduzir
    protected void getAllTextViews(View view) {
        if (view instanceof TextView || view instanceof Button) {
            textViewList.add(view);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                getAllTextViews(viewGroup.getChildAt(i));
            }
        }
    }

    // Funcao que traduz todos TextView e Button
    protected void translateAllTextViews() {
        for (View view : textViewList) {
            String textToTranslate;
            if (view instanceof TextView) {
                textToTranslate = ((TextView) view).getText().toString();
            } else if (view instanceof Button) {
                textToTranslate = ((Button) view).getText().toString();
            } else {
                continue;
            }

            List<TextToTranslate> texts = Collections.singletonList(new TextToTranslate(textToTranslate));
            TranslationRequest request = new TranslationRequest(texts, selectedLanguage);
            Call<List<TranslationResponse>> call = translatorService.translate(selectedLanguage, texts);

            call.enqueue(new Callback<List<TranslationResponse>>() {
                @Override
                public void onResponse(Call<List<TranslationResponse>> call, Response<List<TranslationResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<TranslationResponse> translationResponses = response.body();
                        if (!translationResponses.isEmpty() && !translationResponses.get(0).translations.isEmpty()) {
                            String translatedText = translationResponses.get(0).translations.get(0).text;
                            if (view instanceof TextView) {
                                ((TextView) view).setText(translatedText);
                            } else if (view instanceof Button) {
                                ((Button) view).setText(translatedText);
                            }
                        }
                    } else {
                        if (view instanceof TextView) {
                            ((TextView) view).setText("Translation failed.");
                        } else if (view instanceof Button) {
                            ((Button) view).setText("Translation failed.");
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<TranslationResponse>> call, Throwable t) {
                    if (view instanceof TextView) {
                        ((TextView) view).setText("Error: " + t.getMessage());
                    } else if (view instanceof Button) {
                        ((Button) view).setText("Error: " + t.getMessage());
                    }
                }
            });
        }
    }

}