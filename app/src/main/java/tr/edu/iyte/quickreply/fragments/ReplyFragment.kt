package tr.edu.iyte.quickreply.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Fragment
import android.os.Bundle
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
import org.jetbrains.anko.act
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.adapters.ReplyAdapter
import tr.edu.iyte.quickreply.helper.ReplyItemTouchHelperCallback
import tr.edu.iyte.quickreply.interfaces.OnReplyInteractedListener
import tr.edu.iyte.quickreply.interfaces.OnStartDragListener
import tr.edu.iyte.quickreply.services.CallStopService

const val ARG_REPLIES = "replies"
const val LAYOUT_CHANGE_DURATION = 200L

class ReplyFragment : Fragment(),
        OnStartDragListener,
        OnReplyInteractedListener,
        AnkoLogger {

    private val replies = mutableListOf<String>()
    private val adapter = ReplyAdapter(replies, this, this)
    private val callback = ReplyItemTouchHelperCallback(adapter)
    private val touchHelper = ItemTouchHelper(callback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        replies.addAll(arguments.getStringArrayList(ARG_REPLIES))
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?)
        = inflater?.inflate(R.layout.fragment_reply, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        touchHelper.attachToRecyclerView(reply_list)
        replies.addAll(ReplyManager.replies)
        reply_list.layoutManager = LinearLayoutManager(act)
        val decoration = DividerItemDecoration(act, LinearLayout.VERTICAL)
        decoration.setDrawable(ContextCompat.getDrawable(act, R.drawable.line_divider))
        reply_list.addItemDecoration(decoration)
        reply_list.adapter = adapter

        add_reply_button.setOnClickListener {
            val bottomSheetDialogFragment
                    = NewReplyFragment(act as NewReplyFragment.OnAddReplyInteractionListener)
            bottomSheetDialogFragment.show((act as AppCompatActivity)
                    .supportFragmentManager, bottomSheetDialogFragment.tag)
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
            toggleNoReplies(false)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
            = touchHelper.startDrag(viewHolder)

    private fun toggleNoReplies(show: Boolean) {
        if(show) {
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

    companion object {
        fun newInstance(replies: ArrayList<String>): ReplyFragment {
            val fragment = ReplyFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_REPLIES, replies)
            fragment.arguments = args
            return fragment
        }
    }
}