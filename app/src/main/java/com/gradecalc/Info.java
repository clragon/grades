package com.gradecalc;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Info extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.info);
        getLayoutInflater().inflate(R.layout.card_empty, view.findViewById(R.id.info));
        TextView text = view.findViewById(R.id.emptyText);
        text.setText(R.string.not_implemented);
    }

}
