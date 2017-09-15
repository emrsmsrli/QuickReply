package tr.edu.iyte.quickreply.helper

import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import org.jetbrains.anko.verbose
import tr.edu.iyte.quickreply.R
import java.io.File
import java.util.*

class SelectDiskPathDialog(private val listener: OnPathSelectedListener) : AnkoLogger {
    interface OnPathSelectedListener {
        fun onPathSelected(path: String)
    }

    private val stack = Stack<String>()
    private val currentDirectoryChildren = mutableListOf<String>()
    private var path = ""
    private lateinit var dialog: AlertDialog

    fun show(context: Context) {
        val extDirectory = Environment.getExternalStorageDirectory()
        path = extDirectory.absolutePath
        val adapter = ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1,
                getChildrenDirs(extDirectory))

        dialog = AlertDialog.Builder(context)
                .setTitle(path)
                .setAdapter(adapter, null)
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    listener.onPathSelected(path)
                    dialog.dismiss()
                }.setNeutralButton(R.string.up, null).show()

        dialog.listView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ ->
            stack.push(path)

            path = currentDirectoryChildren[position]
            info("select path, now in $path")

            val dir = File(path)
            dialog.setTitle(path)

            adapter.clear()
            val children = getChildrenDirs(dir)
            if(children.isEmpty()) {
                context.toast(context.getString(R.string.no_subdirectories))
            } else {
                adapter.addAll(children)
            }
            adapter.notifyDataSetChanged()
        }

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            if(stack.isEmpty()) {
                TODO("get external sd and internal storage")
            }

            path = stack.pop()
            info("up tapped, now in $path")

            val upDir = File(path)
            dialog.setTitle(path)

            adapter.clear()
            adapter.addAll(getChildrenDirs(upDir))
            adapter.notifyDataSetChanged()
        }

        verbose("dialog init complete")
    }

    private fun getChildrenDirs(file: File): List<String> {
        currentDirectoryChildren.clear()
        val childrenPathNames = file.listFiles()
                .filter { it.isDirectory }
                .map { it.absolutePath }
                .sorted()
        currentDirectoryChildren.addAll(childrenPathNames)

        return childrenPathNames.map { getDirectoryName(it) }
    }

    private fun getDirectoryName(path: String): String {
        val parts = path.split("/")
        return parts[parts.size - 1]
    }
}