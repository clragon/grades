package com.gradestat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class TableEditor extends DialogFragment {

    private Table table;
    private File file;
    private boolean edit;
    private View.OnClickListener onYes;
    private View.OnClickListener onNo;
    private View.OnClickListener onDel;

    private EditText valueTitle;
    private EditText valueValue;
    private Button valueOK;
    private Button valueDelete;
    private EditText tableEdit1;
    private EditText tableEdit2;
    private Switch switch3;
    private SharedPreferences preferences;
    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        valueTitle = view.findViewById(R.id.value_title);
        valueValue = view.findViewById(R.id.value_value);
        LinearLayout valueExtra = view.findViewById(R.id.value_extra);
        valueOK = view.findViewById(R.id.valueOK);
        valueDelete = view.findViewById(R.id.valueDelete);
        Button valueCancel = view.findViewById(R.id.valueCancel);
        LinearLayout tableExtra = view.findViewById(R.id.table_extra);
        tableEdit1 = view.findViewById(R.id.table_edit1);
        tableEdit2 = view.findViewById(R.id.table_edit2);
        switch3 = view.findViewById(R.id.switch3);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        tableEdit1.setHint(df.format(Double.valueOf(preferences.getInt("minGrade", 1))));
        tableEdit2.setHint(df.format(Double.valueOf(preferences.getInt("maxGrade", 6))));

        valueExtra.setVisibility(View.GONE);
        if (preferences.getBoolean("advanced", false)) {
            tableExtra.setVisibility(View.VISIBLE);
        }

        valueValue.setClickable(false);
        valueValue.setFocusable(false);

        valueTitle.setHint(R.string.table_name);

        FrameLayout editorHolder = view.findViewById(R.id.editorHolder);

        editorHolder.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hideKeyboard(v);
            }
        });

        valueCancel.setOnClickListener(v -> {
            onNo.onClick(v);
            hideKeyboard(v);
            dismiss();
        });

        if (edit) {
            editTable(table);
        } else {
            createTable(file);
        }
    }

    private void editTable(final Table table) {
        valueTitle.setText(table.name);
        valueValue.setText(df.format(table.getAverage()));
        tableEdit1.setText(df.format(table.minGrade));
        tableEdit2.setText(df.format(table.maxGrade));
        switch3.setChecked(table.useWeight);

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                table.name = valueTitle.getText().toString();
                table.minGrade = Double.parseDouble(tableEdit1.getText().toString());
                table.maxGrade = Double.parseDouble(tableEdit2.getText().toString());
                table.useWeight = switch3.isChecked();
                try {
                    table.write();
                } catch (Exception ex) {
                    // oh no
                }
                onYes.onClick(v);
                hideKeyboard(v);
                dismiss();
            }
        });

        valueDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation))
                .setMessage(String.format(getResources().getString(R.string.delete_object), table.name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        table.delete();
                    } catch (IOException ex) {
                        // oof
                    }
                    onDel.onClick(v);
                    hideKeyboard(v);
                    dismiss();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_warning)
                .show());

        valueDelete.setVisibility(View.VISIBLE);
    }

    private void createTable(final File file) {
        valueValue.setText(df.format(0));
        tableEdit1.setText(df.format(Double.valueOf(preferences.getInt("minGrade", 1))));
        tableEdit2.setText(df.format(Double.valueOf(preferences.getInt("maxGrade", 6))));
        switch3.setChecked(preferences.getBoolean("useWeight", true));


        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                Table table = new Table(valueTitle.getText().toString());
                if (preferences.getBoolean("advanced", false)) {
                    table.minGrade = Double.parseDouble(tableEdit1.getText().toString());
                    table.maxGrade = Double.parseDouble(tableEdit2.getText().toString());
                    table.useWeight = switch3.isChecked();
                } else {
                    table.minGrade = preferences.getInt("minGrade", 1);
                    table.maxGrade = preferences.getInt("maxGrade", 6);
                    table.useWeight = preferences.getBoolean("useWeight", true);
                }
                table.saveFile = file.getPath();
                try {
                    table.write();
                } catch (IOException ex) {
                }
                onYes.onClick(v);
                hideKeyboard(v);
                dismiss();
            }
        });
    }

    private boolean checkFields() {
        boolean valid = true;
        if (valueTitle.getText().toString().replaceAll("\\s+", "").equals("")) {
            valueTitle.setError(getString(R.string.name_cannot_be_empty));
            valid = false;
        } else if (valueTitle.getText().toString().equals(getString(R.string.add_table))) {
            valueTitle.setError(getString(R.string.name_cannot_be_empty));
            valid = false;
        } else {
            valueTitle.setError(null);
        }
        return valid;
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static class Builder {
        private Table table = null;
        private File file = null;
        private boolean edit;
        private View.OnClickListener onYes = v -> {

        };
        private View.OnClickListener onNo = v -> {

        };
        private View.OnClickListener onDel;
        private FragmentManager manager;

        public Builder(@NonNull FragmentManager manager, @NonNull Table table) {
            this.manager = manager;
            this.table = table;
            edit = true;
        }

        public Builder(@NonNull FragmentManager manager, @NonNull File file) {
            this.manager = manager;
            this.file = file;
            edit = false;
        }

        public Builder setPositiveButton(View.OnClickListener onYes) {
            this.onYes = onYes;
            return this;
        }

        public Builder setNegativeButton(View.OnClickListener onNo) {
            this.onNo = onNo;
            return this;
        }

        public Builder setDeleteButton(View.OnClickListener onDel) {
            this.onDel = onDel;
            return this;
        }

        public void show() {
            TableEditor editor = new TableEditor();
            editor.table = this.table;
            editor.file = this.file;
            editor.edit = this.edit;
            editor.onYes = this.onYes;
            editor.onNo = this.onNo;
            if (this.onDel == null) {
                onDel = onYes;
            }
            editor.onDel = this.onDel;
            editor.show(manager, "editor");
        }
    }
}
