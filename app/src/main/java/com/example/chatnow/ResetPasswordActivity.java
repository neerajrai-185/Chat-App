package com.example.chatnow;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private TextView textViewMessage;
    private LinearLayout llResetPassword, llMessage;
    private Button buttonRetry;
    private View viewProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editTextEmail=findViewById(R.id.etEmail);
        textViewMessage=findViewById(R.id.tvMessage);
        llMessage=findViewById(R.id.llMessage);
        llResetPassword=findViewById(R.id.llResetPassword);
        buttonRetry=findViewById(R.id.btnRetry);
        viewProgressBar=findViewById(R.id.progressBar);

    }

    public void btnResetPasswordClick(View view) {

        String email = editTextEmail.getText().toString().trim();

        if (email.equals(""))
        {
            editTextEmail.setError("Enter email");
        }
        else {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            viewProgressBar.setVisibility(View.VISIBLE);
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    viewProgressBar.setVisibility(View.GONE);
                    llResetPassword.setVisibility(View.GONE);
                    llMessage.setVisibility(View.VISIBLE);


                    if (task.isSuccessful())
                    {

                        textViewMessage.setText(getString(R.string.reset_password_instructions,email));//important
                        new CountDownTimer(60000,1000)
                        {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                buttonRetry.setText(getString( R.string.resend_timer,  String.valueOf(millisUntilFinished/1000)));
                                buttonRetry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                buttonRetry.setText(R.string.retry);
                                buttonRetry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        llResetPassword.setVisibility(View.VISIBLE);
                                        llMessage.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }.start();
                    }
                    else
                    {
                        textViewMessage.setText(getString(R.string.email_sent_failed, task.getException()));//important
                        buttonRetry.setText(R.string.retry);

                        buttonRetry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                llResetPassword.setVisibility(View.VISIBLE);
                                llMessage.setVisibility(View.GONE);
                            }
                        });
                    }

                }
            });

        }
    }

    public void btnCloseClick(View view) { //it will show with retry.
        finish();
    }
}