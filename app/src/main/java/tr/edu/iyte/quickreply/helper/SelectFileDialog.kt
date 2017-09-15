package tr.edu.iyte.quickreply.helper

import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.widget.AdapterView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import org.jetbrains.anko.verbose
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.adapters.FileAdapter
import tr.edu.iyte.quickreply.adapters.FileItem
import java.io.File
import java.util.*

class SelectFileDialog(private val listener: OnFileSelectedListener,
                       private val shouldIncludeFiles: Boolean = false) : AnkoLogger {
    interface OnFileSelectedListener {
        fun onFileSelected(path: String)
    }

    private val stack = Stack<String>()
    private var path = ""
    private lateinit var dialog: AlertDialog

    fun show(context: Context) {
        val extDirectory = Environment.getExternalStorageDirectory()
        path = extDirectory.absolutePath
        val adapter = FileAdapter(context, getChildrenFiles(extDirectory).toMutableList())

        dialog = AlertDialog.Builder(context)
                .setTitle(path)
                .setAdapter(adapter, null)
                .setNeutralButton(R.string.up, null)
                .also {
                    /*
                                val titleView = LayoutInflater.from(context)
                                        .inflate(R.layout.custom_dialog_title, null)

                                it.setCustomTitle(titleView)*/
                    if(shouldIncludeFiles) {
                        it.setPositiveButton(android.R.string.cancel) {
                            dialog, _ -> dialog.dismiss()
                        }
                    } else {
                        it.setNegativeButton(android.R.string.cancel) {
                            dialog, _ -> dialog.dismiss()
                        }.setPositiveButton(android.R.string.ok) { dialog, _ ->
                            listener.onFileSelected(path)
                            dialog.dismiss()
                        }
                    }
                }.show()

        dialog.listView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ ->
            val file = adapter.getItem(position)

            if(!file.isDirectory) {
                path += "${File.separator}${file.name}"
                listener.onFileSelected(path)
                dialog.dismiss()
                return@OnItemClickListener
            }

            stack.push(path)

            path += "${File.separator}${file.name}"
            info("down, now in $path")

            val dir = File(path)
            dialog.setTitle(path)

            adapter.clear()
            val children = getChildrenFiles(dir)
            if(children.isEmpty()) {
                context.toast(context.getString(R.string.no_subdirectories))
            } else {
                adapter.addAll(children)
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            if(stack.isEmpty()) {
                TODO("get external sd and internal storage")
            }

            path = stack.pop()
            info("up, now in $path")

            val upDir = File(path)
            dialog.setTitle(path)

            adapter.clear()
            adapter.addAll(getChildrenFiles(file = upDir))
        }

        verbose("dialog init complete")
    }

    private fun getChildrenFiles(file: File): List<FileItem> {
        val children = file.listFiles()
        val directoryNames = children
                .filter { it.isDirectory }
                .sorted()
                .map { FileItem(getFileName(it.absolutePath), true) }

        if(!shouldIncludeFiles)
            return directoryNames
        return directoryNames + children
                .filter { it.isFile }
                .sorted()
                .map {  FileItem(getFileName(it.absolutePath), false) }
    }

    private fun getFileName(path: String): String {
        val parts = path.split("/")
        return parts[parts.size - 1]
    }
}