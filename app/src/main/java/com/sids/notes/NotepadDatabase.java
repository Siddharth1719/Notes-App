package com.sids.notes;


import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;


@androidx.room.Database(entities = NoteEntity.class, version = 1, exportSchema = false)
public abstract class NotepadDatabase extends RoomDatabase {
    private static NotepadDatabase notepadDatabase;

    public static synchronized NotepadDatabase getNoteDatabase(Context context) {

        if (notepadDatabase == null) {
            notepadDatabase = Room.databaseBuilder(context, NotepadDatabase.class, "noteDatabase").build();
        }

        return notepadDatabase;
    }

    public abstract NotepadDao notepadDao();
}