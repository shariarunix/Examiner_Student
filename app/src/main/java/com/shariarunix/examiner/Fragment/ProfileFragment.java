package com.shariarunix.examiner.Fragment;

import static android.content.Context.MODE_PRIVATE;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.Adapter.CustomAdapter;
import com.shariarunix.examiner.DataModel.ExamResultModel;
import com.shariarunix.examiner.DataModel.StudentDataModel;
import com.shariarunix.examiner.LoginActivity;
import com.shariarunix.examiner.PassShowHide;
import com.shariarunix.examiner.R;
import com.shariarunix.examiner.SignupActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final String U_DATA = "arg1";
    String userName, userEmail, userPhone, userCourse, userPass, userID;
    List<ExamResultModel> examResultModelList = new ArrayList<>();
    DatabaseReference mReference;
    FirebaseUser user;
    ListView resultList;
    ProgressBar resultProgressBar;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean findSpecialChar = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment getInstance(StudentDataModel studentDataModel) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();

        bundle.putSerializable(U_DATA, studentDataModel);

        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("examinerPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        userID = sharedPreferences.getString("userID", "");

        mReference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        TextView txtProfileShowName = view.findViewById(R.id.txt_profile_show_name);

        LinearLayout personalInfo = view.findViewById(R.id.personal_info);
        LinearLayout changePass = view.findViewById(R.id.change_password);
        LinearLayout forgotPass = view.findViewById(R.id.forgot_pass);
        LinearLayout deleteAccount = view.findViewById(R.id.delete_account);

        ImageButton imgBtnLogOut = view.findViewById(R.id.img_btn_logout);

        AppCompatButton btnProfileSeeResult = view.findViewById(R.id.btn_profile_see_result);

        // Getting user data from arguments by object. And adding user's data to the variable from StudentDataModel object
        if (getArguments() != null) {
            StudentDataModel studentDataModel = (StudentDataModel) getArguments().getSerializable(U_DATA);

            assert studentDataModel != null;
            userName = studentDataModel.getName();
            userEmail = studentDataModel.getEmail();
            userPhone = studentDataModel.getPhone();
            userCourse = studentDataModel.getCourse();
            userPass = studentDataModel.getPassword();

            // Set name of user
            txtProfileShowName.setText(userName);
        }

        // Button for log out user
        imgBtnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutUser();
            }
        });

        // Button for deleting user account
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount(userID, userPass);
            }
        });

        // Dialog for showing user's exam result
        btnProfileSeeResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResultDialog();
            }
        });

        // Dialog for showing user's personal information
        personalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPersonalInfoDialog();
            }
        });

        // Dialog
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePassDialog();
            }
        });

        // Forgot Password
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPassDialog();
            }
        });

        return view;
    }
    private void showForgotPassDialog() {
        BottomSheetDialog forgotPassDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(forgotPassDialog);
        forgotPassDialog.setContentView(R.layout.bottom_dialog_forgot_pass);
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
                    validator(edtForgotDialogEmail, forgotDialogShowError, "Please enter your email address");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    assert forgotDialogShowError != null;
                    validator(edtForgotDialogEmail, forgotDialogShowError, "Please enter a valid email address");
                    return;
                }

                forgotPassDialog.dismiss();

                showOTPDialog();
            }
        });
    }

    private void showOTPDialog() {
        BottomSheetDialog otpDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(otpDialog);
        otpDialog.setContentView(R.layout.bottom_dialog_otp);
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
                Toast.makeText(requireActivity(), "OTP Sent again", Toast.LENGTH_SHORT).show();
            }
        });

        assert enterOtp != null;
        enterOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otpOne = edtOtpOne.getText().toString();
                String otpTwo = edtOtpTwo.getText().toString();
                String otpThree = edtOtpThree.getText().toString();
                String otpFour = edtOtpFour.getText().toString();

                String otp = otpOne + otpTwo + otpThree + otpFour;

                //OTP Verification
                otpDialog.dismiss();

                resetPassDialog();
            }
        });
    }

    private void resetPassDialog() {
        final boolean[] resetPassToggle = {false, false};

        BottomSheetDialog resetPassDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(resetPassDialog);
        resetPassDialog.setContentView(R.layout.bottm_dialog_new_password);

        EditText setPassword = resetPassDialog.findViewById(R.id.edt_reset_pass);
        EditText setConfirmPassword = resetPassDialog.findViewById(R.id.edt_reset_confirm_pass);

        AppCompatButton btnResetPass = resetPassDialog.findViewById(R.id.btn_reset_pass);

        TextView resetDialogShowError = resetPassDialog.findViewById(R.id.reset_dialog_show_error);

        ImageView resetPass = resetPassDialog.findViewById(R.id.ic_pass_show);
        ImageView resetConPass = resetPassDialog.findViewById(R.id.ic_confirm_pass_show);

        resetPassDialog.show();

        // Password show or hide
        assert resetPass != null;
        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!resetPassToggle[0]) {
                    new PassShowHide(setPassword, resetPass, false).passShow();
                    resetPassToggle[0] = true;
                } else {
                    new PassShowHide(setPassword, resetPass, true).passHide();
                    resetPassToggle[0] = false;
                }
            }
        });

        assert resetConPass != null;
        resetConPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!resetPassToggle[1]) {
                    new PassShowHide(setConfirmPassword, resetConPass, false).passShow();
                    resetPassToggle[1] = true;
                } else {
                    new PassShowHide(setConfirmPassword, resetConPass, true).passHide();
                    resetPassToggle[1] = false;
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

                Toast.makeText(requireActivity(), "Password changed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePassDialog() {
        final boolean[] oldPassShowToggle = {false, false};

        BottomSheetDialog changePassDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(changePassDialog);
        changePassDialog.setContentView(R.layout.bottom_dialog_change_password);

        EditText edtOldPass = changePassDialog.findViewById(R.id.edt_old_pass);
        EditText edtNewPass = changePassDialog.findViewById(R.id.edt_new_pass);

        TextView showErrorChangePass = changePassDialog.findViewById(R.id.show_error);

        AppCompatButton btnChangePassContinue = changePassDialog.findViewById(R.id.btn_change_pass_continue);

        ImageView oldPassShow = changePassDialog.findViewById(R.id.ic_pass_show);
        ImageView newPassShow = changePassDialog.findViewById(R.id.ic_new_pass_show);

        assert oldPassShow != null;
        oldPassShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!oldPassShowToggle[0]) {
                    new PassShowHide(edtOldPass, oldPassShow, false).passShow();
                    oldPassShowToggle[0] = true;
                } else {
                    new PassShowHide(edtOldPass, oldPassShow, true).passHide();
                    oldPassShowToggle[0] = false;
                }
            }
        });
        assert newPassShow != null;
        newPassShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!oldPassShowToggle[1]) {
                    new PassShowHide(edtNewPass, newPassShow, false).passShow();
                    oldPassShowToggle[1] = true;
                } else {
                    new PassShowHide(edtNewPass, newPassShow, true).passHide();
                    oldPassShowToggle[1] = false;
                }
            }
        });

        assert btnChangePassContinue != null;
        btnChangePassContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert edtOldPass != null;
                String oldPass = edtOldPass.getText().toString().trim();
                assert edtNewPass != null;
                String newPass = edtNewPass.getText().toString().trim();

                // Checking old password
                if (oldPass.isEmpty()) {
                    assert showErrorChangePass != null;
                    validator(edtOldPass, showErrorChangePass, "Please enter your password");
                    return;
                }
                if (oldPass.length() < 8) {
                    assert showErrorChangePass != null;
                    validator(edtOldPass, showErrorChangePass, "Password must be at least 8 characters");
                    return;
                }
                // Checking old password
                if (newPass.isEmpty()) {
                    assert showErrorChangePass != null;
                    validator(edtNewPass, showErrorChangePass, "Please enter your password");
                    return;
                }
                if (newPass.length() < 8) {
                    assert showErrorChangePass != null;
                    validator(edtNewPass, showErrorChangePass, "Password must be at least 8 characters");
                    return;
                }

                // Checking is old password is valid or not
                if (userPass.equals(oldPass)) {
                    String uId = user.getUid();
                    mReference.child("student").child(uId).child("password").setValue(newPass);

                    changePassword(userEmail, oldPass, newPass);

                    changePassDialog.dismiss();
                } else {
                    assert showErrorChangePass != null;
                    validator(edtOldPass, showErrorChangePass, "Password you entered as old password is not valid");
                }
            }
        });

        changePassDialog.show();
    }

    private void showPersonalInfoDialog() {
        BottomSheetDialog personalInfoDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(personalInfoDialog);
        personalInfoDialog.setContentView(R.layout.bottom_dialog_personal_info);

        TextView personalInfoName = personalInfoDialog.findViewById(R.id.personal_info_name);
        TextView personalInfoEmail = personalInfoDialog.findViewById(R.id.personal_info_email);
        TextView personalInfoPhone = personalInfoDialog.findViewById(R.id.personal_info_phone);
        TextView personalInfoCourse = personalInfoDialog.findViewById(R.id.personal_info_course);

        AppCompatButton btnChangeInformation = personalInfoDialog.findViewById(R.id.btn_change_information);

        assert personalInfoName != null;
        personalInfoName.setText(userName);

        assert personalInfoEmail != null;
        personalInfoEmail.setText(userEmail);

        assert personalInfoPhone != null;
        personalInfoPhone.setText(userPhone);

        assert personalInfoCourse != null;
        personalInfoCourse.setText(userCourse);

        personalInfoDialog.show();

        assert btnChangeInformation != null;
        btnChangeInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personalInfoDialog.dismiss();

                showChangeInfoDialog();
            }
        });
    }

    private void showChangeInfoDialog() {
        BottomSheetDialog changeInfoDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(changeInfoDialog);
        changeInfoDialog.setContentView(R.layout.bottom_dialog_change_personal_info);

        EditText edtChangeName = changeInfoDialog.findViewById(R.id.edt_change_name);
        EditText edtChangePhone = changeInfoDialog.findViewById(R.id.edt_change_phone);

        TextView showErrorChangeInfo = changeInfoDialog.findViewById(R.id.show_error);

        AppCompatButton btnChangeInformation = changeInfoDialog.findViewById(R.id.btn_change_information);

        assert edtChangeName != null;
        edtChangeName.setHint(userName);
        assert edtChangePhone != null;
        edtChangePhone.setHint(userPhone);

        changeInfoDialog.show();

        assert btnChangeInformation != null;
        btnChangeInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do change operation
                String changeName = edtChangeName.getText().toString().trim();
                String changePhone = edtChangePhone.getText().toString().trim();

                // Checking is name ok or not?
                String[] specialCharacter = new String[]{"~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+", "=", "/", "\\", "<", ">", "{", "}", "[", "]", ",", "?", "|", "`"};
                for (String s : specialCharacter) {
                    if (changeName.contains(s)) {
                        findSpecialChar = true;
                        break;
                    }
                }
                if (findSpecialChar) {
                    assert showErrorChangeInfo != null;
                    validator(edtChangeName, showErrorChangeInfo, "Please remove special character's from your name");
                    findSpecialChar = false;
                    return;
                }
                if (changeName.isEmpty()) {
                    assert showErrorChangeInfo != null;
                    validator(edtChangeName, showErrorChangeInfo, "Please enter your name");
                    return;
                }
                // Checking is phone number ok or not?
                if (changePhone.isEmpty()) {
                    assert showErrorChangeInfo != null;
                    validator(edtChangePhone, showErrorChangeInfo, "Please enter your phone number");
                    return;
                }
                if (!changePhone.matches("^(?:\\+88|0088)?(01[3-9]\\d{8})$")) {
                    assert showErrorChangeInfo != null;
                    validator(edtChangePhone, showErrorChangeInfo, "Please enter a valid phone number");
                    return;
                }

                mReference.child("student").child(userID).child("name").setValue(changeName);
                mReference.child("student").child(userID).child("phone").setValue(changePhone);

                changeInfoDialog.dismiss();
            }
        });
    }

    private void showResultDialog() {
        BottomSheetDialog resultDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);
        bottomDialog(resultDialog);
        resultDialog.setContentView(R.layout.bottom_dialog_result);

        resultDialog.setCancelable(false);
        resultDialog.setCanceledOnTouchOutside(true);

        resultList = resultDialog.findViewById(R.id.result_list);
        resultProgressBar = resultDialog.findViewById(R.id.result_progress_bar);

        ImageButton btnResultDialogHide = resultDialog.findViewById(R.id.img_btn_dialog_close);

        loadResultAndShow(resultList);
        resultDialog.show();

        assert btnResultDialogHide != null;
        btnResultDialogHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultDialog.dismiss();
            }
        });
    }

    // Method for deleting user account
    private void deleteAccount(String uID, String password) {
        final boolean[] deleteAcPassShowToggle = {false};

        BottomSheetDialog deleteAccountDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);
        bottomDialog(deleteAccountDialog);
        deleteAccountDialog.setContentView(R.layout.bottom_dialog_delete_account);

        EditText edtDeleteAcPassword = deleteAccountDialog.findViewById(R.id.edt_delete_ac_password);

        TextView deleteAcShowError = deleteAccountDialog.findViewById(R.id.show_error);

        ImageView icPassShow = deleteAccountDialog.findViewById(R.id.ic_pass_show);

        AppCompatButton btnDeleteAcDialogYes = deleteAccountDialog.findViewById(R.id.btn_delete_ac_dialog_yes);
        AppCompatButton btnDeleteAcDialogNo = deleteAccountDialog.findViewById(R.id.btn_delete_ac_dialog_no);

        deleteAccountDialog.show();

        icPassShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!deleteAcPassShowToggle[0]) {
                    new PassShowHide(edtDeleteAcPassword, icPassShow, false).passShow();
                    deleteAcPassShowToggle[0] = true;
                } else {
                    new PassShowHide(edtDeleteAcPassword, icPassShow, true).passHide();
                    deleteAcPassShowToggle[0] = false;
                }
            }
        });

        assert btnDeleteAcDialogYes != null;
        btnDeleteAcDialogYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert edtDeleteAcPassword != null;
                String edtPassword = edtDeleteAcPassword.getText().toString().trim();

                // Checking password
                if (!edtPassword.isEmpty()) {
                    if (edtPassword.equals(password)) {
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    removeDataFromSharedPref();

                                    deleteAccountDialog.dismiss();

                                    FirebaseDatabase.getInstance().getReference().child("student").child(uID).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(requireActivity(), "Your account has been deleted.", Toast.LENGTH_SHORT).show();

                                                    Intent goToSignUp = new Intent(new Intent(requireActivity(), SignupActivity.class));

                                                    startActivity(goToSignUp);
                                                    requireActivity().finish();
                                                }
                                            });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(requireActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        removeDataFromSharedPref();

                    } else {
                        assert deleteAcShowError != null;
                        validator(edtDeleteAcPassword, deleteAcShowError, "Please enter valid password.");
                    }
                } else {
                    assert deleteAcShowError != null;
                    validator(edtDeleteAcPassword, deleteAcShowError, "Please enter your password.");
                }
            }
        });

        assert btnDeleteAcDialogNo != null;
        btnDeleteAcDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountDialog.dismiss();
            }
        });
    }

    // Method for user logging out
    private void logOutUser() {
        BottomSheetDialog logOutDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);
        bottomDialog(logOutDialog);
        logOutDialog.setContentView(R.layout.bottom_dialog_logout);

        AppCompatButton btnLogOutDialogYes = logOutDialog.findViewById(R.id.btn_logout_dialog_yes);
        AppCompatButton btnLogOutDialogNo = logOutDialog.findViewById(R.id.btn_logout_dialog_no);

        logOutDialog.show();
        assert btnLogOutDialogYes != null;
        btnLogOutDialogYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                removeDataFromSharedPref();

                mReference.child("student")
                        .child(userID)
                        .child("isLoggedIn")
                        .setValue(false);

                Toast.makeText(requireActivity(), "You've Been Logged Out.", Toast.LENGTH_SHORT).show();

                logOutDialog.dismiss();

                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            }
        });
        btnLogOutDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutDialog.dismiss();
            }
        });
    }

    private void removeDataFromSharedPref() {
        editor.putBoolean("userCheck", false);
        editor.putString("userID", "");
        editor.putString("userEmail", "");
        editor.putString("userName", "");
        editor.putString("prevExamResult", "0");
        editor.putString("prevExamTotalMarks", "0");
        editor.putBoolean("introDialog", false);

        editor.apply();
    }

    // Method for loading user result data from database
    private void loadResultAndShow(ListView resultList) {
        mReference
                .child("result")
                .orderByChild("userId")
                .equalTo(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        examResultModelList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ExamResultModel newERM = dataSnapshot.getValue(ExamResultModel.class);
                            examResultModelList.add(newERM);
                        }

                        Collections.reverse(examResultModelList);

                        resultProgressBar.setVisibility(View.GONE);
                        resultList.setVisibility(View.VISIBLE);
                        CustomAdapter resListAdapter = new CustomAdapter(requireActivity(), R.layout.list_item_result, examResultModelList.size());
                        resListAdapter.setExamResultModelList(examResultModelList);

                        resultList.setAdapter(resListAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireActivity(), "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    // Bottom Dialog Style Change
    private void bottomDialog(BottomSheetDialog bottomSheetDialog) {
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.getBehavior().setSkipCollapsed(true);
        bottomSheetDialog.getBehavior().setState(STATE_EXPANDED);
    }

    // Showing error
    private void validator(EditText editText, TextView textView, String string) {
        textView.setVisibility(View.VISIBLE);
        textView.setText(string);
        editText.requestFocus();
    }

    // Changing Password
    private void changePassword(String email, String oldPass, String newPass) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                                        logOutUser();
                                    } else {
                                        Toast.makeText(requireActivity(), "Something went wrong please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(requireActivity(), "Something went wrong please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}