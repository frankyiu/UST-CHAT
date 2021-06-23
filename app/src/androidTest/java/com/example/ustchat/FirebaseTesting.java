package com.example.ustchat;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class FirebaseTesting {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    @Before
    public void setUp() throws Exception {
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("test/");
        mStorageReference = FirebaseStorage.getInstance().getReference("test/");
    }


    @Test
    public void testDatabaseWriteRead() throws InterruptedException {
        final String[] result = {""};
        final CountDownLatch dbSignal = new CountDownLatch(1);
        System.out.print("hi"+mDatabaseReference);
        mDatabaseReference.setValue("Testing", new DatabaseReference.CompletionListener(){
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        result[0] = dataSnapshot.getValue(String.class);
                        assertThat(result[0], is(equalTo("Testing")));
                        dbSignal.countDown();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        assertThat(result[0], is(equalTo("Testing")));
                        dbSignal.countDown();
                    }
                });
            }

        });
        dbSignal.await();
    }

    @Test
    public void testDatabaseDelete() throws InterruptedException {
        final CountDownLatch dbSignal = new CountDownLatch(1);
        final String[] result = {""};
        mDatabaseReference.setValue("Testing", new DatabaseReference.CompletionListener(){
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                mDatabaseReference.removeValue(new DatabaseReference.CompletionListener(){
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                result[0] = dataSnapshot.getValue(String.class);
                                assertThat(result[0], is(equalTo(null)));
                                dbSignal.countDown();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                assertThat(result[0], is(equalTo(null)));
                                dbSignal.countDown();
                            }
                        });
                    }
                });
            }
        });
        dbSignal.await();
    }

    @Test
    public void testAuth() throws InterruptedException {
        final String[] result = {""};
        final CountDownLatch authSignal = new CountDownLatch(1);
        mAuth.signInWithEmailAndPassword("ktyiuaa@connect.ust.hk", "123123")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            result[0] = "Login:success";
                        } else {
                            result[0] = task.getException().getMessage();
                        }
                        assertThat(result[0], is(equalTo("Login:success")));
                        authSignal.countDown();
                    }
                });
        authSignal.await();
    }

    @After
    public void tearDown() throws Exception {
        if(mAuth.getCurrentUser()!=null){
            mAuth.signOut();
        }
        mDatabaseReference.removeValue();
    }
}