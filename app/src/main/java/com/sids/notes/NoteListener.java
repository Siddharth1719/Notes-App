package com.sids.notes;

public interface NoteListener {
    void noteClicked(NoteEntity noteEntity, int position);

    void noteLongClicked(NoteEntity noteEntity, int position);
}