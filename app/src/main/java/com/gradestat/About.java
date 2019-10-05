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

public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.about);
        TextView appName = view.findViewById(R.id.appname);
        TextView appVers = view.findViewById(R.id.appversion);
        ImageView appIcon = view.findViewById(R.id.appicon);

        appName.setText(R.string.app_name);
        appVers.setText(BuildConfig.VERSION_NAME);
        if (savedInstanceState == null) {
            appIcon.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", BuildConfig.APPLICATION_ID)));
                startActivity(browserIntent);
            });
        }
    }

}
