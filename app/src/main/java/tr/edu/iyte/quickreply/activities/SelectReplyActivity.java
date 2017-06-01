package tr.edu.iyte.quickreply.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import tr.edu.iyte.quickreply.services.CallStopService;
import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.adapters.ReplyAdapter;

public class SelectReplyActivity extends Activity {
    private View mainL;
    private EditText newReplyText;
    private View addReplyLayout;
    private View newReplyLayout;
    private View noReplies;

    private ReplyAdapter adapter;

    private boolean isItemTapped = false;
    private boolean isSwiping = false;

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

        final ListView list = (ListView) findViewById(R.id.reply_list);
        final ArrayList<String> replies = new ArrayList<>();

        final View.OnTouchListener listener = new View.OnTouchListener() {
            private float startingX;
            private int swipeSlop = -1; //  ->  distance in pixels which a
                                        //      continuous touches is considered a scroll

            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                if(swipeSlop < 0)
                    swipeSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();

                switch(event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        if(isItemTapped)
                            return false;

                        isItemTapped = true;
                        startingX = event.getX();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float x = event.getX() + v.getTranslationX();
                        float deltax = x - startingX;
                        float deltaxabs = Math.abs(deltax);
                        if(!isSwiping && deltaxabs > swipeSlop) {
                            isSwiping = true;
                            list.requestDisallowInterceptTouchEvent(true);
                        }
                        if(isSwiping)
                            v.setTranslationX(x - startingX);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if(isSwiping) {
                            float x = event.getX() + v.getTranslationX();
                            float deltaX = x - startingX;
                            float deltaXAbs = Math.abs(deltaX);
                            float fractionCovered;
                            float endX;
                            final boolean remove;

                            // determine whether remove the item or not
                            // 1/4 is enough to delete
                            if(deltaXAbs > v.getWidth() / 4) {
                                fractionCovered = deltaXAbs / v.getWidth();
                                endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                                remove = true;
                            } else {
                                fractionCovered = 1 - deltaXAbs / v.getWidth();
                                endX = 0;
                                remove = false;
                            }

                            long duration = (int) ((1 - fractionCovered) * 200);
                            list.setEnabled(true);
                            v.animate().setDuration(duration).translationX(endX).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    v.setTranslationX(0);
                                    if(remove) {
                                        // TODO animate removal
                                        removeReply(list.getPositionForView(v));
                                    } else {
                                        isSwiping = false;
                                        list.setEnabled(true);
                                    }
                                }
                            }).start();
                        } else
                            onReplySelected(adapter.getItem(list.getPositionForView(v)));
                    }
                    case MotionEvent.ACTION_CANCEL: {
                        v.setTranslationX(0);
                        isItemTapped = false;
                        break;
                    }
                    default:
                        return false;
                }

                return true;
            }
        };

        adapter = new ReplyAdapter(this, listener, replies);

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

    public void onReplySelected(String reply) {
        QuickReplyTile.selectReply(reply);
        startService(new Intent(SelectReplyActivity.this, CallStopService.class));
        finish();
    }

    private void removeReply(int idx) {
        adapter.remove(idx);
        if(QuickReplyTile.hasNoReply())
            toggleNoReplies(false);
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
