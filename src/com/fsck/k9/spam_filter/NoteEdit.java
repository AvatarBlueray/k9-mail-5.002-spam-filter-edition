/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fsck.k9.spam_filter;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.fsck.k9.K9;
import com.fsck.k9.R;

public class NoteEdit extends Activity {

    private EditText mTitleText;
    private EditText mFromText;
    private EditText mSubjText;
    private CheckBox mHideCb;
    private CheckBox mDelCb;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createHelper();

        setContentView(R.layout.note_edit);
        setTitle(R.string.spam_filter_edit_filter);

        mTitleText = (EditText) findViewById(R.id.title);
        mFromText = (EditText) findViewById(R.id.from);
        mSubjText = (EditText) findViewById(R.id.subj);

        mHideCb = (CheckBox) findViewById(R.id.hide);
        mDelCb = (CheckBox) findViewById(R.id.del);


        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button donteButton = (Button) findViewById(R.id.donate);


        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
									: null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });

        donteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                android.content.ClipboardManager clipboardManager =
                        (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("donate for k9-mail spam filter edition", "1hRm41a1roEBHS7uxF9auvfekUYHvFctF");
                clipboardManager.setPrimaryClip(clip);
            }

        });
    }

    private void createHelper(){
        if (mDbHelper == null){
            mDbHelper = new NotesDbAdapter(this);
            mDbHelper.open();
        }
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));

            mFromText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_FROM)));
            mSubjText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_SUBJ)));



            mHideCb.setChecked(note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_HIDE)) != 0);
            mDelCb.setChecked(note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DEL)) != 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        createHelper();


        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();

        String from = mFromText.getText().toString();
        String subj = mSubjText.getText().toString();

        long hide = mHideCb.isChecked()? 1:0;
        long del = mDelCb.isChecked()? 1:0;

        if (mRowId == null) {
            long id = mDbHelper.createNote(title,from,subj,hide,del);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title,from,subj,hide,del);
        }

        mDbHelper.close();
        mDbHelper = null;

    }

}
