package com.gradestat;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

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
    private DecimalFormat df = new DecimalFormat("#.##");

    // max length for any label of the chart
    private int maxLength = 12;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        table = (Table) getArguments().getSerializable("table");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.overview);

        ((TextView) view.findViewById(R.id.emptyText)).setText(R.string.no_subjects);

        // check if any subjects are available else display appropriate text
        // prevent chart from loading on empty list of subjects
        if (!checkList()) {
            return;
        }

        // get list of subjects and create chart data set
        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, (float) table.getAverage()));

        // add 2 reserved positions, one for AAA and one space position
        float pos = 2;

        // reverse list so the order fits the real order
        // chart order is reversed.
        for (Table.Subject s : reverseList(table.getSubjects())) {
            double subjectWeight = 0;
            for (Table.Subject.Grade g : s.getGrades()) {
                subjectWeight += g.weight;
            }
            if (subjectWeight != 0) {
                entries.add(new BarEntry(pos, (float) s.getAverage(), s));
                pos++;
            }
        }

        class SubjectFormatter extends ValueFormatter {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                switch ((int) value) {
                    case 0:
                        // position reserved for the Average of All Averages (AAA)
                        return getResources().getString(R.string.total);
                    case 1:
                        // position reserved for space between AAA and subject averages
                        return "";
                    default:
                        // return name of the subject
                        String name = ((Table.Subject) entries.get((int) value - 1).getData()).name;
                        if (name.length() >= maxLength) {
                            // cut string at nearest space or maxLength if it exceeds maxLength
                            name = name.substring(0, name.lastIndexOf(' ', maxLength - 3) == -1 ? maxLength - 3 : name.lastIndexOf(' ', maxLength - 3)) + "…";
                        }
                        return name;
                }
            }
        }

        // get default text color
        int textColor = ((MainActivity) getActivity()).getAttr(android.R.attr.textColorPrimary);

        BarDataSet dataSet = new BarDataSet(entries, "subjects");
        dataSet.setHighLightAlpha(255);
        dataSet.setColors(ContextCompat.getColor(getActivity(), R.color.design_default_color_primary));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        data.setValueTextSize(12);
        data.setValueTextColor(textColor);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return df.format(value);
            }
        });


        HorizontalBarChart chart = view.findViewById(R.id.barchart);
        chart.getPaint(Chart.PAINT_INFO).setTextSize(Utils.convertDpToPixel(18f));
        // barChart.setNoDataText(getResources().getString(R.string.no_subjects));

        // change height of the bar chart depending on how many subjects are available
        chart.getLayoutParams().height = (int) Utils.convertDpToPixel(entries.size() * 50);

        // populate bar chart
        chart.setNoDataText(getResources().getString(R.string.no_subjects));
        chart.setData(data);
        chart.setFitBars(true);
        chart.setTouchEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();


        // axis with subject names
        XAxis xAxis = chart.getXAxis();
        // display axis only on the left
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // use custom value formatter so subject names are displayed along the axis
        xAxis.setValueFormatter(new SubjectFormatter());
        // one point on the axis should equal one subject
        xAxis.setGranularity(1);
        // make sure each subject name is displayed on the axis
        xAxis.setLabelCount(table.getSubjects().size() + 2);
        // make sure text style matches the rest of the app
        xAxis.setTextColor(textColor);
        xAxis.setTextSize(16);

        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        // axis with average numbers
        YAxis yAxis = chart.getAxisLeft();
        // make axis start at 0 if the minimum grade should be 1
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

        // disable second yAxis
        YAxis yAxis2 = chart.getAxisRight();
        yAxis2.setDrawLabels(false);
        yAxis2.setDrawGridLines(false);
        yAxis2.setDrawAxisLine(false);


        // load settings to check if compensation should be displayed
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        TextView comp = view.findViewById(R.id.comp);
        if (preferences.getBoolean("compensate", true)) {
            String plus = "";
            if (table.getCompensation() > 0) {
                plus = "+";
            }
            comp.setText(String.format("%s: %s%s", getResources().getString(R.string.compensation), plus, df.format(table.getCompensation(preferences.getBoolean("compensateDouble", true)))));
        } else {
            comp.setVisibility(View.GONE);
        }
    }

    private <T> List<T> reverseList(List<T> l) {
        ArrayList<T> r = new ArrayList<>(l);
        Collections.reverse(r);
        return r;
    }

    private boolean checkList() {
        View view = getView();
        View overscroll = view.findViewById(R.id.overscroll);

        double tableWeight = 0;
        for (Table.Subject s : table.getSubjects()) {
            double subjectWeight = 0;
            for (Table.Subject.Grade g : s.getGrades()) {
                subjectWeight += g.weight;
            }
            tableWeight += subjectWeight;
        }

        if (table.getSubjects().isEmpty() || tableWeight == 0) {
            overscroll.setVisibility(View.GONE);
            view.findViewById(R.id.emptyCard).setVisibility(CardView.VISIBLE);
            return false;
        } else {
            overscroll.setVisibility(View.VISIBLE);
            view.findViewById(R.id.emptyCard).setVisibility(CardView.GONE);
            return true;
        }
    }

}
