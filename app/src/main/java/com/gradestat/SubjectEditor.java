package com.gradestat;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.text.DecimalFormat;


public class SubjectEditor extends DialogFragment {

    private Builder builder;

    private EditText valueTitle;
    private EditText valueValue;
    private Button valueOK;
    private Button valueDelete;
    private DecimalFormat df = new DecimalFormat("#.##");

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

        valueExtra.setVisibility(View.GONE);
        valueValue.setClickable(false);
        valueValue.setFocusable(false);

        valueTitle.setHint(R.string.subject_name);

        FrameLayout editorHolder = view.findViewById(R.id.editorHolder);

        editorHolder.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        valueCancel.setOnClickListener(v -> {
            builder.onNo.onClick(v);
            dismiss();
        });

        if (builder.edit) {
            editSubject(builder.subject);
        } else {
            createSubject(builder.table);
        }
    }

    private void editSubject(final Table.Subject subject) {
        valueTitle.setText(subject.name);
        valueValue.setText(df.format(subject.getAverage()));

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                subject.name = valueTitle.getText().toString();
                builder.onYes.onClick(v);
                dismiss();
            }
        });

        valueDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation))
                .setMessage(String.format(getResources().getString(R.string.delete_object), subject.name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    subject.getParent().remSubject(subject);
                    builder.onDel.onClick(v);
                    dismiss();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_warning)
                .show());

        valueDelete.setVisibility(View.VISIBLE);
    }

    private void createSubject(final Table table) {
        valueValue.setText(df.format(0));

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                table.addSubject(valueTitle.getText().toString());
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
        return valid;
    }

    public static class Builder {

        private Table.Subject subject = null;
        private Table table = null;
        private boolean edit;
        private View.OnClickListener onYes = v -> {
        };
        private View.OnClickListener onNo = v -> {
        };
        private View.OnClickListener onDel;

        private FragmentManager manager;

        public Builder(@NonNull FragmentManager manager, @NonNull Table.Subject subject) {
            this.manager = manager;
            this.subject = subject;
            edit = true;
        }

        public Builder(@NonNull FragmentManager manager, @NonNull Table table) {
            this.manager = manager;
            this.table = table;
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
            SubjectEditor editor = new SubjectEditor();
            editor.builder = this;
            editor.show(manager, "editor");
        }
    }
}
