package tr.edu.iyte.quickreply

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

enum class ReplyLogType { CALL, SMS }

data class ReplyLog(val id: Long = 0,
                    val callerNumber: String,
                    val reply: String,
                    val type: ReplyLogType,
                    val sentOn: Long,
                    val isDelivered: Boolean) {
    val args: Array<Pair<String, Any>>
        get() = arrayOf("callerNumber" to callerNumber,
                "reply" to reply, "type" to type.name,
                "sentOn" to sentOn, "isDelivered" to if(isDelivered) 1 else 0)
}

class Database(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "QuickReplyDatabase", version = 1) {
    private val historyTableName = "ReplyHistory"
    //private val pageCount = 20

    override fun onCreate(db: SQLiteDatabase?) {
        db?.createTable(historyTableName, true,
                "id" to SqlType.create("INTEGER PRIMARY KEY AUTOINCREMENT"),
                "callerNumber" to TEXT + NOT_NULL,
                "reply" to TEXT + NOT_NULL,
                "type" to TEXT + NOT_NULL,
                "sentOn" to INTEGER + NOT_NULL,
                "isDelivered" to INTEGER + NOT_NULL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.dropTable(historyTableName, ifExists = true)
        onCreate(db)
    }

    fun addToHistory(log: ReplyLog) {
        use {
            insert(historyTableName, *log.args)
        }
    }

    fun removeFromHistory(log: ReplyLog) {
        deleteLogsWhere(args = "id = ${log.id}")
    }

    fun clearHistory() {
        deleteLogsWhere()
    }

    private fun deleteLogsWhere(args: String = "") {
        use {
            delete(historyTableName, args)
        }
    }

    fun getHistory(/*page: Int = 1*/): List<ReplyLog> {
        /*if(page < 1)
            throw IllegalArgumentException("page cannot be less than one")*/

        return use {
            select(historyTableName)
                    //.limit(page * pageCount, pageCount)
                    .orderBy("sentOn", SqlOrderDirection.DESC)
                    .parseList(rowParser {
                        id: Long, callerNum: String, reply: String, type: String, sentOn: Long, isDelivered: Int ->
                        ReplyLog(id, callerNum, reply, ReplyLogType.valueOf(type), sentOn, isDelivered == 1)
                    })
        }
    }

    companion object {
        private var instance: Database? = null

        @Synchronized
        fun getInstance(ctx: Context): Database {
            if (instance == null)
                instance = Database(ctx)
            return instance!!
        }
    }
}