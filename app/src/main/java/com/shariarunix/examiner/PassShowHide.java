package com.shariarunix.examiner;

import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;

public class PassShowHide {

    boolean passShowToggle;
    EditText passInputField;
    ImageView icPassShowBtn;

    public PassShowHide(EditText passInputField, ImageView icPassShowBtn, boolean passShowToggle) {
        this.passInputField = passInputField;
        this.icPassShowBtn = icPassShowBtn;
        this.passShowToggle = passShowToggle;
    }

    public void passShow() {
        if (!passShowToggle) {
            passInputField.setTransformationMethod(null);
            icPassShowBtn.setImageResource(R.drawable.ic_pass_show);
            passInputField.setSelection(passInputField.length());
            passShowToggle = true;
        }
    }

    public void passHide() {
        if (passShowToggle) {
            passInputField.setTransformationMethod(new PasswordTransformationMethod());
            icPassShowBtn.setImageResource(R.drawable.ic_pass_hide);
            passInputField.setSelection(passInputField.length());
            passShowToggle = false;
        }
    }
}
