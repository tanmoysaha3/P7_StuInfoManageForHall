package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p7_stuinfomanageforhall.models.SeatModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.preference.PowerPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Seats extends Base {

    LayoutInflater inflater;

    Spinner floorNoSeatS, roomNoSeatS;
    RecyclerView seatRecV;
    FirestorePagingAdapter<SeatModel,SeatViewHolder> seatAdapter;
    String seatNo;

    String floorNoIntent, roomNoIntent, adminAssignedHallId, adminAssignedHallType;

    String floorNoNewSeat, roomNoNewSeat;
    Long currentSeat;

    String floorNoSeatRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_seats,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_hall);

        adminAssignedHallId = PowerPreference.getDefaultFile().getString("AdminAssignedHallId");
        adminAssignedHallType=PowerPreference.getDefaultFile().getString("AdminAssignedHallType");

        Query lastDocQuery=fStore.collection("Halls").document(adminAssignedHallId).collection("Floors")
                .document("1").collection("Rooms").limit(1);
        lastDocQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    QuerySnapshot querySnapshot=task.getResult();
                    if (querySnapshot.size()==0){
                        Toast.makeText(Seats.this, "No room to create seat", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent data=getIntent();
                        String tempFloorNo=data.getStringExtra("FloorNo");
                        floorNoIntent= Optional.ofNullable(tempFloorNo).orElse("1");
                        String tempRoomNo=data.getStringExtra("RoomNo");
                        roomNoIntent=Optional.ofNullable(tempRoomNo).orElse("1");

                        floorNoSeatS=findViewById(R.id.floorNoSeatS);
                        roomNoSeatS=findViewById(R.id.roomNoSeatS);
                        seatRecV=findViewById(R.id.seatsRecV);

                        createNewB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(Seats.this, "Clicked", Toast.LENGTH_SHORT).show();
                                createSeatDialog();
                            }
                        });

                        CollectionReference floorRef=fStore.collection("Halls").document(adminAssignedHallId)
                                .collection("Floors");
                        List<String> floors=new ArrayList<>();
                        ArrayAdapter<String> floorAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,floors);
                        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        floorNoSeatS.setAdapter(floorAdapter);
                        floorRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                        String floor=queryDocumentSnapshot.getString("FloorNo");
                                        floors.add(floor);
                                    }
                                    floorAdapter.notifyDataSetChanged();
                                    floorNoSeatS.setSelection(floorAdapter.getPosition(floorNoIntent));
                                }
                                else {
                                    Toast.makeText(Seats.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        floorNoSeatS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                floorNoSeatRV=parent.getItemAtPosition(position).toString();
                                CollectionReference roomRef=fStore.collection("Halls").document(adminAssignedHallId)
                                        .collection("Floors").document(floorNoSeatRV).collection("Rooms");
                                List<String> rooms=new ArrayList<>();
                                ArrayAdapter<String> roomAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,rooms);
                                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                roomNoSeatS.setAdapter(roomAdapter);
                                roomRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()){
                                            for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                                String room=queryDocumentSnapshot.getString("RoomNo");
                                                rooms.add(room);
                                            }
                                            roomAdapter.notifyDataSetChanged();
                                            roomNoSeatS.setSelection(roomAdapter.getPosition(roomNoIntent));
                                        }
                                        else {
                                            Toast.makeText(Seats.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                seatRecVRefresh(floorNoSeatRV,roomNoIntent);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        roomNoSeatS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedRoom=parent.getItemAtPosition(position).toString();
                                if ((!floorNoIntent.equals(floorNoSeatRV)) || (!roomNoIntent.equals(selectedRoom))){
                                    seatRecVRefresh(floorNoSeatRV,selectedRoom);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        seatRecVRefresh(floorNoIntent,roomNoIntent);
                    }
                }
                else {
                    Toast.makeText(Seats.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void seatRecVRefresh(String floorNoIntent, String roomNoIntent) {
        Query seatQuery=fStore.collection("Halls").document(adminAssignedHallId).collection("Floors")
                .document(floorNoIntent).collection("Rooms").document(roomNoIntent).collection("Seats");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<SeatModel> options=new FirestorePagingOptions.Builder<SeatModel>()
                .setLifecycleOwner(Seats.this)
                .setQuery(seatQuery, config, new SnapshotParser<SeatModel>() {
                    @NonNull
                    @Override
                    public SeatModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        SeatModel seatModel=snapshot.toObject(SeatModel.class);
                        seatNo=snapshot.getString("SeatNo");
                        seatModel.setSeatNo(seatNo);
                        return seatModel;
                    }
                })
                .build();

        seatAdapter=new FirestorePagingAdapter<SeatModel, SeatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SeatViewHolder holder, int position, @NonNull SeatModel model) {
                if (position%2!=0){
                    holder.seatCard.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.listGray));
                }
                holder.seatNoSeatList.setText(model.getSeatNo());
                holder.stuIdSeatList.setText(model.getAssignedStuId());
                holder.stuNameSeatList.setText(model.getAssignedStuName());
            }

            @NonNull
            @Override
            public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_seat,parent,false);
                return new SeatViewHolder(view);
            }
        };

        seatRecV.setHasFixedSize(true);
        seatRecV.setLayoutManager(new LinearLayoutManager(Seats.this));
        seatRecV.setAdapter(seatAdapter);
    }

    private void createSeatDialog() {
        Dialog dialog=new Dialog(Seats.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_create_seat);

        Spinner floorNoCreateSeatS=dialog.findViewById(R.id.floorNoCreateSeatS);
        Spinner roomNoCreateSeatS=dialog.findViewById(R.id.roomNoCreateSeatS);
        TextView currentTSeatsCreateSeat=dialog.findViewById(R.id.currentTSeatsCreateSeat);
        EditText createNewTSeatsCreateSeat=dialog.findViewById(R.id.createNewTSeatsCreateSeat);
        Button createSeatB=dialog.findViewById(R.id.createSeatB);

        CollectionReference floorRef=fStore.collection("Halls").document(adminAssignedHallId).collection("Floors");
        List<String> floors=new ArrayList<>();
        ArrayAdapter<String> floorAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,floors);
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorNoCreateSeatS.setAdapter(floorAdapter);
        floorRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        String floor=queryDocumentSnapshot.getString("FloorNo");
                        floors.add(floor);
                    }
                    floorAdapter.notifyDataSetChanged();
                    floorNoCreateSeatS.setSelection(floorNoSeatS.getSelectedItemPosition());
                }
                else {
                    Toast.makeText(Seats.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        floorNoCreateSeatS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                floorNoNewSeat=parent.getItemAtPosition(position).toString();
                CollectionReference roomRef=fStore.collection("Halls").document(adminAssignedHallId)
                        .collection("Floors").document(floorNoNewSeat).collection("Rooms");
                List<String> rooms=new ArrayList<>();
                ArrayAdapter<String> roomAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, rooms);
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roomNoCreateSeatS.setAdapter(roomAdapter);
                roomRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                String room=queryDocumentSnapshot.getString("RoomNo");
                                rooms.add(room);
                            }
                            roomAdapter.notifyDataSetChanged();
                            roomNoCreateSeatS.setSelection(roomNoSeatS.getSelectedItemPosition());
                        }
                        else {
                            Toast.makeText(Seats.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        roomNoCreateSeatS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                roomNoNewSeat=parent.getItemAtPosition(position).toString();
                DocumentReference seatRoomRef=fStore.collection("Halls").document(adminAssignedHallId)
                        .collection("Floors").document(floorNoNewSeat).collection("Rooms")
                        .document(roomNoNewSeat);
                seatRoomRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        currentSeat=value.getLong("TotalSeatInRoom");
                        currentTSeatsCreateSeat.setText(currentSeat.toString());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        createSeatB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String totalNewSeat=createNewTSeatsCreateSeat.getText().toString();
                if (totalNewSeat.isEmpty()){
                    createNewTSeatsCreateSeat.setError("Enter a valid value");
                    return;
                }
                if (Integer.parseInt(totalNewSeat)+currentSeat>5){
                    createNewTSeatsCreateSeat.setError("At max, 5 seats in 1 room");
                    return;
                }

                for (int i=1;i<=Integer.parseInt(totalNewSeat);i++){
                    String x=String.valueOf(i+currentSeat);
                    DocumentReference seatRef=fStore.collection("Halls").document(adminAssignedHallId)
                            .collection("Floors").document(floorNoNewSeat).collection("Rooms")
                            .document(roomNoNewSeat).collection("Seats").document(x);
                    String uniqueSeatId=adminAssignedHallId+floorNoNewSeat+roomNoNewSeat+x;
                    Map<String,Object> seatNew=new HashMap<>();
                    seatNew.put("SeatNo",x);
                    seatNew.put("UniqueSeatId",uniqueSeatId);
                    seatNew.put("IsAssigned","0");
                    seatNew.put("AssignedStuId","0");
                    seatNew.put("AssignedStuName","Empty");
                    seatRef.set(seatNew).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Seats.this, "Seat Created", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Seats.this, "Failed to create new seat", Toast.LENGTH_SHORT).show();
                        }
                    });

                    /*String uniqueSeatId=adminAssignedHallId+floorNoNewSeat+roomNoNewSeat+x;
                    DocumentReference uniqueSeatRef=fStore.collection("Created Seats").document(uniqueSeatId);
                    Map<String,Object> uniqueSeat=new HashMap<>();
                    uniqueSeat.put("HallId",adminAssignedHallId);
                    uniqueSeat.put("FloorNo",floorNoNewSeat);
                    uniqueSeat.put("RoomNo",roomNoNewSeat);
                    uniqueSeat.put("SeatNo",x);
                    uniqueSeat.put("UniqueSeatId",uniqueSeatId);
                    uniqueSeat.put("IsAssigned","0");
                    uniqueSeat.put("AssignedStuId","0");
                    uniqueSeat.put("SeatType",adminAssignedHallType);
                    uniqueSeatRef.set(uniqueSeat).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Seats.this, "Unique Seat Created", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Seats.this, "Failed to create unique seat", Toast.LENGTH_SHORT).show();
                        }
                    });*/
                }
                DocumentReference roomRef=fStore.collection("Halls").document(adminAssignedHallId)
                        .collection("Floors").document(floorNoNewSeat).collection("Rooms")
                        .document(roomNoNewSeat);
                roomRef.update("TotalSeatInRoom", FieldValue.increment(Integer.parseInt(totalNewSeat)));

                DocumentReference floorRef=fStore.collection("Halls").document(adminAssignedHallId)
                        .collection("Floors").document(floorNoNewSeat);
                floorRef.update("TotalSeatInFloor",FieldValue.increment(Integer.parseInt(totalNewSeat)));

                DocumentReference hallRef=fStore.collection("Halls").document(adminAssignedHallId);
                hallRef.update("TotalSeatInHall",FieldValue.increment(Integer.parseInt(totalNewSeat)));

                Intent intent=new Intent(getApplicationContext(),Seats.class);

                dialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        dialog.show();
    }

    private class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView seatNoSeatList, stuIdSeatList, stuNameSeatList;
        CardView seatCard;
        View view;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatNoSeatList=itemView.findViewById(R.id.seatNoSeatList);
            stuIdSeatList=itemView.findViewById(R.id.stuIdSeatList);
            stuNameSeatList=itemView.findViewById(R.id.stuNameSeatList);
            seatCard=itemView.findViewById(R.id.seatCard);
            view=itemView;
        }
    }
}