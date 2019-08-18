package com.gradestat;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.text.DecimalFormat;


public class Subjects extends Fragment {

    private Table table;
    private RecyclerView recycler;
    private DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        table = (Table) getArguments().getSerializable("table");
        recycler = view.findViewById(R.id.recyclerView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(table.name);

        FloatingActionButton fab = view.findViewById(R.id.addItem);
        fab.setOnClickListener(v -> {
            SubjectEditor editor = new SubjectEditor();
            Bundle args = new Bundle();
            args.putSerializable("table", table);
            editor.setArguments(args);
            editor.setOnDismissListener(dialog -> {
                try {
                    table.write();
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
                Table.Subject s = table.getSubjects().get(viewHolder.getAdapterPosition());
                table.remSubject(s);
                table.addSubject(s, target.getAdapterPosition());
                try {
                    table.write();
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
        text.setText(R.string.no_subjects);

        checkList();
    }

    private void checkList() {
        if (!table.getSubjects().isEmpty()) {
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

                itemView.setOnClickListener(v -> {
                    Fragment fragment = new Grades();
                    Bundle args = new Bundle();
                    args.putSerializable("subject", table.getSubjects().get(getAdapterPosition()));
                    fragment.setArguments(args);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                    transaction.replace(R.id.fragment, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                });
            }
        }

        @Override
        public int getItemCount() {
            return table.getSubjects().size();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_value, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder view, int i) {
            final Table.Subject s = table.getSubjects().get(i);

            view.value.setText(doubleFormat.format(s.getAverage()));
            view.name.setText(s.name);
            view.text1.setText(String.format("%s: %s", getResources().getString(R.string.grades), doubleFormat.format(s.getGrades().size())));
            view.icon1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_grade));
            view.text2.setText(s.getLatest().format(dateFormat));
            view.icon2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_lastest));

            view.edit.setOnClickListener(v -> {
                SubjectEditor editor = new SubjectEditor();
                Bundle args = new Bundle();
                args.putSerializable("subject", s);
                editor.setArguments(args);
                editor.setOnDismissListener(dialog -> {
                    recycler.getAdapter().notifyDataSetChanged();
                    try {
                        table.write();
                    } catch (IOException ex) {
                        // TODO: something went wrong :(
                    }
                    checkList();
                });
                editor.show(getFragmentManager(), "editor");
            });
        }
    }
}