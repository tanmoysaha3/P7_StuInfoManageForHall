package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Halls extends Base {

    LayoutInflater inflater;

    RecyclerView hallsRecV;
    FirestorePagingAdapter<HallModel,HallViewHolder> hallAdapter;
    String hallName;

    String assignedAdminId, assignedAdminName;

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
                Long emptySeat=model.getTotalSeatInHall()-model.getTotalStuInHall();
                holder.hallNameHallList.setText(hallNameType);
                holder.hallAdminHallList.setText(model.getHallAdmin());
                holder.seatsHallList.setText(model.getTotalSeatInHall().toString()+" ("+model.getTotalStuInHall().toString()+"+"+
                        emptySeat.toString()+")");
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),Floors.class);
                        intent.putExtra("HallId",model.getHallId());
                        intent.putExtra("HallName",model.getHallName());
                        startActivity(intent);
                    }
                });

                //ImageView hallOptionHallList=holder.view.findViewById(R.id.hallOptionHallList);
                holder.hallOptionHallList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu=new PopupMenu(v.getContext(),v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit Hall Name").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                String hallIdEditName=model.getHallId();
                                editHallNameDialog(hallIdEditName);
                                return true;
                            }
                        });
                        popupMenu.getMenu().add("Assign New Admin").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Toast.makeText(Halls.this, "Clicked", Toast.LENGTH_SHORT).show();
                                String hallNameAssign=model.getHallName();
                                String adminIdAssign=model.getHallAdminId();
                                String adminNameAssign=model.getHallAdmin();
                                String hallIdAssign=model.getHallId();
                                String hallTypeAssign=model.getHallType();
                                assignHallAdminDialog(hallIdAssign, hallNameAssign, adminIdAssign, adminNameAssign,hallTypeAssign);
                                return false;
                            }
                        });
                        /*if (model.getHallAdmin().equals("Empty")){
                            popupMenu.getMenu().add("Assign Admin").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    return true;
                                }
                            });
                        }
                        else {
                            popupMenu.getMenu().add("Update Admin").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    return true;
                                }
                            });
                        }*/
                        popupMenu.show();
                    }
                });
                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String name=model.getHallName();
                        String type=model.getHallType();
                        String admin=model.getHallAdmin();
                        Long totalFloor=model.getTotalFloorInHall();
                        Long totalRoom=model.getTotalRoomInHall();
                        Long totalSeat=model.getTotalSeatInHall();
                        Long totalStu=model.getTotalStuInHall();
                        List<String> subAdmins=model.getHallSubAdmins();
                        hallDetailsDialog(name,type,admin,totalFloor,totalRoom,totalSeat,totalStu,subAdmins);
                        Toast.makeText(Halls.this, "Long Click"+subAdmins.toString(), Toast.LENGTH_SHORT).show();
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

    private void hallDetailsDialog(String name, String type, String admin, Long totalFloor, Long totalRoom, Long totalSeat, Long totalStu, List<String> subAdmins) {
        Dialog dialog=new Dialog(Halls.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_hall_details);

        TextView hallNameHallDetails=dialog.findViewById(R.id.hallNameHallDetails);
        TextView hallAdminHallDetails=dialog.findViewById(R.id.hallAdminHallDetails);
        RecyclerView subAdminsRecVHallDetails=dialog.findViewById(R.id.subAdminsRecVHallDetails);
        TextView totalFloorsHallDetails=dialog.findViewById(R.id.totalFloorsHallDetails);
        TextView totalRoomsHallDetails=dialog.findViewById(R.id.totalRoomHallDetails);
        TextView totalSeatsHallDetails=dialog.findViewById(R.id.totalSeatsHallDetails);
        TextView totalStuHallDetails=dialog.findViewById(R.id.totalStuHallDetails);

        hallNameHallDetails.setText(name + " ("+ type+")");
        hallAdminHallDetails.setText(admin);
        totalFloorsHallDetails.setText(totalFloor.toString());
        totalRoomsHallDetails.setText(totalRoom.toString());
        totalSeatsHallDetails.setText(totalSeat.toString());
        totalStuHallDetails.setText(totalStu.toString());

        RecyclerView.Adapter adapter;

        adapter = new PlanetAdapter((ArrayList<String>) subAdmins,getApplicationContext());
        subAdminsRecVHallDetails.setHasFixedSize(true);
        subAdminsRecVHallDetails.setLayoutManager(new LinearLayoutManager(this));
        subAdminsRecVHallDetails.setAdapter(adapter);
        dialog.show();
    }

    class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder> {

        ArrayList<String> planetList;
        public PlanetAdapter(ArrayList<String> planetList, Context context) {
            this.planetList = planetList;
        }

        @Override
        public PlanetAdapter.PlanetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_admin,parent,false);
            PlanetViewHolder viewHolder=new PlanetViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(PlanetAdapter.PlanetViewHolder holder, int position) {
            holder.text.setText(planetList.get(position).toString());
        }

        @Override
        public int getItemCount() {
            return planetList.size();
        }

        class PlanetViewHolder extends RecyclerView.ViewHolder{
            protected TextView text;
            public PlanetViewHolder(View itemView) {
                super(itemView);
                text= (TextView) itemView.findViewById(R.id.subAdminsHallList);
            }
        }
    }

    private void editHallNameDialog(String hallIdEditName) {
        Dialog dialog=new Dialog(Halls.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_edit_hall_name);

        EditText hallNameEditHallName=dialog.findViewById(R.id.hallNameEditHallName);
        EditText reHallNameEditHallName=dialog.findViewById(R.id.reHallNameEditHallName);
        Button editHallNameB=dialog.findViewById(R.id.editHallNameB);

        editHallNameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=hallNameEditHallName.getText().toString();
                String reName=reHallNameEditHallName.getText().toString();

                if (name.equals(reName)){
                    String hallIdName=name + " (" + hallIdEditName + ")";
                    DocumentReference hallDoc=fStore.collection("Halls").document(hallIdEditName);
                    Map<String,Object> updateHall=new HashMap<>();
                    updateHall.put("HallName",hallIdName);
                    hallDoc.update(updateHall).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Halls.this, "Updated hall name", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Halls.this, "Error in updating hall name", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
                else {
                    reHallNameEditHallName.setError("Don't match");
                }
            }
        });
        dialog.show();
    }

    private void assignHallAdminDialog(String hallIdAssign, String hallNameAssign, String adminIdAssign, String adminNameAssign, String hallTypeAssign) {
        Dialog dialog=new Dialog(Halls.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_assign_hall_admin);

        TextView hallNameAssignHallAdmin=dialog.findViewById(R.id.hallNameAssignHallAdmin);
        TextView currAdminAssignHallAdmin=dialog.findViewById(R.id.currAdminAssignHallAdmin);
        Spinner selectNewAssignHallAdminS=dialog.findViewById(R.id.selectNewAssignHallAdminS);
        Button assignHallAdminB=dialog.findViewById(R.id.assignHallAdminB);

        hallNameAssignHallAdmin.setText(hallNameAssign);
        currAdminAssignHallAdmin.setText(adminNameAssign);

        Query adminsQuery=fStore.collection("Verified Admins").whereEqualTo("IsAdmin","0");
        List<String> admins=new ArrayList<>();
        List<String> adminsId=new ArrayList<>();
        ArrayAdapter<String> adminAdapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item,admins);
        adminAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectNewAssignHallAdminS.setAdapter(adminAdapter);
        adminsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String admin = document.getString("Name");
                        String adminId=document.getId();
                        admins.add(admin);
                        adminsId.add(adminId);
                    }
                    adminAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(Halls.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        assignHallAdminB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignedAdminId=adminsId.get(selectNewAssignHallAdminS.getSelectedItemPosition());
                assignedAdminName=admins.get(selectNewAssignHallAdminS.getSelectedItemPosition());
                if (!adminIdAssign.equals("Empty")){
                    DocumentReference oldAdminDoc=fStore.collection("Verified Admins").document(adminIdAssign);
                    Map<String,Object> updateOldAdmin=new HashMap<>();
                    updateOldAdmin.put("IsAdmin","0");
                    updateOldAdmin.put("AssignedHallId","0");
                    oldAdminDoc.update(updateOldAdmin).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Halls.this, "Successfully updated old admin doc", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Halls.this, "Error in updating old admin doc", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                DocumentReference assignedAdminRef=fStore.collection("Verified Admins").document(assignedAdminId);
                Map<String,Object> updateNewAdmin=new HashMap<>();
                updateNewAdmin.put("IsAdmin","2");
                updateNewAdmin.put("AssignedHallName",hallNameAssign);
                updateNewAdmin.put("AssignedHallType",hallTypeAssign);
                assignedAdminRef.update(updateNewAdmin).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Halls.this, "New Admin doc updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Halls.this, "Error in updating new admin doc", Toast.LENGTH_SHORT).show();
                    }
                });

                DocumentReference hallRef=fStore.collection("Halls").document(hallIdAssign);
                Map<String,Object> updateHall=new HashMap<>();
                updateHall.put("IsHallAdminAssigned","Yes");
                updateHall.put("HallAdmin",assignedAdminName);
                updateHall.put("HallAdminId",assignedAdminId);
                hallRef.update(updateHall).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Halls.this, "Hall info updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Halls.this, "Error in updating hall info", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
        dialog.show();
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
                                createHall.put("HallAdminId","Empty");
                                createHall.put("IsHallAdminAssigned","No");
                                createHall.put("TotalFloorInHall",0);
                                createHall.put("TotalRoomInHall",0);
                                createHall.put("TotalSeatInHall",0);
                                createHall.put("TotalStuInHall",0);
                                createHall.put("HallType",hallTypeCreateHallS.getSelectedItem().toString());
                                createHall.put("HallSubAdmins", Arrays.asList());

                                docRef.set(createHall).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Halls.this, "Hall created", Toast.LENGTH_SHORT).show();
                                        startActivity(getIntent());
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
        TextView hallNameHallList, hallAdminHallList, seatsHallList;
        ImageView hallOptionHallList;
        CardView hallCard;
        View view;

        public HallViewHolder(@NonNull View itemView) {
            super(itemView);
            hallNameHallList=itemView.findViewById(R.id.hallNameHallList);
            hallAdminHallList=itemView.findViewById(R.id.hallAdminHallList);
            seatsHallList=itemView.findViewById(R.id.seatsHallList);
            hallOptionHallList=itemView.findViewById(R.id.hallOptionHallList);
            hallCard=itemView.findViewById(R.id.hallCard);
            view=itemView;
        }
    }
}