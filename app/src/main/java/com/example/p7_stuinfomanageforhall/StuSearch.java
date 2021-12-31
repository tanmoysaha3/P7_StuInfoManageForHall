package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p7_stuinfomanageforhall.models.StuModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.preference.PowerPreference;

public class StuSearch extends Base {

    LayoutInflater inflater;

    EditText mSearchStuId/*, mSearchStuName*/;
    Button mSearchStuDetailsButton;
    RecyclerView stuSearchRecV;

    String adminAssignedHallId, adminAssignedHallType, studentId;
    FirestorePagingAdapter<StuModel,StuViewHolder> stuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_stu_search,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_hall);
        createNewB.setVisibility(View.INVISIBLE);

        mSearchStuId=findViewById(R.id.searchStuId);
        //mSearchStuName=findViewById(R.id.searchStuName);
        mSearchStuDetailsButton=findViewById(R.id.searchStuDetailsButton);
        stuSearchRecV=findViewById(R.id.stuSearchRecV);

        adminAssignedHallId= PowerPreference.getDefaultFile().getString("AdminAssignedHallId");
        adminAssignedHallType=PowerPreference.getDefaultFile().getString("AdminAssignedHallType");

        mSearchStuDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stu=mSearchStuId.getText().toString();
                Query query;
                if(stu.matches("[0-9]+")) {
                    query=fStore.collection("Verified Students").whereEqualTo("StudentId",stu);
                } else {
                    query=fStore.collection("Verified Students").whereEqualTo("Name",stu);
                }
                recV(query);
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
                        Toast.makeText(StuSearch.this, "Options coming", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(StuSearch.this, "See Student Profile", Toast.LENGTH_SHORT).show();
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

        stuSearchRecV.setHasFixedSize(true);
        stuSearchRecV.setLayoutManager(new LinearLayoutManager(this));
        stuSearchRecV.setAdapter(stuAdapter);
    }

    private void seeStuDetailsDialog(String studentId) {
        Dialog dialog=new Dialog(StuSearch.this);
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