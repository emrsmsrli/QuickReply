package tr.edu.iyte.quickreply.interfaces

interface OnReplyInteractedListener {
    fun onReplySelected(reply: String)
    fun onReplyDismissed()
}