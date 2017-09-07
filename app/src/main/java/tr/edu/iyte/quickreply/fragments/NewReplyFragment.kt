package tr.edu.iyte.quickreply.fragments

import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import tr.edu.iyte.quickreply.R
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find

class NewReplyFragment : BottomSheetDialogFragment(), AnkoLogger {
    interface OnAddReplyInteractionListener {
        fun onNewReplySaved(reply: String)
    }

    private val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                debug("User hid new reply dialog")
                dismiss()
            }
        }
    }

    private var listener: OnAddReplyInteractionListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is OnAddReplyInteractionListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        val content = View.inflate(context, R.layout.fragment_new_reply, null)
        dialog?.setContentView(content)
        val params = (content.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior != null && behavior is BottomSheetBehavior<*>)
            behavior.setBottomSheetCallback(callback)

        val replyTextLayout = content.find<TextInputLayout>(R.id.reply_text_layout)
        val replyText = replyTextLayout.find<EditText>(R.id.reply_text)

        // make background transparent
        dialog?.window?.findViewById(R.id.design_bottom_sheet)?.setBackgroundResource(android.R.color.transparent)

        replyText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(replyTextLayout.error != null)
                    replyTextLayout.error = null
            }
        })

        content.find<View>(R.id.save_button).setOnClickListener {
            val reply = replyText.text.toString().trim { it <= ' ' }

            if(reply.isEmpty()) {
                replyTextLayout.error = getString(R.string.no_reply)
                return@setOnClickListener
            }

            listener?.onNewReplySaved(reply)
            dismiss()
        }
    }
}