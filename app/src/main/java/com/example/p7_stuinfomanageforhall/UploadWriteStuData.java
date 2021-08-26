package com.example.p7_stuinfomanageforhall;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.p7_stuinfomanageforhall.models.FileModel;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.util.Log.e;

public class UploadWriteStuData extends Base {

    LayoutInflater inflater;

    ImageButton selectFileIB;
    Button uploadFileB;
    TextView selectedFileName;
    FirebaseFirestore fStore;
    FirebaseStorage fStorage;
    ActivityResultLauncher<String> requestPermissionLauncher;
    ActivityResultLauncher<Intent> selectingFileLauncher;

    static String nopath = "Select .xls files only";
    Uri fileUri;
    ProgressDialog progressDialog;
    String fileName;

    RecyclerView stuFilesRecView;
    FirestorePagingAdapter<FileModel,FileViewHolder> fileAdapter;
    AsyncHttpClient client;
    String fileNameItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_upload_write_stu_data,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_super);

        selectFileIB=findViewById(R.id.selectFileIB);
        uploadFileB=findViewById(R.id.uploadFileB);
        selectedFileName=findViewById(R.id.selectedFileName);
        fStore=FirebaseFirestore.getInstance();
        fStorage=FirebaseStorage.getInstance();

        stuFilesRecView=findViewById(R.id.stuFilesRecView);

        selectFileIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requirePermission();
            }
        });

        requestPermissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
            if (isGranted){
                selectFile();
                Toast.makeText(this, "Permission already allowed", Toast.LENGTH_SHORT).show();
            }
            else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                AlertDialog.Builder reRequestPermission=new AlertDialog.Builder(this)
                        .setTitle("Storage Permission")
                        .setMessage("To select file you need to give permission to read storage")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requirePermission();
                            }
                        })
                        .setNegativeButton("Deny",null);
                reRequestPermission.show();
            }
            else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        });

        selectingFileLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data=result.getData();
                        fileUri=data.getData();
                        try{
                            Toast.makeText(UploadWriteStuData.this, "FileUri"+fileUri, Toast.LENGTH_SHORT).show();
                            String path = getPath(UploadWriteStuData.this, fileUri);
                            File file = new File(path);
                            fileName = file.getName();

                            selectedFileName.setText(fileName);
                            Log.e("TAG", "File Name: " + fileName);
                        }catch(Exception e){
                            e("Err", e.toString()+"");
                        }
                    }
                });

        uploadFileB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileUri!=null){
                    if (fileName.endsWith(".csv")){
                        uploadFile(fileUri);
                    }
                    else {
                        Toast.makeText(UploadWriteStuData.this, "Select a csv file", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(UploadWriteStuData.this, "Select a file first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Query fileQuery= FirebaseFirestore.getInstance().collection("Uploads");

        PagedList.Config config=new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<FileModel> options=new FirestorePagingOptions.Builder<FileModel>()
                .setLifecycleOwner(this)
                .setQuery(fileQuery, config, new SnapshotParser<FileModel>() {
                    @NonNull
                    @Override
                    public FileModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        FileModel fileModel=snapshot.toObject(FileModel.class);
                        fileNameItem=snapshot.getString("Name");
                        fileModel.setName(fileNameItem);
                        return fileModel;
                    }
                })
                .build();

        fileAdapter=new FirestorePagingAdapter<FileModel, FileViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FileViewHolder holder, int position, @NonNull FileModel model) {
                holder.fileName.setText(model.getName());
                holder.fileStatus.setText(model.getStatus());
                String url=model.getURL();
                if (model.getStatus().equals("Not Done")){
                    holder.fileWork.setText("Write");
                    holder.fileWork.setTextColor(getResources().getColor(R.color.blue,null));
                    holder.fileWork.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(UploadWriteStuData.this, "url"+url, Toast.LENGTH_SHORT).show();
                            holder.fileWork.setTextColor(getResources().getColor(R.color.black,null));
                            Toast.makeText(UploadWriteStuData.this, "Clicked", Toast.LENGTH_SHORT).show();

                            client=new AsyncHttpClient();
                            client.get(url, new FileAsyncHttpResponseHandler(UploadWriteStuData.this){
                                @Override
                                public void onFailure(Throwable e, File response) {
                                    super.onFailure(e, response);
                                    Toast.makeText(UploadWriteStuData.this, "Downloading Failed", Toast.LENGTH_SHORT).show();
                                }
                                String[] fields= {"0"};
                                Integer i=0;
                                @Override
                                public void onSuccess(File file) {
                                    super.onSuccess(file);
                                    try {
                                        BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
                                        String line="";

                                        while ((line=bufferedReader.readLine())!=null){
                                            if (i!=0){
                                                String[] values=line.split(",");
                                                DocumentReference documentReference=fStore.collection("Students Data").document(values[0]);
                                                Map<String,Object> data=new HashMap<>();
                                                for (int j=0; j<values.length; j++){
                                                    data.put(fields[j],values[j]);
                                                }
                                                documentReference.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //Toast.makeText(WriteFromFile.this, "id"+fields[i], Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(UploadWriteStuData.this, "Failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            else {
                                                fields=line.split(",");
                                            }
                                            //Toast.makeText(WriteFromFile.this, "line-"+line, Toast.LENGTH_SHORT).show();
                                            i++;
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            DocumentReference documentReference=fStore.collection("Uploads").document(model.getName());
                            Map<String,Object> file=new HashMap<>();
                            file.put("Status","Done");
                            documentReference.update(file).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UploadWriteStuData.this, "File Status Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadWriteStuData.this, "Failed to update file status", Toast.LENGTH_SHORT).show();
                                }
                            });
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                        }
                    });
                }
                else {
                    holder.fileWork.setText("Completed");
                    holder.fileWork.setTextColor(getResources().getColor(R.color.black,null));
                }
            }

            @NonNull
            @Override
            public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file,parent,false);
                return new FileViewHolder(view);
            }
        };

        stuFilesRecView.setHasFixedSize(true);
        stuFilesRecView.setLayoutManager(new LinearLayoutManager(this));
        stuFilesRecView.setAdapter(fileAdapter);
    }



    private void requirePermission() {
        String permission= Manifest.permission.READ_EXTERNAL_STORAGE;
        requestPermissionLauncher.launch(permission);
    }

    private void selectFile() {
        Intent intent=new Intent();
        intent.setType("text/comma-separated-values");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectingFileLauncher.launch(intent);
    }

    public static String getPath(final Context context, final Uri uri) {

        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/"
                        + split[1];
            }
        }

        // Downloads don't work properly for >= API 29
        else if (isDownloadsDocument(uri)) {
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        }

        else if (isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Toast.makeText(context, "URI1"+contentUri, Toast.LENGTH_SHORT).show();
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[] { split[1] };

            return getDataColumn(context, contentUri, selection,
                    selectionArgs);
        }

        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return nopath;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return nopath;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    private void uploadFile(Uri fileUri) {
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading File");
        progressDialog.setProgress(0);
        progressDialog.show();

        StorageReference storageReference=fStorage.getReference();
        storageReference.child("Uploads").child(fileName).putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> task=taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url=uri.toString();

                                DocumentReference documentReference=fStore.collection("Uploads").document(fileName);
                                Map<String, Object> file=new HashMap<>();
                                file.put("Name", fileName);
                                file.put("URL", url);
                                file.put("Status","Not Done");
                                documentReference.set(file).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(UploadWriteStuData.this, "File successfully uploaded", Toast.LENGTH_SHORT).show();
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UploadWriteStuData.this, "Error in file uploading", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadWriteStuData.this, "File not uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress=(int)(100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileStatus, fileWork;
        CardView fileCard;
        View view;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName=itemView.findViewById(R.id.fileName);
            fileStatus=itemView.findViewById(R.id.fileStatus);
            fileWork=itemView.findViewById(R.id.fileWork);
            fileCard=itemView.findViewById(R.id.fileCard);
            view=itemView;
        }
    }
}