package tr.edu.iyte.quickreply.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import tr.edu.iyte.quickreply.services.CallStopService;
import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.adapters.ReplyAdapter;

public class SelectReplyActivity extends Activity {

    private View mainL;
    private EditText newReplyText;
    private RelativeLayout addReplyLayout;
    private RelativeLayout newReplyLayout;
    private FrameLayout noReplies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_reply);

        mainL = findViewById(R.id.main_select_layout);
        noReplies = (FrameLayout) findViewById(R.id.no_replies);
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
                newReplyLayout.animate().alpha(1).setDuration(100).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        newReplyLayout.setVisibility(View.VISIBLE);
                        newReplyLayout.bringToFront();
                        mainL.requestLayout();
                        mainL.invalidate();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        addReplyLayout.setVisibility(View.GONE);
                    }
                }).start();
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
                if(reply.isEmpty()) {
                    newReplyText.setError(getString(R.string.no_reply));
                    newReplyText.requestFocus();
                    return;
                }
                resetNewReply();
                adapter.add(reply);
                if(QuickReplyTile.addReply(reply))
                    toggleNoReplies(true);
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
                String reply = (String)parent.getItemAtPosition(position);
                adapter.remove(reply);
                if(QuickReplyTile.removeReply(reply))
                    toggleNoReplies(false);
                return true;
            }
        });
    }

    private void resetNewReply() {
        newReplyText.setText("");
        newReplyText.setError(null);
        newReplyLayout.animate().alpha(0).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                addReplyLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                newReplyLayout.setVisibility(View.GONE);
                addReplyLayout.bringToFront();
                mainL.requestLayout();
                mainL.invalidate();
            }
        }).start();
    }

    private void toggleNoReplies(boolean isToggled) {
        if(!isToggled) {
            noReplies.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    noReplies.setVisibility(View.VISIBLE);
                }
            }).setDuration(100).start();
        } else {
            noReplies.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    noReplies.setVisibility(View.GONE);
                }
            }).setDuration(100).start();
        }
    }
}
