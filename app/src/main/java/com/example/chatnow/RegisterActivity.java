package com.example.chatnow;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatnow.Common.NodeNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail,editTextName, editTextPassword,editTextConfirmPassword;
    private String email,name,password,confirmPassword;




    private ImageView imageViewProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;


    private StorageReference fileStorage;
    private Uri localFileUri, serverFileUri;
    private View viewProgressBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.etEmail);
        editTextName = findViewById(R.id.etName);
        editTextPassword = findViewById(R.id.etPassword);
        editTextConfirmPassword = findViewById(R.id.etConfirmPassword);
        imageViewProfile = findViewById(R.id.ivProfile);

        fileStorage = FirebaseStorage.getInstance().getReference();
        viewProgressBar=findViewById(R.id.progressBar);

    }


    public  void pickImage(View v)
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








    public void updateNameAndPhoto()
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
                                        String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put(NodeNames.NAME, editTextName.getText().toString().trim());
                                        hashMap.put(NodeNames.EMAIL,editTextEmail.getText().toString().trim());
                                        hashMap.put(NodeNames.ONLINE,"true");
                                        hashMap.put(NodeNames.PHOTO, serverFileUri.getPath());


                                        databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(RegisterActivity.this,"User Created Successfully",Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                            }
                                        });
                                    }

                                    else
                                    {
                                        Toast.makeText(RegisterActivity.this,"Failed to updated profile",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }










    public void updateOnlyName()
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
                    hashMap.put(NodeNames.EMAIL,editTextEmail.getText().toString().trim());
                    hashMap.put(NodeNames.ONLINE,"true");
                    hashMap.put(NodeNames.PHOTO,"");




                    viewProgressBar.setVisibility(View.VISIBLE);

                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {



                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            viewProgressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "User Created successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));

                        }
                    });

                }
                else {
                    Toast.makeText(RegisterActivity.this,"Failed to update profile",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }










    public void btnSignupClick(View view)
    {
        email = editTextEmail.getText().toString().trim();
        name  = editTextName.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (email.equals(""))
        {
            editTextEmail.setError("Enter a email");
        }
        else if (name.equals(""))
        {
            editTextName.setError("Enter a Name");
        }
        else if (password.equals(" "))
        {
            editTextPassword.setError("Enter a Password");
        }
        else if (editTextConfirmPassword.equals(""))
        {
            editTextConfirmPassword.setError("Enter a Confirm Password");
        }


        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            editTextEmail.setError("Enter Correct Email");
        }
        else if (!password.equals(confirmPassword))
        {
            editTextConfirmPassword.setError("Password Mismatch");
        }

        else
        {
            viewProgressBar.setVisibility(View.VISIBLE);

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    viewProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {

                        firebaseUser = firebaseAuth.getCurrentUser();

                        if (localFileUri != null)
                            updateNameAndPhoto();
                        else
                            updateOnlyName();

                    } else {

                        Toast.makeText(RegisterActivity.this,"Signup Failed" + task.getException(),Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));


                    }

                }
            });
        }
    }

}


