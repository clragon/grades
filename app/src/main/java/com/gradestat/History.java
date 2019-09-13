package com.gradestat;


import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

public class History extends Fragment {

    Table table;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        table = (Table) getArguments().getSerializable("table");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.history);

        getLayoutInflater().inflate(R.layout.card_empty, view.findViewById(R.id.history));
        TextView text = view.findViewById(R.id.emptyText);
        text.setText(R.string.not_implemented);
        View emptyCard = view.findViewById(R.id.emptyCard);
        emptyCard.setVisibility(View.VISIBLE);
        View linechart = view.findViewById(R.id.linechart);
        linechart.setVisibility(View.GONE);
        if (true) {
            return;
        }

        ((TextView) view.findViewById(R.id.emptyText)).setText(R.string.no_subjects);

        // check if any subjects are available else display appropriate text
        checkList();
        // prevent chart from loading on empty list of subjects
        if (table.getSubjects().size() == 0) {
            return;
        }

        LocalDate first = table.getFirst();

        List<ILineDataSet> dataSets = new ArrayList<>();

        for (Table.Subject s : table.getSubjects()) {
            List<Entry> entries = new ArrayList<>();

            List<Table.Subject.Grade> sorted = sortList(s.getGrades());

            for (Table.Subject.Grade g : sorted) {
                entries.add(new Entry(DAYS.between(first, g.creation), (float) g.value));
            }

            LineDataSet dataSet = new LineDataSet(entries, s.name);

            dataSets.add(dataSet);
        }

        LineData data = new LineData(dataSets);

        LineChart chart = view.findViewById(R.id.linechart);

        chart.setData(data);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();

        int textColor = ((MainActivity) getActivity()).getAttr(android.R.attr.textColorPrimary);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setTextColor(textColor);
        xAxis.setTextSize(16);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis yAxis = chart.getAxisLeft();
        if (table.minGrade == 1) {
            yAxis.setAxisMinimum((float) table.minGrade - 1);
        } else {
            yAxis.setAxisMinimum((float) table.minGrade);
        }
        yAxis.setAxisMaximum((float) table.maxGrade);

        yAxis.setTextColor(textColor);
        yAxis.setTextSize(16);

        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);

        YAxis yAxis2 = chart.getAxisRight();
        yAxis2.setDrawLabels(false);
        yAxis2.setDrawGridLines(false);
        yAxis2.setDrawAxisLine(false);

    }

    private List<Table.Subject.Grade> sortList(List<Table.Subject.Grade> l) {
        ArrayList<Table.Subject.Grade> s = new ArrayList<>(l);
        Collections.sort(s, (g1, g2) -> g1.creation.compareTo(g2.creation));
        return s;
    }

    private void checkList() {
        View view = getView();
        View linechart = view.findViewById(R.id.linechart);
        if (!table.getSubjects().isEmpty()) {
            linechart.setVisibility(View.VISIBLE);
            view.findViewById(R.id.emptyCard).setVisibility(CardView.GONE);
        } else {
            linechart.setVisibility(View.GONE);
            view.findViewById(R.id.emptyCard).setVisibility(CardView.VISIBLE);
        }
    }

}
