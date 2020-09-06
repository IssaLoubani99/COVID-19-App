package com.example.firebasedemo.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.firebasedemo.R;


public class ChangeUsernameDialog extends AlertDialog.Builder {
    public EditText usernameView;
    private String default_input = "";

    public ChangeUsernameDialog(Context context, String default_input) {
        super(context);
        this.default_input = default_input;
        setTitle("Enter a username");
        initEditText(context);
        setView(usernameView);
        this.setIcon(R.drawable.ic_edit);
    }

    private void initEditText(Context context) {
        usernameView = new EditText(context);
        usernameView.setText(getDefault_input());
    }

    // to change dialog button color
    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.color_main_Color));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.color_main_Color));
            showKeyboardAndFocus(dialog.getContext());
            // Log.w("alertDialog status@", "Showed");
        });

        dialog.setOnDismissListener(dialogInterface -> {
            dismissKeyboard(dialog.getContext());
            // Log.w("alertDialog status@", "Dismissed");
        });
        return dialog;
    }

    public String getDefault_input() {
        return default_input;
    }

    public void setDefault_input(String default_input) {
        this.default_input = default_input;
    }

    public void showKeyboardAndFocus(Context context) {
        usernameView.requestFocus();
        usernameView.selectAll();
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void dismissKeyboard(Context context) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public String getInput() {
        return usernameView.getText().toString().trim();
    }
}