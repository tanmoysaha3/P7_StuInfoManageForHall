package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p7_stuinfomanageforhall.models.RoomModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ColorFilter extends Base {

    private static final String TAG = "ColorFilterActivity";

    LayoutInflater inflater;

    CardView colorFilterCard;
    Spinner distColorFilterS, deptColorFilterS, aYColorFilterS, hallIdColorFilterS, floorNoColorFilterS;
    Button colorFilterB;
    RecyclerView colorFilterRecV;
    FirestorePagingAdapter<RoomModel, colorViewHolder> colorAdapter;
    String roomNo;

    String selectedDist, selectedDept, selectedAY, selectedHall,
            selectedHallId, selectedFloor;
    String distQueryName, distQueryValue, deptQueryName, deptQueryValue,
            aYQueryName, aYQueryValue, hallQueryName, hallQueryValue,
            floorQueryName, floorQueryValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_color_filter,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_super);
        createNewB.setVisibility(View.INVISIBLE);

        colorFilterCard=findViewById(R.id.colorFilterCardA);
        distColorFilterS=findViewById(R.id.distColorFilterS);
        deptColorFilterS=findViewById(R.id.deptColorFilterS);
        aYColorFilterS=findViewById(R.id.aYColorFilterS);
        hallIdColorFilterS=findViewById(R.id.hallIdColorFilterS);
        floorNoColorFilterS=findViewById(R.id.floorNoColorFilterS);
        colorFilterB=findViewById(R.id.colorFilterB);
        colorFilterRecV=findViewById(R.id.colorFilterRecV);

        populateDistS();
        distColorFilterS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDist=parent.getItemAtPosition(position).toString();
                if (!selectedDist.equals("All")){
                    distQueryName="District";
                    distQueryValue=selectedDist;
                }
                else {
                    distQueryName="NullValue";
                    distQueryValue="Null";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                distQueryName="NullValue";
                distQueryValue="Null";
            }
        });

        populateDeptS();
        deptColorFilterS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDept=parent.getItemAtPosition(position).toString();
                if (!selectedDept.equals("All")){
                    deptQueryName="Department";
                    deptQueryValue=selectedDept;
                }
                else {
                    deptQueryName="NullValue";
                    deptQueryValue="Null";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        populateAYS();
        aYColorFilterS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAY=parent.getItemAtPosition(position).toString();
                if (!selectedAY.equals("All")){
                    aYQueryName="AcademicYear";
                    aYQueryValue=selectedAY;
                }
                else {
                    aYQueryName="NullValue";
                    aYQueryValue="Null";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CollectionReference hallsRef=fStore.collection("Halls");
        List<String> halls=new ArrayList<>();
        ArrayAdapter<String> hallAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,halls);
        hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hallIdColorFilterS.setAdapter(hallAdapter);

        hallsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().size()>0){
                        for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            String hall=queryDocumentSnapshot.getString("HallName");
                            halls.add(hall);
                        }
                        hallAdapter.notifyDataSetChanged();
                    }
                    else {
                        Log.d(TAG,"No hall was found");
                    }
                }
                else {
                    Log.d(TAG,"Getting halls name was unsuccessful");
                }
            }
        });

        hallIdColorFilterS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHall=parent.getItemAtPosition(position).toString();
                selectedHallId=selectedHall.substring(Math.max(0,selectedHall.length()-2),Math.max(0,selectedHall.length()-1));
                hallQueryName="HallId";
                hallQueryValue=selectedHallId;
                Toast.makeText(ColorFilter.this, "selected hall id"+selectedHallId, Toast.LENGTH_SHORT).show();

                CollectionReference floorRef=fStore.collection("Halls").document(selectedHallId).collection("Floors");
                List<String> floors=new ArrayList<>();
                ArrayAdapter<String> floorAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,floors);
                floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                floorNoColorFilterS.setAdapter(floorAdapter);
                floorRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().size()>0){
                                for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                    String floor=queryDocumentSnapshot.getString("FloorNo");
                                    floors.add(floor);
                                }
                                floorAdapter.notifyDataSetChanged();
                            }
                            else {
                                Log.d(TAG,"No floor was found");
                            }
                        }
                        else {
                            Log.d(TAG,"Getting floors no was unsuccessful"+task.getException());
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        floorNoColorFilterS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFloor=parent.getItemAtPosition(position).toString();
                floorQueryName="FloorNo";
                floorQueryValue=selectedFloor;
                Toast.makeText(ColorFilter.this, "selected floor no"+selectedFloor, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        colorFilterB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ColorFilter.this, "clicked "+selectedHallId+" "+selectedFloor, Toast.LENGTH_SHORT).show();
                Toast.makeText(ColorFilter.this, deptQueryValue+" "+distQueryValue+" "+aYQueryValue, Toast.LENGTH_SHORT).show();
                //colorFilterCard.setVisibility(View.GONE);
                Query colorQuery=fStore.collection("Halls").document(selectedHallId)
                        .collection("Floors").document(selectedFloor).collection("Rooms");

                PagedList.Config config=new PagedList.Config.Builder()
                        .setInitialLoadSizeHint(10)
                        .setPageSize(3)
                        .build();

                FirestorePagingOptions<RoomModel> options=new FirestorePagingOptions.Builder<RoomModel>()
                        .setLifecycleOwner(ColorFilter.this)
                        .setQuery(colorQuery, config, new SnapshotParser<RoomModel>() {
                            @NonNull
                            @Override
                            public RoomModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                                RoomModel roomModel=snapshot.toObject(RoomModel.class);
                                roomNo=snapshot.getString("UpdatedRoomNo");
                                roomModel.setUpdatedRoomNo(roomNo);
                                return roomModel;
                            }
                        })
                        .build();

                colorAdapter=new FirestorePagingAdapter<RoomModel, colorViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull colorViewHolder holder, int position, @NonNull RoomModel model) {
                        holder.roomNoColorFilterList.setText(model.getUpdatedRoomNo());

                        Query stuQuery=fStore.collection("Verified Students").whereEqualTo(hallQueryName,hallQueryValue)
                                .whereEqualTo(floorQueryName,floorQueryValue).whereEqualTo("RoomNo",model.getUpdatedRoomNo().substring(1))
                                .whereEqualTo(deptQueryName,deptQueryValue).whereEqualTo(distQueryName,distQueryValue)
                                .whereEqualTo(aYQueryName,aYQueryValue).limit(1);
                        stuQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    if (task.getResult().size()>0){
                                        holder.colorFilterCard.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.blue));
                                    }
                                }
                                else {
                                    Log.d(TAG,"StuQuery failed");
                                }
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public colorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_color_filter,parent,false);
                        return new colorViewHolder(view);
                    }
                };

                colorFilterRecV.setHasFixedSize(true);
                colorFilterRecV.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                colorFilterRecV.setAdapter(colorAdapter);
            }
        });
    }

    private void populateDistS() {
        ArrayAdapter<String> distAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.districts));
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distColorFilterS.setAdapter(distAdapter);
    }

    private void populateDeptS(){
        ArrayAdapter<String> deptAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.departments));
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptColorFilterS.setAdapter(deptAdapter);
    }

    private void populateAYS(){
        ArrayAdapter<String> aYAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.academicYear));
        aYAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        aYColorFilterS.setAdapter(aYAdapter);
    }

    private class colorViewHolder extends RecyclerView.ViewHolder {
        TextView roomNoColorFilterList;
        CardView colorFilterCard;
        View view;

        public colorViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNoColorFilterList=itemView.findViewById(R.id.roomNoColorFilterList);
            colorFilterCard=itemView.findViewById(R.id.colorFilterCard);
            view=itemView;
        }
    }
}