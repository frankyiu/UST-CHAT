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
import androidx.annotation.Nullable;
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
import android.widget.TextView;

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
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

public class ChatInputAreaFragment extends Fragment {
    private static final String TAG = "ChatInputAreaFragment";
    private final boolean isPrivate;
    private final String chatroomTitle;
    private String username;
    private String chatId;
    private boolean userRepliedBefore;

    private EditText etReply;
    private EditText etName;

    private ImageButton ibSubmitReply;
    private ImageButton ibAlbum;

    // handling the image picking
    private static final int PICK_IMAGE = 1;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStoreRef;

    public ChatInputAreaFragment(String chatId, String chatroomTitle, String username, boolean isPrivate, boolean userRepliedBefore) {
        this.username = username;
        this.userRepliedBefore = userRepliedBefore;
        this.isPrivate = isPrivate;
        this.chatroomTitle = chatroomTitle;
        this.chatId = chatId;
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStoreRef = FirebaseStorage.getInstance().getReference();
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
            etName.setTextColor(getResources().getColor(R.color.gray_D5));
        }
        else {
            if(mAuth.getCurrentUser() != null){
                generateUsername();
            }
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
                    submitTextReply();
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
        if(mAuth.getCurrentUser() ==null){
            etReply.setEnabled(false);
            ibAlbum.setEnabled(false);
        }
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


    public void checkUserNameValid(String name, Callback callback){

        Query query = mDatabaseRef.child("nameToId/"+chatId).child(name);
        //checkNameExist
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()){
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if(!mAuth.getCurrentUser().getUid().equals(postSnapshot.getValue(String.class))) {
                            Log.d(TAG, "other user used your name");
                            Toast.makeText((ChatroomChatActivity)getActivity(), "Name repeated. Please pick another one",
                                    Toast.LENGTH_SHORT).show();
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
        String name = etName.getText().toString();
        if (!message.isEmpty() && !name.isEmpty()) {
            if (!isPrivate) {
                checkUserNameValid(etName.getText().toString(), new Callback() {
                    @Override
                    public void callback() {
                        ChatroomChatActivity chatActivity = (ChatroomChatActivity) getActivity();
                        chatActivity.sendTextReply(message, name);
                        etReply.setText("");
                        LinearLayout llQuoteArea = getActivity().findViewById(R.id.ll_chat_input_area_quote);
                        llQuoteArea.setVisibility(View.GONE);
                    }
                });
            } else {
                PrivateMessageChatActivity chatActivity = (PrivateMessageChatActivity) getActivity();
                chatActivity.invokeSendTextReply(message, name);
                etReply.setText("");
                LinearLayout llQuoteArea = getActivity().findViewById(R.id.ll_chat_input_area_quote);
                llQuoteArea.setVisibility(View.GONE);
            }
        }
    }


    public void submitImageReply() {
        // TO-DO : Submit an image reply (backend)
        String name =etName.getText().toString();
        if(!isPrivate){
            checkUserNameValid(etName.getText().toString(), new Callback() {
                @Override
                public void callback() {
                    ChatroomChatActivity chatActivity = (ChatroomChatActivity) getActivity();
                    chatActivity.sendImageReply(imageUri,name);
                }
            });
        }else{
            PrivateMessageChatActivity chatActivity = (PrivateMessageChatActivity) getActivity();
            chatActivity.invokeSendImageReply(imageUri, name);
        }

        // cancel quote area if there is quote area
        LinearLayout llQuoteArea = getActivity().findViewById(R.id.ll_chat_input_area_quote);
        llQuoteArea.setVisibility(View.GONE);
    }

    public void generateUsername() {
        // Generate a username in the format of Student[\d]{5} that does not exist in the chatroom
        // used when the user has not replied in the chatroom before
        String proposedUsername = "Student" + Utility.generateIntegerWithLeadingZeros(100000, 5);
        // TO-DO : need to check if it exists
        Query query = mDatabaseRef.child("nameToId/"+chatId).child(proposedUsername);
        //checkNameExist
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()){
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if(!mAuth.getCurrentUser().getUid().equals(postSnapshot.getValue(String.class))) {
                            generateUsername();
                        }
                    }
                }else{
                    username = proposedUsername;
                    etName.setText(username);
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
                chatInputAreaFragment.submitImageReply();
                dismiss();
            }
        });
    }

}