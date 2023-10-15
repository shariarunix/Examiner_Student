package com.shariarunix.examiner.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariarunix.examiner.Adapter.CustomAdapter;
import com.shariarunix.examiner.DataModel.ResourceDataModel;
import com.shariarunix.examiner.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceFragment extends Fragment {


    List<ResourceDataModel> resourceDataModelList = new ArrayList<>();
    DatabaseReference mReference;
    public  ResourceFragment(){
        // Default Empty Constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource, container, false);

        mReference = FirebaseDatabase.getInstance().getReference();

        ListView resList = view.findViewById(R.id.res_list);
        loadRes(resList);
        
        return view;
    }

    private void loadRes(ListView listView){

        mReference.child("resource").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resourceDataModelList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ResourceDataModel rDm = dataSnapshot.getValue(ResourceDataModel.class);

                    resourceDataModelList.add(rDm);
                }

                Collections.reverse(resourceDataModelList);

                CustomAdapter adapterResource = new CustomAdapter(getActivity(),
                        R.layout.resource_list_item,
                        resourceDataModelList.size());

                adapterResource.setResourceDataModelList(resourceDataModelList);

                listView.setAdapter(adapterResource);
//                listView.setStackFromBottom(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}