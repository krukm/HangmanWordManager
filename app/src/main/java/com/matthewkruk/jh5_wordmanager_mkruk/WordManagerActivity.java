package com.matthewkruk.jh5_wordmanager_mkruk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import static com.matthewkruk.jh5_wordmanager_mkruk.MyAlertDialogHandler.DialogType;


public class WordManagerActivity extends ActionBarActivity
        implements AdapterView.OnItemClickListener, FileOperations {

    public enum WordOrder {SORTED, FRONT_INSERT, APPEND}

    ;

    private static final String EXTRA_WORDS = "words";
    ArrayList<String> myWordListArray = new ArrayList<String>();

    ArrayAdapter<String> arrayListAdapter;
    ListView myListView;
    WordFileManager wordFileManager = new WordFileManager(this);
    private String currentFileName;

    TextView fileNameDisplay;
    WordOrder wordOrder = WordOrder.SORTED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        myListView = (ListView) findViewById(R.id.myListView);

        arrayListAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myWordListArray);

        myListView.setAdapter(arrayListAdapter);

        // Not really going to use ListView OnClickListener, but it could be useful for other applications
        myListView.setOnItemClickListener(this);

        updateFromResource(R.array.countries_array);

        // Constructing the Inner class that handles the EditText box
        // Interesting question on why it's OK that this variable is not used, but why
        // aren't we worrying about the Garbage collector?  Hint: There is an EditText created that
        // has called OnKeyListener and passes this address.
        MyEditText myEditText = new MyEditText();

        registerForContextMenu(myListView);

        fileNameDisplay = (TextView) findViewById(R.id.file_name);
        String lastFileName = wordFileManager.getMyLastFileName();

        open(lastFileName);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //enum WordOrder {Sorted, FRONT_INSERT, APPEND};
            case R.id.insert_at_front:
                wordOrder = WordOrder.FRONT_INSERT;
                return true;
            case R.id.insert_at_end:
                wordOrder = WordOrder.APPEND;
                return true;
            case R.id.sort_words:
                wordOrder = WordOrder.SORTED;
                updateList();
                return true;
            case R.id.save:
                Log.d("Mine", "save option menu");
                saveList();
                return true;
            case R.id.open_file:
                Log.d("Mine", "open option menu");
                MyAlertDialogHandler dho = new MyAlertDialogHandler(this, wordFileManager,
                        "Select Category to Open, or hit Back button",
                        this, DialogType.OPEN);
                return true;
            case R.id.new_file:
                Log.d("Mine", "new option menu");
                NewFileDialogHandler nfdh = new NewFileDialogHandler(this, this);
                nfdh.show();
                return true;
            case R.id.delete_file:
                Log.d("Mine", "delete option menu");
                MyAlertDialogHandler dhd = new MyAlertDialogHandler(this, wordFileManager,
                        "Select Category to Delete, or hit Back button",
                        this, DialogType.DELETE);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void onPause() {
        super.onPause();
        saveList();
    }

    private void saveList() {
        Log.d("Mine", "saveList " + myWordListArray);
        ArrayList<String> arr = myWordListArray;
        wordFileManager.saveMyWords(arr, currentFileName);
        wordFileManager.storeMyFileName(currentFileName);
    }

    private void changeFileName(String fileName) {
        currentFileName = fileName;
        if (currentFileName == null)
            currentFileName = "countries.lst";
        fileNameDisplay.setText(currentFileName);
        wordFileManager.storeMyFileName(currentFileName);
    }


    @Override
    public void newFile(String fileName) {
        ArrayList<String> arr = new ArrayList<String>();
        updateFromArrayList(arr);
        changeFileName(fileName);
    }


    @Override
    public void open(String fileName) {
        changeFileName(fileName);
        ArrayList<String> arr = wordFileManager.getMyWords(currentFileName);
        updateFromArrayList(arr);
        wordFileManager.storeMyFileName(currentFileName);
    }

    @Override
    public void delete(String fileName) {
        wordFileManager.deleteFile(fileName);
        ArrayList<String> arr = new ArrayList<String>();
        updateFromArrayList(arr);
        changeFileName("");
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(R.string.word_context_menu_prompt);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.word_list_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        AdapterView.AdapterContextMenuInfo menuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int index = menuInfo.position;
        TextView text = (TextView) menuInfo.targetView;
        String value = text.getText().toString();


        switch (item.getItemId()) {
            case R.id.remove_word:
                removeWord(index);
                return true;

            case R.id.capitalize_word:
                replaceWord(index, value.toUpperCase());
                return true;

            case R.id.lower_case_word:
                replaceWord(index, value.toLowerCase());
                return true;

            case R.id.capitalize_first_letter:
                char c = value.charAt(0);
                value = Character.toUpperCase(c) + value.substring(1).toLowerCase();
                replaceWord(index, value);
                return true;

            case R.id.edit_word:
                EditWordDialogHandler ewdh = new EditWordDialogHandler(this, index,
                        value, this);
                ewdh.show();
                return true;

        }

        return false;
    }

    private void updateList() {
        if (wordOrder == WordOrder.SORTED)
            Collections.sort(myWordListArray);
        arrayListAdapter.notifyDataSetChanged();
    }

    public void updateFromResource(int resource) {
        String[] words = getResources().getStringArray(resource);
        myWordListArray.clear();
        for (int i = 0; i < words.length; i++)
            myWordListArray.add(words[i]);

        updateList();
    }

    public void updateFromArrayList(ArrayList<String> arr) {
        myWordListArray.clear();
        for (int i = 0; i < arr.size(); i++)
            myWordListArray.add(arr.get(i));

        updateList();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // GridView grid = (GridView) parent;
        TextView text = (TextView) view;
        String str = text.getText() + " pos=" + position + " id=" + id;
        Log.d("Mine", "onItemClick: " + str);
    }

    public void addWord(String word) {
        if (wordOrder == WordOrder.FRONT_INSERT)
            myWordListArray.add(0, word);
        else
            myWordListArray.add(word);
        updateList();
    }

    public void removeWord(int index) {
        myWordListArray.remove(index);
        updateList();
    }

    public void replaceWord(int index, String value) {
        myWordListArray.set(index, value);
        updateList();
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.return_category_button: {
                    Intent intent = getIntent();
                    Bundle extras = intent.getExtras();
                    extras.putStringArrayList(EXTRA_WORDS, myWordListArray);
                    setResult(Activity.RESULT_OK, intent);
                }
            }
        }
    };

    class MyEditText implements View.OnKeyListener {
        EditText myEdit;

        MyEditText() {
            myEdit = (EditText) findViewById(R.id.myEditText);
            myEdit.setOnKeyListener(MyEditText.this);
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    addWord(myEdit.getText().toString());
                    myEdit.setText("");
                    return true;
                }
            return false;
        }

    }

}
