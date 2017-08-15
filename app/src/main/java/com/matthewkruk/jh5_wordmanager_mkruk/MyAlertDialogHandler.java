package com.matthewkruk.jh5_wordmanager_mkruk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class MyAlertDialogHandler implements DialogInterface.OnClickListener {
    public enum DialogType {
        OPEN,
        DELETE
    }

    private Context context;
    private String[] fileList;
    private DialogType dialogType;
    private WordFileManager wordFileManager;
    private FileOperations fileOperations;

    public MyAlertDialogHandler(Context context, WordFileManager wordFileManager, String title,
                                FileOperations fileOperations, DialogType dialogType) {

        this.context = context;
        this.dialogType = dialogType;
        this.wordFileManager = wordFileManager;
        this.fileOperations = fileOperations;

        fileList = wordFileManager.getFileList();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setItems(fileList, this);
        alertDialog.show();
    }

    public void onClick(DialogInterface dialoginterface, int index) {
        final String fileName = fileList[index];
        switch (dialogType) {
            case DELETE: {
                fileOperations.delete(fileName);
                break;
            }
            case OPEN: {
                fileOperations.open(fileName);
                break;
            }
        }
    }
}
