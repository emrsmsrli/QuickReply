package tr.edu.iyte.quickreply;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class SelectReplyActivity extends Activity {

    private View mainL;
    private EditText newReplyText;
    private RelativeLayout addReplyLayout;
    private RelativeLayout newReplyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_reply);

        mainL = findViewById(R.id.main_select_layout);
        addReplyLayout = (RelativeLayout) findViewById(R.id.add_reply_layout); //with 2 buttons
        final Button addReply = (Button) addReplyLayout.findViewById(R.id.add_reply);
        final ImageButton cancelPickingReply = (ImageButton) addReplyLayout.findViewById(R.id.cancel);

        newReplyLayout = (RelativeLayout) findViewById(R.id.new_reply_layout); //with edit text and 2 buttons
        final ImageButton addWrittenReply = (ImageButton) newReplyLayout.findViewById(R.id.ok);
        final ImageButton cancelAddingReply = (ImageButton) newReplyLayout.findViewById(R.id.cancel_r);
        newReplyText = (EditText) newReplyLayout.findViewById(R.id.new_reply);

        final ListView list = (ListView) findViewById(R.id.reply_list);
        final ArrayList<String> replies = new ArrayList<>();
        final ReplyAdapter adapter = new ReplyAdapter(this, replies);

        addReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReplyLayout.setVisibility(View.GONE);
                newReplyLayout.setVisibility(View.VISIBLE);
                newReplyLayout.bringToFront();
                mainL.requestLayout();
                mainL.invalidate();
            }
        });

        cancelPickingReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addWrittenReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reply = newReplyText.getText().toString();
                if(reply.isEmpty())
                    return;
                resetNewReply();
                adapter.add(reply);
                QuickReplyTile.addReply(reply);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(addWrittenReply.getWindowToken(), 0);
            }
        });

        cancelAddingReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetNewReply();
            }
        });

        replies.addAll(QuickReplyTile.getReplies());
        list.setAdapter(adapter);
        setOnListItemSelected(list, adapter);
    }

    private void setOnListItemSelected(ListView list, final ReplyAdapter adapter) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QuickReplyTile.selectReply((String)parent.getItemAtPosition(position));
                startService(new Intent(SelectReplyActivity.this, CallStopService.class));
                finish();
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.remove((String)parent.getItemAtPosition(position));
                QuickReplyTile.removeReply((String)parent.getItemAtPosition(position));
                return false;
            }
        });
    }

    private void resetNewReply() {
        newReplyText.setText("");
        addReplyLayout.setVisibility(View.VISIBLE);
        newReplyLayout.setVisibility(View.GONE);
        addReplyLayout.bringToFront();
        mainL.requestLayout();
        mainL.invalidate();
    }
}
