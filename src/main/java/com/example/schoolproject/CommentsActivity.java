package com.example.schoolproject;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private EditText commentField;
    private ImageView commentPostBtn;

    private RecyclerView commentListView;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String postId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("New Post Comment");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        postId = getIntent().getStringExtra("post_id");

        commentField = findViewById(R.id.comment_field);
        commentPostBtn = findViewById(R.id.comment_post_btn);
        commentListView = findViewById(R.id.comment_list);

        // RecyclerView Firebase List
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        commentListView.setHasFixedSize(true);
        commentListView.setLayoutManager(new LinearLayoutManager(this));
        commentListView.setAdapter(commentsRecyclerAdapter);

        db.collection("Posts/" + postId + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshots != null) {
                            if (!documentSnapshots.isEmpty()) {


                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String commentId = doc.getDocument().getId();
                                        Comments comments = doc.getDocument().toObject(Comments.class);
                                        commentsList.add(comments);
                                        commentsRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }

                            }
                        }
                    }
                });

        commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentMsg = commentField.getText().toString();

                if (!TextUtils.isEmpty(commentMsg)) {
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMsg);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("Posts/" + postId + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (!task.isSuccessful()) {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(CommentsActivity.this, "Comment Post Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            } else {
                                commentField.setText("");
                            }
                        }
                    });
                } else {
                    Toast.makeText(CommentsActivity.this, "Posting a empty comment?", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
