package com.gradecalc;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.text.DecimalFormat;


public class SubjectEditor extends DialogFragment {

    View view;
    EditText valueTitle;
    EditText valueValue;
    LinearLayout valueExtra;
    Button valueOK;
    Button valueDelete;
    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_editor, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        valueTitle = view.findViewById(R.id.valueTitle);
        valueValue = view.findViewById(R.id.valueValue);
        valueExtra = view.findViewById(R.id.valueExtra);
        valueOK = view.findViewById(R.id.valueOK);
        valueDelete = view.findViewById(R.id.valueDelete);
        Button valueCancel = view.findViewById(R.id.valueCancel);

        valueExtra.setVisibility(View.GONE);

        FrameLayout editorHolder = view.findViewById(R.id.editorHolder);

        editorHolder.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        valueCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: need own yes no dialogue
                dismiss();
            }
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
        valueValue.setKeyListener(null);

        valueOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields()) {
                    subject.name = valueTitle.getText().toString();
                    dismiss();
                }
            }
        });

        valueDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: need own yes no dialogue
                subject.getOwnerTable().remSubject(subject);
                dismiss();
            }
        });
    }

    private void createSubject(final Table table) {
        valueValue.setText(df.format(0));
        valueValue.setKeyListener(null);

        valueOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields()) {
                    table.addSubject(valueTitle.getText().toString());
                    dismiss();
                }
            }
        });

        valueDelete.setVisibility(View.GONE);
    }

    private boolean checkFields() {
        boolean valid = true;
        if (valueTitle.getText().toString().replaceAll("\\s+", "").equals("")) {
            valueTitle.setError("name kann nicht nicht leer sein.");
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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

}
