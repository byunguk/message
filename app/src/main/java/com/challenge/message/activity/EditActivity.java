package com.challenge.message.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.challenge.message.R;
import com.challenge.message.data.MessageDataSource;

import java.sql.SQLException;

/**
 * Created by kbw815 on 8/18/15.
 */
public class EditActivity extends AppCompatActivity {
    public final static String BUNDLE_ID = "bundle_id";
    public final static String BUNDLE_CONTENT = "bundle_content";
    private long mId;
    private EditText mMessageEditText;
    private Button mUpdateButton;
    private MessageDataSource mDataSource;

    private View.OnClickListener mUpdateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = mMessageEditText.getText().toString();
            if (!message.isEmpty()) {
                mDataSource.updateMessage(mId, message);
                Intent data = new Intent();
                data.putExtra(BUNDLE_ID, mId);
                setResult(RESULT_OK, data);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mId = getIntent().getLongExtra(BUNDLE_ID, 0);
        mDataSource = new MessageDataSource(this);
        try {
            mDataSource.open();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        bindUIElements();
        setUpListeners();
        mMessageEditText.setText(getIntent().getStringExtra(BUNDLE_CONTENT));
        mMessageEditText.setSelection(mMessageEditText.getText().length());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataSource.close();
    }

    private void bindUIElements() {
        mMessageEditText = (EditText)findViewById(R.id.message_edit_text);
        mUpdateButton = (Button)findViewById(R.id.update_button);
    }

    private void setUpListeners() {
        mUpdateButton.setOnClickListener(mUpdateClickListener);
    }
}
