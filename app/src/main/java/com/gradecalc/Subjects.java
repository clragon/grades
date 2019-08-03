package com.gradecalc;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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


public class Subjects extends Fragment {

    Table table;
    RecyclerView recycler;
    FloatingActionButton fab;
    DecimalFormat df = new DecimalFormat("#.##");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_subjects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        table = (Table) getArguments().getSerializable("table");
        recycler = view.findViewById(R.id.recyclerView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(table.name);

        fab = view.findViewById(R.id.addSubject);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubjectEditor editor = new SubjectEditor();
                Bundle args = new Bundle();
                args.putSerializable("table", table);
                editor.setArguments(args);
                editor.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        recycler.getAdapter().notifyDataSetChanged();
                        try {
                            table.write();
                        } catch (IOException ex) {

                        }
                        checkList();
                    }
                });
                editor.show(getFragmentManager(), "editor");
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new Adapter());

        getLayoutInflater().inflate(R.layout.card_empty, (FrameLayout) view.findViewById(R.id.SubjectLayout));
        TextView text = view.findViewById(R.id.emptyText);
        text.setText("Keine Fächer vorhanden");

        checkList();

    }

    private void checkList() {
        if (!table.getSubjects().isEmpty()) {
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
            TextView grades;
            TextView average;
            ImageButton edit;
            ImageView dateIcon;
            ImageView gradesIcon;


            ViewHolder(View itemView) {
                super(itemView);

                card = itemView.findViewById(R.id.valueCard);
                average = itemView.findViewById(R.id.valueValue);
                name = itemView.findViewById(R.id.valueTitle);
                grades = itemView.findViewById(R.id.valueWeight);
                gradesIcon = itemView.findViewById(R.id.valueIcon1);
                date = itemView.findViewById(R.id.valueDate);
                dateIcon = itemView.findViewById(R.id.valueIcon2);
                edit = itemView.findViewById(R.id.valueEdit);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment fragment = new Grades();
                        Bundle args = new Bundle();
                        args.putSerializable("subject", table.getSubjects().get(getAdapterPosition()));
                        fragment.setArguments(args);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                        transaction.replace(R.id.framelayout, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return table.getSubjects().size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_value, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder view, int i) {
            final Table.Subject s = table.getSubjects().get(i);

            view.average.setText(df.format(s.getAverage()));
            view.name.setText(s.name);
            view.grades.setText(String.format("Noten: %s", df.format(s.getGrades().size())));

            view.date.setText(dateFormat.format(s.getLatest()));
            view.gradesIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_exam));
            view.dateIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_lastest));
            view.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubjectEditor editor = new SubjectEditor();
                    Bundle args = new Bundle();
                    args.putSerializable("subject", s);
                    editor.setArguments(args);
                    editor.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            recycler.getAdapter().notifyDataSetChanged();
                            try {
                                table.write();
                            } catch (IOException ex) {

                            }
                            checkList();
                        }
                    });
                    editor.show(getFragmentManager(), "editor");
                }
            });
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }

}
