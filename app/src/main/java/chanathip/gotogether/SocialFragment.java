package chanathip.gotogether;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by neetc on 4/5/2017.
 */

public class SocialFragment extends Fragment {
    private Context context;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SocialAdapter socialAdapter;
    private List<SocialData> friendUserdatas;
    private List<SocialData> socialDatas;
    private List<SocialData> groupDatas;
    private UserData currentUserData;

    public static SocialFragment newInstance() {
        SocialFragment fragment = new SocialFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        socialAdapter = new SocialAdapter(context, socialDatas, recyclerView);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(socialAdapter);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserData.userUid = firebaseUser.getUid();
        currentUserData.email = firebaseUser.getEmail();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendUserdatas = new ArrayList<>();
        socialDatas = new ArrayList<>();
        currentUserData = new UserData();
        groupDatas = new ArrayList<>();
    }

    @Override
    public void onStart() {
        super.onStart();
        friendUserdatas.clear();
        DatabaseReference currentuserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserData.userUid);
        currentuserDatabaseReference.keepSynced(true);
        currentuserDatabaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUserData.setData(
                                currentUserData.userUid,
                                dataSnapshot.child("display name").getValue().toString(),
                                dataSnapshot.child("email").getValue().toString()
                        );

                        //get group list
                        //check group
                        groupDatas.clear();
                        Map<String, String> groupUserdataMap = (Map<String, String>) dataSnapshot.child("group").getValue();
                        if (groupUserdataMap != null) {
                            for (HashMap.Entry<String, String> entry : groupUserdataMap.entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();

                                final SocialData groupData = new SocialData();
                                final String GroupUid = key;
                                final String Rank = value;

                                DatabaseReference groupdatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups").child(key);
                                groupdatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        groupData.groupData = new GroupData();
                                        groupData.groupData.setData(
                                                GroupUid,
                                                String.valueOf(dataSnapshot.child("name").getValue()),
                                                String.valueOf(dataSnapshot.child("description").getValue()),
                                                Rank,
                                                String.valueOf(dataSnapshot.child("settingpoint").getValue()),
                                                String.valueOf(dataSnapshot.child("membercount").getValue()),
                                                currentUserData.userUid
                                        );
                                        groupData.Type = "Group";
                                        groupData.CurrentuserUid = currentUserData.userUid;
                                        groupData.CurrentuserDisplayname = currentUserData.displayname;

                                        groupDatas.add(groupData);

                                        updateUI();


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        //get friend list
                        friendUserdatas.clear();
                        Map<String, String> friendUserdataMap = (Map<String, String>) dataSnapshot.child("friend").getValue();
                        if (friendUserdataMap != null) {
                            for (HashMap.Entry<String, String> entry : friendUserdataMap.entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();

                                if (value.equals("true")) {
                                    final UserData friendUserdata = new UserData();
                                    final String Uid = key;

                                    DatabaseReference FriendDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(key);
                                    FriendDatabaseReference
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    friendUserdata.userUid = Uid;
                                                    friendUserdata.displayname = dataSnapshot.child("display name").getValue().toString();
                                                    friendUserdata.email = dataSnapshot.child("email").getValue().toString();

                                                    DatabaseReference checkUnreadDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserData.userUid)
                                                            .child("messages").child(Uid);
                                                    checkUnreadDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Map<String, Object> massageMap = (Map<String, Object>) dataSnapshot.getValue();
                                                            if (massageMap != null) {
                                                                int unreadcount = 0;
                                                                for (HashMap.Entry<String, Object> entry : massageMap.entrySet()) {
                                                                    String key2 = entry.getKey();
                                                                    Map<String, String> value2 = (Map<String, String>) entry.getValue();

                                                                    if (value2.get("read").equals("unread")) {
                                                                        unreadcount = unreadcount + 1;
                                                                    }
                                                                }

                                                                friendUserdata.unreadMassage = unreadcount;
                                                            } else {
                                                                friendUserdata.unreadMassage = 0;
                                                            }

                                                            SocialData friendSocialData = new SocialData();
                                                            friendSocialData.userData = friendUserdata;
                                                            friendSocialData.Type = "FriendList";
                                                            friendSocialData.CurrentuserUid = currentUserData.userUid;
                                                            friendSocialData.CurrentuserDisplayname = currentUserData.displayname;

                                                            friendUserdatas.add(friendSocialData);
                                                            updateUI();
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                }
                            }
                        }

                        updateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }

        );
    }

    private void updateUI() {
        socialDatas.clear();

        SocialData socialData = new SocialData();

        if(!groupDatas.isEmpty()){

            socialData.Type = "Title";
            socialData.titlename = "group";
            socialDatas.add(socialData);
            socialDatas.addAll(groupDatas);
        }

        if(!friendUserdatas.isEmpty()){
            socialData = new SocialData();
            socialData.Type = "Title";
            socialData.titlename = "Friend";
            socialDatas.add(socialData);
            socialDatas.addAll(friendUserdatas);

        }

        socialAdapter.notifyDataSetChanged();
    }
}