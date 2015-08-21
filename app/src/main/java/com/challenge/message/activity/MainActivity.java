package com.challenge.message.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.challenge.message.R;
import com.challenge.message.data.MessageDataSource;
import com.challenge.message.model.Message;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int EDIT_REQUEST_CODE = 1000;
    private ListView mListView;
    private EditText mMessageEditText;
    private MessageDataSource mDataSource;
    private MessageAdapter mAdapter;
    private Button mSaveButton;
    private ActionMode mActionMode;
    private List<Message> mMessages;
    private int mCheckedPosition = -1;
    private ShareActionProvider mShareActionProvider;

    private View.OnClickListener mSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = mMessageEditText.getText().toString();
            if (!message.isEmpty()) {
                long id = mDataSource.addMessage(message);
                Message msg = mDataSource.getMessage(id);
                mMessages.add(msg);
                mAdapter.notifyDataSetChanged();
                mMessageEditText.setText("");
                scrollToBottom();
            }
        }
    };

    private View.OnClickListener mMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            TextView contentTextView = (TextView)v.findViewById(R.id.content_text_view);
            ClipData clip = ClipData.newPlainText("message", contentTextView.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    };

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);
            MenuItem item = menu.findItem(R.id.context_share);

            mShareActionProvider = new ShareActionProvider(MainActivity.this);
            Message message = mMessages.get(mCheckedPosition);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message.getContent());
            mShareActionProvider.setShareIntent(shareIntent);
            MenuItemCompat.setActionProvider(item, mShareActionProvider);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_delete:
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.msg_delete_confirm)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Message message = mMessages.get(mCheckedPosition);
                                    mDataSource.deleteMessage(message.getId());
                                    mMessages.remove(mCheckedPosition);
                                    mAdapter.notifyDataSetChanged();
                                    mActionMode.finish();
                                }

                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                    return true;

                case R.id.context_edit:
                    Message message = mMessages.get(mCheckedPosition);
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra(EditActivity.BUNDLE_ID, message.getId());
                    intent.putExtra(EditActivity.BUNDLE_CONTENT, message.getContent());
                    startActivityForResult(intent, EDIT_REQUEST_CODE);
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mCheckedPosition != -1)
            {
                mListView.setItemChecked(mCheckedPosition, false);
                mCheckedPosition = -1;
            }
            mActionMode = null;
        }
    };

    private View.OnLongClickListener mMessageLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mActionMode != null) {
                return false;
            }
            int position = mListView.getPositionForView(v);
            mListView.setItemChecked(position, true);
            mCheckedPosition = position;
            mActionMode = startSupportActionMode(mActionModeCallback);

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDataSource = new MessageDataSource(this);
        try {
            mDataSource.open();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        mMessages = new ArrayList<>();
        bindUIElement();
        setUpListeners();
        setUpListView();
        GetAllMessagesAsyncTask task =  new GetAllMessagesAsyncTask();
        task.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataSource.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                long id = data.getLongExtra(EditActivity.BUNDLE_ID, 0);
                Message msg = mDataSource.getMessage(id);
                mMessages.get(mCheckedPosition).setContent(msg.getContent());
                mMessages.get(mCheckedPosition).setUpdatedAt(msg.getUpdatedAt());
                mAdapter.notifyDataSetChanged();
            }
            mActionMode.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindUIElement() {
        mListView = (ListView)findViewById(R.id.list_view);
        mMessageEditText = (EditText)findViewById(R.id.message_edit_text);
        mSaveButton = (Button)findViewById(R.id.save_button);
    }

    private void setUpListeners() {
        mSaveButton.setOnClickListener(mSaveClickListener);
    }

    private void setUpListView() {
        mAdapter = new MessageAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    private void scrollToBottom() {
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mAdapter.getCount() - 1);
            }
        }, 200);
    }

    private class MessageAdapter extends BaseAdapter {
        private Context context;

        public MessageAdapter(Context context) {
            this.context = context;
        }
        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            if (mMessages.size() > position && position >= 0)
                return mMessages.get(position);
            else
                return null;
        }

        @Override
        public long getItemId(int position) {
            if (mMessages.size() > position && position >= 0)
                return mMessages.get(position).getId();
            else
                return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.view_message_item, parent, false);

                holder = new ViewHolder();
                holder.messageRelativeLayout = (RelativeLayout)convertView.findViewById(R.id.message_relative_layout);
                holder.contentTextView = (TextView)convertView.findViewById(R.id.content_text_view);
                holder.updatedAtTextView = (TextView)convertView.findViewById(R.id.updated_at_text_view);

                holder.messageRelativeLayout.setOnClickListener(mMessageClickListener);
                holder.messageRelativeLayout.setOnLongClickListener(mMessageLongClickListener);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Message message = mMessages.get(position)  ;
            holder.contentTextView.setText(message.getContent());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            holder.updatedAtTextView.setText(sdf.format(message.getUpdatedAt()));
            return convertView;
        }

        private class ViewHolder {
            RelativeLayout messageRelativeLayout;
            TextView contentTextView;
            TextView updatedAtTextView;
        }
    }

    private class GetAllMessagesAsyncTask extends AsyncTask<Void, Void, List<Message>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            return mDataSource.getMessages();
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            mMessages = messages;
            mAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }
}
