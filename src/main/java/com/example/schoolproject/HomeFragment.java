package com.example.schoolproject;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView postListView;
    private List<Post> postList;
    private List<User> userList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PostRecyclerAdapter postRecyclerAdapter;
    private LinearLayout linear;

    private DocumentSnapshot lastVisible;
    private Boolean firstLoad = true;
    private Toolbar mainToolbar;
    private DrawerLayout mainDrawerNav;
    private ActionBarDrawerToggle mainDrawerToggle;
    private NavigationView navigationView;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
       // ((MainActivity) getActivity()).getSupportActionBar().setTitle("Talking Space");
        postList = new ArrayList<>();
        userList = new ArrayList<>();
        postListView = view.findViewById(R.id.post_list_view);
        linear=view.findViewById(R.id.linear);
        mAuth = FirebaseAuth.getInstance();


        postRecyclerAdapter = new PostRecyclerAdapter(postList, userList);
        postListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        postListView.setAdapter(postRecyclerAdapter);
        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent post = new Intent(getActivity(), NewPostActivity.class);
                startActivity(post);
            }
        });
        if (mAuth.getCurrentUser() != null) {

            db = FirebaseFirestore.getInstance();

            postListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {
                        Toast.makeText(container.getContext(), "Reached bottom", Toast.LENGTH_SHORT).show();
                        loadPost();
                    }
                }
            });

            // Sort results by descending (latest posts first)
            Query firstQuery = db.collection("Posts").limit(10);

            // Set real time database listener
            firstQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {

                            if (firstLoad) {
                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                postList.clear();
                                userList.clear();

                            }

                            for (DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String newPostId = doc.getDocument().getId();
                                    final Post newPost = doc.getDocument().toObject(Post.class).withId(newPostId);

                                    String postUserId = doc.getDocument().getString("user_id");
                                    if (postUserId == null)
                                        return;
                                    else{


                                    db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                if (firstLoad) {
                                                    userList.add(user);
                                                    postList.add(newPost);
                                                } else {
                                                    userList.add(0, user);
                                                    postList.add(0, newPost);
                                                }

                                            }

                                            postRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });


                                }
                            }

                            firstLoad = false;
                        }
                    }
                }      }
            });
        }



        // Inflate the layout for this fragment
        return view;
    }

    public void loadPost() {
        if (mAuth.getCurrentUser() != null) {
            // Sort results by descending (latest posts first)
            Query nextQuery = db.collection("Posts")
                    .startAfter(lastVisible)
                    .limit(10);

            // Set real time database listener
            nextQuery.addSnapshotListener(getActivity() ,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String newPostId = doc.getDocument().getId();
                                    final Post newPost = doc.getDocument().toObject(Post.class).withId(newPostId);

                                    String postUserId = doc.getDocument().getString("user_id");
                                    if (postUserId == null)
                                        return;
                                    else{


                                    db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                userList.add(user);
                                                postList.add(newPost);

                                            }

                                            postRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }          }
            });
        }
    }

}
