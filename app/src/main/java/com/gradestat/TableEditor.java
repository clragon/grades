package com.gradestat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.text.DecimalFormat;

import es.dmoral.toasty.Toasty;


public class TableEditor extends DialogFragment {

    private Builder builder;

    private EditText valueTitle;
    private TextView valueValue;
    private Button valueOK;
    private Button valueDelete;
    private EditText tableEdit1;
    private EditText tableEdit2;
    private Switch switch3;
    private ImageView valueCircle;
    private SharedPreferences preferences;
    private final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (builder == null) {
            dismiss();
        }

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
        valueCircle = view.findViewById(R.id.value_circle);
        CardView card = view.findViewById(R.id.valueCard);

        int background = MainActivity.getAttr(getActivity(), android.R.attr.colorBackground);
        card.setCardBackgroundColor(background);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        tableEdit1.setHint(df.format(Double.valueOf(preferences.getInt("minGrade", 1))));
        tableEdit2.setHint(df.format(Double.valueOf(preferences.getInt("maxGrade", 6))));

        valueExtra.setVisibility(View.GONE);
        if (preferences.getBoolean("advanced", false)) {
            tableExtra.setVisibility(View.VISIBLE);
        }

        valueTitle.setHint(R.string.table_name);

        FrameLayout editorHolder = view.findViewById(R.id.editorHolder);

        editorHolder.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hideKeyboard(v);
            }
        });

        valueCancel.setOnClickListener(v -> {
            builder.onNo.onClick(v);
            hideKeyboard(v);
            dismiss();
        });

        if (builder.edit) {
            editTable(builder.table);
        } else {
            createTable(builder.file);
        }
    }

    private void editTable(final Table table) {
        valueTitle.setText(table.name);
        valueValue.setText(df.format(table.getAverage()));
        tableEdit1.setText(df.format(table.minGrade));
        tableEdit2.setText(df.format(table.maxGrade));
        switch3.setChecked(table.useWeight);

        if (preferences.getBoolean("colorRings", true)) {
            ((GradientDrawable) valueCircle.getDrawable().mutate()).setColor(MainActivity.getGradeColor(getActivity(), table, table.getAverage()));
        }

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                table.name = valueTitle.getText().toString();
                table.minGrade = Double.parseDouble(tableEdit1.getText().toString());
                table.maxGrade = Double.parseDouble(tableEdit2.getText().toString());
                table.useWeight = switch3.isChecked();
                if (!table.save()) {
                    Toasty.error(getActivity(), getString(R.string.table_no_write), Toast.LENGTH_SHORT, true).show();
                }
                builder.onYes.onClick(v);
                hideKeyboard(v);
                dismiss();
            }
        });

        valueDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation))
                .setMessage(String.format(getResources().getString(R.string.delete_object), table.name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (!table.delete()) {
                        Toasty.error(getActivity(), getString(R.string.table_no_write), Toast.LENGTH_SHORT, true).show();
                    }
                    builder.onDel.onClick(v);
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
        valueTitle.requestFocus();
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
                if (!table.save()) {
                    Toasty.error(getActivity(), getString(R.string.table_no_write), Toast.LENGTH_SHORT, true).show();
                }
                builder.onYes.onClick(v);
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
        if (preferences.getBoolean("advanced", false)) {
            if (tableEdit1.getText().toString().equals("")) {
                tableEdit1.setError(getString(R.string.value_cannot_be_empty));
                valid = false;
            } else {
                tableEdit1.setError(null);
            }
            if (tableEdit2.getText().toString().equals("")) {
                tableEdit2.setError(getString(R.string.value_cannot_be_empty));
                valid = false;
            } else {
                tableEdit2.setError(null);
            }
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
        private final boolean edit;
        private View.OnClickListener onYes = v -> {
        };
        private View.OnClickListener onNo = v -> {
        };
        private View.OnClickListener onDel;

        private final FragmentManager manager;

        public Builder(@NonNull FragmentManager manager, @NonNull Table table) {
            this.manager = manager;
            this.table = table;
            this.edit = true;
        }

        public Builder(@NonNull FragmentManager manager, @NonNull File file) {
            this.manager = manager;
            this.file = file;
            this.edit = false;
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

        public TableEditor build() {
            if (this.onDel == null) {
                this.onDel = this.onYes;
            }
            TableEditor editor = new TableEditor();
            editor.builder = this;
            return editor;
        }

        public void show() {
            this.build().show(manager, "editor");
        }
    }
}


