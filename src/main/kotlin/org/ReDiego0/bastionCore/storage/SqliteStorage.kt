package org.ReDiego0.bastionCore.storage

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.PlayerData
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class SqliteStorage(private val plugin: BastionCore) : DataStorage {

    private var connection: Connection? = null

    override fun init() {
        try {
            val file = File(plugin.dataFolder, "database.db")
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.absolutePath)
            val sql = """
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(32),
                    rank INTEGER DEFAULT 1,
                    xp INTEGER DEFAULT 0
                );
            """.trimIndent()

            connection?.createStatement()?.use { stmt ->
                stmt.execute(sql)
            }
            plugin.logger.info("Conexión SQLite establecida.")

        } catch (e: Exception) {
            plugin.logger.severe("Error fatal iniciando SQLite: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun close() {
        connection?.close()
    }

    override fun loadPlayer(uuid: UUID, name: String): PlayerData {
        val data = PlayerData(uuid, name)
        try {
            val pstmt = connection?.prepareStatement("SELECT rank, xp FROM player_data WHERE uuid = ?")
            pstmt?.setString(1, uuid.toString())
            val rs = pstmt?.executeQuery()

            if (rs != null && rs.next()) {
                data.hunterRank = rs.getInt("rank")
                data.guildPoints = rs.getInt("xp")
            }
            rs?.close()
            pstmt?.close()
        } catch (e: Exception) {
            plugin.logger.warning("Error cargando SQL para $uuid: ${e.message}")
        }
        return data
    }

    override fun savePlayer(data: PlayerData) {
        try {
            val sql = "REPLACE INTO player_data (uuid, name, rank, xp) VALUES (?, ?, ?, ?)"
            val pstmt = connection?.prepareStatement(sql)

            pstmt?.setString(1, data.uuid.toString())
            pstmt?.setString(2, data.name)
            pstmt?.setInt(3, data.hunterRank)
            pstmt?.setInt(4, data.guildPoints)

            pstmt?.executeUpdate()
            pstmt?.close()
        } catch (e: Exception) {
            plugin.logger.warning("Error guardando SQL para ${data.name}: ${e.message}")
        }
    }
}