package tr.edu.iyte.quickreply.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.find
import tr.edu.iyte.quickreply.R

interface FileItem {
    val name: String
    val isDirectory: Boolean
}

data class StandartFileItem(override val name: String,
                            override val isDirectory: Boolean = false) : FileItem
data class UpFileItem(override val name: String,
                      override val isDirectory: Boolean) : FileItem

class FileAdapter(private val ctx: Context,
                  private val notifyOnChange: Boolean = true,
                  private val onItemClick: (FileItem) -> Unit) :
        RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img = v.find<ImageView>(R.id.icon)
        val fileName = v.find<TextView>(R.id.name)
    }

    private val files = mutableListOf<FileItem>()
    private val up = UpFileItem(ctx.getString(R.string.up), isDirectory = true)
    private val inflater = LayoutInflater.from(ctx)
    private val lock = Any()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            = ViewHolder(inflater.inflate(R.layout.list_item_file, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = files[position]
        holder.img.setImageDrawable(ctx.getDrawable(
                when {
                    item is UpFileItem -> R.drawable.ic_folder_upload_24dp
                    item.isDirectory   -> R.drawable.ic_folder_black_24dp
                    else               -> R.drawable.ic_file_black_24dp
                }))

        holder.fileName.text = item.name
        holder.itemView.setOnClickListener {
            onItemClick(files[holder.adapterPosition])
        }
    }

    fun addAll(items: Collection<FileItem>, includeUp: Boolean = false) {
        val offset = if(includeUp) 1 else 0
        synchronized(lock) { if(includeUp) files.add(up); files.addAll(items) }
        if(notifyOnChange) notifyItemRangeInserted(offset, items.size + offset)
    }

    fun newFolder(name: String) {
        val item = StandartFileItem(name, isDirectory = true)
        val idx = (1 until files.size)
                .firstOrNull { files[it].name > name }
                ?.let { it }
                ?: files.size - 1
        synchronized(lock) { files.add(idx, item) }
        if(notifyOnChange) notifyItemInserted(idx)
    }

    fun clear() {
        val size = files.size
        synchronized(lock) { files.clear() }
        if(notifyOnChange) notifyItemRangeRemoved(0, size)
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount() = files.size
}