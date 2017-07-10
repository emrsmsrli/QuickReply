package tr.edu.iyte.quickreply.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import tr.edu.iyte.quickreply.interfaces.OnReplyInteractedListener
import tr.edu.iyte.quickreply.interfaces.OnStartDragListener
import kotlinx.android.synthetic.main.activity_select_reply.*
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.adapters.ReplyAdapter
import tr.edu.iyte.quickreply.helper.ReplyItemTouchHelperCallback
import tr.edu.iyte.quickreply.services.CallStopService

class SelectReplyActivity :
        Activity(),
        OnStartDragListener,
        OnReplyInteractedListener {
    private val LAYOUT_CHANGE_DURATION = 100L

    private lateinit var adapter: ReplyAdapter
    private lateinit var touchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_reply)

        val replies = mutableListOf<String>()
        adapter = ReplyAdapter(replies, this, this)
        val callback = ReplyItemTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(reply_list)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        add_reply.setOnClickListener {
            new_reply_layout.animate()
                    .alpha(1f)
                    .apply { duration = LAYOUT_CHANGE_DURATION }
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            super.onAnimationStart(animation)
                            with(new_reply_layout) { visibility = View.VISIBLE; bringToFront() }
                            with(main_select_layout) { requestLayout(); invalidate() }
                            new_reply.requestFocus()
                            imm?.toggleSoftInputFromWindow(reply_list.windowToken, InputMethodManager.SHOW_FORCED, 0)
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            add_reply_layout.visibility = View.GONE
                        }
                    }).start()
        }

        cancel.setOnClickListener { finish() }

        ok.setOnClickListener {
            val reply = new_reply.text.toString()
            if (reply.trim({ it <= ' ' }).isEmpty()) {
                new_reply.error = getString(R.string.no_reply)
                new_reply.requestFocus()
                return@setOnClickListener
            }

            resetNewReply()
            adapter.add(reply)
            if(ReplyManager.hasNoReply())
                toggleNoReplies(true)
            ReplyManager.addReply(reply)
            imm?.hideSoftInputFromWindow(reply_list.windowToken, 0)
        }

        cancel_r.setOnClickListener {
            imm?.hideSoftInputFromWindow(reply_list.windowToken, 0)
            resetNewReply()
        }

        replies.addAll(ReplyManager.replies)
        reply_list.layoutManager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, LinearLayout.VERTICAL)
        decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider))
        reply_list.addItemDecoration(decoration)
        reply_list.adapter = adapter

        if(ReplyManager.hasNoReply()) {
            no_replies.alpha = 1f
            no_replies.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        when(new_reply_layout.visibility) {
            View.VISIBLE -> resetNewReply()
            else -> super.onBackPressed()
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
            = touchHelper.startDrag(viewHolder)

    override fun onReplySelected(reply: String) {
        ReplyManager.currentReply = reply
        startService<CallStopService>()
        toast("${getString(R.string.qr_started)}: $reply")
        finish()
    }

    override fun onReplyDismissed() {
        if(ReplyManager.hasNoReply())
            toggleNoReplies(false)
    }

    private fun resetNewReply() {
        new_reply.setText("")
        new_reply.error = null
        new_reply_layout.animate()
                .alpha(0f)
                .apply { duration = LAYOUT_CHANGE_DURATION }
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        add_reply_layout.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        new_reply_layout.visibility = View.GONE
                        add_reply_layout.bringToFront()
                        main_select_layout.requestLayout()
                        main_select_layout.invalidate()
                    }
                }).start()
    }

    private fun toggleNoReplies(isToggled: Boolean) {
        if (!isToggled) {
            no_replies.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    no_replies.visibility = View.VISIBLE
                }
            }).apply { duration = LAYOUT_CHANGE_DURATION }.start()
        } else {
            no_replies.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    no_replies.visibility = View.GONE
                }
            }).apply { duration = LAYOUT_CHANGE_DURATION }.start()
        }
    }
}