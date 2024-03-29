package com.shariarunix.examiner.Fragment;

import static android.content.Context.MODE_PRIVATE;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.shariarunix.examiner.SignupActivity.TB_STUDENT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.shariarunix.examiner.OTPSender.OTPSenderClass;
import com.shariarunix.examiner.PassShowHide;
import com.shariarunix.examiner.R;
import com.shariarunix.examiner.SignupActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ProfileFragment extends Fragment {
    private static final String U_DATA = "arg1";
    String userName, userEmail, userPhone, userGuardianPhone, userCourse, userPass, userID;
    List<ExamResultModel> examResultModelList = new ArrayList<>();
    DatabaseReference mReference;
    FirebaseUser user;
    ListView resultList;
    ProgressBar resultProgressBar;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean findSpecialChar = false;
    FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

        TextView txtProfileShowName = view.findViewById(R.id.txt_profile_show_name);

        LinearLayout personalInfo = view.findViewById(R.id.personal_info);
        LinearLayout changePass = view.findViewById(R.id.change_password);
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
            userGuardianPhone = studentDataModel.getGuardianPhone();
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

        return view;
    }

    // Password change dialog
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

    // Showing personal info
    private void showPersonalInfoDialog() {
        BottomSheetDialog personalInfoDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(personalInfoDialog);
        personalInfoDialog.setContentView(R.layout.bottom_dialog_personal_info);

        TextView personalInfoName = personalInfoDialog.findViewById(R.id.personal_info_name);
        TextView personalInfoEmail = personalInfoDialog.findViewById(R.id.personal_info_email);
        TextView personalInfoPhone = personalInfoDialog.findViewById(R.id.personal_info_phone);
        TextView personalInfoGuardianPhone = personalInfoDialog.findViewById(R.id.personal_info_guardian_phone);
        TextView personalInfoCourse = personalInfoDialog.findViewById(R.id.personal_info_course);

        AppCompatButton btnChangeInformation = personalInfoDialog.findViewById(R.id.btn_change_information);

        assert personalInfoName != null;
        personalInfoName.setText(userName);

        assert personalInfoEmail != null;
        personalInfoEmail.setText(userEmail);

        assert personalInfoPhone != null;
        personalInfoPhone.setText(userPhone);

        assert personalInfoGuardianPhone != null;
        personalInfoGuardianPhone.setText(userGuardianPhone);

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

    // Personal info change dialog
    private void showChangeInfoDialog() {
        BottomSheetDialog changeInfoDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        bottomDialog(changeInfoDialog);
        changeInfoDialog.setContentView(R.layout.bottom_dialog_change_personal_info);

        EditText edtChangeName = changeInfoDialog.findViewById(R.id.edt_change_name);
        EditText edtChangePhone = changeInfoDialog.findViewById(R.id.edt_change_phone);
        EditText edtChangeGuardianPhone = changeInfoDialog.findViewById(R.id.edt_change_guardian_phone);

        TextView showErrorChangeInfo = changeInfoDialog.findViewById(R.id.show_error);

        AppCompatButton btnChangeInformation = changeInfoDialog.findViewById(R.id.btn_change_information);

        assert edtChangeName != null;
        edtChangeName.setHint(userName);

        assert edtChangePhone != null;
        edtChangePhone.setHint(userPhone);

        assert edtChangeGuardianPhone != null;
        edtChangeGuardianPhone.setHint(userGuardianPhone);

        changeInfoDialog.show();

        assert btnChangeInformation != null;
        btnChangeInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do change operation
                String changeName = edtChangeName.getText().toString().trim();
                String changePhone = edtChangePhone.getText().toString().trim();
                String changeGuardianPhone = edtChangeGuardianPhone.getText().toString().trim();

                assert showErrorChangeInfo != null;

                // Checking is name ok or not?
                if (!changeName.isEmpty() && !edtChangeName.getHint().equals(changeName)) {
                    if (changeName.matches(".*[^a-zA-Z 0-9].*")) {
                        validator(edtChangeName, showErrorChangeInfo, "Special character and number isn't allowed");
                        return;
                    }
                    if (changeName.matches(".*[0-9].*")) {
                        validator(edtChangeName, showErrorChangeInfo, "Special character and number isn't allowed");
                        return;
                    }
                    mReference.child("student").child(userID).child("name").setValue(changeName);
                }

                // Checking is phone number ok or not?
                if (!changePhone.isEmpty() && !edtChangePhone.getHint().equals(changePhone)) {
                    if (!changePhone.matches("^(?:\\+88|0088)?(01[3-9]\\d{8})$")) {
                        validator(edtChangePhone, showErrorChangeInfo, "Please enter a valid phone number");
                        return;
                    }
                    mReference.child("student").child(userID).child("phone").setValue(changePhone);
                }

                // Checking is guardian phone number ok or not?
                if (!changePhone.isEmpty() && !edtChangePhone.getHint().equals(changePhone)) {
                    if (!changePhone.matches("^(?:\\+88|0088)?(01[3-9]\\d{8})$")) {
                        validator(edtChangeGuardianPhone, showErrorChangeInfo, "Please enter a valid phone number");
                        return;
                    }
                    mReference.child("student").child(userID).child("guardianPhone").setValue(changeGuardianPhone);
                }

                changeInfoDialog.dismiss();
            }
        });
    }

    // Result Showing Dialog
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

        assert icPassShow != null;
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

                mReference.child("student")
                        .child(userID)
                        .child("isLoggedIn")
                        .setValue(false);

                removeDataFromSharedPref();

                Toast.makeText(requireActivity(), "You've Been Logged Out.", Toast.LENGTH_SHORT).show();

                logOutDialog.dismiss();

                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            }
        });
        assert btnLogOutDialogNo != null;
        btnLogOutDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutDialog.dismiss();
            }
        });
    }

    // Remove user data from sharedpref
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
                        if (isAdded()) {
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