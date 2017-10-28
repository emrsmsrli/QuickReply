package tr.edu.iyte.quickreply.helper

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.adapters.FileAdapter
import tr.edu.iyte.quickreply.adapters.FileItem
import tr.edu.iyte.quickreply.adapters.StandartFileItem
import tr.edu.iyte.quickreply.adapters.UpFileItem
import java.io.File
import java.util.*

class SelectFileDialog(private val fileSelectMode: Boolean = false,
                       private val onFileSelectedListener: (String) -> Unit) : AnkoLogger {
    private val stack = Stack<String>()
    private var path = ""
    private lateinit var dialog: AlertDialog
    private lateinit var subTitle: TextView
    private lateinit var upButton: Button
    private lateinit var adapter: FileAdapter

    @SuppressLint("InflateParams")
    fun show(context: Context) {
        val extDirectory = Environment.getExternalStorageDirectory()
        path = extDirectory.absolutePath
        adapter = FileAdapter(context) onItemClick@{ file ->
            if(file is UpFileItem) {
                path = stack.pop()
                info("up, now in $path")

                val upDir = File(path)
                subTitle.text = path

                adapter.clear()
                if(stack.isEmpty()) {
                    adapter.addAll(getChildrenFiles(file = upDir), includeUp = false)
                } else {
                    adapter.addAll(getChildrenFiles(file = upDir), includeUp = true)
                }

                return@onItemClick
            }

            if(!file.isDirectory) {
                path += "${File.separator}${file.name}"
                onFileSelectedListener(path)
                dialog.dismiss()
                return@onItemClick
            }

            stack.push(path)

            path += "${File.separator}${file.name}"
            info("down, now in $path")

            val dir = File(path)
            subTitle.text = path

            adapter.clear()
            val children = getChildrenFiles(file = dir)
            if(children.isEmpty()) {
                context.toast(context.getString(R.string.no_subdirectories))
            }

            adapter.addAll(children, includeUp = true)
        }

        dialog = AlertDialog.Builder(context)
                .setNeutralButton(R.string.new_folder, null)
                .also {
                    val inflater = LayoutInflater.from(context)
                    val titleView = inflater.inflate(R.layout.custom_dialog_title, null)
                    titleView.find<TextView>(R.id.title).text =
                            if(fileSelectMode) context.getString(R.string.select_file)
                            else context.getString(R.string.select_folder)
                    subTitle = titleView.find(R.id.subtitle)
                    subTitle.text = path

                    it.setCustomTitle(titleView)

                    val customView = inflater.inflate(R.layout.custom_dialog_view, null)
                    val recycler = customView.find<RecyclerView>(R.id.recycler)
                    recycler.layoutManager = LinearLayoutManager(context)
                    recycler.adapter = adapter
                    val decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
                    decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.line_divider))
                    recycler.addItemDecoration(decoration)
                    it.setView(customView)

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
        adapter.addAll(getChildrenFiles(extDirectory))

        upButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        upButton.setOnClickListener {
             NewFolderDialog().show(context)
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
                .map { StandartFileItem(getFileName(it.absolutePath), isDir) }
                .sortedBy { it.name.toLowerCase() }
        val directoryNames = filter(isDir = true)

        if(!fileSelectMode)
            return directoryNames
        return directoryNames + filter(isDir = false)
    }

    inner class NewFolderDialog {
        private lateinit var layout: TextInputLayout
        private lateinit var textField: TextInputEditText

        @SuppressLint("InflateParams")
        fun show(context: Context) {
            val dialog = with(AlertDialog.Builder(context)) {
                setPositiveButton(android.R.string.ok, null)
                setNegativeButton(android.R.string.cancel) { di, _ -> di.dismiss() }
                layout = LayoutInflater.from(context).inflate(R.layout.text_input, null) as TextInputLayout
                textField = layout.find(R.id.reply_text)
                textField.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if(!s.isBlank())
                            layout.error = null
                    }
                })
                setView(layout)
                show()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if(textField.text.isBlank())
                    layout.error = "Text cannot be empty"
                else {
                    val newPath = "$path${File.separator}${textField.text}"
                    File(newPath).mkdir()
                    adapter.newFolder(textField.text.toString())
                    dialog.dismiss()
                }
            }
        }
    }
}