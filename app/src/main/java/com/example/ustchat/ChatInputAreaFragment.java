package com.example.ustchat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ChatInputAreaFragment extends Fragment {
    private static final String TAG = "ChatInputAreaFragment";
    private final boolean isPrivate;
    private String targetUserId;
    private String username;
    private boolean userRepliedBefore;

    private EditText etReply;
    private EditText etName;

    private ImageButton ibSubmitReply;
    private ImageButton ibAlbum;

    // handling the image picking
    private static final int PICK_IMAGE = 1;
    private Uri imageUri;

    private String chatId;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStoreRef;

    public ChatInputAreaFragment(String chatId, String username, boolean isPrivate, boolean userRepliedBefore) {
        this.username = username;
        this.userRepliedBefore = userRepliedBefore;
        this.chatId = chatId;
        this.isPrivate = isPrivate;
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStoreRef = FirebaseStorage.getInstance().getReference();
        getTargetUserID();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_input_area, container, false);

        etReply = view.findViewById(R.id.et_input_area_reply);
        etName = view.findViewById(R.id.et_input_area_name);
        if (userRepliedBefore) {
            etName.setEnabled(false);
        }
        etName.setText(username);

        etReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    ibSubmitReply.setEnabled(false);
                }
                else {
                    ibSubmitReply.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        ibSubmitReply = view.findViewById(R.id.iv_input_area_submit);
        ibSubmitReply.setEnabled(false);
        ibSubmitReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ibSubmitReply.isEnabled()) {
                    checkUserNameValid(new Callback() {
                        @Override
                        public void callback() {
                            submitTextReply();
                        }
                    });
                }
            }
        });

        ibAlbum = view.findViewById(R.id.iv_input_area_album);
        ibAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Select Photo"), PICK_IMAGE);
            }
        });

        return view;
    }

    public void openConfirmPhotoDialog(Drawable drawable) {
        Dialog dialog = new ConfirmPhotoDialog(getContext(), this, drawable);
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                bitmap.setDensity(Bitmap.DENSITY_NONE);
                Drawable drawable = new BitmapDrawable(bitmap);
                openConfirmPhotoDialog(drawable);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void checkUserNameValid( Callback callback){
        String name = etName.getText().toString();

        Query query = mDatabaseRef.child("nameToId/"+chatId).child(name);
        //checkNameExist
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //TODO add popup for using other name
                if(dataSnapshot.exists()){
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if(!mAuth.getCurrentUser().getUid().equals(postSnapshot.getValue(String.class))) {
                            Log.d(TAG, "other user used your name");
                            return;
                        }
                    }
                }else{
                    Log.d(TAG, "nameNOtExist");
                    mDatabaseRef.child("nameToId/"+chatId).child(name+"/id/").setValue(mAuth.getCurrentUser().getUid());
                    userRepliedBefore = true;
                    etName.setEnabled(false);
                }
                //update message
                callback.callback();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }
    public void submitTextReply() {
        // TO-DO : submit a text reply (Backend)
        String message = etReply.getText().toString();
        String name =etName.getText().toString();
        if(!message.equals("") && !name.equals("")) {
            ChatroomChatRecord chat = new ChatroomChatRecord(name, message, "", ServerValue.TIMESTAMP, "", true);

            mDatabaseRef.child("message/" + chatId).push().setValue(chat);
            if(!isPrivate) {
                updateChatMeta(name, message);
            }else{
                updatePrivateChatMeta(name, message);
                notifyTargetUser();
            }

            etReply.setText("");
        }
    }

//    public void submitPrivateTextReply() {
//        String message = etReply.getText().toString();
//        String name =etName.getText().toString();
//        if(!message.equals("") && !name.equals("")) {
//            PrivateChatRecord chat = new PrivateChatRecord(name, message, "", ServerValue.TIMESTAMP, "", true);
//
//            //update pChatRoom meta
//            updatePrivateChatMeta(name, message);
//            //update message
//            mDatabaseRef.child("message/" + chatId).push().setValue(chat);
//
//            //update notify TargetUser
//            notifyTargetUser();
//            etReply.setText("");
//        }
//    }

    public void submitImageReply() {
        // TO-DO : Submit an image reply (backend)

        if(imageUri != null){
            String messageId = mDatabaseRef.child("message/" + chatId).push().getKey();
            String imgPath = "images/"+chatId+"/"+messageId+"_"+imageUri.getLastPathSegment();
            StorageReference imageRef = mStoreRef.child(imgPath);
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "onFailure: uploadfile fail");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String name =etName.getText().toString();
                    if(!name.equals("")) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ChatroomChatRecord chat = new ChatroomChatRecord(name, "",uri.toString(), ServerValue.TIMESTAMP, "", true);
                                mDatabaseRef.child("message/" + chatId + "/" + messageId).setValue(chat);
                                if(!isPrivate) {
                                    //update chatRoom meta
                                    updateChatMeta(name, "{photo}");
                                }else{
                                    mDatabaseRef.child("message/" + chatId + "/" + messageId).setValue(chat);
                                    //update chatRoom meta
                                    updatePrivateChatMeta(name, "{photo}");
                                    notifyTargetUser();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void updateChatMeta(String name, String message){
        mDatabaseRef.child("chatroom/" + chatId+"/chatCount").setValue(ServerValue.increment(1));
        mDatabaseRef.child("chatroom/" + chatId+"/latestName").setValue(name);
        mDatabaseRef.child("chatroom/" + chatId+"/latestReply").setValue(message);
    }
    private void updatePrivateChatMeta(String name, String message){
        mDatabaseRef.child("privateChat/" + chatId+"/latestName").setValue(name);
        mDatabaseRef.child("privateChat/" + chatId+"/latestReply").setValue(message);
        mDatabaseRef.child("privateChat/" + chatId+"/latestReplyTime").setValue(ServerValue.TIMESTAMP);
    }

    private void notifyTargetUser(){
        mDatabaseRef.child("users/"+targetUserId+"/privateChat/"+chatId+"/unread/").setValue(ServerValue.increment(1));
    }

    //only for privateMessage
    private void getTargetUserID(){
        Query query = mDatabaseRef.child("nameToId/"+chatId);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String id = postSnapshot.child("id").getValue(String.class);
                    if(!id.equals(mAuth.getCurrentUser().getUid())){
                        targetUserId = id;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }
}

class ConfirmPhotoDialog extends Dialog {
    private ImageButton ibSubmit;
    private ImageView tvPhoto;
    Drawable photo;
    ChatInputAreaFragment chatInputAreaFragment;

    public ConfirmPhotoDialog(final Context context, ChatInputAreaFragment chatInputAreaFragment, Drawable photo) {
        super(context);
        this.chatInputAreaFragment = chatInputAreaFragment;
        this.photo = photo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_confirm_image);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ibSubmit = findViewById(R.id.btn_confirm_image_submit);
        tvPhoto = findViewById(R.id.tv_confirm_image_image);
        tvPhoto.setImageDrawable(photo);

        ibSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                chatInputAreaFragment.checkUserNameValid(new Callback() {
                    @Override
                    public void callback() {
                        chatInputAreaFragment.submitImageReply();
                    }
                });
                dismiss();
            }
        });
    }

}