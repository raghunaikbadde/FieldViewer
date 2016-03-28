package com.jobviewer.exception;

import android.support.v7.app.AlertDialog;

/**
 * Created by e050027 on 10/29/2015.
 */
public abstract interface IDialogListener {
    public abstract void onPositiveButtonClick(AlertDialog dialog);

    public abstract void onNegativeButtonClick(AlertDialog dialog);
}
