package com.gradecalc;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GradeEditor extends DialogFragment {

    View view;
    Table table;
    EditText valueTitle;
    EditText valueValue;
    EditText valueWeight;
    TextView valueDate;
    Button valueOK;
    Button valueDelete;
    SeekBar valueSeekWeight;
    DecimalFormat df = new DecimalFormat("#.##");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_editor, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        valueTitle = view.findViewById(R.id.valueTitle);
        valueValue = view.findViewById(R.id.valueValue);
        valueWeight = view.findViewById(R.id.valueText1);
        valueDate = view.findViewById(R.id.valueText2);
        valueOK = view.findViewById(R.id.valueOK);
        valueDelete = view.findViewById(R.id.valueDelete);
        Button valueCancel = view.findViewById(R.id.valueCancel);
        ImageButton valueEditDate = view.findViewById(R.id.valueEditDate);
        valueSeekWeight = view.findViewById(R.id.valueSeekWeight);

        FrameLayout editorHolder = view.findViewById(R.id.editorHolder);

        editorHolder.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                checkFields();
            }
        });

        valueEditDate.setOnClickListener(v -> {
            // TODO: implement date picker
        });

        valueSeekWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double weight;
                switch (progress) {
                    case 0:
                        weight = 0.25;
                        break;
                    case 1:
                        weight = 0.5;
                        break;
                    case 2:
                        weight = 1;
                        break;
                    default:
                        weight = 1;
                        break;
                }
                valueWeight.setText(df.format(weight));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        valueCancel.setOnClickListener(v -> {
            // TODO: need own yes no dialogue
            dismiss();
        });

        try {
            if (getArguments().containsKey("grade")) {
                Table.Subject.Grade grade = (Table.Subject.Grade) getArguments().getSerializable("grade");
                table = grade.getOwnerSubject().getOwnerTable();
                gradeEdit(grade);
            } else if (getArguments().containsKey("subject")) {
                Table.Subject subject = (Table.Subject) getArguments().getSerializable("subject");
                table = subject.getOwnerTable();
                gradeCreate(subject);
            }
        } catch (Exception e) {
            // TODO: something went wrong :(
        }
    }

    private void gradeEdit(final Table.Subject.Grade grade) {

        valueTitle.setText(grade.name);
        valueValue.setText(df.format(grade.value));
        valueWeight.setText(df.format(grade.weight));
        valueDate.setText(dateFormat.format(grade.creation));

        int progress;
        switch (valueWeight.getText().toString()) {
            case "0.25":
                progress = 0;
                break;
            case "0.5":
                progress = 1;
                break;
            case "1":
                progress = 2;
                break;
            default:
                progress = 2;
        }
        valueSeekWeight.setProgress(progress);

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                grade.name = valueTitle.getText().toString();
                grade.value = Double.parseDouble(valueValue.getText().toString());
                grade.weight = Double.parseDouble(valueWeight.getText().toString());
                try {
                    grade.creation = dateFormat.parse(valueDate.getText().toString());
                } catch (ParseException e) { // TODO: something went wrong :(
                }
                dismiss();
            }
        });

        valueDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation))
                .setMessage(String.format(getResources().getString(R.string.delete_object), grade.name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    grade.getOwnerSubject().remGrade(grade);
                    dismiss();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_warning)
                .show());

        valueDelete.setVisibility(View.VISIBLE);
    }

    private void gradeCreate(final Table.Subject subject) {

        valueTitle.setText(String.format("%s %x", subject.name, subject.getGrades().size() + 1));
        valueTitle.setSelectAllOnFocus(true);
        valueWeight.setText(df.format(1.0));
        valueDate.setText(dateFormat.format(new Date()));

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                String Name = valueTitle.getText().toString();
                double Value = Double.parseDouble(valueValue.getText().toString());
                double Weight = Double.parseDouble(valueWeight.getText().toString());
                Date creation = new Date();
                try {
                    creation = dateFormat.parse(valueDate.getText().toString());
                } catch (ParseException e) { // TODO: something went wrong :(
                }
                subject.addGrade(Value, Weight, Name, creation);
                dismiss();
            }
        });
    }

    private boolean checkFields() {
        boolean valid = true;
        if (valueTitle.getText().toString().replaceAll("\\s+", "").equals("")) {
            valueTitle.setError(getString(R.string.name_cannot_be_empty));
            valid = false;
        } else {
            valueTitle.setError(null);
        }
        try {
            double value = Double.parseDouble(valueValue.getText().toString());
            if (!(value >= table.minGrade)) {
                valid = false;
            } else {
                // valueValue.setError(null);
            }
            if (!(value <= table.maxGrade)) {
                valid = false;
            } else {
                // valueValue.setError(null);
            }
        } catch (Exception e) {
            valid = false;
        }
        try {
            double weight = Double.parseDouble(valueWeight.getText().toString());
            if (!(weight >= 0)) {
                valueWeight.setError(getString(R.string.value_too_low));
                valid = false;
            } else {
                valueWeight.setError(null);
            }
            if (!(weight <= 1)) {
                valueValue.setError(getString(R.string.value_too_high));
                valid = false;
            } else {
                valueWeight.setError(null);
            }
        } catch (Exception e) {
            valueWeight.setError(getString(R.string.value_cannot_be_empty));
            valid = false;
        }
        return valid;
    }

    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

}
