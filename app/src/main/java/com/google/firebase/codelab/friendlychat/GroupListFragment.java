package com.google.firebase.codelab.friendlychat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends Fragment {

    private final int REQUEST_CODE = 100;
    private final String TAG = "GroupListFragment";
    private final String GROUP_CHILD = "groups";
    private final String JOIN_CHILD = "join";
    private final String MAX_GROUP_ID = "maxGroupId";

    private View mView; // for findViewById
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Group, GroupViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private Button mAddGroupButton;
    private FragmentManager mFragmentManager;
    private EditText mNewGroupName;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private String mUid;

    public GroupListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_group_list, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddGroupButton = mView.findViewById(R.id.addButton);
        mRecyclerView = mView.findViewById(R.id.groupRecyclerView);
        mNewGroupName = mView.findViewById(R.id.groupname);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mProgressBar = mView.findViewById(R.id.progressBar);
        setUid();

        mAddGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MakeNewGroupDialog dialog = new MakeNewGroupDialog();
                dialog.setTargetFragment(getParentFragment(), REQUEST_CODE);
                dialog.show(getChildFragmentManager(), TAG);
            }
        });

        mNewGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mAddGroupButton.setEnabled(true);
                } else {
                    mAddGroupButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // New Child entries
        SnapshotParser<Group> parser = new SnapshotParser<Group>() {
            @Override
            public Group parseSnapshot(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    // TODO: implement
                    // group.setId();
                }
                return group;
            }
        };

        DatabaseReference groupRef = mDatabaseReference.child(GROUP_CHILD);
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
                .setQuery(groupRef, parser)
                .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Group, GroupViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final GroupViewHolder viewHolder, int position, Group group) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (group.getName() != null) {
                    viewHolder.mGroupName.setText(group.getName());
                } else {
                    // TODO: implement
                }
            }

            @Override
            public GroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                GroupViewHolder viewHolder = new GroupViewHolder(inflater.inflate(R.layout.item_group, viewGroup, false));

                viewHolder.setOnClickListener(new GroupViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        System.out.println("onItemClick");
                        Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                    }
                });
                return viewHolder;
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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == DialogInterface.BUTTON_POSITIVE) {
                String groupName = mNewGroupName.getText().toString();
                mNewGroupName.getEditableText().clear();
                addGroupToDatabase(groupName);
                Toast.makeText(getContext(), "add " + groupName, Toast.LENGTH_SHORT).show();
            } else if (resultCode == DialogInterface.BUTTON_NEGATIVE) {
                Toast.makeText(getContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addGroupToDatabase(final String groupName) {
        mDatabaseReference.child(MAX_GROUP_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxGroupId = dataSnapshot.getValue(Long.class);

                // add new group
                mDatabaseReference
                        .child(GROUP_CHILD)
                        .push()
                        .setValue(new Group(maxGroupId, groupName, mUid));

                // add group to user's group list
                mDatabaseReference.child(JOIN_CHILD).child(mUid).push().setValue(maxGroupId);

                // increment maxGroupId
                mDatabaseReference.child(MAX_GROUP_ID).setValue(maxGroupId + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException("The read failed: " + databaseError.getCode());
            }
        });
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

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        private GroupViewHolder.ClickListener mClickListenr;

        TextView mGroupName;

        public interface ClickListener {
            public void onItemClick(View view, int position);
        }

        public GroupViewHolder(View itemView) {
            super(itemView);

            mGroupName = itemView.findViewById(R.id.groupNameView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListenr.onItemClick(view, getAdapterPosition());
                }
            });
        }

        public void setOnClickListener(GroupViewHolder.ClickListener clickListener) {
            mClickListenr = clickListener;
        }
    }
}
