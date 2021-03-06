package chanathip.gotogether;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PersonChatActivity extends AppCompatActivity {
    private UserData currentChatUserData;
    private UserData userData;
    private List<UserMessage> userMessages;
    private RecyclerView.LayoutManager layoutManager;
    private ChatAdapter chatAdapter;
    private ChildEventListener currentChatUserDatabaseReferencechildEventListener;
    private ChildEventListener userDataUserDatabaseReferencechildEventListener;
    private DatabaseReference currentChatUserDatabaseReference;
    private DatabaseReference userDataUserDatabaseReference;

    @BindView(R.id.txt_submit_chat)
    TextInputLayout txt_submit_chat;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.bind(this);

        currentChatUserData = new UserData();
        userData = new UserData();
        userMessages = new ArrayList<>();

        //get information from bundle
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                currentChatUserData.userUid = null;
                currentChatUserData.displayname = null;
                userData.userUid = null;
                userData.displayname = null;
            } else {
                currentChatUserData.userUid = extras.getString("currentChatuserUid");
                currentChatUserData.displayname = extras.getString("currentChatuserDisplayname");
                userData.userUid = extras.getString("userUid");
                userData.displayname = extras.getString("UserDisplayname");
            }
        } else {
            currentChatUserData.userUid = (String) savedInstanceState.getSerializable("currentChatuserUid");
            currentChatUserData.displayname = (String) savedInstanceState.getSerializable("currentChatuserDisplayname");
            userData.userUid = (String) savedInstanceState.getSerializable("userUid");
            userData.displayname = (String) savedInstanceState.getSerializable("UserDisplayname");
        }
        setTitle(currentChatUserData.displayname + "'s message");

        currentChatUserDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentChatUserData.userUid).child("messages").child(userData.userUid);
        currentChatUserDatabaseReferencechildEventListener = new ChildEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, String> ChatUserdataMap = (Map<String, String>) dataSnapshot.getValue();
                    UserMessage userMessage = new UserMessage();

                    userMessage.message = ChatUserdataMap.get("message");
                    userMessage.Type = "self";
                    userMessage.sender = userData.displayname;

                    Locale locale;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        locale = getResources().getConfiguration().getLocales().get(0);
                    } else {
                        locale = getResources().getConfiguration().locale;
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", locale);
                    try {
                        userMessage.calendar = Calendar.getInstance();
                        userMessage.calendar.setTime(dateFormat.parse(dataSnapshot.getKey()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);
                    userMessage.time = dateFormat1.format(userMessage.calendar.getTime());
                    if (ChatUserdataMap.get("read") != null) {
                        userMessage.readstatus = ChatUserdataMap.get("read");
                    } else {
                        userMessage.readstatus = "unread";
                    }

                    userMessages.add(userMessage);
                    Collections.sort(userMessages);
                    chatAdapter.notifyDataSetChanged();
                    layoutManager.scrollToPosition(userMessages.size() - 1);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                UserMessage userMessage = new UserMessage();
                if (dataSnapshot.getValue() != null) {
                    Map<String, String> ChatUserdataMap = (Map<String, String>) dataSnapshot.getValue();

                    userMessage.message = ChatUserdataMap.get("message");
                    userMessage.Type = "self";
                    userMessage.sender = userData.displayname;

                    Locale locale;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        locale = getResources().getConfiguration().getLocales().get(0);
                    } else {
                        locale = getResources().getConfiguration().locale;
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", locale);
                    try {
                        userMessage.calendar = Calendar.getInstance();
                        userMessage.calendar.setTime(dateFormat.parse(dataSnapshot.getKey()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);
                    userMessage.time = dateFormat1.format(userMessage.calendar.getTime());
                    if (ChatUserdataMap.get("read") != null) {
                        userMessage.readstatus = ChatUserdataMap.get("read");
                    } else {
                        userMessage.readstatus = "unread";
                    }
                }

                for(UserMessage obj : userMessages){
                    if(obj.time.equals(userMessage.time)){
                        obj.readstatus = userMessage.readstatus;
                        chatAdapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        userDataUserDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userData.userUid).child("messages").child(currentChatUserData.userUid);
        userDataUserDatabaseReferencechildEventListener = new ChildEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, String> ChatUserdataMap = (Map<String, String>) dataSnapshot.getValue();
                    UserMessage userMessage = new UserMessage();

                    userMessage.message = ChatUserdataMap.get("message");
                    userMessage.Type = "notself";
                    userMessage.sender = currentChatUserData.displayname;

                    Locale locale;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        locale = getResources().getConfiguration().getLocales().get(0);
                    } else {
                        locale = getResources().getConfiguration().locale;
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", locale);
                    try {
                        userMessage.calendar = Calendar.getInstance();
                        userMessage.calendar.setTime(dateFormat.parse(dataSnapshot.getKey()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);
                    userMessage.time = dateFormat1.format(userMessage.calendar.getTime());
                    if (ChatUserdataMap.get("read") != null) {
                        userMessage.readstatus = ChatUserdataMap.get("read");
                    } else {
                        userMessage.readstatus = "unread";
                    }
                    userMessages.add(userMessage);
                    Collections.sort(userMessages);
                    chatAdapter.notifyDataSetChanged();

                    DatabaseReference markReadDatabaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(userData.userUid).child("messages").child(currentChatUserData.userUid)
                            .child(dataSnapshot.getKey()).child("read");
                    markReadDatabaseReference.setValue("read");


                    layoutManager.scrollToPosition(userMessages.size() - 1);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentChatUserDatabaseReference.addChildEventListener(currentChatUserDatabaseReferencechildEventListener);
        userDataUserDatabaseReference.addChildEventListener(userDataUserDatabaseReferencechildEventListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        currentChatUserDatabaseReference.removeEventListener(currentChatUserDatabaseReferencechildEventListener);
        userDataUserDatabaseReference.removeEventListener(userDataUserDatabaseReferencechildEventListener);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("currentChatuserUid", currentChatUserData.userUid);
        savedInstanceState.putString("currentChatuserDisplayname", currentChatUserData.displayname);
        savedInstanceState.putString("userUid", userData.userUid);
        savedInstanceState.putString("UserDisplayname", userData.displayname);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateUI() {
        Collections.sort(userMessages);

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(this, userMessages, recyclerView);
        recyclerView.setAdapter(chatAdapter);

    }


    @OnClick(R.id.submit)
    public void submitchat() {
        String now;
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", locale);

        Calendar calendar = new GregorianCalendar();
        now = dateFormat.format(calendar.getTime());

        DatabaseReference submitcurrentChatUserDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentChatUserData.userUid).child("messages").child(userData.userUid).child(now);
        if (txt_submit_chat.getEditText().getText().toString().length() != 0) {
            submitcurrentChatUserDatabaseReference.child("message").setValue(txt_submit_chat.getEditText().getText().toString());
            submitcurrentChatUserDatabaseReference.child("read").setValue("unread");

            txt_submit_chat.getEditText().setText("");
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            layoutManager.scrollToPosition(userMessages.size() - 1);

            GotogetherNotificationManager gotogetherNotificationManager = new GotogetherNotificationManager(this);
            gotogetherNotificationManager.sendPersonChat(currentChatUserData.userUid, userData.displayname);
        }
    }
}
