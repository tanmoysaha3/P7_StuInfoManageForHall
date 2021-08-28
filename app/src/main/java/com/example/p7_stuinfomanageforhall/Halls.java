package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p7_stuinfomanageforhall.models.HallModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class Halls extends Base {

    LayoutInflater inflater;

    RecyclerView hallsRecV;
    FirestorePagingAdapter<HallModel,HallViewHolder> hallAdapter;
    String hallName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_halls,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_super);

        hallsRecV=findViewById(R.id.hallsRecV);

        createNewB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHallDialog();
            }
        });

        Query hallQuery=fStore.collection("Halls");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<HallModel> options=new FirestorePagingOptions.Builder<HallModel>()
                .setLifecycleOwner(this)
                .setQuery(hallQuery, config, new SnapshotParser<HallModel>() {
                    @NonNull
                    @Override
                    public HallModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        HallModel hallModel=snapshot.toObject(HallModel.class);
                        hallName=snapshot.getString("HallName");
                        hallModel.setHallName(hallName);
                        return hallModel;
                    }
                })
                .build();

        hallAdapter=new FirestorePagingAdapter<HallModel, HallViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull HallViewHolder holder, int position, @NonNull HallModel model) {
                //String hallNameType=new StringBuilder(model.getHallName()).append(" (").append(model.getHallType()).append(")").toString();
                //changed as android studio suggestion
                String hallNameType= model.getHallName() + " (" + model.getHallType() + ")";
                holder.hallNameHallList.setText(hallNameType);
                holder.hallAdminHallList.setText(model.getHallAdmin());
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),Floors.class);
                        intent.putExtra("HallId",model.getHallId());
                        intent.putExtra("HallName",model.getHallName());
                        intent.putExtra("TotalFloorInHall",model.getTotalFloorInHall());
                        startActivity(intent);
                    }
                });
                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String name=model.getHallName();
                        String type=model.getHallType();
                        String admin=model.getHallAdmin();
                        String totalFloor=model.getTotalFloorInHall();
                        String totalRoom=model.getTotalRoomInHall();
                        String totalSeat=model.getTotalSeatInHall();
                        String totalStu=model.getTotalStuInHall();
                        Toast.makeText(Halls.this, "Long Click", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }

            @NonNull
            @Override
            public HallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_hall,parent,false);
                return new HallViewHolder(view);
            }
        };

        hallsRecV.setHasFixedSize(true);
        hallsRecV.setLayoutManager(new LinearLayoutManager(this));
        hallsRecV.setAdapter(hallAdapter);
    }

    private void createHallDialog() {
        Dialog dialog=new Dialog(Halls.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_create_hall);

        EditText hallNameCreateHall=dialog.findViewById(R.id.hallNameCreateHall);
        EditText hallIdCreateHall=dialog.findViewById(R.id.hallIdCreateHall);
        Spinner hallTypeCreateHallS=dialog.findViewById(R.id.hallTypeCreateHallS);
        Button createHallB=dialog.findViewById(R.id.createHallB);

        ArrayAdapter<String> hallTypeAdapter=new ArrayAdapter<>
                (this,R.layout.support_simple_spinner_dropdown_item,getResources().getStringArray(R.array.hallTypes));
        hallTypeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        hallTypeCreateHallS.setAdapter(hallTypeAdapter);

        createHallB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hallName=hallNameCreateHall.getText().toString();
                String hallId=hallIdCreateHall.getText().toString();

                if (hallName.isEmpty()){
                    hallNameCreateHall.setError("Can't be empty");
                    return;
                }
                if (hallId.length() != 1){
                    hallIdCreateHall.setError("Enter valid value");
                    return;
                }

                String hallNameId= hallName + " (" + hallId + ")";
                DocumentReference docRef=fStore.collection("Halls").document(hallId);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot hallSnap=task.getResult();
                            if (hallSnap!=null && hallSnap.exists()){
                                Toast.makeText(Halls.this, "Hall Exists", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Map<String,Object> createHall=new HashMap<>();
                                createHall.put("HallName",hallNameId);
                                createHall.put("HallId",hallId);
                                createHall.put("HallAdmin","Empty");
                                createHall.put("IsHallAdminAssigned","No");
                                createHall.put("TotalFloorInHall","0");
                                createHall.put("TotalRoomInHall","0");
                                createHall.put("TotalSeatInHall","0");
                                createHall.put("TotalStuInHall","0");
                                createHall.put("HallType",hallTypeCreateHallS.getSelectedItem().toString());

                                docRef.set(createHall).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Halls.this, "Hall created", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Halls.this, "Creating hall failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else {
                            Toast.makeText(Halls.this, "Failed with"+task.getException(), Toast.LENGTH_SHORT).show();
                        }
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

    private class HallViewHolder extends RecyclerView.ViewHolder {
        TextView hallNameHallList, hallAdminHallList;
        CardView hallCard;
        View view;

        public HallViewHolder(@NonNull View itemView) {
            super(itemView);
            hallNameHallList=itemView.findViewById(R.id.hallNameHallList);
            hallAdminHallList=itemView.findViewById(R.id.hallAdminHallList);
            hallCard=itemView.findViewById(R.id.hallCard);
            view=itemView;
        }
    }
}