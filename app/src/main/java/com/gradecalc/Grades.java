package com.gradecalc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class Grades extends Fragment {

    Table.Subject subject;
    RecyclerView recycler;
    FloatingActionButton fab;
    DecimalFormat df = new DecimalFormat("#.##");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_grades, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        subject = (Table.Subject) getArguments().getSerializable("subject");
        recycler = view.findViewById(R.id.recyclerView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(subject.name);

        fab = view.findViewById(R.id.addGrade);
        fab.setOnClickListener(v -> {
            GradeEditor editor = new GradeEditor();
            Bundle args = new Bundle();
            args.putSerializable("subject", subject);
            editor.setArguments(args);
            editor.setOnDismissListener(dialog -> {
                recycler.getAdapter().notifyDataSetChanged();
                try {
                    subject.getOwnerTable().write();
                } catch (IOException ex) {
                    // TODO: something went wrong :(
                }
                checkList();
            });
            editor.show(getFragmentManager(), "editor");
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new Adapter());

        getLayoutInflater().inflate(R.layout.card_empty, view.findViewById(R.id.GradeLayout));
        TextView text = view.findViewById(R.id.emptyText);
        text.setText("Keine Noten vorhanden");

        checkList();

    }

    private void checkList() {
        if (!subject.getGrades().isEmpty()) {
            recycler.setVisibility(RecyclerView.VISIBLE);
            getView().findViewById(R.id.emptyCard).setVisibility(CardView.GONE);
        } else {
            recycler.setVisibility(RecyclerView.GONE);
            getView().findViewById(R.id.emptyCard).setVisibility(CardView.VISIBLE);
        }
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private class ViewHolder extends RecyclerView.ViewHolder {

            CardView card;
            TextView name;
            TextView date;
            TextView value;
            TextView weight;
            ImageButton edit;
            ImageView dateIcon;
            ImageView weightIcon;


            ViewHolder(View itemView) {
                super(itemView);

                card = itemView.findViewById(R.id.valueCard);
                value = itemView.findViewById(R.id.valueValue);
                name = itemView.findViewById(R.id.valueTitle);
                weight = itemView.findViewById(R.id.valueWeight);
                date = itemView.findViewById(R.id.valueDate);
                weightIcon = itemView.findViewById(R.id.valueIcon1);
                dateIcon = itemView.findViewById(R.id.valueIcon2);
                edit = itemView.findViewById(R.id.valueEdit);

                itemView.setOnClickListener(v -> edit.performClick());
            }
        }

        @Override
        public int getItemCount() {
            return subject.getGrades().size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_value, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder view, final int i) {
            final Table.Subject.Grade g = subject.getGrades().get(i);

            view.value.setText(df.format(g.value));
            view.name.setText(g.name);
            view.weight.setText(String.format("Gewicht: %s", df.format(g.weight)));
            view.date.setText(dateFormat.format(g.creation));
            view.weightIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_weight));
            view.dateIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_calendar));
            view.edit.setOnClickListener(v -> {
                GradeEditor editor = new GradeEditor();
                Bundle args = new Bundle();
                args.putSerializable("grade", g);
                editor.setArguments(args);
                editor.setOnDismissListener(dialog -> {
                    recycler.getAdapter().notifyDataSetChanged();
                    try {
                        subject.getOwnerTable().write();
                    } catch (IOException ex) {

                    }
                    checkList();
                });
                editor.show(getFragmentManager(), "editor");
            });
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }

}
