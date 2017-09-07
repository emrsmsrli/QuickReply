package tr.edu.iyte.quickreply.fragments

import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import tr.edu.iyte.quickreply.R
import android.support.design.widget.CoordinatorLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find

class NewReplyFragment() : BottomSheetDialogFragment(), AnkoLogger {
    interface OnAddReplyInteractionListener {
        fun onNewReplySaved()
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

    private lateinit var listener: OnAddReplyInteractionListener

    constructor(listener: OnAddReplyInteractionListener) : this() {
        this.listener = listener
    }

    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        val content = View.inflate(context, R.layout.fragment_new_reply, null)
        dialog?.setContentView(content)
        val params = (content.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior != null && behavior is BottomSheetBehavior<*>)
            behavior.setBottomSheetCallback(callback)

        // make background transparent
        dialog?.window?.findViewById(R.id.design_bottom_sheet)?.setBackgroundResource(android.R.color.transparent)
        content.find<View>(R.id.save_button).setOnClickListener {
            listener.onNewReplySaved()
            dismiss()
        }
    }

}