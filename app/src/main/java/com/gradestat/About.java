package com.gradestat;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gradestat.BuildConfig;

public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.about);
        TextView appname = view.findViewById(R.id.appname);
        TextView appvers = view.findViewById(R.id.appversion);
        ImageView appicon = view.findViewById(R.id.appicon);

        appname.setText(R.string.app_name);
        appvers.setText(BuildConfig.VERSION_NAME);
        if (savedInstanceState == null) {
            appicon.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", BuildConfig.APPLICATION_ID)));
                startActivity(browserIntent);
            });
        }
    }

}
