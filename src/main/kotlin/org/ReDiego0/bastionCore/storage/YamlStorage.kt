package org.ReDiego0.bastionCore.storage

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.PlayerData
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class YamlStorage(private val plugin: BastionCore) : DataStorage {

    private val folder = File(plugin.dataFolder, "userdata")

    override fun init() {
        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    override fun close() {}

    override fun loadPlayer(uuid: UUID, name: String): PlayerData {
        val file = File(folder, "$uuid.yml")

        val data = PlayerData(uuid, name)

        if (file.exists()) {
            val config = YamlConfiguration.loadConfiguration(file)
            data.hunterRank = config.getInt("rank", 1)
            data.guildPoints = config.getInt("xp", 0)
        }
        return data
    }

    override fun savePlayer(data: PlayerData) {
        val file = File(folder, "${data.uuid}.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        config.set("name", data.name)
        config.set("rank", data.hunterRank)
        config.set("xp", data.guildPoints)

        try {
            config.save(file)
        } catch (e: Exception) {
            plugin.logger.severe("Error guardando datos YAML para ${data.name}: ${e.message}")
        }
    }
}