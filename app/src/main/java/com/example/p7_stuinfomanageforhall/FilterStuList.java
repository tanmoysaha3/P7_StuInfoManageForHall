package com.example.p7_stuinfomanageforhall;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p7_stuinfomanageforhall.models.StuModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.preference.PowerPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterStuList extends Base {

    LayoutInflater inflater;

    CardView filterStuListCard;
    Spinner assignedOrNotFilterStuListS, distFilterStuListS, deptFilterStuListS, aYFilterStuListS;
    Button filterStuListB;
    ImageView filterStuListCollapseIV;
    RecyclerView filterStuListRecV;
    FirestorePagingAdapter<StuModel,StuViewHolder> stuAdapter;
    String adminAssignedHallId, adminAssignedHallType, studentId;

    String selectedAssignStatus, selectedDist, selectedDept, selectedAY;
    String statusQueryName, statusQueryValue, distQueryName, distQueryValue,
            deptQueryName, deptQueryValue, aYQueryName, aYQueryValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_filter_stu_list,null,false);
        drawerLayout.addView(contentView,0);

        filterStuListCard=findViewById(R.id.filterStuListCard);
        assignedOrNotFilterStuListS=findViewById(R.id.assignedOrNotFilterStuListS);
        distFilterStuListS=findViewById(R.id.distFilterStuListS);
        deptFilterStuListS=findViewById(R.id.deptFilterStuListS);
        aYFilterStuListS=findViewById(R.id.aYFilterStuListS);
        filterStuListB=findViewById(R.id.filterStuListB);
        filterStuListCollapseIV=findViewById(R.id.filterStuListCollapseIV);
        filterStuListRecV=findViewById(R.id.filterStuListRecV);

        adminAssignedHallId= PowerPreference.getDefaultFile().getString("AdminAssignedHallId");
        adminAssignedHallType=PowerPreference.getDefaultFile().getString("AdminAssignedHallType");

        populateAOrNS();
        assignedOrNotFilterStuListS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAssignStatus=parent.getItemAtPosition(position).toString();
                if (selectedAssignStatus.equals("Assigned")){
                    statusQueryName="IsAssigned";
                    statusQueryValue="1";
                }
                else if (selectedAssignStatus.equals("Waiting")){
                    statusQueryName="IsAssigned";
                    statusQueryValue="0";
                }
                else if (selectedAssignStatus.equals("All")){
                    statusQueryName="NullValue";
                    statusQueryValue="Null";
                }
                /*if (selectedAssignStatus.equals("Assigned")){
                    query=query.whereEqualTo("IsAssigned","1");
                }
                else if (selectedAssignStatus.equals("Waiting")){
                    query=query.whereEqualTo("IsAssigned","0");
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                statusQueryName="NullValue";
                statusQueryValue="Null";
            }
        });

        populateDistS();
        distFilterStuListS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDist=parent.getItemAtPosition(position).toString();
                if (!selectedDist.equals("All")){
                    distQueryName="District";
                    distQueryValue=selectedDist;
                }
                else{
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

        /*DocumentReference deptDoc=fStore.collection("Departments").document("All Depts");
        deptDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        Map<String,Object> deptWithCode=task.getResult().getData();
                        Map<String,String> deptWithCodeString=(Map) deptWithCode;
                        List<String> depts=new ArrayList<>(deptWithCodeString.values());
                        depts.add(0,"All");
                        ArrayAdapter<String> deptAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,depts);
                        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        deptFilterStuListS.setAdapter(deptAdapter);
                    }
                }
                else {
                    Toast.makeText(FilterStuList.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        populateDeptS();
        deptFilterStuListS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        aYFilterStuListS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        filterStuListB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterStuListCard.setVisibility(View.GONE);
                filterStuListCollapseIV.setImageResource(R.drawable.ic_baseline_expand_more_24);
                Query query=fStore.collection("Verified Students").whereEqualTo(statusQueryName,statusQueryValue)
                        .whereEqualTo(distQueryName,distQueryValue).whereEqualTo(deptQueryName,deptQueryValue)
                        .whereEqualTo(aYQueryName,aYQueryValue).whereEqualTo("Gender",adminAssignedHallType);
                recV(query);
            }
        });

        filterStuListCollapseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterStuListCard.getVisibility()==View.VISIBLE){
                    filterStuListCard.setVisibility(View.GONE);
                    filterStuListCollapseIV.setImageResource(R.drawable.ic_baseline_expand_more_24);
                }
                else if (filterStuListCard.getVisibility()==View.GONE){
                    filterStuListCard.setVisibility(View.VISIBLE);
                    filterStuListCollapseIV.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });
    }

    private void recV(Query query) {
        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<StuModel> options=new FirestorePagingOptions.Builder<StuModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<StuModel>() {
                    @NonNull
                    @Override
                    public StuModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        StuModel stuModel=snapshot.toObject(StuModel.class);
                        studentId=snapshot.getString("StudentId");
                        stuModel.setStudentId(studentId);
                        return stuModel;
                    }
                })
                .build();

        stuAdapter=new FirestorePagingAdapter<StuModel, StuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull StuViewHolder holder, int position, @NonNull StuModel model) {
                holder.stuIdStuList.setText(model.getStudentId());
                holder.stuNameStuList.setText(model.getName());
                holder.stuOptionStuList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(FilterStuList.this, "Options coming", Toast.LENGTH_SHORT).show();
                        PopupMenu popupMenu=new PopupMenu(v.getContext(),v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Update Seat").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent intent=new Intent(getApplicationContext(),SeatAssign.class);
                                intent.putExtra("StuId",model.getStudentId());
                                startActivity(intent);
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(FilterStuList.this, "See Student Profile", Toast.LENGTH_SHORT).show();
                        seeStuDetailsDialog(model.getStudentId());
                    }
                });
            }

            @NonNull
            @Override
            public StuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stu,parent,false);
                return new StuViewHolder(view);
            }
        };

        filterStuListRecV.setHasFixedSize(true);
        filterStuListRecV.setLayoutManager(new LinearLayoutManager(this));
        filterStuListRecV.setAdapter(stuAdapter);
    }

    private void seeStuDetailsDialog(String studentId) {
        Dialog dialog=new Dialog(FilterStuList.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_see_stu_details);

        TextView nameSeeStuDetails, stuIdSeeStuDetails, emailSeeStuDetails, contactSeeStuDetails,
                distSeeStuDetails, dOBSeeStuDetails, deptSeeStuDetails, aYSeeStuDetails,
                assignStatusSeeStuDetails, hallNameSeeStuDetails, floorNoSeeStuDetails, roomNoSeeStuDetails,
                seatNoSeeStuDetails;
        ImageView closeSeeStuDetailsIV;

        nameSeeStuDetails=dialog.findViewById(R.id.nameSeeStuDetails);
        stuIdSeeStuDetails=dialog.findViewById(R.id.stuIdSeeStuDetails);
        emailSeeStuDetails=dialog.findViewById(R.id.emailSeeStuDetails);
        contactSeeStuDetails=dialog.findViewById(R.id.contactSeeStuDetails);
        distSeeStuDetails=dialog.findViewById(R.id.distSeeStuDetails);
        dOBSeeStuDetails=dialog.findViewById(R.id.dOBSeeStuDetails);
        deptSeeStuDetails=dialog.findViewById(R.id.deptSeeStuDetails);
        aYSeeStuDetails=dialog.findViewById(R.id.aYSeeStuDetails);
        assignStatusSeeStuDetails=dialog.findViewById(R.id.assignStatusSeeStuDetails);
        hallNameSeeStuDetails=dialog.findViewById(R.id.hallNameSeeStuDetails);
        floorNoSeeStuDetails=dialog.findViewById(R.id.floorNoSeeStuDetails);
        roomNoSeeStuDetails=dialog.findViewById(R.id.roomNoSeeStuDetails);
        seatNoSeeStuDetails=dialog.findViewById(R.id.seatNoSeeStuDetails);
        closeSeeStuDetailsIV=dialog.findViewById(R.id.closeSeeStuDetailsIV);

        DocumentReference stuDoc=fStore.collection("Verified Students").document(studentId);
        stuDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                nameSeeStuDetails.setText(documentSnapshot.getString("Name"));
                stuIdSeeStuDetails.setText(documentSnapshot.getString("StudentId"));
                emailSeeStuDetails.setText(documentSnapshot.getString("Email"));
                contactSeeStuDetails.setText(documentSnapshot.getString("ContactNo"));
                distSeeStuDetails.setText(documentSnapshot.getString("District"));
                dOBSeeStuDetails.setText(documentSnapshot.getString("DateOfBirth"));
                deptSeeStuDetails.setText(documentSnapshot.getString("Department"));
                aYSeeStuDetails.setText(documentSnapshot.getString("AcademicYear"));
                if (documentSnapshot.getString("IsAssigned").equals("0")){
                    assignStatusSeeStuDetails.setText("Not Assigned");
                    hallNameSeeStuDetails.setText("Not Assigned");
                    floorNoSeeStuDetails.setText("Not Assigned");
                    roomNoSeeStuDetails.setText("Not Assigned");
                    seatNoSeeStuDetails.setText("Not Assigned");
                }
                else if (documentSnapshot.getString("IsAssigned").equals("1")){
                    assignStatusSeeStuDetails.setText("Assigned");
                    hallNameSeeStuDetails.setText(documentSnapshot.getString("HallName"));
                    floorNoSeeStuDetails.setText(documentSnapshot.getString("FloorNo"));
                    roomNoSeeStuDetails.setText(documentSnapshot.getString("RoomNo"));
                    seatNoSeeStuDetails.setText(documentSnapshot.getString("SeatNo"));
                }
            }
        });

        closeSeeStuDetailsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void populateAOrNS() {
        ArrayAdapter<String> aONAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.assignStatus));
        aONAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignedOrNotFilterStuListS.setAdapter(aONAdapter);
    }

    private void populateDistS() {
        ArrayAdapter<String> distAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.districts));
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distFilterStuListS.setAdapter(distAdapter);
    }

    private void populateDeptS(){
        ArrayAdapter<String> deptAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.departments));
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptFilterStuListS.setAdapter(deptAdapter);
    }

    private void populateAYS(){
        ArrayAdapter<String> aYAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.academicYear));
        aYAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        aYFilterStuListS.setAdapter(aYAdapter);
    }

    private class StuViewHolder extends RecyclerView.ViewHolder {
        TextView stuIdStuList, stuNameStuList;
        ImageView stuOptionStuList;
        CardView stuCard;
        View view;

        public StuViewHolder(@NonNull View itemView) {
            super(itemView);
            stuIdStuList=itemView.findViewById(R.id.stuIdStuList);
            stuNameStuList=itemView.findViewById(R.id.stuNameStuList);
            stuOptionStuList=itemView.findViewById(R.id.stuOptionStuList);
            stuCard=itemView.findViewById(R.id.stuCard);
            view=itemView;
        }
    }
}