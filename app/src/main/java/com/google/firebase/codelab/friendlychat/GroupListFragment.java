package com.google.firebase.codelab.friendlychat;


import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.google.firebase.codelab.friendlychat.MainActivity.BUNDLE_UID_KEY;
import static com.google.firebase.codelab.friendlychat.MainActivity.GROUP_CHILD;
import static com.google.firebase.codelab.friendlychat.MainActivity.JOIN_CHILD;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends Fragment {

    private final int REQUEST_CODE = 100;
    private final String TAG = "GroupListFragment";

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Group, GroupViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private Button mAddGroupButton;
    private FragmentManager mFragmentManager;
    private EditText mNewGroupName;
    private DatabaseReference mDatabaseReference;
    private GroupListListener mListener;
    private Fragment mFragment;
    private Bundle mBundle;

    public GroupListFragment() {
        // Required empty public constructor
    }

    public interface GroupListListener {
        public void onClickMakeGroupButton(Group Group);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        mAddGroupButton = view.findViewById(R.id.addButton);
        mRecyclerView = view.findViewById(R.id.groupRecyclerView);
        mNewGroupName = view.findViewById(R.id.groupname);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mProgressBar = view.findViewById(R.id.progressBar);
        mFragmentManager = getFragmentManager();
        mFragment = this;
        mBundle = getArguments();

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
        initializeFirebaseAdapter();


        return view;
    }

    private void initializeFirebaseAdapter() {
        DatabaseReference groupRef = mDatabaseReference.child(GROUP_CHILD);
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
                .setQuery(groupRef, Group.class)
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
                        Group group = mFirebaseAdapter.getItem(position);
                        if (mListener != null) {
                            mListener.onClickMakeGroupButton(group);
                            mFragmentManager.beginTransaction().remove(mFragment).commit();
                        }
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
        mDatabaseReference.child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // add new group
                DatabaseReference ref = mDatabaseReference
                        .child(GROUP_CHILD)
                        .push();
                ref.setValue(new Group(ref.getKey(), groupName));
                ref.child("user").push().setValue(mBundle.getString(BUNDLE_UID_KEY));

                // add group to user's group list
                mDatabaseReference
                        .child(JOIN_CHILD)
                        .child(mBundle.getString(BUNDLE_UID_KEY))
                        .push().setValue(ref.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException("The read failed: " + databaseError.getCode());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GroupListListener) {
            mListener = (GroupListListener) context;
        }
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        private GroupViewHolder.ClickListener mClickListenr;

        TextView mGroupName;

        public interface ClickListener {
            public void onItemClick(View view, int position);
        }

        public GroupViewHolder(View iteview) {
            super(iteview);

            mGroupName = iteview.findViewById(R.id.groupNameView);
            iteview.setOnClickListener(new View.OnClickListener() {
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
