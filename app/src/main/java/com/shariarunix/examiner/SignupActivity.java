package com.shariarunix.examiner;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
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
import com.shariarunix.examiner.DataModel.StudentDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    ListView courseList;
    RelativeLayout courseSelectorLayout;
    TextView txtSelectCourse, showError;
    private EditText edtSignUpName, edtSignUpEmail, edtSignUpPhone, edtSignUpPass, edtSignUpConfirmPass;
    ImageView icPassShow, icConfirmPassShow;
    List<String> courseListData = new ArrayList<>();
    boolean findSpecialChar = false;
    boolean passShowToggle = false;
    boolean conPassShowToggle = false;

    // Data Model for Student
    private StudentDataModel sDataModel;

    // Declaring Firebase Auth
    private FirebaseAuth mAuth;

    // Declaring Database Reference
    private DatabaseReference mReference;
    Dialog loadingDialog;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();

        loadingDialog = new Dialog(SignupActivity.this);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.progressbar_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Adding item to the course list
        loadCourseList();

        courseSelectorLayout = findViewById(R.id.course_selector_layout);
        txtSelectCourse = findViewById(R.id.txt_select_course);
        edtSignUpName = findViewById(R.id.edt_sign_up_name);
        edtSignUpEmail = findViewById(R.id.edt_sign_up_email);
        edtSignUpPhone = findViewById(R.id.edt_sign_up_phone);
        edtSignUpPass = findViewById(R.id.edt_sign_up_pass);
        edtSignUpConfirmPass = findViewById(R.id.edt_sign_up_confirm_pass);
        showError = findViewById(R.id.show_error);

        icPassShow = findViewById(R.id.ic_pass_show);
        icConfirmPassShow = findViewById(R.id.ic_confirm_pass_show);

        // Making The Dialog
        BottomSheetDialog personalInfoDialog = new BottomSheetDialog(SignupActivity.this, R.style.bottom_sheet_dialog);
        Objects.requireNonNull(personalInfoDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        personalInfoDialog.getBehavior().setSkipCollapsed(true);
        personalInfoDialog.getBehavior().setState(STATE_EXPANDED);
        personalInfoDialog.setContentView(R.layout.course_select_dialog);

        // Selecting course from dialog
        courseSelectorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personalInfoDialog.show();
            }
        });

        // Making The List of Course
        courseList = personalInfoDialog.findViewById(R.id.course_list);
        assert courseList != null;
        courseList.setAdapter(new ArrayAdapter<>(SignupActivity.this,
                R.layout.course_list_item,
                R.id.txt_list_item, courseListData));

        // Firing on item click listener on list item
        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                txtSelectCourse.setText(courseListData.get(i));
                personalInfoDialog.dismiss();
            }
        });

        // Change the activity to login page on clicking sign in text
        findViewById(R.id.txt_btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
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
        String course = txtSelectCourse.getText().toString().trim();
        String pass = edtSignUpPass.getText().toString().trim();
        String confirmPass = edtSignUpConfirmPass.getText().toString().trim();
        // Checking is name ok or not?
        String[] specialCharacter = new String[]{"~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+", "=", "/", "\\", "<", ">", "{", "}", "[", "]", ",", "?", "|", "`"};
        for (String s : specialCharacter) {
            if (name.contains(s)) {
                findSpecialChar = true;
                break;
            }
        }
        if (findSpecialChar) {
            validator(edtSignUpName, "Please remove special character's from your name");
            findSpecialChar = false;
            return;
        }
        if (name.isEmpty()) {
            validator(edtSignUpName, "Please enter your name");
            return;
        }
        // Checking is email ok or not?
        if (email.isEmpty()) {
            validator(edtSignUpEmail, "Please enter your email address");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validator(edtSignUpEmail, "Please enter a valid email address");
            return;
        }
        // Checking is phone number ok or not?
        if (phone.isEmpty()) {
            validator(edtSignUpPhone, "Please enter your phone number");
            return;
        }
        if (!phone.matches("^(?:\\+88|0088)?(01[3-9]\\d{8})$")) {
            validator(edtSignUpPhone, "Please enter a valid phone number");
            return;
        }
        // Checking is course selected or not
        if (course.isEmpty()) {
            showError.setVisibility(View.VISIBLE);
            showError.setText("Please select your course");
            return;
        }
        // Checking password
        if (pass.isEmpty()) {
            validator(edtSignUpPass, "Please enter your password");
            return;
        }
        if (pass.length() < 8) {
            validator(edtSignUpPass, "Password must be at least 8 characters");
            return;
        }
        if (!confirmPass.equals(pass)) {
            validator(edtSignUpConfirmPass, "Password and confirm password is not same");
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

                    sDataModel = new StudentDataModel(name, email, phone, course, confirmPass, userID, 0, 0);

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
                    validator(edtSignUpEmail, "Please use different email");
                }
            }
        });
    }

    // Set Student's Data to Firebase Database
    private void setData(String key) {
        mReference.child("student").child(key).setValue(sDataModel);
    }

    // Load Course list
    private void loadCourseList() {
        mReference.child("courseList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseListData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    courseListData.add(dataSnapshot.getValue(String.class));
                }
                courseListData.remove("All");
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Checking input field
    private void validator(EditText editText, String string) {
        showError.setVisibility(View.VISIBLE);
        showError.setText(string);
        editText.requestFocus();
    }
}