package com.shariarunix.examiner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.DataModel.StudentDataModel;
import com.shariarunix.examiner.Fragment.HomeFragment;
import com.shariarunix.examiner.Fragment.ProfileFragment;
import com.shariarunix.examiner.Fragment.ResourceFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navBottom;
    private DatabaseReference mReference;
    private StudentDataModel studentDataModel;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReference = FirebaseDatabase.getInstance().getReference();

        loadingDialog = new Dialog(MainActivity.this);

        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.progressbar_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Getting Data from Shared Preferences
        sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String uId =sharedPreferences.getString("userID","");

        // Load data & Default fragment
        loadData(uId);

        // Add Operation on Bottom Navigation Bar
        navBottom = findViewById(R.id.nav_bar);
        navBottom.getMenu().findItem(R.id.home).setChecked(true);
        navBottom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.profile){
                    // Loading Profile Fragment
                    loadFrag(ProfileFragment.getInstance(studentDataModel.getName(),
                            studentDataModel.getEmail(),
                            studentDataModel.getPhone(),
                            studentDataModel.getCourse(),
                            studentDataModel.getPassword()), 1);
                }
                if (item.getItemId() == R.id.home) {
                    // Load Home Fragment
                    loadFrag(HomeFragment.getInstance(studentDataModel.getName(),
                            studentDataModel.getCourse(),
                            studentDataModel.getPrevExamResult(),
                            studentDataModel.getPrevExamTotalMarks()), 1);
                }
                if (item.getItemId() == R.id.resource) {
                    loadFrag(ResourceFragment.getInstance(studentDataModel.getCourse()), 1);
                }
                return true;
            }
        });
    }
    //Swapping Fragment
    public void loadFrag(Fragment fragment, int flag){

        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();

        if (flag == 0){
            fragTrans.add(R.id.fragment_frame, fragment);
        }else{
            fragTrans.replace(R.id.fragment_frame, fragment);
        }

        fragTrans.commit();
    }

    // Customizing BackPressed For Fragment
    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.fragment_frame);

        if (currentFragment instanceof HomeFragment) {
            super.onBackPressed();
        }else {
            navBottom.getMenu().findItem(R.id.home).setChecked(true);
            // Load Home Fragment
            loadFrag(HomeFragment.getInstance(studentDataModel.getName(),
                    studentDataModel.getCourse(),
                    studentDataModel.getPrevExamResult(),
                    studentDataModel.getPrevExamTotalMarks()), 1);
        }
    }

    // Load user Data
    private void loadData(String key){

        mReference.child("student").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                studentDataModel = snapshot.getValue(StudentDataModel.class);
                assert studentDataModel != null;

                // Setting data to shared pref
                editor.putString("userEmail", studentDataModel.getEmail());
                editor.apply();

                // Default Fragment
                loadFrag(HomeFragment.getInstance(studentDataModel.getName(),
                        studentDataModel.getCourse(),
                        studentDataModel.getPrevExamResult(),
                        studentDataModel.getPrevExamTotalMarks()), 0);

                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(MainActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }
}


