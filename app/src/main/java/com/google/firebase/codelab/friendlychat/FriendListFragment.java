package com.google.firebase.codelab.friendlychat;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
public class FriendListFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<String, FriendViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private Context mContext;
    private Button mSearchButton;
    private EditText mMailAddress;
    private Bundle mBundle;
    private HashMap<String, User> mUidUserIndex;
    private HashMap<String, User> mAddressUserIndex;

    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        mUidUserIndex = new HashMap<>();
        mAddressUserIndex = new HashMap<>();
        mProgressBar = view.findViewById(R.id.progressBar);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mContext = getContext();
        mBundle = getArguments();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mMailAddress = view.findViewById(R.id.mailAddress);
        //TODO: set Filter (or not)
        mMailAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mMailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSearchButton.setEnabled(true);
                } else {
                    mSearchButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSearchButton = view.findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Detect when the user has already registered in the friend list
                // TODO: Do not register oneself.

                String mailAddress = mMailAddress.getText().toString() + "@gmail.com";
                User user = mAddressUserIndex.get(mailAddress);
                if (user == null) {
                    Toast.makeText(getContext(), "Address: " + mailAddress + " not found.", Toast.LENGTH_SHORT).show();
                } else {
                    showRegistrationDialog(user);
                }
            }
        });

        initializeHashMap();
        initializeFirebaseAdapter(view);

        return view;
    }

    public void initializeHashMap() {
        DatabaseReference friendRef = mDatabaseReference.child(USER_CHILD);
        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    mUidUserIndex.put(data.getKey(), user);
                    mAddressUserIndex.put(user.getMail(), user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException("Failed to read: " + databaseError.getCode());
            }
        });
    }

    public void initializeFirebaseAdapter(View view) {
        DatabaseReference friendRef = mDatabaseReference.child(FRIENDS_CHILD).child(mBundle.getString("uid"));
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(friendRef, String.class).build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(FriendViewHolder viewHodler, int position, String uid) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                User user = mUidUserIndex.get(uid);
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
                FriendViewHolder viewHolder = new FriendViewHolder(inflater.inflate(R.layout.item_friend, viewGroup, false));

                viewHolder.setOnClickListener(new FriendViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO: implement click event
                        Toast.makeText(getActivity(), "click", Toast.LENGTH_SHORT).show();
                    }
                });

                return viewHolder;
            };
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

        mRecyclerView = view.findViewById(R.id.friendRecyclerView);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }

    public void showRegistrationDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you register " + user.getName() + " in the friend list?")
                .setTitle(R.string.friend_registration_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = getArguments();
                        String uid = bundle.getString("uid");
                        DatabaseReference newRef = mDatabaseReference
                                .child(FRIENDS_CHILD)
                                .child(uid)
                                .push();
                        newRef.setValue(user.getUid());

                        mMailAddress.getEditableText().clear();
                        Toast.makeText(getContext(), "add " + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
        builder.create().show();
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
