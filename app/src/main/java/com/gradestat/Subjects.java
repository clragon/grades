package com.gradestat;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
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

import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class Subjects extends Fragment {

    private Table table;
    private RecyclerView recycler;
    private SharedPreferences preferences;
    private final DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        table = (Table) getArguments().getSerializable("table");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(table.name);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recycler = view.findViewById(R.id.recyclerView);

        FloatingActionButton fab = view.findViewById(R.id.addItem);
        fab.setOnClickListener(v -> new SubjectEditor.Builder(getFragmentManager(), table)
                .setPositiveButton(v1 -> {
                    table.save();
                    updateList();
                }).show());

        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.setAdapter(new Adapter());

        ((TextView) view.findViewById(R.id.emptyText)).setText(R.string.no_subjects);

        updateList();
    }

    private void updateList() {
        if (checkList()) {
            sortList();
        }
    }

    private boolean checkList() {
        if (!table.getSubjects().isEmpty()) {
            recycler.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.emptyCard).setVisibility(CardView.GONE);
            return true;
        } else {
            recycler.setVisibility(View.GONE);
            getView().findViewById(R.id.emptyCard).setVisibility(CardView.VISIBLE);
            return false;
        }
    }

    interface SubjectSorter {
        boolean sort(int i, int n);
    }

    private void sortList() {

        String sorting = preferences.getString("sorting", "sorting_custom");

        if (sorting.equals("sorting_custom")) {

            ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    if (viewHolder.getItemViewType() != target.getItemViewType()) {
                        return false;
                    }

                    // Notify the adapter of the move
                    recyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    Table.Subject s = table.getSubjects().get(viewHolder.getAdapterPosition());
                    // move subject to new position
                    table.movSubject(s, target.getAdapterPosition());
                    table.save();
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                }
            };

            new ItemTouchHelper(callback).attachToRecyclerView(recycler);
        } else {

            class AlphabetSort implements SubjectSorter {
                public boolean sort(int i, int n) {
                    return (table.getSubjects().get(i).name.compareTo(table.getSubjects().get(n).name) > 0);
                }
            }

            class LatestSort implements SubjectSorter {
                public boolean sort(int i, int n) {
                    if (table.getSubjects().get(n).isValid()) {
                        if (table.getSubjects().get(i).isValid()) {
                            return table.getSubjects().get(i).getLast().isBefore(table.getSubjects().get(n).getLast());
                        } else {
                            return true;
                        }
                    }
                    return false;
                }
            }

            class GreatestSort implements SubjectSorter {
                public boolean sort(int i, int n) {
                    return (table.getSubjects().get(i).getAverage() < table.getSubjects().get(n).getAverage());
                }
            }

            Map<String, SubjectSorter> sorters = new HashMap<>();
            {
                sorters.put("sorting_alphabet", new AlphabetSort());
                sorters.put("sorting_latest", new LatestSort());
                sorters.put("sorting_greatest", new GreatestSort());
            }


            System.out.println("sorters = " + sorters);
            for (int i = 0; i <= table.getSubjects().size(); i++) {
                for (int n = i + 1; n < table.getSubjects().size(); n++) {
                    if (sorters.get(preferences.getString("sorting", "sorting_alphabet")).sort(i, n)) {
                        table.movSubject(table.getSubjects().get(n), table.getSubjects().indexOf(table.getSubjects().get(i)));
                    }
                }
            }

            if (preferences.getBoolean("sorting_invert", false)) {
                for (int i = 0; i < table.getSubjects().size(); i++) {
                    table.movSubject(table.getSubjects().get(table.getSubjects().size() - 1), i);
                }
            }
        }

        recycler.getAdapter().notifyDataSetChanged();
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private class ViewHolder extends RecyclerView.ViewHolder {

            final TextView name;
            final TextView value;
            final TextView text1;
            final TextView text2;
            final ImageButton edit;
            final ImageView icon1;
            final ImageView icon2;
            final ImageView circle;

            ViewHolder(View itemView) {
                super(itemView);
                value = itemView.findViewById(R.id.value_value);
                name = itemView.findViewById(R.id.value_title);
                text1 = itemView.findViewById(R.id.value_text1);
                text2 = itemView.findViewById(R.id.value_text2);
                icon1 = itemView.findViewById(R.id.value_icon1);
                icon2 = itemView.findViewById(R.id.value_icon2);
                edit = itemView.findViewById(R.id.value_edit);
                circle = itemView.findViewById(R.id.value_circle);

                itemView.setOnClickListener(v -> {
                    Fragment fragment = new Grades();
                    Bundle args = new Bundle();
                    args.putSerializable("subject", table.getSubjects().get(getAdapterPosition()));
                    fragment.setArguments(args);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    // slide in and out from the bottom animation set
                    transaction.setCustomAnimations(R.anim.slide_in_up, android.R.anim.fade_out, android.R.anim.fade_in, R.anim.slide_out_down);
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
            view.text2.setText(s.getLast().format(dateFormat));
            view.icon2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_lastest));

            if (preferences.getBoolean("colorRings", true)) {
                ((GradientDrawable) view.circle.getDrawable().mutate()).setColor((MainActivity.getGradeColor(getActivity(), table, s.getAverage())));
            }

            view.edit.setOnClickListener(v -> new SubjectEditor.Builder(getFragmentManager(), s)
                    .setPositiveButton(v1 -> {
                        table.save();
                        updateList();
                    }).show());
        }
    }
}
