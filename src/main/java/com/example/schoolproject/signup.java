package com.example.schoolproject;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.schoolproject.Login.login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.HashMap;

public class signup extends AppCompatActivity {

    private EditText register_user_name;
    private EditText register_email;
    private EditText register_password;
    private EditText register_con_password;
    private Button register_create;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        //initialize variables
        register_user_name = findViewById(R.id.username);
        register_email = findViewById(R.id.email);
        register_password = findViewById(R.id.password);
        register_con_password = findViewById(R.id.cpassword);

        register_create = findViewById(R.id.btn_register);
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();


        register_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_name = register_user_name.getText().toString();
                String email = register_email.getText().toString();
                String password = register_password.getText().toString();
                String con_password  = register_con_password.getText().toString();
                boolean cancel = false;
                View focusView = null;

                ValidateUserInfo validate = new ValidateUserInfo();

                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    register_email.setError("empty email");
                    focusView = register_email;
                    cancel = true;
                } else if (TextUtils.isEmpty(password)) {
                    register_password.setError("empty password");
                    focusView =register_password;
                    cancel = true;
                } else if (!validate.isEmailValid(email)) {
                    register_email.setError("invalid email");
                    focusView = register_email;
                    cancel = true;
                } else if (TextUtils.isEmpty(con_password)) {
                    register_con_password.setError("field required");
                    focusView = register_con_password;
                    cancel = true;
                }

                else if (!ValidateUserInfo.isPasswordValid(password)) {
                    register_password.setError("invalid password (use caps,special character and number) ");
                    focusView = register_password;
                    cancel = true;
                }
                else{
                    signinuser(user_name,email,password);
                    Toast.makeText(signup.this,"please wait",Toast.LENGTH_SHORT).show();

                }
            }
        });

    }



    private void signinuser(final String user_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    String device_token = String.valueOf(FirebaseInstallations.getInstance().getId());


                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", user_name);
                    userMap.put("status", "Hi there I'm using TalkIt App.");
                    userMap.put("thumb_image", "gs://schoolprojectx-b0e50.appspot.com/profile_images/default.jpg");
                    userMap.put("device_token", device_token);
                    mDatabase.keepSynced(true);
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){


                                sendemail();
                                if(!user.isEmailVerified()){
                                    Toast.makeText(signup.this, "please verify email and login", Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(getApplicationContext(), login.class));
                                }


                            }
                        }


                });


                }else{

                    String error = task.getException().toString();
                    Toast.makeText(signup.this,error,Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void sendemail() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(signup.this,"check email for verification",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }


    private boolean checkpassword(String password, String con_password) {
        Log.d("login","in check password");
        if(password.equals(con_password))
            return true;
        else{
            Log.d("login","in toast");
            Toast.makeText(signup.this,"password and confirm password has to match",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

 
}

