package com.shariarunix.examiner;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPass;
    ImageView icPassShow, icRememberCheck;
    TextView txtBtnForgotPass, txtBtnCreateOne, showError;
    AppCompatButton btnSignIn;
    LinearLayout rememberCheck;
    boolean passShowToggle = false;
    boolean rememberMeToggle = false;

    boolean resetPassToggle = false;
    boolean resetConPassToggle = false;

    // Declaring Firebase Auth
    private FirebaseAuth mAuth;

    // Shared Preferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    // Dialogs declaration for forgot password
    private BottomSheetDialog forgotPassDialog, otpDialog, resetPassDialog;

    private String otp = "", otpOne, otpTwo, otpThree, otpFour;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize All View
        edtEmail = findViewById(R.id.edt_email);
        edtPass = findViewById(R.id.edt_pass);
        icPassShow = findViewById(R.id.ic_pass_show);
        icRememberCheck = findViewById(R.id.ic_remember_check);
        txtBtnForgotPass = findViewById(R.id.txt_btn_forgot_pass);
        txtBtnCreateOne = findViewById(R.id.txt_btn_create_one);
        btnSignIn = findViewById(R.id.btn_signin);
        rememberCheck = findViewById(R.id.remember_check);
        showError = findViewById(R.id.show_error);

        // Dialogs for Forgot Password
        forgotPassDialog = new BottomSheetDialog(LoginActivity.this, R.style.bottom_sheet_dialog);
        otpDialog = new BottomSheetDialog(LoginActivity.this, R.style.bottom_sheet_dialog);
        resetPassDialog = new BottomSheetDialog(LoginActivity.this, R.style.bottom_sheet_dialog);

        // Forgot Password Activity
        txtBtnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog(forgotPassDialog);
                forgotPassDialog.setContentView(R.layout.forgot_pass_dialog);
                forgotPassDialog.show();

                TextView forgotDialogShowError = forgotPassDialog.findViewById(R.id.forgot_dialog_show_error);
                EditText edtForgotDialogEmail = forgotPassDialog.findViewById(R.id.edt_forgot_dialog_email);
                AppCompatButton btnForgotDialogEmail = forgotPassDialog.findViewById(R.id.btn_forgot_dialog_continue);

                assert btnForgotDialogEmail != null;
                btnForgotDialogEmail.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view) {

                        assert edtForgotDialogEmail != null;
                        String email = edtForgotDialogEmail.getText().toString().trim();

                        if (email.isEmpty()) {
                            assert forgotDialogShowError != null;
                            validator(edtEmail, forgotDialogShowError, "Please enter your email address");
                            return;
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            assert forgotDialogShowError != null;
                            validator(edtEmail, forgotDialogShowError, "Please enter a valid email address");
                            return;
                        }

                        forgotPassDialog.dismiss();
                        bottomDialog(otpDialog);
                        otpDialog.setContentView(R.layout.otp_dialog);
                        otpDialog.show();

                        EditText edtOtpOne = otpDialog.findViewById(R.id.otp_one);
                        EditText edtOtpTwo = otpDialog.findViewById(R.id.otp_two);
                        EditText edtOtpThree = otpDialog.findViewById(R.id.otp_three);
                        EditText edtOtpFour = otpDialog.findViewById(R.id.otp_four);

                        TextView txtBtnOtpResend = otpDialog.findViewById(R.id.txt_btn_otp_resend);
                        AppCompatButton enterOtp = otpDialog.findViewById(R.id.btn_enter_otp);


                        assert edtOtpOne != null;
                        edtOtpOne.requestFocus();
                        edtOtpOne.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (!charSequence.toString().trim().isEmpty()) {
                                    assert edtOtpTwo != null;
                                    edtOtpTwo.requestFocus();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });
                        assert edtOtpTwo != null;
                        edtOtpTwo.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (!charSequence.toString().trim().isEmpty()) {
                                    assert edtOtpThree != null;
                                    edtOtpThree.requestFocus();
                                }
                                if (charSequence.toString().isEmpty()) {
                                    edtOtpOne.requestFocus();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });
                        assert edtOtpThree != null;
                        edtOtpThree.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (!charSequence.toString().trim().isEmpty()) {
                                    assert edtOtpFour != null;
                                    edtOtpFour.requestFocus();
                                }
                                if (charSequence.toString().isEmpty()) {
                                    edtOtpTwo.requestFocus();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });
                        assert edtOtpFour != null;
                        edtOtpFour.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (charSequence.toString().isEmpty()) {
                                    edtOtpThree.requestFocus();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });

                        assert txtBtnOtpResend != null;
                        txtBtnOtpResend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(LoginActivity.this, "OTP Sent again", Toast.LENGTH_SHORT).show();
                            }
                        });

                        assert enterOtp != null;
                        enterOtp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                otpOne = edtOtpOne.getText().toString();
                                otpTwo = edtOtpTwo.getText().toString();
                                otpThree = edtOtpThree.getText().toString();
                                otpFour = edtOtpFour.getText().toString();

                                otp = otpOne + otpTwo + otpThree + otpFour;

                                //OTP Verification
                                otpDialog.dismiss();
                                bottomDialog(resetPassDialog);
                                resetPassDialog.setContentView(R.layout.new_password_dialog);
                                resetPassDialog.show();

                                EditText setPassword = resetPassDialog.findViewById(R.id.edt_reset_pass);
                                EditText setConfirmPassword = resetPassDialog.findViewById(R.id.edt_reset_confirm_pass);
                                AppCompatButton btnResetPass = resetPassDialog.findViewById(R.id.btn_reset_pass);

                                TextView resetDialogShowError = resetPassDialog.findViewById(R.id.reset_dialog_show_error);

                                ImageView resetPass = resetPassDialog.findViewById(R.id.ic_pass_show);
                                ImageView resetConPass = resetPassDialog.findViewById(R.id.ic_confirm_pass_show);

                                // Password show or hide
                                assert resetPass != null;
                                resetPass.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (!resetPassToggle) {
                                            new PassShowHide(setPassword, resetPass, false).passShow();
                                            resetPassToggle = true;
                                        } else {
                                            new PassShowHide(setPassword, resetPass, true).passHide();
                                            resetPassToggle = false;
                                        }
                                    }
                                });

                                assert resetConPass != null;
                                resetConPass.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (!resetConPassToggle) {
                                            new PassShowHide(setConfirmPassword, resetConPass, false).passShow();
                                            resetConPassToggle = true;
                                        } else {
                                            new PassShowHide(setConfirmPassword, resetConPass, true).passHide();
                                            resetConPassToggle = false;
                                        }
                                    }
                                });

                                // Reset Password
                                assert btnResetPass != null;
                                btnResetPass.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        assert setPassword != null;
                                        String password = setPassword.getText().toString().trim();
                                        assert setConfirmPassword != null;
                                        String conPassword = setConfirmPassword.getText().toString().trim();

                                        // Checking password
                                        if (password.isEmpty()) {
                                            assert resetDialogShowError != null;
                                            validator(setPassword, resetDialogShowError, "Please enter your password");
                                            return;
                                        }
                                        if (password.length() < 8) {
                                            assert resetDialogShowError != null;
                                            validator(setPassword, resetDialogShowError, "Password must be at least 8 characters");
                                            return;
                                        }
                                        if (!conPassword.equals(password)) {
                                            assert resetDialogShowError != null;
                                            validator(setConfirmPassword, resetDialogShowError, "Password and confirm password is not same");
                                            return;
                                        }

                                        resetPassDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Password changed.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        // Password Show or Hide
        icPassShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!passShowToggle) {
                    new PassShowHide(edtPass, icPassShow, false).passShow();
                    passShowToggle = true;
                } else {
                    new PassShowHide(edtPass, icPassShow, true).passHide();
                    passShowToggle = false;
                }
            }
        });

        // Remember Me Toggle
        rememberCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!rememberMeToggle) {
                    icRememberCheck.setImageResource(R.drawable.ic_checked);

                    editor.putBoolean("userCheck", true);

                    rememberMeToggle = true;
                } else {
                    icRememberCheck.setImageResource(R.drawable.ic_uncheck);
                    rememberMeToggle = false;
                }
            }
        });

        // Sign Up Activity
        txtBtnCreateOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            }
        });

        // Sign in Button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSignIn();
            }
        });
    }

    private void bottomDialog(BottomSheetDialog bottomSheetDialog) {
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.getBehavior().setSkipCollapsed(true);
        bottomSheetDialog.getBehavior().setState(STATE_EXPANDED);
    }

    @SuppressLint("SetTextI18n")
    private void userSignIn() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        // Checking the validity of email
        // Checking is email ok or not?
        if (email.isEmpty()) {
            validator(edtEmail, showError, "Please enter your email address");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validator(edtEmail, showError, "Please enter a valid email address");
            return;
        }

        //checking the validity of the password
        // Checking password
        if (password.isEmpty()) {
            validator(edtPass, showError, "Please enter your password");
            return;
        }
        if (password.length() < 8) {
            validator(edtPass, showError, "Password must be at least 8 characters");
            return;
        }

        // Sign with firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            String key = user.getUid();

                            editor.putString("userID", key);

                            editor.apply();

                            Intent iNext = new Intent(LoginActivity.this, MainActivity.class);
//                        iNext.putExtra("uID", key);
                            startActivity(iNext);
                            finish();
                        } else {
                            showError.setVisibility(View.VISIBLE);
                            showError.setText("Please enter valid email and password");
                            edtEmail.requestFocus();
                        }
                    }
                });
    }

    private void validator(EditText editText, TextView textView, String string) {
        textView.setVisibility(View.VISIBLE);
        textView.setText(string);
        editText.requestFocus();
    }
}