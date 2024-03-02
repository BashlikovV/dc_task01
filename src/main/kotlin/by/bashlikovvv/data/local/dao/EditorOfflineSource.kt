package by.bashlikovvv.data.local.dao

import java.sql.Connection
import by.bashlikovvv.data.local.contract.DatabaseContract.EditorsTable
import by.bashlikovvv.domain.model.Editor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement

class EditorOfflineSource(private val connection: Connection) {

    companion object {
        /* Create editors table */
        private const val CREATE_TABLE_EDITORS =
            "CREATE TABLE ${EditorsTable.TABLE_NAME} (" +
                    "${EditorsTable.COLUMN_ID} SERIAL PRIMARY KEY, " +
                    "${EditorsTable.COLUMN_LOGIN} VARCHAR(64), " +
                    "${EditorsTable.COLUMN_PASSWORD} VARCHAR(128), " +
                    "${EditorsTable.COLUMN_FIRSTNAME} VARCHAR(64), " +
                    "${EditorsTable.COLUMN_LASTNAME} VARCHAR(64)" +
            ");"
        /* Add new editor at table */
        private const val INSERT_EDITOR =
            "INSERT INTO ${EditorsTable.TABLE_NAME} (" +
                    "${EditorsTable.COLUMN_LOGIN}, " +
                    "${EditorsTable.COLUMN_PASSWORD}, " +
                    "${EditorsTable.COLUMN_FIRSTNAME}, " +
                    EditorsTable.COLUMN_LASTNAME +
            ") VALUES (?, ?, ?, ?);"
        /* Get editor by id */
        private const val SELECT_EDITOR_BY_ID =
            "SELECT " +
                    "${EditorsTable.COLUMN_LOGIN}, " +
                    "${EditorsTable.COLUMN_PASSWORD}, " +
                    "${EditorsTable.COLUMN_FIRSTNAME}, " +
                    "${EditorsTable.COLUMN_LASTNAME} " +
            "FROM ${EditorsTable.TABLE_NAME} " +
            "WHERE ${EditorsTable.COLUMN_ID} = ?;"
        /* Get all editors */
        private const val SELECT_EDITORS =
            "SELECT " +
                    "${EditorsTable.COLUMN_ID}, " +
                    "${EditorsTable.COLUMN_LOGIN}, " +
                    "${EditorsTable.COLUMN_PASSWORD}, " +
                    "${EditorsTable.COLUMN_FIRSTNAME}, " +
                    "${EditorsTable.COLUMN_LASTNAME} " +
            "FROM ${EditorsTable.TABLE_NAME};"
        /* Update exists editor */
        private const val UPDATE_EDITOR =
            "UPDATE ${EditorsTable.TABLE_NAME} " +
            "SET " +
                    "${EditorsTable.COLUMN_LOGIN} = ?, " +
                    "${EditorsTable.COLUMN_PASSWORD} = ?, " +
                    "${EditorsTable.COLUMN_FIRSTNAME} = ?, " +
                    "${EditorsTable.COLUMN_LASTNAME} = ? " +
            "WHERE ${EditorsTable.COLUMN_ID} = ?;"
        /* Delete exists editor */
        private const val DELETE_EDITOR =
            "DELETE FROM ${EditorsTable.TABLE_NAME} " +
            "WHERE ${EditorsTable.COLUMN_ID} = ?;"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_EDITORS)
    }

    suspend fun create(editor: Editor): Long = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_EDITOR, Statement.RETURN_GENERATED_KEYS)
        statement.apply {
            setString(1, editor.login)
            setString(2, editor.password)
            setString(3, editor.firstname)
            setString(4, editor.lastname)
            executeUpdate()
        }

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getLong(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted editor")
        }
    }

    suspend fun read(id: Long): Editor = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_EDITOR_BY_ID)
        statement.setLong(1, id)

        val resultSet = statement.executeQuery()
        if (resultSet.next()) {
            val login = resultSet.getString(EditorsTable.COLUMN_LOGIN)
            val password = resultSet.getString(EditorsTable.COLUMN_PASSWORD)
            val firstname = resultSet.getString(EditorsTable.COLUMN_FIRSTNAME)
            val lastname = resultSet.getString(EditorsTable.COLUMN_LASTNAME)
            return@withContext Editor(
                id = id,
                login = login,
                password = password,
                firstname = firstname,
                lastname = lastname
            )
        } else {
            throw Exception("Editor record not found")
        }
    }

    suspend fun readAll(): List<Editor?> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Editor>()
        val statement = connection.prepareStatement(SELECT_EDITORS)

        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            val id = resultSet.getLong(EditorsTable.COLUMN_ID)
            val login = resultSet.getString(EditorsTable.COLUMN_LOGIN)
            val password = resultSet.getString(EditorsTable.COLUMN_PASSWORD)
            val firstname = resultSet.getString(EditorsTable.COLUMN_FIRSTNAME)
            val lastname = resultSet.getString(EditorsTable.COLUMN_LASTNAME)
            result.add(
                Editor(
                    id = id,
                    login = login,
                    password = password,
                    firstname = firstname,
                    lastname = lastname
                )
            )
        }

        result
    }

    suspend fun update(id: Long, editor: Editor) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_EDITOR)
        statement.apply {
            setString(1, editor.login)
            setString(2, editor.password)
            setString(3, editor.firstname)
            setString(4, editor.lastname)
            setLong(5, id)
        }.executeUpdate()
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_EDITOR)
        statement.apply {
            setLong(1, id)
        }.executeUpdate()
    }

}