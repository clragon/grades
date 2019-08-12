package com.gradecalc;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        return inflater.inflate(R.layout.frag_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        subject = (Table.Subject) getArguments().getSerializable("subject");
        recycler = view.findViewById(R.id.recyclerView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(subject.name);

        fab = view.findViewById(R.id.addItem);
        fab.setOnClickListener(v -> {
            GradeEditor editor = new GradeEditor();
            Bundle args = new Bundle();
            args.putSerializable("subject", subject);
            editor.setArguments(args);
            editor.setOnDismissListener(dialog -> {
                try {
                    subject.getOwnerTable().write();
                } catch (IOException ex) {
                    // TODO: something went wrong :(
                }
                recycler.getAdapter().notifyDataSetChanged();
                checkList();
            });
            editor.show(getFragmentManager(), "editor");
        });

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
                value = itemView.findViewById(R.id.valueValue);
                name = itemView.findViewById(R.id.valueTitle);
                text1 = itemView.findViewById(R.id.valueText1);
                text2 = itemView.findViewById(R.id.valueText2);
                icon1 = itemView.findViewById(R.id.valueIcon1);
                icon2 = itemView.findViewById(R.id.valueIcon2);
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
            view.text1.setText(String.format("%s: %s", getResources().getString(R.string.weight), df.format(g.weight)));
            view.icon1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_weight));
            view.text2.setText(dateFormat.format(g.creation));
            view.icon2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_calendar));

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
