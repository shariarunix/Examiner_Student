package com.shariarunix.examiner.Fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.Adapter.CustomAdapter;
import com.shariarunix.examiner.DataModel.ResourceDataModel;
import com.shariarunix.examiner.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ResourceFragment extends Fragment {

    public static final String USER_COURSE = "Arg1";
    String allCourse = "All", userCourse;
    List<ResourceDataModel> courseResourceDataModelList = new ArrayList<>();
    List<ResourceDataModel> allCourseResourceDataModelList = new ArrayList<>();
    List<ResourceDataModel> resourceDataModelList = new ArrayList<>();
    DatabaseReference mReference;
    ListView resList;
    ProgressBar resourceProgressBar;

    public ResourceFragment() {
        // Default Empty Constructor
    }

    public static ResourceFragment getInstance(String userCourse) {
        ResourceFragment resourceFragment = new ResourceFragment();
        Bundle bundle = new Bundle();

        bundle.putString(USER_COURSE, userCourse);

        resourceFragment.setArguments(bundle);
        return resourceFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource, container, false);

        if (getArguments() != null) {
            userCourse = getArguments().getString(USER_COURSE);
        }

        resourceProgressBar = view.findViewById(R.id.resource_progress_bar);
        resList = view.findViewById(R.id.res_list);
        loadRes(resList, userCourse);

        return view;
    }

    private void loadRes(ListView listView, String specificCourse) {
        mReference = FirebaseDatabase.getInstance().getReference();

        mReference.child("resource")
                .orderByChild("course")
                .equalTo(specificCourse)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isAdded()) {
                            courseResourceDataModelList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ResourceDataModel resourceDataModel = dataSnapshot.getValue(ResourceDataModel.class);

                                courseResourceDataModelList.add(resourceDataModel);
                            }
                            mergeList(listView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        mReference.child("resource")
                .orderByChild("course")
                .equalTo(allCourse)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isAdded()){
                            allCourseResourceDataModelList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ResourceDataModel resourceDataModel = dataSnapshot.getValue(ResourceDataModel.class);

                                allCourseResourceDataModelList.add(resourceDataModel);
                            }
                            mergeList(listView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mergeList(ListView listView) {
        resourceDataModelList.clear();
        resourceDataModelList.addAll(courseResourceDataModelList);
        resourceDataModelList.addAll(allCourseResourceDataModelList);

        // Comparing based on date and time
        resourceDataModelList.sort(new Comparator<ResourceDataModel>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public int compare(ResourceDataModel rDm1, ResourceDataModel rDm2) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                int[] timeOneInt = normalToInt(rDm1.getTime());
                int[] timeTwoInt = normalToInt(rDm2.getTime());

                LocalTime timeOne = LocalTime.parse(timeValidation(timeOneInt[0], timeOneInt[1]));
                LocalTime timeTwo = LocalTime.parse(timeValidation(timeTwoInt[0], timeTwoInt[1]));

                try {
                    Date dateOne = dateTimeFormat.parse(rDm1.getDate() + " " + timeOne);
                    Date dateTwo = dateTimeFormat.parse(rDm2.getDate() + " " + timeTwo);

                    assert dateOne != null;
                    return dateOne.compareTo(dateTwo);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        resList.setVisibility(View.VISIBLE);
        resourceProgressBar.setVisibility(View.GONE);

        CustomAdapter resListAdapter = new CustomAdapter(requireActivity(),
                R.layout.list_item_resource,
                resourceDataModelList.size());

        resListAdapter.setResourceDataModelList(resourceDataModelList);

        listView.setAdapter(resListAdapter);
    }

    private int[] normalToInt(String time) {
        String[] newTime = time.split(" ");
        String[] hrMin = newTime[0].split(":");

        int hr, min;

        if (newTime[1].equals("PM") || newTime[1].equals("Pm") || newTime[1].equals("pm")) {
            hr = Integer.parseInt(hrMin[0]);
            if (hr < 12) {
                hr += 12;
            }
            min = Integer.parseInt(hrMin[1]);

        } else {
            hr = Integer.parseInt(hrMin[0]);
            if (hr == 12) {
                hr = 0;
            }
            min = Integer.parseInt(hrMin[1]);
        }

        return new int[]{hr, min};
    }

    private String timeValidation(int hr, int min) {
        String newHr = hr < 10 ? "0" + hr : String.valueOf(hr);
        String newMin = min < 10 ? "0" + min : String.valueOf(min);
        return newHr + ":" + newMin;
    }
}
