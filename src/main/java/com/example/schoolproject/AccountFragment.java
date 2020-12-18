package com.example.schoolproject;


import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {
    private Toolbar toolbar;
    private TextView title;
    private ImageView back_btn;
    private CircleImageView profile_image;
    private CardView name, email, password, notification;
    private TextView profile_name, profile_email, profile_password, notification_text, version;
    private Button logout_btn;
    private Switch nbtn;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String photo_url, username, useremail, provider;
    private String userId;
    private FirebaseFirestore db;
    private Uri mainImageURI = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_profile2, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        title = view.findViewById(R.id.title);

        profile_image = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        notification = view.findViewById(R.id.notification);
        profile_name = view.findViewById(R.id.profile_name);
        profile_email = view.findViewById(R.id.profile_email);
        profile_password = view.findViewById(R.id.profile_password);
        notification_text = view.findViewById(R.id.notification_text);
        version = view.findViewById(R.id.version);
        logout_btn = view.findViewById(R.id.logout_btn);
        nbtn = view.findViewById(R.id.nbtn);




        //Changing the Typefaces
        Typeface extravaganzza = Typeface.createFromAsset(getActivity().getAssets(), "fonts/extravaganzza.ttf");
        title.setTypeface(extravaganzza);
        profile_name.setTypeface(extravaganzza);
        profile_email.setTypeface(extravaganzza);
        profile_password.setTypeface(extravaganzza);
        notification_text.setTypeface(extravaganzza);
        version.setTypeface(extravaganzza);
        logout_btn.setTypeface(extravaganzza);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        setHasOptionsMenu(false);
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageURI = Uri.parse(image);
                        profile_name.setText(name);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String profemail = user.getEmail();
                        profile_email.setText(profemail);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_profile);

                        Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest).load(image).into(profile_image);

                    }
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(getActivity(), "Firestore Load Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }

            }
        });

        db = FirebaseFirestore.getInstance();
        final String user_id = mAuth.getCurrentUser().getUid();
        // Inflate the layout for this fragment
        return view;
    }


}
