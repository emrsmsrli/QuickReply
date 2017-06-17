package tr.edu.iyte.quickreply.adapters;

import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.interfaces.ReplyItemTouchHelperAdapter;
import tr.edu.iyte.quickreply.interfaces.ViewHolderInteractionListener;
import tr.edu.iyte.quickreply.interfaces.OnReplyInteractedListener;
import tr.edu.iyte.quickreply.interfaces.OnStartDragListener;

public class ReplyAdapter
        extends RecyclerView.Adapter<ReplyAdapter.ViewHolder>
        implements ReplyItemTouchHelperAdapter {

    private final List<String> replies;
    private final OnStartDragListener sdListener;
    private final OnReplyInteractedListener riListener;

    public ReplyAdapter(List<String> replies, OnStartDragListener sdListener, OnReplyInteractedListener riListener) {
        super();
        this.replies = replies;
        this.sdListener = sdListener;
        this.riListener = riListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reply, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.textView.setText(replies.get(position));

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    sdListener.onStartDrag(holder);
                }
                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                riListener.onReplySelected(holder.textView.getText().toString());
            }
        });
    }

    @Override
    public void onReplyDismissed(int position) {
        remove(position);
        riListener.onReplyDismissed();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    public void add(String reply) {
        replies.add(reply);
        notifyItemInserted(replies.size() - 1);
    }

    private void remove(int idx) {
        QuickReplyTile.removeReply(replies.get(idx));
        replies.remove(idx);
        notifyItemRemoved(idx);
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder
            implements ViewHolderInteractionListener {
        private TextView textView;

        private ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.reply_text);
        }

        @Override
        public void onInteractionStart() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onInteractionEnd() {
            itemView.setBackgroundColor(0);
        }
    }
}
