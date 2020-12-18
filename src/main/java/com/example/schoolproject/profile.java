package com.example.schoolproject;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import com.example.schoolproject.Utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class profile extends AppCompatActivity {

    private ImageView back_btn, header_image;
    private CircleImageView profile_image;
    private TextView followers_count, following_count, profile_name, error;
    private Button follow_btn,consult;
    private RecyclerView post_list;
    private FirebaseAuth mAuth;
    private String user_id, name, photo_url;
    private Uri mainImageURI = null;
    private FirebaseFirestore db;

    private FloatingActionButton add;
    private String userId;
    private String postId;
    SharedPref sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Finding view by ID

        header_image = findViewById(R.id.header_image);
        profile_image = findViewById(R.id.profile_image);
        followers_count = findViewById(R.id.followers_count);
        following_count = findViewById(R.id.following_count);
        profile_name = findViewById(R.id.profile_name);
        follow_btn = findViewById(R.id.follow_btn);
        post_list = findViewById(R.id.post_list);
        error = findViewById(R.id.error);
        consult=findViewById(R.id.consult);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        sharedPref = new SharedPref(this);
        final String userID = sharedPref.getuserId();
        mAuth = FirebaseAuth.getInstance();
        //preventing null pointers
        final String myusername = sharedPref.getusername();
        if(myusername==null){
        }
        consult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Contact_us.class));
            }
        });


        //Setting user details
        setUserDetails();
    }

    private void setUserDetails(){


        if (itsMe()){

            user_id = mAuth.getCurrentUser().getUid();
            name = mAuth.getCurrentUser().getDisplayName();
            DocumentReference user_doc = FirebaseFirestore.getInstance().collection("Users").document(user_id);
            final String image = sharedPref.getimage();
            final String pic = sharedPref.getpic();
            //catching null pointers
            if (image == null) {

            }
            if (pic == null) {
                Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
            }

            profile_name.setText(name);
            Glide.with(profile.this)
                    .applyDefaultRequestOptions(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .load(pic)
                    .into(profile_image);

            Glide.with(profile.this)
                    .applyDefaultRequestOptions(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .load(image)
                    .into(header_image);

            blurImage();


            user_doc.collection("Followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        followers_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            user_doc.collection("Following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        following_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            follow_btn.setText("Settings");



        }else {



            final DocumentReference user_doc = FirebaseFirestore.getInstance().collection("Users").document(user_id);

            user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){

                         //getting data from sharedpref
                        final String image = sharedPref.getimage();
                        final String pic = sharedPref.getpic();
                        final String name=sharedPref.getname();
                        //catching null pointers
                        if (image == null && pic==null && name==null) {

                        }

                        profile_name.setText(name);
                        Glide.with(profile.this)
                                .applyDefaultRequestOptions(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .load(pic)
                                .into(profile_image);

                        Glide.with(profile.this)
                                .applyDefaultRequestOptions(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .load(image)
                                .into(header_image);

                        blurImage();


                    }

                }
            });

            user_doc.collection("Followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        followers_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            user_doc.collection("Following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        following_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            user_doc.collection("Followers").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (documentSnapshot.exists()) {


                        follow_btn.setText("Following");
                        follow_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
                        follow_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

//                                user_doc.collection("Followers").document(mAuth.getCurrentUser().getUid()).delete()
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (!task.isSuccessful()) {
//                                                    Toast.makeText(profile.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
                                Map<String, Object> follower = new HashMap<>();
                                follower.put("Name", mAuth.getCurrentUser().getDisplayName());
                                follower.put("Id", mAuth.getCurrentUser().getUid());

                              //  FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Following").document(user_id).delete();

                            }
                        });


                    } else {

                        follow_btn.setText("Follow");
                        follow_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        follow_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Map<String, Object> follower = new HashMap<>();
                                follower.put("Name", mAuth.getCurrentUser().getDisplayName());
                                follower.put("Id", mAuth.getCurrentUser().getUid());

                                user_doc.collection("Followers").document(mAuth.getCurrentUser().getUid())
                                        .set(follower)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(profile.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                                                }                                            }
                                        });

                                FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Following").document(user_id)
                                        .set(follower);
                            }
                        });

                    }

                }
            });

        }
    }
        //function to know if its me
    private boolean itsMe(){
          //getting fata from shared pref
        final String myusername = sharedPref.getusername();
        final String userID = sharedPref.getuserId();
        //catching null pointer
        if (myusername == null && userID==null) {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
        }



        if (myusername.equals(userID)){
            
            return true;

        }else {
            return false;
        }
    }
     //fuction to blur image
    private void blurImage(){

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                header_image.setVisibility(View.VISIBLE);
                Blurry.with(profile.this)
                        .radius(30)
                        .sampling(1)
                        .async()
                        .capture(findViewById(R.id.header_image))
                        .into((ImageView) findViewById(R.id.header_image));
            }
        }, 1000);
    }


}