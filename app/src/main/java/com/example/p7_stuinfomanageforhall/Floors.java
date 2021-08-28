package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.p7_stuinfomanageforhall.models.FloorModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Floors extends Base {

    LayoutInflater inflater;

    RecyclerView floorsRecV;
    Spinner hallNameFloorsS;
    FirestorePagingAdapter<FloorModel,FloorViewHolder> floorAdapter;
    String floorNo;

    String hallId, hallName, selectedHall, hallIdNewFloor;

    String totalFloor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_floors,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_super);

        Query lastDocQuery=fStore.collection("Halls").limit(1);
        lastDocQuery.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot querySnapshot=task.getResult();
                if (querySnapshot.size()==0) {
                    Toast.makeText(Floors.this, "Create Hall First", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),Halls.class));
                }
                else {
                    Intent data=getIntent();
                    String tempHallId=data.getStringExtra("HallId");
                    hallId= Optional.ofNullable(tempHallId).orElse("1");
                    hallName=data.getStringExtra("HallName");

                    floorsRecV=findViewById(R.id.floorsRecV);
                    hallNameFloorsS=findViewById(R.id.hallNameFloorsS);

                    createNewB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createFloorDialog();
                        }
                    });

                    CollectionReference hallsRef=fStore.collection("Halls");
                    List<String> halls=new ArrayList<>();
                    ArrayAdapter<String> hallAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,halls);
                    hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    hallNameFloorsS.setAdapter(hallAdapter);

                    hallsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                    String hall=documentSnapshot.getString("HallName");
                                    halls.add(hall);
                                }
                                hallAdapter.notifyDataSetChanged();
                                if (hallName!=null){
                                    hallNameFloorsS.setSelection(hallAdapter.getPosition(hallName));
                                }
                                else {
                                    hallName=hallNameFloorsS.getItemAtPosition(0).toString();
                                }
                            }
                            else {
                                Toast.makeText(Floors.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    hallNameFloorsS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedHall=parent.getItemAtPosition(position).toString();
                            hallIdNewFloor=selectedHall.substring(Math.max(0, selectedHall.length() - 2),Math.max(0, selectedHall.length() - 1));
                            if (!hallName.equals(selectedHall)){
                                hallId=hallIdNewFloor;
                                floorRecVRefresh(hallId);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    floorRecVRefresh(hallId);
                }
            }
        });
    }

    private void createFloorDialog() {
        Dialog dialog=new Dialog(Floors.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_create_floor);

        Spinner hallNameCreateFloorS=dialog.findViewById(R.id.hallNameCreateFloorS);
        TextView currentTFloorsCreateFloor=dialog.findViewById(R.id.currentTFloorsCreateFloor);
        EditText creatingNewTFloorsCreateFloor=dialog.findViewById(R.id.creatingNewTFloorsCreateFloor);
        Button createFloorB=dialog.findViewById(R.id.createFloorB);

        CollectionReference hallsRef=fStore.collection("Halls");
        List<String> halls=new ArrayList<>();
        ArrayAdapter<String> hallAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,halls);
        hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hallNameCreateFloorS.setAdapter(hallAdapter);
        //hallNameFloorsS.setSelection(halls.indexOf(hallName));

        hallsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        String hall=documentSnapshot.getString("HallName");
                        halls.add(hall);
                    }
                    hallAdapter.notifyDataSetChanged();
                }
            }
        });

        hallNameCreateFloorS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedHall=parent.getItemAtPosition(position).toString();
                hallIdNewFloor=selectedHall.substring(Math.max(0, selectedHall.length() - 2),Math.max(0, selectedHall.length() - 1));
                DocumentReference documentReference=fStore.collection("Halls").document(hallIdNewFloor);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        totalFloor=value.getString("TotalFloorInHall");
                        currentTFloorsCreateFloor.setText(totalFloor);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        createFloorB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFloors=creatingNewTFloorsCreateFloor.getText().toString();
                if (newFloors.isEmpty()){
                    creatingNewTFloorsCreateFloor.setError("Enter a valid value");
                    return;
                }

                Integer totalFloorInt=Integer.parseInt(totalFloor);
                for(int i=1; i<=Integer.parseInt(newFloors);i++) {
                    String x = String.valueOf(i+totalFloorInt);
                    DocumentReference documentReference = fStore.collection("Halls").document(hallIdNewFloor)
                            .collection("Floors").document(x);
                    Map<String,Object> user = new HashMap<>();
                    user.put("FloorNo", x);
                    user.put("TotalRoomInFloor","0");
                    user.put("TotalSeatInFloor","0");
                    user.put("TotalStuInFloor","0");

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Floors.this, "Floor Created", Toast.LENGTH_SHORT).show();
                            Log.d("TAG","onSuccess : user profile is created for " + x);
                        }
                    });
                }

                DocumentReference documentReference = fStore.collection("Halls").document(hallIdNewFloor);
                Map<String, Object> updatedValue=new HashMap<>();
                updatedValue.put("TotalFloorInHall",String.valueOf(totalFloorInt+Integer.parseInt(newFloors)));
                documentReference.update(updatedValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Floors.this, "Total floor updated", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
        dialog.show();
    }

    private void floorRecVRefresh(String hallId) {
        Query floorQuery=fStore.collection("Halls").document(hallId)
                .collection("Floors");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<FloorModel> options=new FirestorePagingOptions.Builder<FloorModel>()
                .setLifecycleOwner(this)
                .setQuery(floorQuery, config, new SnapshotParser<FloorModel>() {
                    @NonNull
                    @Override
                    public FloorModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        FloorModel floorModel=snapshot.toObject(FloorModel.class);
                        floorNo=snapshot.getString("FloorNo");
                        floorModel.setFloorNo(floorNo);
                        return floorModel;
                    }
                })
                .build();

        floorAdapter=new FirestorePagingAdapter<FloorModel, FloorViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FloorViewHolder holder, int position, @NonNull FloorModel model) {
                holder.floorNoFloorList.setText(model.getFloorNo());
                holder.totalRoomFloorList.setText(model.getTotalRoomInFloor());
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Intent intent=new Intent(getApplicationContext(),Rooms.class);
                        intent.putExtra("HallId",hallIdNewFloor);
                        intent.putExtra("HallName", selectedHall);
                        intent.putExtra("FloorNo",model.getFloorNo());
                        startActivity(intent);*/
                    }
                });
            }

            @NonNull
            @Override
            public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_floor,parent,false);
                return new FloorViewHolder(view);
            }
        };

        floorsRecV.setHasFixedSize(true);
        floorsRecV.setLayoutManager(new LinearLayoutManager(this));
        floorsRecV.setAdapter(floorAdapter);
    }

    private class FloorViewHolder extends RecyclerView.ViewHolder {
        TextView floorNoFloorList, totalRoomFloorList;
        CardView floorCard;
        View view;
        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            floorNoFloorList=itemView.findViewById(R.id.floorNoFloorList);
            totalRoomFloorList=itemView.findViewById(R.id.totalRoomsFloorList);
            floorCard=itemView.findViewById(R.id.floorCard);
            view=itemView;
        }
    }
}