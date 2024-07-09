package com.example.food_recognition_with_machine_learning_v2.lista_classes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_recognition_with_machine_learning_v2.R;
import com.example.food_recognition_with_machine_learning_v2.ReceitasActivity;

import java.util.ArrayList;
import java.util.List;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassViewHolder> {

    private List<ClassItem> classList;
    private List<ClassItem> classListFull;
    private Context context;
    private String selectedLanguage;
    public static final String CLASSE = "";

    public ClassesAdapter(Context context, List<ClassItem> classList, String selectedLanguage) {
        this.context = context;
        this.classList = classList;
        this.classListFull = new ArrayList<>(classList);
        this.selectedLanguage = selectedLanguage;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.class_item, parent, false);
        return new ClassViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassItem currentItem = classList.get(position);
        holder.textViewName.setText(currentItem.getNomeClasse());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReceitasActivity.class);
            intent.putExtra(CLASSE, currentItem.getIdentificador());
            intent.putExtra("selectedLanguage", selectedLanguage);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public void filter(String text) {
        classList.clear();
        if (text.isEmpty()) {
            classList.addAll(classListFull);
        } else {
            text = text.toLowerCase();
            for (ClassItem item : classListFull) {
                if (item.getNomeClasse().toLowerCase().contains(text)) {
                    classList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
        }
    }
}
