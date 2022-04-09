package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    EditText etPhone,etCode,etCountryCode;
    Button btnCode,btnVerify,btnResend;
    ProgressDialog dialog;
    FirebaseAuth mAuth;
    String verificationID,sPhone;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        initViews();
        mAuth = FirebaseAuth.getInstance();

        //callbacks
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Check Your Phone for OTP!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                btnResend.setEnabled(true);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                authenticateUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginActivity.this, "Invalid Phone Number or OTP. Try again!" + e.getMessage(), Toast.LENGTH_LONG).show();
                etCountryCode.setVisibility(View.VISIBLE);
                etCountryCode.setText("");
                etPhone.setVisibility(View.VISIBLE);
                etPhone.setText("");
                btnCode.setVisibility(View.VISIBLE);
                etCode.setVisibility(View.GONE);
                btnVerify.setVisibility(View.GONE);
                btnResend.setVisibility(View.GONE);
                dialog.dismiss();
            }
        };

        // Send OTP
        btnCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etCountryCode.getText().toString().isEmpty())
                {
                    etCountryCode.setError("Enter Country Code");
                    return;
                }
                if(etPhone.getText().toString().isEmpty())
                {
                    etPhone.setError("Enter Phone Number");
                    return;
                }
                sPhone = "+"+etCountryCode.getText().toString().trim()+etPhone.getText().toString().trim();
                dialog = new ProgressDialog(LoginActivity.this);
                dialog.setMessage("Please Wait...");
                dialog.setCancelable(false);
                dialog.show();
                sendCode();
                etCountryCode.setVisibility(View.GONE);
                etPhone.setVisibility(View.GONE);
                btnCode.setVisibility(View.GONE);
                etCode.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
                btnResend.setVisibility(View.VISIBLE);
                btnResend.setEnabled(false);
            }
        });
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etCode.getText().toString().trim().length()<6)
                {
                    etCode.setError("Invalid OTP!");
                    return;
                }
                dialog.show();
                verifyCode(etCode.getText().toString().trim());
            }
        });
        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                sendCode();
                btnResend.setEnabled(false);
            }
        });
    }
    public void initViews()
    {
        etPhone = findViewById(R.id.etPhone);
        etCode = findViewById(R.id.etCode);
        btnCode = findViewById(R.id.btnCode);
        btnVerify = findViewById(R.id.btnVerify);
        etCountryCode = findViewById(R.id.etCountryCode);
        btnResend = findViewById(R.id.btnResend);
    }

    // Send Code
    public void sendCode()
    {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setActivity(LoginActivity.this)
                .setCallbacks(callbacks)
                .setPhoneNumber(sPhone)
                .setTimeout(60L,TimeUnit.SECONDS)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyCode(String otp)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID,otp);
        authenticateUser(credential);
    }

    // Authenticate sign in
    public void authenticateUser(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    // Move to home screen
                                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            },2000);
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Failed: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}