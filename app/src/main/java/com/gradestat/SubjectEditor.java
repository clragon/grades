package com.gradestat;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.evernote.android.state.State;

import java.text.DecimalFormat;


public class SubjectEditor extends DialogFragment {

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
            // TODO: need own yes no dialogue
            dismiss();
        });

        try {
            if (getArguments().containsKey("subject")) {
                Table.Subject subject = (Table.Subject) getArguments().getSerializable("subject");
                editSubject(subject);
            } else if (getArguments().containsKey("table")) {
                Table table = (Table) getArguments().getSerializable("table");
                createSubject(table);
            }
        } catch (Exception e) {
            // TODO: something went wrong :(
        }
    }

    private void editSubject(final Table.Subject subject) {
        valueTitle.setText(subject.name);
        valueValue.setText(df.format(subject.getAverage()));

        valueOK.setOnClickListener(v -> {
            if (checkFields()) {
                subject.name = valueTitle.getText().toString();
                dismiss();
            }
        });

        valueDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation))
                .setMessage(String.format(getResources().getString(R.string.delete_object), subject.name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    subject.getOwnerTable().remSubject(subject);
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

    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

}