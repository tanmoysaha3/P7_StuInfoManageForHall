package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.preference.PowerPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SeatAssign extends Base {

    LayoutInflater inflater;

    String adminAssignedHallId, stuIdSearch, adminAssignedHallType, adminAssignedHallName;

    TextView filterTextSeatAssign;
    Spinner floorNoSeatAssignS, roomNoSeatAssignS, seatNoSeatAssignS;
    EditText stuIdSeatAssign;
    Button seatAssignB;

    TextView warningSeatAssign, showNameSeatAssign, showIdSeatAssign, showAYSeatAssign,
            showDistSeatAssign, showPhoneSeatAssign;

    String emptyFloor, emptyRoom, emptySeat, selectedFloor, selectedRoom,
            selectedSeat, stuStatus, uniqueSeatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_seat_assign,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_hall);

        PowerPreference.init(this);

        adminAssignedHallName = PowerPreference.getDefaultFile().getString("AdminAssignedHallName");
        adminAssignedHallId = PowerPreference.getDefaultFile().getString("AdminAssignedHallId");
        adminAssignedHallType = PowerPreference.getDefaultFile().getString("AdminAssignedHallType");

        Intent data=getIntent();
        String stuIdIntent=data.getStringExtra("StuId");

        filterTextSeatAssign=findViewById(R.id.filterTextSeatAssign);
        floorNoSeatAssignS=findViewById(R.id.floorNoSeatAssignS);
        roomNoSeatAssignS=findViewById(R.id.roomNoSeatAssignS);
        seatNoSeatAssignS=findViewById(R.id.seatNoSeatAssignS);
        stuIdSeatAssign=findViewById(R.id.stuIdSeatAssign);
        seatAssignB=findViewById(R.id.seatAssignB);
        warningSeatAssign=findViewById(R.id.warningSeatAssign);
        showNameSeatAssign=findViewById(R.id.showNameSeatAssign);
        showIdSeatAssign=findViewById(R.id.showIdSeatAssign);
        showAYSeatAssign=findViewById(R.id.showAYSeatAssign);
        showDistSeatAssign=findViewById(R.id.showDistSeatAssign);
        showPhoneSeatAssign=findViewById(R.id.showPhoneSeatAssign);

        seatAssignB.setEnabled(false);

        filterTextSeatAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),FilterStuList.class));
                finish();
            }
        });

        Query checkQuery=fStore.collection("Created Seats").whereEqualTo("HallId",adminAssignedHallId)
                .whereEqualTo("IsAssigned","0").limit(1);
        checkQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().size()==1){
                        DocumentSnapshot emptyDoc=task.getResult().getDocuments().get(0);
                        emptyFloor=emptyDoc.getString("FloorNo");
                        emptyRoom=emptyDoc.getString("RoomNo");
                        emptySeat=emptyDoc.getString("SeatNo");

                        CollectionReference floorRef=fStore.collection("Halls").document(adminAssignedHallId)
                                .collection("Floors");
                        List<String> floors=new ArrayList<>();
                        ArrayAdapter<String> floorAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,floors);
                        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        floorNoSeatAssignS.setAdapter(floorAdapter);
                        floorRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                        String floor=queryDocumentSnapshot.getString("FloorNo");
                                        floors.add(floor);
                                    }
                                    floorAdapter.notifyDataSetChanged();
                                    floorNoSeatAssignS.setSelection(floorAdapter.getPosition(emptyFloor));
                                }
                                else {
                                    Toast.makeText(SeatAssign.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        floorNoSeatAssignS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedFloor=parent.getItemAtPosition(position).toString();
                                CollectionReference roomRef=fStore.collection("Halls").document(adminAssignedHallId)
                                        .collection("Floors").document(selectedFloor).collection("Rooms");
                                List<String> rooms=new ArrayList<>();
                                ArrayAdapter<String> roomAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,rooms);
                                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                roomNoSeatAssignS.setAdapter(roomAdapter);
                                roomRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()){
                                            for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                                String room=queryDocumentSnapshot.getString("RoomNo");
                                                rooms.add(room);
                                            }
                                            roomAdapter.notifyDataSetChanged();
                                            if (emptyFloor.equals(selectedFloor)){
                                                roomNoSeatAssignS.setSelection(roomAdapter.getPosition(emptyRoom));
                                            }
                                        }
                                        else {
                                            Toast.makeText(SeatAssign.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        roomNoSeatAssignS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedRoom=parent.getItemAtPosition(position).toString();
                                Query seatRef=fStore.collection("Halls").document(adminAssignedHallId)
                                        .collection("Floors").document(selectedFloor)
                                        .collection("Rooms").document(selectedRoom)
                                        .collection("Seats").whereEqualTo("IsAssigned","0");
                                List<String> seats=new ArrayList<>();
                                ArrayAdapter<String> seatAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,seats);
                                seatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                seatNoSeatAssignS.setAdapter(seatAdapter);
                                seatRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()){
                                            for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                                String seat=queryDocumentSnapshot.getString("SeatNo");
                                                seats.add(seat);
                                            }
                                            seatAdapter.notifyDataSetChanged();
                                            if (emptyFloor.equals(selectedFloor) && emptyRoom.equals(selectedRoom)){
                                                seatNoSeatAssignS.setSelection(seatAdapter.getPosition(emptySeat));
                                            }
                                        }
                                        else {
                                            Toast.makeText(SeatAssign.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        seatNoSeatAssignS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedSeat=parent.getItemAtPosition(position).toString();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        TextWatcher textWatcher=new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                showStuDetails();
                            }
                        };

                        stuIdSeatAssign.addTextChangedListener(textWatcher);
                        if (stuIdIntent!=null){
                            stuIdSeatAssign.setText(stuIdIntent);
                        }
                    }
                    else {
                        Toast.makeText(SeatAssign.this, "No empty Seat. Please create a seat first", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(SeatAssign.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        seatAssignB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stuStatus.equals("1")){
                    removeAlert();
                }
                else {
                    assignProcess();
                }
            }
        });
    }

    private void showStuDetails() {
        String stuId=stuIdSeatAssign.getText().toString();
        if (stuId.length()==6){
            stuIdSearch=stuId;
            DocumentReference stuDoc=fStore.collection("Verified Students").document(stuIdSearch);
            stuDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot stuSnap=task.getResult();
                        if (stuSnap!=null && stuSnap.exists()){
                            String stuGender=stuSnap.getString("Gender");
                            if (stuGender.equals(adminAssignedHallType)){
                                stuStatus=stuSnap.getString("IsAssigned");
                                seatAssignB.setEnabled(true);
                                if (stuStatus.equals("1")){
                                    warningSeatAssign.setText("Student already assigned to one seat");
                                    warningSeatAssign.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
                                }
                                else {
                                    warningSeatAssign.setText(null);
                                }
                                showNameSeatAssign.setText(stuSnap.getString("Name"));
                                showIdSeatAssign.setText(stuSnap.getString("StudentId"));
                                showAYSeatAssign.setText(stuSnap.getString("AcademicYear"));
                                showDistSeatAssign.setText(stuSnap.getString("District"));
                                showPhoneSeatAssign.setText(stuSnap.getString("ContactNo"));
                            }
                            else {
                                warningSeatAssign.setText(stuGender+" students can't be assigned in "+adminAssignedHallType+" hall");
                                warningSeatAssign.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
                                showStuNull();
                            }
                        }
                        else {
                            warningSeatAssign.setText("No such student in waiting list.");
                            warningSeatAssign.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.yellow));
                            showStuNull();
                        }
                    }
                    else {
                        Toast.makeText(SeatAssign.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            warningSeatAssign.setText("Enter valid Student Id.");
            warningSeatAssign.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
            showStuNull();
        }
    }

    private void assignProcess() {
        uniqueSeatId=adminAssignedHallId+selectedFloor+selectedRoom+selectedSeat;
        DocumentReference stuRef=fStore.collection("Verified Students").document(stuIdSearch);
        Map<String,Object> stu=new HashMap<>();
        stu.put("IsAssigned","1");
        stu.put("UniqueSeatId",uniqueSeatId);
        stu.put("HallName",adminAssignedHallName);
        stu.put("HallId",adminAssignedHallId);
        stu.put("FloorNo",selectedFloor);
        stu.put("RoomNo",selectedRoom);
        stu.put("SeatNo",selectedSeat);
        stuRef.update(stu).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SeatAssign.this, "Student info updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SeatAssign.this, "Error in updating student", Toast.LENGTH_SHORT).show();
            }
        });

        DocumentReference seatRef=fStore.collection("Halls").document(adminAssignedHallId)
                .collection("Floors").document(selectedFloor)
                .collection("Rooms").document(selectedRoom)
                .collection("Seats").document(selectedSeat);
        Map<String,Object> seat=new HashMap<>();
        seat.put("IsAssigned","1");
        seat.put("AssignedStuId",stuIdSearch);
        seatRef.update(seat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SeatAssign.this, "Seat info updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SeatAssign.this, "Error in updating seat", Toast.LENGTH_SHORT).show();
            }
        });

        /*DocumentReference uniqueSeatRef=fStore.collection("Created Seats").document(uniqueSeatId);
        Map<String,Object> uniqueSeat=new HashMap<>();
        uniqueSeat.put("IsAssigned","1");
        uniqueSeat.put("AssignedStuId",stuIdSearch);
        uniqueSeatRef.update(uniqueSeat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SeatAssign.this, "UniqueSeat info updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SeatAssign.this, "Error in updating uniqueSeat", Toast.LENGTH_SHORT).show();
            }
        });*/


    }

    private void removeAlert() {
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Already Assigned")
                .setMessage("Student is already assigned to one seat. Are you sure to assign him to new seat")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        assignProcess();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SeatAssign.this, "Assign process cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
        warning.show();
    }

    private void showStuNull() {
        showNameSeatAssign.setText(null);
        showIdSeatAssign.setText(null);
        showAYSeatAssign.setText(null);
        showDistSeatAssign.setText(null);
        showPhoneSeatAssign.setText(null);
    }
}