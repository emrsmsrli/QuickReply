package tr.edu.iyte.quickreply.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

import tr.edu.iyte.quickreply.adapters.ReplyAdapter;
import tr.edu.iyte.quickreply.helper.ReplyItemTouchHelperCallback;
import tr.edu.iyte.quickreply.interfaces.OnReplyInteractedListener;
import tr.edu.iyte.quickreply.interfaces.OnStartDragListener;
import tr.edu.iyte.quickreply.services.CallStopService;
import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;

public class SelectReplyActivity
        extends Activity
        implements OnStartDragListener, OnReplyInteractedListener {
    private static final int LAYOUT_CHANGE_DURATION = 100;

    private View mainL;
    private EditText newReplyText;
    private View addReplyLayout;
    private View newReplyLayout;
    private View noReplies;

    private ReplyAdapter adapter;
    private ItemTouchHelper touchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_reply);

        mainL = findViewById(R.id.main_select_layout);
        noReplies = findViewById(R.id.no_replies);
        addReplyLayout = findViewById(R.id.add_reply_layout); //with 2 buttons
        final Button addReply = (Button) addReplyLayout.findViewById(R.id.add_reply);
        final ImageButton cancelPickingReply = (ImageButton) addReplyLayout.findViewById(R.id.cancel);

        newReplyLayout = findViewById(R.id.new_reply_layout); //with edit text and 2 buttons
        final ImageButton addWrittenReply = (ImageButton) newReplyLayout.findViewById(R.id.ok);
        final ImageButton cancelAddingReply = (ImageButton) newReplyLayout.findViewById(R.id.cancel_r);
        newReplyText = (EditText) newReplyLayout.findViewById(R.id.new_reply);

        final RecyclerView list = (RecyclerView) findViewById(R.id.reply_list);
        final ArrayList<String> replies = new ArrayList<>();

        adapter = new ReplyAdapter(replies, this, this);
        ItemTouchHelper.Callback callback = new ReplyItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(list);

        addReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReplyLayout.animate().alpha(1).setDuration(LAYOUT_CHANGE_DURATION).setListener(new AnimatorListenerAdapter() {
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
                if(reply.trim().isEmpty()) {
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
        list.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration = new DividerItemDecoration(this, LinearLayout.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        list.addItemDecoration(decoration);
        list.setAdapter(adapter);

        if(QuickReplyTile.hasNoReply()) {
            noReplies.setAlpha(1);
            noReplies.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if(newReplyLayout.getVisibility() == View.VISIBLE)
            resetNewReply();
        else
            super.onBackPressed();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onReplySelected(String reply) {
        QuickReplyTile.selectReply(reply);
        startService(new Intent(SelectReplyActivity.this, CallStopService.class));
        finish();
    }

    @Override
    public void onReplyDismissed() {
        if(QuickReplyTile.hasNoReply())
            toggleNoReplies(false);
    }

    private void resetNewReply() {
        newReplyText.setText("");
        newReplyText.setError(null);
        newReplyLayout.animate().alpha(0).setDuration(LAYOUT_CHANGE_DURATION).setListener(new AnimatorListenerAdapter() {
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
            }).setDuration(LAYOUT_CHANGE_DURATION).start();
        } else {
            noReplies.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    noReplies.setVisibility(View.GONE);
                }
            }).setDuration(LAYOUT_CHANGE_DURATION).start();
        }
    }
}
