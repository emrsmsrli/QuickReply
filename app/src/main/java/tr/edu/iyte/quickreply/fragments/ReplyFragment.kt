package tr.edu.iyte.quickreply.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_reply.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.support.v4.toast
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.adapters.ReplyAdapter
import tr.edu.iyte.quickreply.helper.Constants
import tr.edu.iyte.quickreply.helper.ReplyItemTouchHelperCallback
import tr.edu.iyte.quickreply.interfaces.OnReplyInteractedListener
import tr.edu.iyte.quickreply.interfaces.OnStartDragListener
import tr.edu.iyte.quickreply.services.CallStopService

class ReplyFragment : Fragment(),
        OnStartDragListener,
        OnReplyInteractedListener,
        AnkoLogger {
    private val adapter = ReplyAdapter(mutableListOf(), this, this)
    private val callback = ReplyItemTouchHelperCallback(adapter)
    private val touchHelper = ItemTouchHelper(callback)

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?)
        = inflater?.inflate(R.layout.fragment_reply, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        touchHelper.attachToRecyclerView(reply_list)
        reply_list.layoutManager = LinearLayoutManager(act)
        val decoration = DividerItemDecoration(act, LinearLayout.VERTICAL)
        decoration.setDrawable(ContextCompat.getDrawable(act, R.drawable.line_divider))
        reply_list.addItemDecoration(decoration)
        reply_list.adapter = adapter
        adapter.addAll(ReplyManager.replies)

        add_reply_button.setOnClickListener {
            val bottomSheetDialogFragment = NewReplyFragment()
            bottomSheetDialogFragment.show((act as AppCompatActivity)
                    .supportFragmentManager, Constants.BOTTOM_SHEET_DIALOG_TAG)
        }

        if(ReplyManager.hasNoReply()) {
            no_replies.alpha = 1f
            no_replies.visibility = View.VISIBLE
        }
    }

    override fun onReplySelected(reply: String) {
        ReplyManager.currentReply = reply
        startService<CallStopService>()
        toast("${getString(R.string.qr_started)}: $reply")
        act.finish()
    }

    override fun onReplyDismissed() {
        if(ReplyManager.hasNoReply())
            toggleNoReplies(true)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
            = touchHelper.startDrag(viewHolder)

    fun addReply(reply: String) {
        if(ReplyManager.hasNoReply())
            toggleNoReplies(false)
        ReplyManager.addReply(reply)
        adapter.add(reply)
    }

    private fun toggleNoReplies(show: Boolean) {
        if(show) {
            no_replies.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    no_replies.visibility = View.VISIBLE
                }
            }).apply { duration = Constants.LAYOUT_CHANGE_DURATION }.start()
        } else {
            no_replies.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    no_replies.visibility = View.GONE
                }
            }).apply { duration = Constants.LAYOUT_CHANGE_DURATION }.start()
        }
    }
}