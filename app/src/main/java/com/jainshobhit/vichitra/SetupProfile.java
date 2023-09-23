package com.jainshobhit.vichitra;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import soup.neumorphism.NeumorphButton;

public class SetupProfile extends AppCompatActivity {
    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private static int PICK_IMAGE=123;
    private Uri imagepath;

    private EditText mgetusername;

    private AppCompatButton msetupprofile;

    private FirebaseAuth firebaseAuth;
    private String name;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String ImageUriAcessToken;
    private FirebaseFirestore firebaseFirestore;

    ProgressBar mprogressbarofsetupprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        mgetusername=findViewById(R.id.getusername);
        mgetuserimage=findViewById(R.id.getuserimage);
        mgetuserimageinimageview=findViewById(R.id.getuserimageinimageview);
        msetupprofile=findViewById(R.id.setupprofile);
        mprogressbarofsetupprofile=findViewById(R.id.progressbarofprofilesetup);

        mgetuserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE);
            }
        });
        msetupprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=mgetusername.getText().toString();
                if (name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath==null){
                    Toast.makeText(SetupProfile.this, "Image is Empty", Toast.LENGTH_SHORT).show();
                }
                else {

                    mprogressbarofsetupprofile.setVisibility(View.VISIBLE);

                    sendDataForNewuser();
                    mprogressbarofsetupprofile.setVisibility((View.INVISIBLE));
                    Intent intent=new Intent(SetupProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();


                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            imagepath=data.getData();
            mgetuserimageinimageview.setImageURI(imagepath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void sendDataForNewuser(){
        sendDataToRealtimeDatabase();
    }
    private void sendDataToRealtimeDatabase(){

        name=mgetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users/").child(firebaseAuth.getUid());

        Map<String, String> muserprofile = new HashMap<>();
        muserprofile.put("username",name);
        muserprofile.put("userUID",firebaseAuth.getUid());
        databaseReference.setValue(muserprofile);
        sendImagetoStorage();

    }
    private void sendImagetoStorage(){
        StorageReference imageref=storageReference.child("images").child(firebaseAuth.getUid()).child("Profile Pic");
        Bitmap bitmap=null;
        try {
            bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data=byteArrayOutputStream.toByteArray();

        UploadTask uploadTask=imageref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAcessToken=uri.toString();
                        sendDataTocloudFirestore();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetupProfile.this, "Some Error Occurred!!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetupProfile.this, "Some Error Occurred!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataTocloudFirestore() {

        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String,Object> userdata=new HashMap<>();
        userdata.put("name",name);
        userdata.put("image",ImageUriAcessToken);
        userdata.put("uid",firebaseAuth.getUid());
        userdata.put("status","Online");

        documentReference.set(userdata);

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You Can not Go Back At this Stage", Toast.LENGTH_SHORT).show();
    }
}