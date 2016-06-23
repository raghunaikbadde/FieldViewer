package com.jobviewer.exception;

import android.support.v7.app.AlertDialog;

/**
 * Created by e050027 on 10/29/2015.
 */
public interface IDialogListener {
    void onPositiveButtonClick(AlertDialog dialog);

    void onNegativeButtonClick(AlertDialog dialog);
}
