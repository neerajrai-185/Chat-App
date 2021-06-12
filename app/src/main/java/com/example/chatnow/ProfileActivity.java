package com.example.chatnow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatnow.Common.NodeNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextEmail,editTextName;
    private ImageView imageViewProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private StorageReference fileStorage;
    private Uri localFileUri,serverFileUri;
    private FirebaseAuth firebaseAuth;
    private View viewProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        editTextEmail=findViewById(R.id.etEmail);
        editTextName=findViewById(R.id.etName);
        imageViewProfile=findViewById(R.id.ivProfile);

        fileStorage = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        viewProgressBar=findViewById(R.id.progressBar);




        //If Fire Base is not equal to null
        // then display the name and email and photo in update profile.
        if (firebaseUser!=null)
        {
            editTextName.setText(firebaseUser.getDisplayName());
            editTextEmail.setText(firebaseUser.getEmail());

            //IF serverFileUri is not equal to the null then load new image .
            serverFileUri=firebaseUser.getPhotoUrl();
            if (serverFileUri!=null)
            {
                Glide.with(this)
                        .load(serverFileUri)
                        .placeholder(R.drawable.ic_chatupnow)
                        .error(R.drawable.ic_chatupnow)
                        .into(imageViewProfile);
            }
        }

    }


    public void btnLogoutClick(View view) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUSer = firebaseAuth.getCurrentUser();
        startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
        finish();
    }




    public void btnSaveClick(View view) {
        if (editTextName.getText().toString().trim().equals(""))
        {
            editTextName.setError("Enter name");
        }
        else{
            if(localFileUri!=null)
                updateNameAndPhoto(); //calling inside the java code
            else
                updateOnlyName();  //calling inside the java code
        }
    }




    public void changeImage(View view)
    {
        if(serverFileUri==null)
        {
            pickImage(); ///calling inside the java code
        }

        else
        {
            PopupMenu popupMenu = new PopupMenu(this,view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture,popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    int id = item.getItemId();
                    if (id==R.id.mnuChangePic)
                    {
                        pickImage();
                    }
                    else if(id==R.id.mnuRemovePic)
                    {
                        removePhoto();
                    }
                    //Why we are returning false here !!!
                    return false;
                }
            });
            popupMenu.show();
        }
    }


    public void pickImage()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,101);
        }

        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==101)
        {
            if(resultCode==RESULT_OK)
            {

                localFileUri = data.getData();
                imageViewProfile.setImageURI(localFileUri);
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==102)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,101);
            }
            else
            {
                Toast.makeText(this,"Permission Required",Toast.LENGTH_SHORT).show();
            }

        }
    }






    private void removePhoto()
    {
        viewProgressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(editTextName.getText().toString().trim())
                .setPhotoUri(null) //Deleting a photo with the help of "null" which has already added
                .build();



        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                viewProgressBar.setVisibility(View.GONE);
                if (task.isSuccessful())
                {
                    imageViewProfile.setImageResource(R.drawable.ic_chatupnow);
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.PHOTO,"");

                    //This code has litile diferent to the registration

                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this,"Photo Removed Successfully",Toast.LENGTH_SHORT).show();
                        }
                    });

                } else
                {
                    Toast.makeText(ProfileActivity.this,"Failed to update profile"+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void updateNameAndPhoto()
    {
        String strFileName = firebaseUser.getUid() + ".jpg";

        StorageReference fileRef = fileStorage.child("images/"+strFileName);
        viewProgressBar.setVisibility(View.VISIBLE);
        fileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                viewProgressBar.setVisibility(View.GONE);
                if (task.isSuccessful())
                {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverFileUri = uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(editTextName.getText().toString().trim())
                                    .setPhotoUri(serverFileUri)
                                    .build();







                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        String userId = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put(NodeNames.NAME,editTextName.getText().toString().trim());
                                        hashMap.put(NodeNames.PHOTO,serverFileUri.getPath());


                                        databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                finish();
                                            }
                                        });
                                    }else
                                    {
                                        Toast.makeText(ProfileActivity.this,"Failed to update profile"+task.getException(),Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    });
                }

            }
        });
    }






    private void updateOnlyName()
    {
        viewProgressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(editTextName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                viewProgressBar.setVisibility(View.GONE);
                if (task.isSuccessful())
                {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.NAME,editTextName.getText().toString().trim());


                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,"Failed to update profile"+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    public void btnChangePasswordClick(View view)
    {
        startActivity(new Intent(ProfileActivity.this,ChangePasswordActivity.class));
    }


}

