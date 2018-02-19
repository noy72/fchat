package com.google.firebase.codelab.friendlychat;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MakeNewGroupDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dismiss();
                Intent intent = new Intent();
                if (getArguments() != null) {
                    // add args for dialog execution to intent.
                    intent.putExtras(getArguments());
                }
                getParentFragment().onActivityResult(getTargetRequestCode(), which, intent);
            }

        };
        return new AlertDialog.Builder(getActivity())
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", listener)
                .create();
    }
}
