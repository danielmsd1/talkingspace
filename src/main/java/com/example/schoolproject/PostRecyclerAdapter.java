package com.example.schoolproject;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.schoolproject.Utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    public List<Post> postList;
    public List<User> userList;
    public Context context;
    public PostRecyclerAdapter adapter;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private String currentUserId;
    private  String userId;
    private FirebaseFirestore db;
    TextView timetxt,locationtxt;
    private FirebaseAuth mAuth;
    SharedPref sharedPref;
    private String image,username;
    private AccountFragment accountFragment;

    public PostRecyclerAdapter(List<Post> postList, List<User> userList) {
        this.postList = postList;
        this.userList = userList;
        this.adapter = this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        context = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final String postId = postList.get(position).PostId.toString();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        sharedPref = new SharedPref(context);
        sharedPref.setpostId(postId);
        
       

        final String descData = postList.get(position).getDesc();
        holder.setDescText(descData);

        final String image_url = postList.get(position).getImage_url();
        String thumb_url = postList.get(position).getThumb_url();
        holder.setPostImage(image_url, thumb_url);

        final String title = postList.get(position).getTitle();
        holder.setTitleText(title);

        final String location = postList.get(position).getlocation();
        holder.setlocation(location);



        final String post_user_id = postList.get(position).getUser_id();

        if (post_user_id.equals(currentUserId)) {
            holder.postDeleteBtn.setEnabled(true);
            holder.postDeleteBtn.setVisibility(View.VISIBLE);
            holder.postLikeBtn.setEnabled(false);
            holder.postLikeBtn.setVisibility(View.INVISIBLE);
        } else {
            holder.postDeleteBtn.setEnabled(false);
            holder.postDeleteBtn.setVisibility(View.INVISIBLE);
        }


        final String userName = userList.get(position).getName();
        final String userImage = userList.get(position).getImage();


        holder.setUserData(userName, userImage);

        try {
            long dateInMs = postList.get(position).getTimestamp().getTime();
            String postDate = DateFormat.format("dd MMM yyyy ", new Date(dateInMs)).toString();
            String posttime = DateFormat.format("hh:mm:ss ", new Date(dateInMs)).toString();
            holder.setDate(postDate);
            holder.settime(posttime);
        } catch (Exception e) {
            // Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Exception: ", e.getMessage());
        }

        // Get Likes Count
        db.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        holder.updateLikesCount(count);
                    } else {
                        holder.updateLikesCount(0);
                    }
                }
            }
        });

        // Get Likes
        db.collection("Posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_active);
                    } else {
                        holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_default);
                    }
                }
            }
        });


        // Like button press handler
        holder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            db.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);
                        } else {
                            db.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        holder.postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("post_id", postId);
                context.startActivity(commentIntent);
            }
        });

        holder.postDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Post")
                        .setMessage("You are about to delete this post, are you sure?")
                        .setIcon(R.drawable.ic_delete_accent)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("Posts").document(postId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        postList.remove(position);
                                        userList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });

        sharedPref = new SharedPref(context);
        boolean pub=sharedPref.getIsGuest();
        if(pub==true){
            holder.postLikeBtn.setVisibility(View.INVISIBLE);
            holder.postDeleteBtn.setVisibility(View.INVISIBLE);
            holder.postCommentBtn.setVisibility(View.INVISIBLE);

            holder.postCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.dialogBoxGuestLogin();
                }
            });
        }
        else{
              holder.postImageView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      holder.postImageView.setOnClickListener(new DoubleClickListener() {

                          @Override
                          public void onSingleClick(View v) {

                          }

                          @Override
                          public void onDoubleClick(View v) {
                              db.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                  @Override
                                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                      if (!task.getResult().exists()) {
                                          Map<String, Object> likesMap = new HashMap<>();
                                          likesMap.put("timestamp", FieldValue.serverTimestamp());

                                          db.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);
                                      } else {
                                          db.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                                      }
                                  }
                              });
                          }
                      });



                  }
              });

            holder.postUserImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accountFragment = new AccountFragment();
                    Bundle args = new Bundle();
                    args.putString("user", userName);

                }
            });

            holder.postCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postDetailIntent = new Intent(context, PostDetailsActivity.class);
                    postDetailIntent.putExtra("Title", title);
                    postDetailIntent.putExtra("Desc", descData);
                    postDetailIntent.putExtra("Image", image_url);
                    postDetailIntent.putExtra("UserId", userName);
                    postDetailIntent.putExtra("userimage", userImage);


                    context.startActivity(postDetailIntent);
                    /// sharedPref = new SharedPref(this);
                    //sharedPref.setUserId(userName);
                }
            });
            holder.postshare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, title);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, descData);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, image_url);
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    context.startActivity(shareIntent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView postImageView, postLikeBtn, postCommentBtn,postshare;
        private TextView titleView, descView, postDateView, postUserName, postLikeCount;

        private CircleImageView postUserImageView;
        private ImageButton postDeleteBtn;

        private CardView postCardView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            postCardView = mView.findViewById(R.id.main_post);
            postLikeBtn = mView.findViewById(R.id.post_like_btn);
            postCommentBtn = mView.findViewById(R.id.post_comment_btn);
            postDeleteBtn = mView.findViewById(R.id.post_delete_btn);
            postshare=mView.findViewById(R.id.post_comment_share);
        }

        public void setTitleText(String titleText) {
            titleView = mView.findViewById(R.id.post_title);
            titleView.setText(titleText);
        }

        public void setlocation(String location) {
            locationtxt = mView.findViewById(R.id.post_location);
            locationtxt.setText(" at " +location+ " Kenya");
        }
        public void settime(String time) {
            timetxt = mView.findViewById(R.id.post_time);
            timetxt.setText(" at "+time);
        }

        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.post_desc);
            descView.setText(descText);
        }

        public void setPostImage(String downloadUri, String downloadThumbUri) {
            postImageView = mView.findViewById(R.id.post_image);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context)
                    .applyDefaultRequestOptions(placeholderOptions)
                    .load(downloadUri)
                    .thumbnail(Glide.with(context)
                            .load(downloadThumbUri))
                    .into(postImageView);
        }

        public void setDate(String date) {
            postDateView = mView.findViewById(R.id.post_date);
            postDateView.setText(date);
        }

        public void setUserData(String name, String image) {
            postUserName = mView.findViewById(R.id.post_username);
            postUserImageView = mView.findViewById(R.id.post_user_image);

            postUserName.setText(name);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(image).into(postUserImageView);
        }

        public void updateLikesCount(int count) {
            postLikeCount = mView.findViewById(R.id.post_like_count);
            postLikeCount.setText(count + " Likes");
        }
        public void dialogBoxGuestLogin() {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Access Denied")
                    .setMessage("You are logged in as a guest user. Access is only allowed to private users")

                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    }).setNeutralButton("I have an account", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    mAuth.signOut();

                    Toast.makeText(context, "please logout", Toast.LENGTH_SHORT).show();
                }
            })
                    .setCancelable(false)
                    .show();

        }


    }
}
