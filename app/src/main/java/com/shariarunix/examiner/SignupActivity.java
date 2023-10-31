package com.shariarunix.examiner;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.Adapter.CustomAdapter;
import com.shariarunix.examiner.DataModel.CourseDataModel;
import com.shariarunix.examiner.DataModel.StudentDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    public static final String TB_STUDENT = "student";
    private static final String TB_COURSE = "courseList";
    ListView courseList;
    RelativeLayout courseSelectorLayout;
    TextView txtSelectCourse, txtCourseEmpty, txtNameEmpty, txtNameSpecialCharacter, txtNameNumber,
            txtEmailValidate, txtEmailEmpty, txtUrNameEmpty, txtUrNameSpCharacter, txtUserNameDot, txtPhoneEmpty,
            txtPhoneValidate, txtGrdPhoneEmpty, txtGrdPhoneValidate, txtPassEmpty, txtPassDigits,
            txtPassUpperCase, txtPassLowerCase, txtPassNum, txtPassSpecialCharacter,
            txtConfirmPassEmpty, txtConfirmPass;
    private EditText edtSignUpName, edtSignUpEmail, edtSignUpPhone, edtSignUpGrdPhone, edtSignUpPass, edtSignUpConfirmPass;
    ImageView icPassShow, icConfirmPassShow;
    List<CourseDataModel> courseListData = new ArrayList<>();
    boolean findSpecialChar = false, passShowToggle = false, conPassShowToggle = false;

    // Data Model for Student
    private StudentDataModel sDataModel;

    // Declaring Firebase Auth
    private FirebaseAuth mAuth;

    // Declaring Database Reference
    private DatabaseReference mReference;
    Dialog loadingDialog;
    private String userID;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();

        loadingDialog = new Dialog(SignupActivity.this);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.dialog_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        courseSelectorLayout = findViewById(R.id.course_selector_layout);
        txtSelectCourse = findViewById(R.id.txt_select_course);
        edtSignUpName = findViewById(R.id.edt_sign_up_name);
        edtSignUpEmail = findViewById(R.id.edt_sign_up_email);
        edtSignUpPhone = findViewById(R.id.edt_sign_up_phone);
        edtSignUpGrdPhone = findViewById(R.id.edt_sign_up_grd_phone);
        edtSignUpPass = findViewById(R.id.edt_sign_up_pass);
        edtSignUpConfirmPass = findViewById(R.id.edt_sign_up_confirm_pass);
        txtCourseEmpty = findViewById(R.id.txt_course_empty);

        //id find for name error
        txtNameEmpty = findViewById(R.id.txt_name_empty);
        txtNameNumber = findViewById(R.id.txt_name_numeric_num);
        txtNameSpecialCharacter = findViewById(R.id.txt_name_special_character);

        //id find for email error
        txtEmailValidate = findViewById(R.id.txt_email_validate);
        txtEmailEmpty = findViewById(R.id.txt_email_empty);

        //id find for phone error
        txtPhoneEmpty = findViewById(R.id.txt_phone_empty);
        txtPhoneValidate = findViewById(R.id.txt_phone_validate);

        //id find for guardian phone error
        txtGrdPhoneValidate = findViewById(R.id.txt_grd_phone_validate);
        txtGrdPhoneEmpty = findViewById(R.id.txt_grd_phone_empty);

        //id find for pass error
        txtPassEmpty = findViewById(R.id.txt_pass_empty);
        txtPassUpperCase = findViewById(R.id.txt_pass_uppercase);
        txtPassLowerCase = findViewById(R.id.txt_pass_lowercase);
        txtPassSpecialCharacter = findViewById(R.id.txt_pass_special_character);
        txtPassDigits = findViewById(R.id.txt_pass_digits);
        txtPassNum = findViewById(R.id.txt_pass_num);

        //id find for confirm pass
        txtConfirmPass = findViewById(R.id.txt_confirm_pass);
        txtConfirmPassEmpty = findViewById(R.id.txt_confirm_pass_empty);

        icPassShow = findViewById(R.id.ic_pass_show);
        icConfirmPassShow = findViewById(R.id.ic_confirm_pass_show);

        //show error for name
        edtSignUpName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String name = edtSignUpName.getText().toString().trim();

                if (!name.isEmpty()) {
                    if (name.matches(".*[^a-zA-Z 0-9].*")) {
                        txtNameSpecialCharacter.setVisibility(View.VISIBLE);
                        txtNameSpecialCharacter.setText("Remove special character");
                    }
                    if (name.matches(".*[0-9].*")) {
                        txtNameNumber.setVisibility(View.VISIBLE);
                        txtNameNumber.setText("Remove numeric value");
                    }
                } else {
                    txtNameEmpty.setVisibility(View.VISIBLE);
                    txtNameEmpty.setText("Enter your name");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String name = edtSignUpName.getText().toString().trim();
                if (!name.isEmpty()) {
                    txtNameEmpty.setVisibility(View.GONE);

                    if (!name.matches(".*[^a-zA-Z 0-9].*")) {
                        txtNameSpecialCharacter.setVisibility(View.GONE);
                    }
                    if (!name.matches(".*[0-9].*")) {
                        txtNameNumber.setVisibility(View.GONE);
                    }
                } else {
                    txtNameNumber.setVisibility(View.GONE);
                    txtNameSpecialCharacter.setVisibility(View.GONE);
                }
            }
        });
        //show error for email
        edtSignUpEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String email = edtSignUpEmail.getText().toString().trim();

                if (!email.isEmpty()) {
                    if (!email.matches("^[a-zA-z0-9_\\-]*@gmail\\.com$")) {
                        txtEmailValidate.setVisibility(View.VISIBLE);
                        txtEmailValidate.setText("Enter a valid email address");
                    }
                } else {
                    txtEmailEmpty.setVisibility(View.VISIBLE);
                    txtEmailEmpty.setText("Enter your email address");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String email = edtSignUpEmail.getText().toString();

                if (!email.isEmpty()) {
                    txtEmailEmpty.setVisibility(View.GONE);

                    if (email.matches("^[a-zA-z0-9_\\-]*@gmail\\.com$")) {
                        txtEmailValidate.setVisibility(View.GONE);
                    }
                } else {
                    txtEmailValidate.setVisibility(View.GONE);
                }
            }
        });
        //show error for phone num
        edtSignUpPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String phone = edtSignUpPhone.getText().toString().trim();

                if (!phone.isEmpty()) {
                    if (!phone.matches("^(?:\\+?88)?01[3-9]\\d{8}$")) {
                        txtPhoneValidate.setVisibility(View.VISIBLE);
                        txtPhoneValidate.setText("Enter a valid phone number");
                    }
                } else {
                    txtPhoneEmpty.setVisibility(View.VISIBLE);
                    txtPhoneEmpty.setText("Enter your phone number");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = edtSignUpPhone.getText().toString().trim();

                if (!phone.isEmpty()) {
                    txtPhoneEmpty.setVisibility(View.GONE);

                    if (phone.matches("^(?:\\+?88|0088)?01[3-9]\\d{8}$")) {
                        txtPhoneValidate.setVisibility(View.GONE);
                    }
                } else {
                    txtPhoneValidate.setVisibility(View.GONE);
                }
            }
        });
        //show error for guardian phone num
        edtSignUpGrdPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String grdPhone = edtSignUpGrdPhone.getText().toString().trim();

                if (!grdPhone.isEmpty()) {
                    if (!grdPhone.matches("^(?:\\+?88)?01[3-9]\\d{8}$")) {
                        txtGrdPhoneValidate.setVisibility(View.VISIBLE);
                        txtGrdPhoneValidate.setText("Enter a valid phone number");
                    }
                } else {
                    txtGrdPhoneEmpty.setVisibility(View.VISIBLE);
                    txtGrdPhoneEmpty.setText("Enter your guardian phone number");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String grdPhone = edtSignUpGrdPhone.getText().toString().trim();

                if (!grdPhone.isEmpty()) {
                    txtGrdPhoneEmpty.setVisibility(View.GONE);

                    if (grdPhone.matches("^(?:\\+?88|0088)?01[3-9]\\d{8}$")) {
                        txtGrdPhoneValidate.setVisibility(View.GONE);
                    }
                } else {
                    txtGrdPhoneValidate.setVisibility(View.GONE);
                }
            }
        });
        //show error foe pass
        edtSignUpPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String pass = edtSignUpPass.getText().toString().trim();

                if (!pass.isEmpty()) {
                    if (pass.length() < 8) {
                        txtPassDigits.setVisibility(View.VISIBLE);
                        txtPassDigits.setText("Password must be contains 8 characters");
                    }
                    if (!pass.matches(".*[^a-zA-Z0-9].*")) {
                        txtPassSpecialCharacter.setVisibility(View.VISIBLE);
                        txtPassSpecialCharacter.setText("At least one special character");
                    }
                    if (!pass.matches(".*[a-z].*")) {
                        txtPassLowerCase.setVisibility(View.VISIBLE);
                        txtPassLowerCase.setText("At least one lowercase");
                    }
                    if (!pass.matches(".*[A-Z].*")) {
                        txtPassUpperCase.setVisibility(View.VISIBLE);
                        txtPassUpperCase.setText("At least one uppercase");
                    }
                    if (!pass.matches(".*[0-9].*")) {
                        txtPassNum.setVisibility(View.VISIBLE);
                        txtPassNum.setText("At least one numeric value");
                    }
                } else {
                    txtPassEmpty.setVisibility(View.VISIBLE);
                    txtPassEmpty.setText("Enter your password");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pass = edtSignUpPass.getText().toString().trim();

                if (!pass.isEmpty()) {
                    txtPassEmpty.setVisibility(View.GONE);

                    if (pass.matches(".*[^a-zA-Z0-9].*")) {
                        txtPassSpecialCharacter.setVisibility(View.GONE);
                    }
                    if (pass.length() >= 8) {
                        txtPassDigits.setVisibility(View.GONE);
                    }
                    if (pass.matches(".*[a-z].*")) {
                        txtPassLowerCase.setVisibility(View.GONE);
                    }
                    if (pass.matches(".*[A-Z].*")) {
                        txtPassUpperCase.setVisibility(View.GONE);
                    }
                    if (pass.matches(".*[0-9].*")) {
                        txtPassNum.setVisibility(View.GONE);
                    }
                } else {
                    txtPassSpecialCharacter.setVisibility(View.GONE);
                    txtPassDigits.setVisibility(View.GONE);
                    txtPassUpperCase.setVisibility(View.GONE);
                    txtPassLowerCase.setVisibility(View.GONE);
                    txtPassNum.setVisibility(View.GONE);
                }
            }
        });
        //show error for confirm pass
        edtSignUpConfirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String pass = edtSignUpPass.getText().toString().trim();
                String confirmPass = edtSignUpConfirmPass.getText().toString().trim();

                if (!confirmPass.isEmpty()) {
                    if (!confirmPass.equals(pass)) {
                        txtConfirmPass.setVisibility(View.VISIBLE);
                        txtConfirmPass.setText("Confirmed password does not match the password");
                    }
                } else {
                    txtConfirmPassEmpty.setVisibility(View.VISIBLE);
                    txtConfirmPassEmpty.setText("Enter your password again");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pass = edtSignUpPass.getText().toString().trim();
                String confirmPass = edtSignUpConfirmPass.getText().toString().trim();

                if (!confirmPass.isEmpty()) {
                    txtConfirmPass.setVisibility(View.GONE);

                    if (!confirmPass.equals(pass)) {
                        txtConfirmPassEmpty.setVisibility(View.GONE);
                    }
                } else {
                    txtConfirmPassEmpty.setVisibility(View.GONE);
                }
            }
        });

        // Making The Dialog
        BottomSheetDialog courseSelectDialog = new BottomSheetDialog(SignupActivity.this, R.style.bottom_sheet_dialog);
        Objects.requireNonNull(courseSelectDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        courseSelectDialog.getBehavior().setSkipCollapsed(true);
        courseSelectDialog.getBehavior().setState(STATE_EXPANDED);
        courseSelectDialog.setContentView(R.layout.bottom_dialog_course_select);

        // Selecting course from dialog
        courseSelectorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseSelectDialog.show();
            }
        });

        // Making The List of Course
        courseList = courseSelectDialog.findViewById(R.id.course_list);

        // Adding item to the course list
        loadCourseList();

        // Firing on item click listener on list item
        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                txtSelectCourse.setText(courseListData.get(i).getCourseName());
                courseSelectDialog.dismiss();
                txtCourseEmpty.setVisibility(View.GONE);
            }
        });

        // Change the activity to login page on clicking sign in text
        findViewById(R.id.txt_btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Setting operation on sign up button
        findViewById(R.id.btn_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSignup();
            }
        });

        // Password and confirm password show or hide
        icPassShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!passShowToggle) {
                    new PassShowHide(edtSignUpPass, icPassShow, false).passShow();
                    passShowToggle = true;
                } else {
                    new PassShowHide(edtSignUpPass, icPassShow, true).passHide();
                    passShowToggle = false;
                }
            }
        });
        icConfirmPassShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!conPassShowToggle) {
                    new PassShowHide(edtSignUpConfirmPass, icConfirmPassShow, false).passShow();
                    conPassShowToggle = true;
                } else {
                    new PassShowHide(edtSignUpConfirmPass, icConfirmPassShow, true).passHide();
                    conPassShowToggle = false;
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void userSignup() {
        String name = edtSignUpName.getText().toString().trim();
        String email = edtSignUpEmail.getText().toString().trim();
        String phone = edtSignUpPhone.getText().toString().trim();
        String guardianPhone = edtSignUpGrdPhone.getText().toString().trim();
        String course = txtSelectCourse.getText().toString().trim();
        String pass = edtSignUpPass.getText().toString().trim();
        String confirmPass = edtSignUpConfirmPass.getText().toString().trim();

        if (name.isEmpty()) {
            txtNameEmpty.setVisibility(View.VISIBLE);
            txtNameEmpty.setText("Enter your name");
            edtSignUpName.requestFocus();
        } else {
            if (txtNameSpecialCharacter.getVisibility() == View.VISIBLE) {
                edtSignUpName.requestFocus();
                return;
            }
            if (txtNameNumber.getVisibility() == View.VISIBLE) {
                edtSignUpName.requestFocus();
                return;
            }
        }

        if (email.isEmpty()) {
            txtEmailEmpty.setVisibility(View.VISIBLE);
            txtEmailEmpty.setText("Enter your email address");
            edtSignUpEmail.requestFocus();
        } else {
            if (txtEmailValidate.getVisibility() == View.VISIBLE) {
                edtSignUpEmail.requestFocus();
                return;
            }
        }

        if (phone.isEmpty()) {
            txtPhoneEmpty.setVisibility(View.VISIBLE);
            txtPhoneEmpty.setText("Enter your phone number");
            edtSignUpPhone.requestFocus();
        } else {
            if (txtPhoneValidate.getVisibility() == View.VISIBLE) {
                edtSignUpPhone.requestFocus();
                return;
            }
        }

        if (guardianPhone.isEmpty()) {
            txtGrdPhoneEmpty.setVisibility(View.VISIBLE);
            txtGrdPhoneEmpty.setText("Enter your guardian phone number");
            edtSignUpGrdPhone.requestFocus();
        } else {
            if (txtGrdPhoneValidate.getVisibility() == View.VISIBLE) {
                edtSignUpGrdPhone.requestFocus();
                return;
            }
        }

        if (course.isEmpty()) {
            txtCourseEmpty.setVisibility(View.VISIBLE);
            txtCourseEmpty.setText("Select your course");
            txtSelectCourse.requestFocus();
        } else {
            if (txtCourseEmpty.getVisibility() == View.VISIBLE) {
                txtSelectCourse.requestFocus();
                return;
            }
        }

        if (pass.isEmpty()) {
            txtPassEmpty.setVisibility(View.VISIBLE);
            txtPassEmpty.setText("Enter your password");
            edtSignUpPass.requestFocus();
        } else {
            if (txtPassUpperCase.getVisibility() == View.VISIBLE) {
                edtSignUpPass.requestFocus();
                return;
            }
            if (txtPassLowerCase.getVisibility() == View.VISIBLE) {
                edtSignUpPass.requestFocus();
                return;
            }
            if (txtPassDigits.getVisibility() == View.VISIBLE) {
                edtSignUpPass.requestFocus();
                return;
            }
            if (txtPassSpecialCharacter.getVisibility() == View.VISIBLE) {
                edtSignUpPass.requestFocus();
                return;
            }
            if (txtPassNum.getVisibility() == View.VISIBLE) {
                edtSignUpPass.requestFocus();
                return;
            }
        }

        if (confirmPass.isEmpty()) {
            txtConfirmPassEmpty.setVisibility(View.VISIBLE);
            txtConfirmPassEmpty.setText("Enter your password again");
            edtSignUpConfirmPass.requestFocus();
        } else {
            if (txtConfirmPass.getVisibility() == View.VISIBLE) {
                edtSignUpConfirmPass.requestFocus();
                return;
            }
        }

        if (name.isEmpty()) {
            edtSignUpName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtSignUpEmail.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            edtSignUpPhone.requestFocus();
            return;
        }
        if (guardianPhone.isEmpty()) {
            edtSignUpGrdPhone.requestFocus();
            return;
        }
        if (course.isEmpty()) {
            txtSelectCourse.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            edtSignUpPass.requestFocus();
            return;
        }
        if (confirmPass.isEmpty()) {
            edtSignUpConfirmPass.requestFocus();
            return;
        }


        // Create new user in Firebase
        mAuth.createUserWithEmailAndPassword(email, confirmPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Getting User UID
                    FirebaseUser user = mAuth.getCurrentUser();
                    assert user != null;
                    userID = user.getUid();

                    sDataModel = new StudentDataModel(userID, "", name, email, phone, guardianPhone, course, confirmPass, "", 0, 0, true);

                    // Setting Data as a Child of UID
                    setData(userID);

                    SharedPreferences sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putBoolean("userCheck", true);
                    editor.putBoolean("introDialog", true);
                    editor.putString("userID", userID);
                    editor.apply();

                    // Starting the Main Activity
                    Intent iNext = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(iNext);
                    finish();
                } else {
                    txtEmailValidate.setVisibility(View.VISIBLE);
                    txtEmailValidate.setText("Please Enter a different email address");
                    edtSignUpEmail.requestFocus();
                }
            }
        });
    }

    // Set Student's Data to Firebase Database
    private void setData(String key) {
        mReference.child(TB_STUDENT).child(key).setValue(sDataModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(SignupActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load Course list
    private void loadCourseList() {
        mReference.child(TB_COURSE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseListData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    courseListData.add(dataSnapshot.getValue(CourseDataModel.class));
                }
                courseListData.removeIf(CourseDataModel -> CourseDataModel.getCourseName().equals("All"));

                CustomAdapter courseListAdapter = new CustomAdapter(SignupActivity.this,
                        R.layout.list_item_course,
                        courseListData.size());
                courseListAdapter.setCourseDataModelList(courseListData);

                courseList.setAdapter(courseListAdapter);
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}