package com.gradestat;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.preference.PreferenceManager;
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

import org.threeten.bp.format.DateTimeFormatter;

import java.text.DecimalFormat;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.FormatStyle;


public class GradeEditor extends DialogFragment {

    private Builder builder;

    private Table table;
    private EditText valueTitle;
    private EditText valueValue;
    private EditText valueWeight;
    private TextView valueDate;
    private Button valueOK;
    private Button valueDelete;
    private ImageButton valueEditDate;
    private SeekBar valueSeekWeight;
    private ConstraintLayout weight_editor;
    private Integer dialogTheme;
    private DecimalFormat df = new DecimalFormat("#.##");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

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
        valueWeight = view.findViewById(R.id.value_text1);
        valueDate = view.findViewById(R.id.value_text2);
        valueOK = view.findViewById(R.id.valueOK);
        valueDelete = view.findViewById(R.id.valueDelete);
        Button valueCancel = view.findViewById(R.id.valueCancel);
        valueEditDate = view.findViewById(R.id.valueEditDate);
        valueSeekWeight = view.findViewById(R.id.value_seek_weight);
        weight_editor = view.findViewById(R.id.weight_editor);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (preferences.getBoolean("dark", true)) {
            dialogTheme = R.style.AppTheme_DatePicker;
        } else {
            dialogTheme = R.style.AppTheme_Light_DatePicker;
        }

        valueTitle.setHint(R.string.grade_name);

        FrameLayout editorHolder = view.findViewById(R.id.editorHolder);
        editorHolder.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                checkFields();
            }
        });

        /*
        ImageView circle = view.findViewById(R.id.value_circle);
        valueValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    GradientDrawable ring = (GradientDrawable) circle.getDrawable().mutate();
                    int color = ((MainActivity) getActivity()).getGradeColor(table, Double.parseDouble(s.toString()));
                    ring.setColor(color);

                    float[] hsv = new float[3];
                    Color.colorToHSV(color, hsv);
                    valueTitle.setText(String.format("%f %f %f", hsv[0], hsv[1], hsv[2]));
                } catch (Exception ex) {
                    // yeet
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        */

        valueSeekWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double weight;
                switch (progress) {
                    case 0:
                        weight = table.getFullWeight() / 4;
                        break;
                    case 1:
                        weight = table.getFullWeight() / 2;
                        break;
                    case 2:
                        weight = table.getFullWeight();
                        break;
                    default:
                        weight = table.getFullWeight();
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
            builder.onNo.onClick(v);
            dismiss();
        });

        if (builder.edit) {
            table = builder.grade.getParent().getParent();
            gradeEdit(builder.grade);
        } else {
            table = builder.subject.getParent();
            gradeCreate(builder.subject);
        }
    }

    private void gradeEdit(final Table.Subject.Grade grade) {

        valueTitle.setText(grade.name);
        valueValue.setText(df.format(grade.value));
        valueWeight.setText(df.format(grade.weight));
        valueDate.setText(dateFormat.format(grade.creation));

        if (!grade.getParent().getParent().useWeight) {
            weight_editor.setVisibility(View.GONE);
        }

        int progress;
        String weight = valueWeight.getText().toString();
        if (weight.equals(df.format(table.getFullWeight() / 4))) {
            progress = 0;
        } else if (weight.equals(df.format(table.getFullWeight() / 2))) {
            progress = 1;
        } else if (weight.equals(df.format(table.getFullWeight()))) {
            progress = 2;
        } else {
            progress = 2;
        }
        valueSeekWeight.setProgress(progress);

        valueEditDate.setOnClickListener(v -> {
            LocalDate current = LocalDate.parse(valueDate.getText().toString(), dateFormat);
            // very weird fix. do not remove +1 and -1. DialogTheme starts months with 1, but threethen with 0 as it seems.
            new DatePickerDialog(getActivity(), dialogTheme, (view, year, month, dayOfMonth) -> valueDate.setText(dateFormat.format(LocalDate.of(year, month + 1, dayOfMonth))), current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()).show();
        });

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                grade.name = valueTitle.getText().toString();
                grade.value = Double.parseDouble(valueValue.getText().toString());
                grade.weight = Double.parseDouble(valueWeight.getText().toString());
                grade.creation = LocalDate.parse(valueDate.getText().toString(), dateFormat);
                builder.onYes.onClick(v);
                dismiss();
            }
        });

        valueDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation))
                .setMessage(String.format(getResources().getString(R.string.delete_object), grade.name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    grade.getParent().remGrade(grade);
                    builder.onDel.onClick(v);
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
        valueDate.setText(dateFormat.format(LocalDate.now()));

        if (!subject.getParent().useWeight) {
            weight_editor.setVisibility(View.GONE);
        }

        valueEditDate.setOnClickListener(v -> {
            LocalDate current = LocalDate.parse(valueDate.getText().toString(), dateFormat);
            new DatePickerDialog(getActivity(), dialogTheme, (view, year, month, dayOfMonth) -> valueDate.setText(dateFormat.format(LocalDate.of(year, month + 1, dayOfMonth))), current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()).show();
        });

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                String Name = valueTitle.getText().toString();
                double Value = Double.parseDouble(valueValue.getText().toString());
                double Weight = Double.parseDouble(valueWeight.getText().toString());
                LocalDate creation = LocalDate.parse(valueDate.getText().toString(), dateFormat);
                subject.addGrade(Value, Weight, Name, creation);
                builder.onYes.onClick(v);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            if (preferences.getBoolean("useLimits", true)) {
                double value = Double.parseDouble(valueValue.getText().toString());
                if (!(value >= table.minGrade)) {
                    valid = false;
                } else {
                    valueValue.setError(null);
                }
                if (!(value <= table.maxGrade)) {
                    valid = false;
                } else {
                    valueValue.setError(null);
                }
            }
        } catch (Exception ex) {
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
            if (!(weight <= 10)) {
                valueWeight.setError(getString(R.string.value_too_high));
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

    public static class Builder {

        private Table.Subject.Grade grade = null;
        private Table.Subject subject = null;
        private boolean edit;
        private View.OnClickListener onYes = v -> {
        };
        private View.OnClickListener onNo = v -> {
        };
        private View.OnClickListener onDel;

        private FragmentManager manager;

        public Builder(@NonNull FragmentManager manager, @NonNull Table.Subject.Grade grade) {
            this.manager = manager;
            this.grade = grade;
            edit = true;
        }

        public Builder(@NonNull FragmentManager manager, @NonNull Table.Subject subject) {
            this.manager = manager;
            this.subject = subject;
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

            if (this.onDel == null) {
                onDel = onYes;
            }
            GradeEditor editor = new GradeEditor();
            editor.builder = this;
            editor.show(manager, "editor");
        }
    }
}
