package com.google.firebase.codelab.friendlychat;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.google.firebase.codelab.friendlychat.MainActivity.FRIENDS_CHILD;
import static com.google.firebase.codelab.friendlychat.MainActivity.USER_CHILD;


/**
 * A simple {@link Fragment} subclass.
 */
public class InviteFriendFragment extends Fragment {

    private TextView mTextView;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<String, FriendViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private FragmentManager mFragmentManager;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private String mUid;
    private HashMap<String, User> mUserIndex;
    private Context mContext;

    public InviteFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        mTextView = view.findViewById(R.id.inviteTextView);
        mRecyclerView = view.findViewById(R.id.inviteRecyclerView);
        mProgressBar = view.findViewById(R.id.progressBar);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mContext = getContext();
        mUserIndex = new HashMap<>();
        setUid();

        initializeHashMap();
        initializeFirebaseAdapter();

        return view;
    }

    public void initializeHashMap() {
        DatabaseReference friendRef = mDatabaseReference.child(USER_CHILD);
        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    String key = data.getKey();
                    mUserIndex.put(key, user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException("Failed to read: " + databaseError.getCode());
            }
        });
    }

    public void initializeFirebaseAdapter() {
        DatabaseReference friendRef = mDatabaseReference.child(FRIENDS_CHILD).child(mUid);
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(friendRef, String.class).build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(FriendViewHolder viewHodler, int position, String uid) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                User user = mUserIndex.get(uid);
                if (user.getName() != null) {
                    viewHodler.mFriendName.setText(user.getName());
                }

                if (user.getPhotoUrl() == null) {
                    viewHodler.mImageView
                            .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(mContext)
                            .load(user.getPhotoUrl())
                            .into(viewHodler.mImageView);
                }
            }

            @Override
            public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new FriendViewHolder(inflater.inflate(R.layout.item_friend, viewGroup, false));
            }
        };
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int groupCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                boolean extract = positionStart >= groupCount - 1 && lastVisiblePosition == positionStart - 1;
                if (lastVisiblePosition == -1 || extract) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }

    private void setUid() {
        Bundle bundle = getArguments();
        mUid = bundle.getString("uid");
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }
}
