package com.example.food_recognition_with_machine_learning_v2.lista_classes;

public class ClassItem {
    private String identificador;
    private String nomeClasse;
    private boolean isMarcado;
    private String textoResumo;

    public ClassItem(String identificador, String nomeClasse, boolean isMarcado, String textoResumo) {
        this.identificador = identificador;
        this.nomeClasse = nomeClasse;
        this.isMarcado = isMarcado;
        this.textoResumo = textoResumo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNomeClasse() {
        return nomeClasse;
    }
}
