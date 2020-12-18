package com.example.schoolproject;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.schoolproject.Login.login;
import com.example.schoolproject.Utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri mainImageURI = null;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private String currentUserId;
    private  String userId;

    private FloatingActionButton addPostBtn;
    private DrawerLayout mainDrawerNav;
    private ActionBarDrawerToggle mainDrawerToggle;
    private NavigationView navigationView;
    private FirebaseDatabase mDatabase;

    private HomeFragment homeFragment;

    private TrendingFragment trendingFragment;
    private ChatsFragment chatsFragment;
    private RequestFragment requestFragment;
    private FriendsFragment friendsFragment;


    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init Toolbar & Navbar
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
       // getSupportActionBar().setTitle("Talking space");

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        NavigationView navigationView = findViewById(R.id.mainNavView);
        View headerView = navigationView.getHeaderView(0);
        final CircleImageView profile_image1=headerView.findViewById(R.id.pimage);
        final TextView navUsername = (TextView) headerView.findViewById(R.id.username);
        final TextView email = (TextView) headerView.findViewById(R.id.email);
        userId = mAuth.getCurrentUser().getUid();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                  try {
                      String username = dataSnapshot.child("name").getValue().toString();
                      final String image = dataSnapshot.child("image").getValue().toString();
                      String bio = dataSnapshot.child("status").getValue().toString();
//                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                      if (image != null && username != null && bio != null) {
                          navUsername.setText(username);
                          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                          String profemail = user.getEmail();
                          email.setText(profemail);
                          profile_image1.setImageResource(R.drawable.background);
                          mainImageURI = Uri.parse(image);
                          SharedPref sharedPref = new SharedPref(MainActivity.this);
                          sharedPref.setname(username);
                          sharedPref.setimage(image);
                          sharedPref.setbio(bio);

                          RequestOptions placeholderRequest = new RequestOptions();
                          placeholderRequest.placeholder(R.drawable.default_profile);

                          Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profile_image1);
                      } else {
                     //finishprofile();
                          Toast.makeText(MainActivity.this, "finish profile", Toast.LENGTH_SHORT).show();

                      }
                  }
                  catch (Exception e){

                    // finishprofile();
                  }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    if (task.getResult().exists()) {
//                        String name = task.getResult().getString("name");
//                        String image = task.getResult().getString("image");
//                        if (name==null || image==null ) {
//                            startActivity(new Intent(getApplicationContext(),SetupActivity.class));
//                        }
//                        else {
//                            navUsername.setText(name);
//                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                            String profemail = user.getEmail();
//                            email.setText(profemail);
//                            profile_image1.setImageResource(R.drawable.background);
//                            mainImageURI = Uri.parse(image);
//                            SharedPref sharedPref=new SharedPref(MainActivity.this);
//                            sharedPref.setname(name);
//                            sharedPref.setimage(image);
//
//                            RequestOptions placeholderRequest = new RequestOptions();
//                            placeholderRequest.placeholder(R.drawable.default_profile);
//
//                            Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profile_image1);
//                        }  }
//                } else {
//                    String errorMessage = task.getException().getMessage();
//                    Toast.makeText(MainActivity.this, "Firestore Load Error: " + errorMessage, Toast.LENGTH_LONG).show();
//                }
//
//            }
//        });
        // Init Toolbar & Navbar
        if (mAuth.getCurrentUser() != null) {

            // Init Fragments
            homeFragment = new HomeFragment();
            trendingFragment=new TrendingFragment();
            chatsFragment=new ChatsFragment();
            friendsFragment=new FriendsFragment();
            requestFragment=new RequestFragment();



            mainDrawerNav = findViewById(R.id.main_drawer_layout);
            mainDrawerToggle = new ActionBarDrawerToggle(this, mainDrawerNav, mainToolbar, R.string.drawer_open, R.string.drawer_close);
            mainDrawerNav.addDrawerListener(mainDrawerToggle);
            mainDrawerToggle.syncState();

            navigationView = findViewById(R.id.mainNavView);
            navigationView.setNavigationItemSelectedListener(this);

            navigationView.setCheckedItem(R.id.nav_home);

            initializeFragment();

            addPostBtn = findViewById(R.id.add_post_btn);
            SharedPref sharedPref;
            sharedPref = new SharedPref(MainActivity.this);
            boolean pub = sharedPref.getIsGuest();
            if (pub == true) {
                addPostBtn.setVisibility(View.INVISIBLE);
            } else {
                addPostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent addPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                        startActivity(addPostIntent);
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            //navigateToLogin();
            Toast.makeText(MainActivity.this, "error getting user", Toast.LENGTH_SHORT).show();
        } else {
            currentUserId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {
                            Toast.makeText(MainActivity.this, "update your ptofile", Toast.LENGTH_SHORT).show();
                             finishprofile();
                            //Intent setupIntent = new Intent(MainActivity.this, SettingsActivity.class);
                            //startActivity(setupIntent);
                            //finish();
                        }

                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mainDrawerNav.isDrawerOpen(GravityCompat.START)) {
            mainDrawerNav.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        switch (item.getItemId()) {
            case R.id.nav_home:
                addPostBtn.setEnabled(true);
                addPostBtn.setVisibility(View.VISIBLE);
                replaceFragment(homeFragment, currentFragment);
                break;
            case R.id.nav_trending:
                addPostBtn.setEnabled(false);
                addPostBtn.setVisibility(View.INVISIBLE);
                replaceFragment(trendingFragment, currentFragment);
                break;
            case R.id.nav_chats:
                addPostBtn.setEnabled(true);
                addPostBtn.setVisibility(View.VISIBLE);
                replaceFragment(chatsFragment, currentFragment);
                break;
            case R.id.nav_friends:
                addPostBtn.setEnabled(false);
                addPostBtn.setVisibility(View.INVISIBLE);
                replaceFragment(friendsFragment, currentFragment);
                break;
            case R.id.nav_requests:
                addPostBtn.setEnabled(false);
                addPostBtn.setVisibility(View.INVISIBLE);
                replaceFragment(requestFragment, currentFragment);
                break;



        }
        mainDrawerNav.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search_btn);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Logout handler
        switch (item.getItemId()) {
            case R.id.action_logout_btn:
                logout();
                return true;

            case R.id.action_profile_image:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.action_all_users:
                startActivity(new Intent(getApplicationContext(), AllUsersActivity.class));
                return true;

            default:
                return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        navigateToLogin();
        SharedPreferences preferences =getSharedPreferences("SchoolProjectx", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        finish();
    }

    private void navigateToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, login.class);
        startActivity(loginIntent);
        finish();
    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, trendingFragment);
        fragmentTransaction.add(R.id.main_container,chatsFragment);
        fragmentTransaction.add(R.id.main_container,friendsFragment);
        fragmentTransaction.add(R.id.main_container,requestFragment);


        fragmentTransaction.hide(trendingFragment);
        fragmentTransaction.hide(chatsFragment);
        fragmentTransaction.hide(friendsFragment);
        fragmentTransaction.hide(requestFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){
            fragmentTransaction.hide(trendingFragment);
            fragmentTransaction.hide(chatsFragment);
            fragmentTransaction.hide(friendsFragment);
            fragmentTransaction.hide(requestFragment);
        }



        if(fragment==trendingFragment)
        {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(chatsFragment);
            fragmentTransaction.hide(friendsFragment);
            fragmentTransaction.hide(requestFragment);
        }
        if(fragment==chatsFragment)
        {

            fragmentTransaction.hide(trendingFragment);
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(friendsFragment);
            fragmentTransaction.hide(requestFragment);
        }
        if(fragment==friendsFragment)
        {

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(chatsFragment);
            fragmentTransaction.hide(trendingFragment);
            fragmentTransaction.hide(requestFragment);
        }
        if(fragment==requestFragment)
        {
         
            fragmentTransaction.hide(trendingFragment);
            fragmentTransaction.hide(chatsFragment);
            fragmentTransaction.hide(friendsFragment);
            fragmentTransaction.hide(homeFragment);
        }




        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
    public void finishprofile() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Incomplete profile")
                .setMessage("You have not set a Profimage or bio. Kindly go to settings and finish your profile")

                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();


                    }
                }).setNeutralButton("update profile", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
               
            }
        })
                .setCancelable(false)
                .show();

    }
}
