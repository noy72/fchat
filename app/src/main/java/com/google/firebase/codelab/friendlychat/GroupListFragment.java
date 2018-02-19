package com.google.firebase.codelab.friendlychat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends Fragment {

    private View mView; // for findViewById
    private RecyclerView mRecyclerView;
    private Button mAddGroupButton;
    private FragmentManager mFragmentManager;

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


        mAddGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MakeNewGroupDialog dialog = new MakeNewGroupDialog();
                dialog.setTargetFragment(getParentFragment(), 100);
                dialog.show(getChildFragmentManager(), "tag");
            }
        });
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == 100) {
            if (resultCode == DialogInterface.BUTTON_POSITIVE) {
                Toast.makeText(getContext(), "pos", Toast.LENGTH_SHORT).show();
            } else if (resultCode == DialogInterface.BUTTON_NEGATIVE) {
                Toast.makeText(getContext(), "neg", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
