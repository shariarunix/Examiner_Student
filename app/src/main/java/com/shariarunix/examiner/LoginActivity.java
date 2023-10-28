package com.shariarunix.examiner;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.shariarunix.examiner.SignupActivity.TB_STUDENT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.DataModel.StudentDataModel;
import com.shariarunix.examiner.OTPSender.OTPSenderClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPass;
    ImageView icPassShow, icRememberCheck;
    TextView txtBtnForgotPass, txtBtnCreateOne, showError;
    AppCompatButton btnSignIn;
    LinearLayout rememberCheck;
    boolean passShowToggle = false, rememberMeToggle = false, resetPassToggle = false, resetConPassToggle = false;

    // Declaring Firebase Auth
    private FirebaseAuth mAuth;

    // Shared Preferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

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
            }
        });

        // Sign in Button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSignIn();
            }
        });

        // Forgot Password Dialog
        txtBtnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPassDialog();
            }
        });
    }

    BottomSheetDialog forgotPassDialog;

    private void showForgotPassDialog() {
        forgotPassDialog = new BottomSheetDialog(LoginActivity.this, R.style.bottom_sheet_dialog);

        bottomDialog(forgotPassDialog);
        forgotPassDialog.setContentView(R.layout.bottom_dialog_forgot_pass);
        forgotPassDialog.show();

        TextView forgotDialogShowError = forgotPassDialog.findViewById(R.id.forgot_dialog_show_error);
        EditText edtForgotDialogEmail = forgotPassDialog.findViewById(R.id.edt_forgot_dialog_email);
        AppCompatButton btnForgotDialogEmail = forgotPassDialog.findViewById(R.id.btn_forgot_dialog_continue);

        assert edtForgotDialogEmail != null;
        edtForgotDialogEmail.requestFocus();

        assert btnForgotDialogEmail != null;
        btnForgotDialogEmail.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                String email = edtForgotDialogEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    assert forgotDialogShowError != null;
                    validator(edtForgotDialogEmail, forgotDialogShowError, "Please enter your email address");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    assert forgotDialogShowError != null;
                    validator(edtForgotDialogEmail, forgotDialogShowError, "Please enter a valid email address");
                    return;
                }

                sendOtpToEmail(edtForgotDialogEmail, forgotDialogShowError, email);
            }
        });
    }

    // List For accessing data of the email we got from forgot password dialog
    List<StudentDataModel> studentDataModelList = new ArrayList<>();

    private void sendOtpToEmail(EditText edtForgotDialogEmail, TextView forgotDialogShowError, String email) {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        mReference.child(TB_STUDENT).orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentDataModelList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        StudentDataModel studentDataModel = dataSnapshot.getValue(StudentDataModel.class);
                        studentDataModelList.add(studentDataModel);
                    }

                    Random random = new Random();

                    int min = 1000;
                    int max = 9999;

                    int randomNumber = random.nextInt(max - min + 1) + min;

                    new OTPSenderClass(email, studentDataModelList.get(0).getName(), "OTP for Password Reset", randomNumber).sendOtp();

                    forgotPassDialog.dismiss();
                    showOTPDialog(String.valueOf(randomNumber), studentDataModelList.get(0).getEmail());

                } else {
                    validator(edtForgotDialogEmail, forgotDialogShowError, "Please enter your email");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOTPDialog(String otpNumber, String email) {
        BottomSheetDialog otpDialog = new BottomSheetDialog(LoginActivity.this, R.style.bottom_sheet_dialog);

        bottomDialog(otpDialog);
        otpDialog.setContentView(R.layout.bottom_dialog_otp);
        otpDialog.show();

        EditText edtOtpOne = otpDialog.findViewById(R.id.otp_one);
        EditText edtOtpTwo = otpDialog.findViewById(R.id.otp_two);
        EditText edtOtpThree = otpDialog.findViewById(R.id.otp_three);
        EditText edtOtpFour = otpDialog.findViewById(R.id.otp_four);

        TextView otpDialogShowError = otpDialog.findViewById(R.id.otp_dialog_show_error);

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

        assert enterOtp != null;
        enterOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otpOne = edtOtpOne.getText().toString();
                otpTwo = edtOtpTwo.getText().toString();
                otpThree = edtOtpThree.getText().toString();
                otpFour = edtOtpFour.getText().toString();

                otp = otpOne + otpTwo + otpThree + otpFour;

                if (otp.equals(otpNumber)) {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showSentMailConfDialog();
                                otpDialog.dismiss();
                            } else {
                                Toast.makeText(LoginActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                otpDialog.dismiss();
                            }
                        }
                    });
                } else {
                    assert otpDialogShowError != null;
                    validator(edtOtpOne, otpDialogShowError, "Please enter correct otp.");
                }
            }
        });
    }

    private void showSentMailConfDialog() {
        BottomSheetDialog sentMailConfDialog = new BottomSheetDialog(LoginActivity.this, R.style.bottom_sheet_dialog);
        bottomDialog(sentMailConfDialog);
        sentMailConfDialog.setContentView(R.layout.bottom_dialog_password_reset_link);

        AppCompatButton btnPassResetLinkContinue = sentMailConfDialog.findViewById(R.id.btn_pass_change_link_continue);

        sentMailConfDialog.show();

        assert btnPassResetLinkContinue != null;
        btnPassResetLinkContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentMailConfDialog.dismiss();
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

                            FirebaseDatabase.getInstance().getReference()
                                    .child("student")
                                    .child(key)
                                    .child("isLoggedIn")
                                    .setValue(true);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("student")
                                    .child(key)
                                    .child("password")
                                    .setValue(password);

                            editor.putString("userID", key);
                            editor.apply();

                            Intent iNext = new Intent(LoginActivity.this, MainActivity.class);
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