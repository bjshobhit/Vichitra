package com.jainshobhit.vichitra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {
    RecyclerView recyclerviewofspecific;
    private TextView name;
    private ImageView userImage;
    private EditText entermessage;
    private ImageButton sendmessage,backbtn;
    String enteredmessage;
    ArrayList<Messages> messagesArrayList;
    MessagesAdapter messagesAdapter;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String msenderuid,mrecieveruid,mrecievername,senderroom,recieverroom,currenttime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        recyclerviewofspecific=findViewById(R.id.recyclerviewofspecific);
        name=findViewById(R.id.Nameofspecificuser);
        userImage=findViewById(R.id.specificuserimageinimageview);
        entermessage=findViewById(R.id.getmessage);
        sendmessage=findViewById(R.id.imageviewsendmessage);
        backbtn=findViewById(R.id.backbuttonofspecificchat);

        messagesArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerviewofspecific.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(SpecificChat.this,messagesArrayList);
        recyclerviewofspecific.setAdapter(messagesAdapter);

        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        msenderuid= FirebaseAuth.getInstance().getUid();
        mrecieveruid=getIntent().getStringExtra("recieveruid");

        mrecievername=getIntent().getStringExtra("name");

        senderroom=msenderuid+mrecieveruid;
        recieverroom=mrecieveruid+msenderuid;


        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("chats").child(senderroom).child("messages");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Messages messages=snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                    recyclerviewofspecific.scrollToPosition(messagesAdapter.getItemCount()-1);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        name.setText(mrecievername);
        String uri=getIntent().getStringExtra("recieverprofile");
        if(uri.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"null is recieved",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Picasso.get().load(uri).into(userImage);
        }

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SpecificChat.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });

        sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredmessage = entermessage.getText().toString();
                if(enteredmessage.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Enter message first",Toast.LENGTH_SHORT).show();
                }else{
                    Date date=new Date();
                    currenttime=simpleDateFormat.format(calendar.getTime());
                    Messages messages=new Messages(enteredmessage,currenttime,FirebaseAuth.getInstance().getUid(),date.getTime());

                    FirebaseDatabase.getInstance().getReference().child("chats")
                            .child(senderroom).child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseDatabase.getInstance().getReference().child("chats")
                                            .child(recieverroom).child("messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                }
                            });
                    entermessage.setText(null);

                }
            }
        });


    }



    @Override
    protected void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(messagesAdapter!=null)
        {
            messagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SpecificChat.this, ChatActivity.class);
        startActivity(intent);
        finish();
    }
}