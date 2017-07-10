package tr.edu.iyte.quickreply.adapters

import android.graphics.Color
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import tr.edu.iyte.quickreply.interfaces.ReplyItemTouchHelperAdapter
import org.jetbrains.anko.find
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.interfaces.OnReplyInteractedListener
import tr.edu.iyte.quickreply.interfaces.OnStartDragListener
import tr.edu.iyte.quickreply.interfaces.ViewHolderInteractionListener

class ReplyAdapter(private val replies: MutableList<String>,
                   private val startDragListener: OnStartDragListener,
                   private val replyInteractedListener: OnReplyInteractedListener) :
        RecyclerView.Adapter<ReplyAdapter.ViewHolder>(),
        ReplyItemTouchHelperAdapter {
    class ViewHolder(v: View) :
            RecyclerView.ViewHolder(v),
            ViewHolderInteractionListener {
        val textView: TextView = v.find<TextView>(R.id.reply_text)

        override fun onInteractionStart() = itemView.setBackgroundColor(Color.LTGRAY)
        override fun onInteractionEnd() = itemView.setBackgroundColor(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.reply, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.textView!!.text = replies[position]

        holder.itemView.setOnTouchListener { _, event ->
            if(MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                startDragListener.onStartDrag(holder)
            false
        }

        holder.itemView.setOnClickListener {
            replyInteractedListener.onReplySelected(holder.textView.text.toString())
        }
    }

    override fun onReplyDismissed(position: Int) {
        remove(replies[position])
        replyInteractedListener.onReplyDismissed()
    }

    fun add(reply: String) {
        replies.add(reply)
        notifyItemInserted(replies.size - 1)
    }

    fun remove(reply: String) {
        ReplyManager.removeReply(reply)
        val i = replies.indexOf(reply)
        replies.removeAt(i)
        notifyItemRemoved(i)
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount() = replies.size
}