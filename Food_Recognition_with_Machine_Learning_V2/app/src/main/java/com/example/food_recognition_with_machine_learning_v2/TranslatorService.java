package com.example.food_recognition_with_machine_learning_v2;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface TranslatorService {
    @Headers({
            "Ocp-Apim-Subscription-Key: bac54e4791514417b1466434b058a6ca",
            "Ocp-Apim-Subscription-Region: francecentral",
            "Content-Type: application/json"
    })
    @POST("/translate?api-version=3.0")
    Call<List<TranslationResponse>> translate(@Query("to") String language, @Body List<TextToTranslate> body);
}

class TranslationRequest {
    private List<TextToTranslate> texts;
    private String to;

    public TranslationRequest(List<TextToTranslate> texts, String to) {
        this.texts = texts;
        this.to = to;
    }

    // Getters and setters (if needed)
}

class TextToTranslate {
    String Text;

    TextToTranslate(String text) {
        this.Text = text;
    }
}

class TranslationResponse {
    List<TranslatedText> translations;
}

class TranslatedText {
    String text;
    String to;
}

