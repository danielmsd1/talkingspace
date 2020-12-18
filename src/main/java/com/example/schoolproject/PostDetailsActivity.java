package com.example.schoolproject;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import com.example.schoolproject.Utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailsActivity extends AppCompatActivity {

    private TextView postDetailTitle, postDetailDesc;

    private Toolbar postDetailToolbar;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CircleImageView comment_image;
    private TextView comment_username;
    private Uri profileImageUri = null;
    private ImageView postDetailImage ;

    private String postId;
    private String currentUserId;
    RelativeLayout relative;
    Context context;
    SharedPref sharedPref;
    ConstraintLayout constrain;
    private AccountFragment accountFragment;
    Fragment mFragment;
    private String userId;
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);



        postDetailToolbar = findViewById(R.id.post_detail_toolbar);
        setSupportActionBar(postDetailToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        comment_image=findViewById(R.id.comment_image);
        comment_username=findViewById(R.id.comment_username);
        postDetailImage = findViewById(R.id.post_detail_image);
        postDetailTitle = findViewById(R.id.post_detail_title);
        postDetailDesc = findViewById(R.id.post_detail_desc);
        constrain=findViewById(R.id.constrain);
        
        relative=findViewById(R.id.relative);
        sharedPref = new SharedPref(PostDetailsActivity.this);



        // Get Extras
        final Intent postDetailIntent = getIntent();
        String title = postDetailIntent.getExtras().getString("Title");
        String desc = postDetailIntent.getExtras().getString("Desc");
        String imageUri = postDetailIntent.getExtras().getString("Image");
         final String user = postDetailIntent.getExtras().getString("UserId");
        final String userimage = postDetailIntent.getExtras().getString("userimage");
        postId = getIntent().getStringExtra("post_id");
        getSupportActionBar().setTitle(title);
        sharedPref.setUserId(postDetailIntent.getExtras().getString("UserId"));
        sharedPref.setimage(postDetailIntent.getExtras().getString("Image"));
        sharedPref.setpic(postDetailIntent.getExtras().getString("userimage"));
        sharedPref.setname(postDetailIntent.getExtras().getString("UserId"));
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String myusername = task.getResult().getString("name");
                        sharedPref.setusername(myusername);
                    }
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(PostDetailsActivity.this, "Firestore Load Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }

            }
        });


        // Set Extras
        postDetailTitle.setText(title);
        postDetailDesc.setText(desc);
        RequestOptions requestOption = new RequestOptions();
        requestOption.placeholder(R.drawable.image_placeholder);
       // postDetailImage=Uri.parse(imageUri);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.image_placeholder);
        profileImageUri = Uri.parse(userimage);


        comment_username.setText(user);
        RequestOptions placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.default_profile);
        Glide.with(this).setDefaultRequestOptions(placeholderRequest).load(imageUri).into(postDetailImage);

        Glide.with(PostDetailsActivity.this).setDefaultRequestOptions(placeholderRequest).load(userimage).into(comment_image);
        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startActivity(new Intent(getApplicationContext(), profile.class));

            }
        });

    }
         





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_post:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Talking Space");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Check out my Talking Space app");
                startActivity(Intent.createChooser(sharingIntent, "Tell a friend via..."));
                break;
            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
