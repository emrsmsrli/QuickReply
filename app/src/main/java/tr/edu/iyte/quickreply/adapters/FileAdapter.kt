package tr.edu.iyte.quickreply.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.find
import tr.edu.iyte.quickreply.R

data class FileItem(val name: String, val isDirectory: Boolean = false)

class FileAdapter(private val ctx: Context,
                  private val files: MutableList<FileItem>,
                  private val notifyOnChange: Boolean = true) : BaseAdapter() {

    private val inflater = LayoutInflater.from(ctx)
    private val lock = Any()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v = convertView
        if(convertView == null) {
            v = inflater.inflate(R.layout.list_item_file, parent, false)
        }

        val item = getItem(position)
        v!!.find<ImageView>(R.id.icon).setImageDrawable(
                if(item.isDirectory) ctx.getDrawable(R.drawable.ic_folder_black_24dp)
                else ctx.getDrawable(R.drawable.ic_file_black_24dp))
        v.find<TextView>(R.id.name).text = item.name

        return v
    }

    fun add(item: FileItem) {
        files.add(item)
        if(notifyOnChange) notifyDataSetChanged()
    }

    fun addAll(items: Collection<FileItem>) {
        synchronized(lock) {
            files.addAll(items)
        }
        if(notifyOnChange) notifyDataSetChanged()
    }

    fun clear() {
        synchronized(lock) {
            files.clear()
        }
        if(notifyOnChange) notifyDataSetChanged()
    }

    override fun getItem(position: Int) = files[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getCount() = files.size
}