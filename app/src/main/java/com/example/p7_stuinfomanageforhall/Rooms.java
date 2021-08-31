package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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

import com.example.p7_stuinfomanageforhall.models.RoomModel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Rooms extends Base {

    LayoutInflater inflater;

    RecyclerView roomsRecV;
    Spinner hallNameRoomsS, floorNoRoomsS;
    FirestorePagingAdapter<RoomModel,RoomViewHolder> roomAdapter;
    String roomNo;

    String hallIdIntent, hallNameIntent, floorNoIntent, selectedHall,
            hallIdNewRoom, floorNoNewRoom;

    Long totalRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_rooms,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_super);

        Query lastDocQuery=fStore.collection("Halls").document("1").collection("Floors").limit(1);
        lastDocQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot querySnapshot=task.getResult();
                if (querySnapshot.size()==0) {
                    Toast.makeText(Rooms.this, "Create Floor First", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),Floors.class));
                }
                else {
                    Intent data=getIntent();
                    String tempHallId=data.getStringExtra("HallId");
                    hallIdIntent= Optional.ofNullable(tempHallId).orElse("1");
                    hallNameIntent=data.getStringExtra("HallName");
                    String tempFloorNo=data.getStringExtra("FloorNo");
                    floorNoIntent=Optional.ofNullable(tempFloorNo).orElse("1");

                    roomsRecV=findViewById(R.id.roomsRecV);
                    hallNameRoomsS=findViewById(R.id.hallNameRoomsS);
                    floorNoRoomsS=findViewById(R.id.floorNoRoomsS);

                    createNewB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createRoomDialog();
                        }
                    });

                    CollectionReference hallsRef=fStore.collection("Halls");
                    List<String> halls=new ArrayList<>();
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,halls);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    hallNameRoomsS.setAdapter(adapter);

                    hallsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                    String hall=documentSnapshot.getString("HallName");
                                    halls.add(hall);
                                }
                                adapter.notifyDataSetChanged();
                                if (hallNameIntent!=null){
                                    hallNameRoomsS.setSelection(adapter.getPosition(hallNameIntent));
                                }
                                else {
                                    hallNameIntent=hallNameRoomsS.getItemAtPosition(0).toString();
                                }
                            }
                            else {
                                Toast.makeText(Rooms.this, "Failed with"+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    hallNameRoomsS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedHall=parent.getItemAtPosition(position).toString();
                            hallIdNewRoom=selectedHall.substring(Math.max(0,selectedHall.length()-2),Math.max(0,selectedHall.length()-1));
                            CollectionReference floorRef=fStore.collection("Halls").document(hallIdNewRoom).collection("Floors");
                            List<String> floors=new ArrayList<>();
                            ArrayAdapter<String> floorAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,floors);
                            floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            floorNoRoomsS.setAdapter(floorAdapter);
                            floorRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                            String floor=documentSnapshot.getString("FloorNo");
                                            floors.add(floor);
                                        }
                                        floorAdapter.notifyDataSetChanged();
                                        if (floorNoIntent!=null){
                                            floorNoRoomsS.setSelection(floorAdapter.getPosition(floorNoIntent));
                                            Toast.makeText(Rooms.this, "FloorNoIntent"+floorNoIntent, Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            floorNoIntent=floorNoRoomsS.getItemAtPosition(0).toString();
                                        }
                                    }
                                    else {
                                        Toast.makeText(Rooms.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            roomRecVRefresh(hallIdNewRoom,floorNoIntent);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    floorNoRoomsS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedFloor=parent.getItemAtPosition(position).toString();
                            Toast.makeText(Rooms.this, "selectedFloor1"+selectedFloor, Toast.LENGTH_SHORT).show();
                            if ((!hallIdIntent.equals(hallIdNewRoom)) || (!floorNoIntent.equals(selectedFloor))){
                                Toast.makeText(Rooms.this, "selected floor"+selectedFloor, Toast.LENGTH_SHORT).show();
                                roomRecVRefresh(hallIdNewRoom,selectedFloor);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    roomRecVRefresh(hallIdIntent,floorNoIntent);
                }
            }
        });
    }

    private void createRoomDialog() {
        Dialog dialog=new Dialog(Rooms.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_create_room);

        Spinner hallNameCreateRoomS=dialog.findViewById(R.id.hallNameCreateRoomS);
        Spinner floorNoCreateRoomS=dialog.findViewById(R.id.floorNoCreateRoomS);
        TextView currentTRoomsCreateRoom=dialog.findViewById(R.id.currentTRoomsCreateRoom);
        EditText createNewTRoomsCreateRoom=dialog.findViewById(R.id.createNewTRoomsCreateRoom);
        Button createRoomB=dialog.findViewById(R.id.createRoomB);

        CollectionReference hallsRef=fStore.collection("Halls");
        List<String> halls=new ArrayList<>();
        ArrayAdapter<String> hallAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,halls);
        hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hallNameCreateRoomS.setAdapter(hallAdapter);

        hallsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        String hall=documentSnapshot.getString("HallName");
                        halls.add(hall);
                    }
                    hallAdapter.notifyDataSetChanged();
                    hallNameCreateRoomS.setSelection(hallNameRoomsS.getSelectedItemPosition());
                }
                else {
                    Toast.makeText(Rooms.this, "Failed with"+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        hallNameCreateRoomS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedHall=parent.getItemAtPosition(position).toString();
                hallIdNewRoom=selectedHall.substring(Math.max(0, selectedHall.length() - 2),Math.max(0, selectedHall.length() - 1));
                CollectionReference floorRef=fStore.collection("Halls").document(hallIdNewRoom).collection("Floors");
                List<String> floors=new ArrayList<>();
                ArrayAdapter<String> floorAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,floors);
                floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                floorNoCreateRoomS.setAdapter(floorAdapter);
                floorRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String floor = document.getString("FloorNo");
                                floors.add(floor);
                            }
                            floorAdapter.notifyDataSetChanged();
                            floorNoCreateRoomS.setSelection(floorNoRoomsS.getSelectedItemPosition());
                        }
                        else {
                            Toast.makeText(Rooms.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        floorNoCreateRoomS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                floorNoNewRoom=parent.getItemAtPosition(position).toString();
                DocumentReference documentReference=fStore.collection("Halls").document(hallIdNewRoom).collection("Floors").document(floorNoNewRoom);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        totalRoom=value.getLong("TotalRoomInFloor");
                        currentTRoomsCreateRoom.setText(totalRoom.toString());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        createRoomB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomsNew=createNewTRoomsCreateRoom.getText().toString();
                if (roomsNew.isEmpty()){
                    createNewTRoomsCreateRoom.setError("Enter a valid value");
                    return;
                }

                //Integer totalRoomInt=Integer.parseInt(totalRoom);
                for (int i=1; i<=Integer.parseInt(roomsNew); i++){
                    String x=String.valueOf(i+totalRoom);
                    String y;
                    if (Integer.parseInt(x)<10){
                        y="0"+x;
                    }
                    else {
                        y=x;
                    }

                    String z=floorNoNewRoom+y;
                    DocumentReference documentReference=fStore.collection("Halls").document(hallIdNewRoom).collection("Floors")
                            .document(floorNoNewRoom).collection("Rooms").document(y);
                    Map<String,Object> roomNew=new HashMap<>();
                    roomNew.put("RoomNo", y);
                    roomNew.put("UpdatedRoomNo", z);
                    roomNew.put("TotalSeatInRoom", 0);
                    roomNew.put("TotalStuInRoom",0);
                    documentReference.set(roomNew).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Rooms.this, "New Rooms Created", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                DocumentReference floorRef=fStore.collection("Halls").document(hallIdNewRoom)
                        .collection("Floors").document(floorNoNewRoom);
                floorRef.update("TotalRoomInFloor",FieldValue.increment(Integer.parseInt(roomsNew)));

                DocumentReference hallRef=fStore.collection("Halls").document(hallIdNewRoom);
                hallRef.update("TotalRoomInHall", FieldValue.increment(Integer.parseInt(roomsNew)));

                Intent intent=new Intent(getApplicationContext(),Rooms.class);
                intent.putExtra("HallName",hallNameCreateRoomS.getSelectedItem().toString());
                intent.putExtra("HallId",hallIdNewRoom);
                intent.putExtra("FloorNo",floorNoCreateRoomS.getSelectedItem().toString());

                dialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        dialog.show();
    }

    private void roomRecVRefresh(String hallIdNewRoom, String selectedFloor) {
        Query roomQuery=fStore.collection("Halls").document(hallIdNewRoom)
                .collection("Floors").document(selectedFloor).collection("Rooms");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<RoomModel> options=new FirestorePagingOptions.Builder<RoomModel>()
                .setLifecycleOwner(this)
                .setQuery(roomQuery, config, new SnapshotParser<RoomModel>() {
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

        roomAdapter=new FirestorePagingAdapter<RoomModel, RoomViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RoomViewHolder holder, int position, @NonNull RoomModel model) {
                holder.roomNoRoomList.setText(model.getUpdatedRoomNo());
                holder.totalSeatsRoomList.setText(model.getTotalSeatInRoom().toString());
                holder.totalStuRoomList.setText(model.getTotalStuInRoom().toString());
            }

            @NonNull
            @Override
            public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_room,parent,false);
                return new RoomViewHolder(view);
            }
        };

        roomsRecV.setHasFixedSize(true);
        roomsRecV.setLayoutManager(new LinearLayoutManager(this));
        roomsRecV.setAdapter(roomAdapter);
    }

    private class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomNoRoomList, totalSeatsRoomList, totalStuRoomList;
        CardView roomCard;
        View view;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNoRoomList=itemView.findViewById(R.id.roomNoRoomList);
            totalSeatsRoomList=itemView.findViewById(R.id.totalSeatsRoomList);
            totalStuRoomList=itemView.findViewById(R.id.totalStuRoomList);
            roomCard=itemView.findViewById(R.id.roomCard);
            view=itemView;
        }
    }
}