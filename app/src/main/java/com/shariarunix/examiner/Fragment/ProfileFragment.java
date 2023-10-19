package com.shariarunix.examiner.Fragment;

import static android.content.Context.MODE_PRIVATE;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.shariarunix.examiner.LoginActivity;
import com.shariarunix.examiner.PassShowHide;
import com.shariarunix.examiner.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final String U_NAME = "arg1";
    private static final String U_EMAIL = "arg2";
    private static final String U_PHONE = "arg3";
    private static final String U_COURSE = "arg4";
    private static final String U_PASS = "arg5";
    String userName , userEmail, userPhone, userCourse, userPass, userID;
    List<ExamResultModel> examResultModelList = new ArrayList<>();
    BottomSheetDialog personalInfoDialog, changeInfoDialog, changePassDialog, resultDialog;
    DatabaseReference mReference;
    FirebaseUser user;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean findSpecialChar = false;
    boolean oldPassShowToggle = false;
    boolean newPassShowToggle = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment getInstance(String uName,String uEmail,String uPhone,String uCourse, String uPass){
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();

        bundle.putString(U_NAME, uName);
        bundle.putString(U_EMAIL, uEmail);
        bundle.putString(U_PHONE, uPhone);
        bundle.putString(U_COURSE, uCourse);
        bundle.putString(U_PASS, uPass);

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
        ImageButton imgBtnLogOut = view.findViewById(R.id.img_btn_logout);

        AppCompatButton btnProfileSeeResult = view.findViewById(R.id.btn_profile_see_result);

        // User Logged Out
        imgBtnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutUser();
                Toast.makeText(requireActivity(), "You've Been Logged Out.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set all info in variable
        if (getArguments() != null){
            userName = getArguments().getString(U_NAME);
            userEmail = getArguments().getString(U_EMAIL);
            userPhone = getArguments().getString(U_PHONE);
            userCourse = getArguments().getString(U_COURSE);
            userCourse = getArguments().getString(U_COURSE);
            userPass = getArguments().getString(U_PASS);
            // Set name of user
            txtProfileShowName.setText(userName);
        }

        // Personal Info
        personalInfoDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);
        changeInfoDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);
        resultDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        // Dialog for showing result
        btnProfileSeeResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog(resultDialog);
                resultDialog.setContentView(R.layout.result_dialog);
                resultDialog.setCancelable(false);

                ListView resultList = resultDialog.findViewById(R.id.result_list);
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
        });

        personalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog(personalInfoDialog);
                personalInfoDialog.setContentView(R.layout.personal_info_dialog);

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

                        bottomDialog(changeInfoDialog);
                        changeInfoDialog.setContentView(R.layout.change_personal_info_dialog);

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
                                String[] specialCharacter = new String[]{"~","!","@","#","$","%","^","&","*","(",")","_","-","+","=","/","\\","<",">","{","}","[","]",",","?","|","`"};
                                for (String s : specialCharacter) {
                                    if (changeName.contains(s)) {
                                        findSpecialChar = true;
                                        break;
                                    }
                                }
                                if (findSpecialChar){
                                    assert showErrorChangeInfo != null;
                                    validator(edtChangeName, showErrorChangeInfo, "Please remove special character's from your name");
                                    findSpecialChar = false;
                                    return;
                                }
                                if (changeName.isEmpty()){
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
                                if (!changePhone.matches("^(?:\\+88|0088)?(01[3-9]\\d{8})$")){
                                    assert showErrorChangeInfo != null;
                                    validator(edtChangePhone, showErrorChangeInfo, "Please enter a valid phone number");
                                    return;
                                }


                                String uId =sharedPreferences.getString("userID","");

                                mReference.child("student").child(uId).child("name").setValue(changeName);
                                mReference.child("student").child(uId).child("phone").setValue(changePhone);

                                changeInfoDialog.dismiss();
                            }
                        });
                    }
                });
            }
        });

        // Change Password
        changePassDialog = new BottomSheetDialog(requireActivity(), R.style.bottom_sheet_dialog);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog(changePassDialog);
                changePassDialog.setContentView(R.layout.change_password_dialog);

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
                        if (!oldPassShowToggle) {
                            new PassShowHide(edtOldPass, oldPassShow, false).passShow();
                            oldPassShowToggle = true;
                        } else {
                            new PassShowHide(edtOldPass, oldPassShow, true).passHide();
                            oldPassShowToggle = false;
                        }
                    }
                });
                assert newPassShow != null;
                newPassShow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!newPassShowToggle) {
                            new PassShowHide(edtNewPass, newPassShow, false).passShow();
                            newPassShowToggle = true;
                        } else {
                            new PassShowHide(edtNewPass, newPassShow, true).passHide();
                            newPassShowToggle = false;
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
                        if (oldPass.isEmpty()){
                            assert showErrorChangePass != null;
                            validator(edtOldPass, showErrorChangePass, "Please enter your password");
                            return;
                        }
                        if (oldPass.length()<8){
                            assert showErrorChangePass != null;
                            validator(edtOldPass, showErrorChangePass,"Password must be at least 8 characters");
                            return;
                        }
                        // Checking old password
                        if (newPass.isEmpty()){
                            assert showErrorChangePass != null;
                            validator(edtNewPass, showErrorChangePass,"Please enter your password");
                            return;
                        }
                        if (newPass.length()<8){
                            assert showErrorChangePass != null;
                            validator(edtNewPass, showErrorChangePass,"Password must be at least 8 characters");
                            return;
                        }

                        // Checking is old password is valid or not
                        if (userPass.equals(oldPass)){
                            String uId = user.getUid();
                            mReference.child("student").child(uId).child("password").setValue(newPass);

                            changePassword(userEmail, oldPass, newPass);
                            changePassDialog.dismiss();
                        } else {
                            assert showErrorChangePass != null;
                            validator(edtOldPass, showErrorChangePass,"Password you entered as old password is not valid");
                        }
                    }
                });

                changePassDialog.show();
            }
        });

        // Forgot Password
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireActivity(), "On Working", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    // Method for user logging out
    private void logOutUser(){
        FirebaseAuth.getInstance().signOut();

        editor.putBoolean("userCheck", false);
        editor.putString("userID", "");
        editor.putString("userEmail", "");
        editor.putString("prevExamResult", "0");
        editor.putString("prevExamTotalMarks", "0");
        editor.putBoolean("introDialog", false);

        editor.apply();

        startActivity(new Intent(requireActivity(), LoginActivity.class));
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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ExamResultModel newERM = dataSnapshot.getValue(ExamResultModel.class);
                    examResultModelList.add(newERM);
                }

                Collections.reverse(examResultModelList);

                CustomAdapter resListAdapter = new CustomAdapter(requireActivity(), R.layout.result_list_item,examResultModelList.size());
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
    private void bottomDialog(BottomSheetDialog bottomSheetDialog){
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.getBehavior().setSkipCollapsed(true);
        bottomSheetDialog.getBehavior().setState(STATE_EXPANDED);
    }

    // Showing error
    private void validator(EditText editText,TextView textView, String string){
        textView.setVisibility(View.VISIBLE);
        textView.setText(string);
        editText.requestFocus();
    }

    // Changing Password
    private void changePassword(String email, String oldPass, String newPass){
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