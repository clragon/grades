package com.gradecalc;

import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Overview extends Fragment {

    private Table table;
    private View view;
    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_overview, container, false);
        return view;
    }

    private <T> List<T> reverseList(List<T> l) {
        ArrayList<T> r = new ArrayList<>(l);
        Collections.reverse(r);
        return r;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        table = (Table) getArguments().getSerializable("table");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.overview);

        HorizontalBarChart all = view.findViewById(R.id.barchart);
        all.setNoDataText(getResources().getString(R.string.no_subjects));
        all.getPaint(Chart.PAINT_INFO).setTextSize(Utils.convertDpToPixel(18f));

        if (table.getSubjects().size() == 0) {
            return;
        }

        List<BarEntry> entries = new ArrayList<>();

        float i = 2;

        entries.add(new BarEntry(0, (float) table.getAverage()));

        for (Table.Subject s : reverseList(table.getSubjects())) {
            entries.add(new BarEntry(i, (float) s.getAverage()));
            i++;
        }

        all.getLayoutParams().height = (int) Utils.convertDpToPixel(entries.size() * 50);

        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        int primaryColor = getActivity().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary}).getColor(0, -1);

        BarDataSet set = new BarDataSet(entries, "subjects");
        set.setHighLightAlpha(255);
        set.setColors(new int[]{ContextCompat.getColor(getActivity(), R.color.design_default_color_primary)});
        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        data.setValueTextColor(primaryColor);
        data.setValueTextSize(12);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return df.format(value);
            }
        });

        class SubjectFormatter extends ValueFormatter {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                switch ((int) value) {
                    case 0:
                        return getResources().getString(R.string.total);
                    case 1:
                        return "";
                    default:
                        return reverseList(table.getSubjects()).get((int) value - 2).name;
                }
            }
        }

        XAxis xAxis = all.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new SubjectFormatter());
        xAxis.setGranularity(1);
        xAxis.setLabelCount(table.getSubjects().size() + 1);
        xAxis.setTextColor(primaryColor);
        xAxis.setTextSize(16);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis yAxis = all.getAxisLeft();
        if (table.minGrade == 1) {
            yAxis.setAxisMinimum((float) table.minGrade - 1);
        } else {
            yAxis.setAxisMinimum((float) table.minGrade);
        }
        yAxis.setAxisMaximum((float) table.maxGrade);
        yAxis.setTextColor(primaryColor);
        yAxis.setTextSize(16);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);

        YAxis yAxis2 = all.getAxisRight();
        yAxis2.setDrawLabels(false);
        yAxis2.setDrawGridLines(false);
        yAxis2.setDrawAxisLine(false);

        all.setNoDataText(getResources().getString(R.string.no_subjects));
        all.getLegend().setEnabled(false);
        all.setData(data);
        all.setFitBars(true);
        all.setTouchEnabled(false);
        all.getDescription().setEnabled(false);
        all.invalidate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        TextView comp = view.findViewById(R.id.comp);
        if (preferences.getBoolean("compensate", true)) {
            String plus = "";
            if (table.getCompensation() > 0) {
                plus = "+";
            }
            comp.setText(String.format("%s: %s%s", getResources().getString(R.string.compensation), plus, df.format(table.getCompensation())));
        } else {
            comp.setVisibility(View.GONE);
        }
    }

}
