package com.sids.notes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    public static final int addNoteCode = 1;
    public static final int updateNoteCode = 2;
    public static final int displayNotesCode = 3;

    private RecyclerView noteRecycler;
    private List<NoteEntity> noteList;
    private NoteAdapter noteAdapter;
    private int clickedNotePosition = -1;
    public int longClickedNotePosition = -2;
    public int noteCounter;

    private AlertDialog deleteNoteDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_notes);

        FloatingActionButton addNoteButton = findViewById(R.id.buttonAddNote);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), NoteEditorActivity.class), addNoteCode);
            }
        });

        noteRecycler = findViewById(R.id.noteRecycler);
        noteRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        noteRecycler.setAdapter(noteAdapter);

        getNote(displayNotesCode);

        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public void noteClicked(NoteEntity noteEntity, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("isNoteUpdated", true);
        intent.putExtra("note", noteEntity);
        startActivityForResult(intent, updateNoteCode);
    }

    @Override
    public void noteLongClicked(NoteEntity noteEntity, int position) {
        longClickedNotePosition = position;
        deleteDialog(noteEntity, longClickedNotePosition);
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private void deleteDialog(NoteEntity noteEntity, int position) {
        if (deleteNoteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.delete_layout, (ViewGroup) findViewById(R.id.layoutDeleteNoteBox));

            builder.setView(view);
            deleteNoteDialog = builder.create();

            if (deleteNoteDialog.getWindow() != null) {
                deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            deleteNoteDialog.setCancelable(false);
            deleteNoteDialog.setCanceledOnTouchOutside(false);

            view.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AsyncTask.execute(() -> NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().deleteNote(noteEntity));
                    noteList.remove(position);
                    noteCounter--;
                    noteCounterSetLabel();
                    noteAdapter.notifyDataSetChanged();
                    deleteNoteDialog.dismiss();
                    reloadActivity();
                }
            });

            view.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNoteDialog.dismiss();
                    reloadActivity();
                }
            });
        }

        deleteNoteDialog.show();
        deleteNoteDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void deleteNote(NoteEntity noteEntity, int position) {

        @SuppressLint("StaticFieldLeak")
        class DeleteNoteClass extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().deleteNote(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                noteList.remove(position);
                noteCounter--;
                noteCounterSetLabel();
                noteAdapter.notifyDataSetChanged();
            }
        }

        new DeleteNoteClass().execute();
    }

    public void reloadActivity() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
    public void noteCounterSetLabel() {
        TextView noteCountView = findViewById(R.id.noteCount);
        noteCountView.setText(noteCounter + " notes");
    }

    private void getNote(final int request) {

        @SuppressLint("StaticFieldLeak")
        class SaveNoteClass extends AsyncTask<Void, Void, List<NoteEntity>> {
            @Override
            protected List<NoteEntity> doInBackground(Void... voids) {
                return NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<NoteEntity> noteEntities) {
                super.onPostExecute(noteEntities);

                switch (request) {
                    case displayNotesCode: {
                        noteList.addAll(noteEntities);
                        noteAdapter.notifyDataSetChanged();
                        noteCounter = noteList.size();

                        noteCounterSetLabel();
                        break;
                    }

                    case addNoteCode: {
                        noteList.add(0, noteEntities.get(0));
                        noteAdapter.notifyItemInserted(0);

                        noteRecycler.getLayoutManager().scrollToPosition(0);

                        noteCounter++;
                        noteCounterSetLabel();
                        reloadActivity();
                        break;
                    }

                    case updateNoteCode: {
                        noteList.remove(clickedNotePosition);
                        noteList.add(clickedNotePosition, noteEntities.get(clickedNotePosition));
                        noteAdapter.notifyItemChanged(clickedNotePosition);
                        reloadActivity();
                        break;
                    }
                }
            }
        }

        new SaveNoteClass().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == addNoteCode && resultCode == RESULT_OK) {
            getNote(addNoteCode);
        }
        else if (requestCode == updateNoteCode && resultCode == RESULT_OK) {
            if (data != null) {
                getNote(updateNoteCode);
            }
        }
    }
}