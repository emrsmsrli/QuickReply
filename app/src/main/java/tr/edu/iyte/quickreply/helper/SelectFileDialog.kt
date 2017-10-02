package tr.edu.iyte.quickreply.helper

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.adapters.FileAdapter
import tr.edu.iyte.quickreply.adapters.FileItem
import java.io.File
import java.util.*

class SelectFileDialog(private val fileSelectMode: Boolean = false,
                       private val onFileSelectedListener: (String) -> Unit) : AnkoLogger {
    private val stack = Stack<String>()
    private var path = ""
    private lateinit var dialog: AlertDialog
    private lateinit var subTitle: TextView
    private lateinit var upButton: Button

    @SuppressLint("InflateParams")
    fun show(context: Context) {
        val extDirectory = Environment.getExternalStorageDirectory()
        path = extDirectory.absolutePath
        val adapter = FileAdapter(context, getChildrenFiles(extDirectory).toMutableList())

        dialog = AlertDialog.Builder(context)
                .setAdapter(adapter, null)
                .setNeutralButton(R.string.up, null)
                .also {
                    val titleView = LayoutInflater.from(context)
                            .inflate(R.layout.custom_dialog_title, null)
                    titleView.find<TextView>(R.id.title).text =
                            if(fileSelectMode) context.getString(R.string.select_file)
                            else context.getString(R.string.select_folder)
                    subTitle = titleView.find(R.id.subtitle)
                    subTitle.text = path

                    it.setCustomTitle(titleView)

                    if(fileSelectMode) {
                        it.setPositiveButton(android.R.string.cancel) {
                            dialog, _ -> dialog.dismiss()
                        }
                    } else {
                        it.setNegativeButton(android.R.string.cancel) {
                            dialog, _ -> dialog.dismiss()
                        }.setPositiveButton(android.R.string.ok) { dialog, _ ->
                            onFileSelectedListener(path)
                            dialog.dismiss()
                        }
                    }
                }.show()

        dialog.listView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ ->
            val file = adapter.getItem(position)

            if(!file.isDirectory) {
                path += "${File.separator}${file.name}"
                onFileSelectedListener(path)
                dialog.dismiss()
                return@OnItemClickListener
            }

            if(upButton.visibility != View.VISIBLE)
                upButton.visibility = View.VISIBLE

            stack.push(path)

            path += "${File.separator}${file.name}"
            info("down, now in $path")

            val dir = File(path)
            subTitle.text = path

            adapter.clear()
            val children = getChildrenFiles(file = dir)
            if(children.isEmpty()) {
                context.toast(context.getString(R.string.no_subdirectories))
            } else {
                adapter.addAll(children)
            }
        }

        upButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        upButton.visibility = View.INVISIBLE
        upButton.setOnClickListener {
            path = stack.pop()
            info("up, now in $path")

            if(stack.isEmpty()) {
                upButton.visibility = View.INVISIBLE
            }

            val upDir = File(path)
            subTitle.text = path

            adapter.clear()
            adapter.addAll(getChildrenFiles(file = upDir))
        }

        verbose("dialog init complete")
    }

    private fun getChildrenFiles(file: File): List<FileItem> {
        val children = file.listFiles()

        fun getFileName(path: String): String {
            val parts = path.split(File.separator)
            return parts[parts.size - 1]
        }

        fun filter(isDir: Boolean)
                = children.filter { it.isDirectory == isDir }
                .sorted()
                .map { FileItem(getFileName(it.absolutePath), isDir) }
        val directoryNames = filter(isDir = true)

        if(!fileSelectMode)
            return directoryNames
        return directoryNames + filter(isDir = false)
    }
}