package com.gradecalc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Overview extends Fragment {

    Table table;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_overview, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        table = (Table) getArguments().getSerializable("table");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.overview);
        getLayoutInflater().inflate(R.layout.card_empty, view.findViewById(R.id.overview));
        TextView text = view.findViewById(R.id.emptyText);
        text.setText(R.string.not_implemented);

    }

}
