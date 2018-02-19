package com.google.firebase.codelab.friendlychat;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.google.firebase.codelab.friendlychat.MainActivity.USER_CHILD;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendListFragment extends Fragment {
    private final String FRIENDS_CHILD = "friends";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private RecyclerView mMessageRecyclerView;
    private Button mSearchButton;
    private EditText mMailAddress;
    private View mView; // view for findViewById

    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_friend_list, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mMailAddress = mView.findViewById(R.id.mailAddress);
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

        mSearchButton = mView.findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String mailAddress = mMailAddress.getText().toString() + "@gmail.com";
                DatabaseReference ref = mFirebaseDatabase.getReference();
                System.out.println(mailAddress);

                ref.child(USER_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // TODO: Linear search is slow. There should be other good implementations.
                        // TODO: Detect when the user has already registered in the friend list
                        // TODO: Do not register oneself.
                        for (DataSnapshot data: dataSnapshot.getChildren()){
                            User user = data.getValue(User.class);
                            System.out.println(user.getMail());
                            if (mailAddress.equals(user.getMail())) {
                                showRegistrationDialog(user);
                                return;
                            }
                        }
                        Toast.makeText(getContext(), "Address: " + mailAddress + " not found.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        throw new RuntimeException("The read failed: " + databaseError.getCode());
                    }
                });
            }
        });
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

}
