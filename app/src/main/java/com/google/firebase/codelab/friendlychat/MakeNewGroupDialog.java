package com.google.firebase.codelab.friendlychat;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

// make a dialog callback to the fragment.
// https://qiita.com/kojionilk/items/9584c012679f61569995

public class MakeNewGroupDialog extends DialogFragment {

    String mGroupName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                dismiss();
                Intent intent = new Intent();
                if (getArguments() != null) {
                    intent.putExtras(getArguments());
                }
                getParentFragment().onActivityResult(getTargetRequestCode(), which, intent);
            }

        };

        // TODO: add the group name to the dialog
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.make_new_group)
                .setMessage(R.string.make_group_message)
                .setPositiveButton(R.string.ok, listener)
                .setNegativeButton(R.string.cancel, listener)
                .create();
    }
}
