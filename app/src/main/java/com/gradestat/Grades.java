package com.gradestat;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.IOException;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.text.DecimalFormat;

import static androidx.core.content.ContextCompat.getDrawable;


public class Grades extends Fragment {

    private Table.Subject subject;
    private RecyclerView recycler;
    private SharedPreferences preferences;
    private DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        subject = (Table.Subject) getArguments().getSerializable("subject");
        recycler = view.findViewById(R.id.recyclerView);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(subject.name);

        FloatingActionButton fab = view.findViewById(R.id.addItem);
        fab.setOnClickListener(v -> new GradeEditor.Builder(getFragmentManager(), subject)
                .setPositiveButton(v1 -> {
                    try {
                        subject.getOwnerTable().write();
                    } catch (IOException ex) {
                        // TODO: something went wrong :(
                    }
                    recycler.getAdapter().notifyDataSetChanged();
                    checkList();
                }).show());

        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.setAdapter(new Adapter());

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (viewHolder.getItemViewType() != target.getItemViewType()) {
                    return false;
                }

                // Notify the adapter of the move
                recyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                Table.Subject.Grade g = subject.getGrades().get(viewHolder.getAdapterPosition());
                subject.remGrade(g);
                subject.addGrade(g, target.getAdapterPosition());
                try {
                    subject.getOwnerTable().write();
                } catch (IOException ex) {
                    // TODO: something went wrong :(
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };

        ItemTouchHelper toucher = new ItemTouchHelper(callback);
        toucher.attachToRecyclerView(recycler);

        TextView text = view.findViewById(R.id.emptyText);
        text.setText(R.string.no_grades);

        checkList();

    }

    private void checkList() {
        if (!subject.getGrades().isEmpty()) {
            recycler.setVisibility(RecyclerView.VISIBLE);
            if (getView() != null) {
                getView().findViewById(R.id.emptyCard).setVisibility(CardView.GONE);
            }
        } else {
            recycler.setVisibility(RecyclerView.GONE);
            if (getView() != null) {
                getView().findViewById(R.id.emptyCard).setVisibility(CardView.VISIBLE);
            }
        }
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private class ViewHolder extends RecyclerView.ViewHolder {

            CardView card;
            TextView name;
            TextView value;
            TextView text1;
            TextView text2;
            ImageButton edit;
            ImageView icon1;
            ImageView icon2;


            ViewHolder(View itemView) {
                super(itemView);

                card = itemView.findViewById(R.id.valueCard);
                value = itemView.findViewById(R.id.value_value);
                name = itemView.findViewById(R.id.value_title);
                text1 = itemView.findViewById(R.id.value_text1);
                text2 = itemView.findViewById(R.id.value_text2);
                icon1 = itemView.findViewById(R.id.value_icon1);
                icon2 = itemView.findViewById(R.id.value_icon2);
                edit = itemView.findViewById(R.id.value_edit);

                itemView.setOnClickListener(v -> edit.performClick());
            }
        }

        @Override
        public int getItemCount() {
            return subject.getGrades().size();
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_value, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder view, final int i) {
            final Table.Subject.Grade g = subject.getGrades().get(i);

            view.value.setText(doubleFormat.format(g.value));
            view.name.setText(g.name);
            if (preferences.getBoolean("useWeight", true)) {
                view.text1.setText(String.format("%s: %s", getResources().getString(R.string.weight), doubleFormat.format(g.weight)));
                view.icon1.setImageDrawable(getDrawable(getActivity(), R.drawable.ic_weight));
                view.text1.setVisibility(View.VISIBLE);
                view.icon1.setVisibility(View.VISIBLE);
            } else {
                view.text1.setVisibility(View.GONE);
                view.icon1.setVisibility(View.GONE);
            }

            view.text2.setText(dateFormat.format(g.creation));
            view.icon2.setImageDrawable(getDrawable(getActivity(), R.drawable.ic_calendar));

            view.edit.setOnClickListener(v -> new GradeEditor.Builder(getFragmentManager(), g)
                    .setPositiveButton(v1 -> {
                        recycler.getAdapter().notifyDataSetChanged();
                        try {
                            subject.getOwnerTable().write();
                        } catch (IOException ex) {
                            // TODO: something went wrong :(
                        }
                        checkList();
                    }).show());
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }

}
