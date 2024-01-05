package com.sids.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteEditorActivity extends AppCompatActivity {

    private EditText inputTitle;
    private EditText inputNote;
    private NoteEntity noteAlreadyExists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        inputTitle = findViewById(R.id.noteTitle);
        inputNote = findViewById(R.id.noteBody);


        ImageView buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                closeKeyboard();
            }
        });

        TextView saveNoteButton = findViewById(R.id.buttonSave);
        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
                closeKeyboard();
            }
        });

        if (getIntent().getBooleanExtra("isNoteUpdated", false)) {
            noteAlreadyExists = (NoteEntity)getIntent().getSerializableExtra("note");
            setNoteUpdate();
        }
    }

    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setNoteUpdate() {
        inputTitle.setText(noteAlreadyExists.getNoteTitle());
        inputNote.setText(noteAlreadyExists.getNoteBody());
    }

    private void saveNote() {
        if (inputTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (inputNote.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        final NoteEntity noteEntity = new NoteEntity();
        noteEntity.setNoteTitle(inputTitle.getText().toString());
        noteEntity.setNoteBody(inputNote.getText().toString());
        if (noteAlreadyExists != null) {
            noteEntity.setNoteID(noteAlreadyExists.getNoteID());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteClass extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().createNote(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteClass().execute();
    }
}