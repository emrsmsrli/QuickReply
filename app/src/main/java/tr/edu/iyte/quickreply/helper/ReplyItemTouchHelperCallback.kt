package tr.edu.iyte.quickreply.helper

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import tr.edu.iyte.quickreply.interfaces.ReplyItemTouchHelperAdapter
import tr.edu.iyte.quickreply.interfaces.ViewHolderInteractionListener

class ReplyItemTouchHelperCallback(val adapter: ReplyItemTouchHelperAdapter)
        : ItemTouchHelper.Callback() {
    private val ALPHA_FULL = 1.0f

    override fun isItemViewSwipeEnabled() = true
    override fun isLongPressDragEnabled() = false

    override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?)
            = ItemTouchHelper.Callback.makeMovementFlags(0, ItemTouchHelper.START or ItemTouchHelper.END)

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?)
            = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int)
            = adapter.onReplyDismissed(viewHolder!!.adapterPosition)

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // fade out the view as it is swiped out of the parent's bounds
            val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder?.itemView!!.width.toFloat()
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // We only want the active item to change
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
            if (viewHolder is ViewHolderInteractionListener)
                viewHolder.onInteractionStart()

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
        super.clearView(recyclerView, viewHolder)

        viewHolder?.itemView!!.alpha = ALPHA_FULL

        if (viewHolder is ViewHolderInteractionListener)
            viewHolder.onInteractionEnd()
    }
}