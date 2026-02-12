package org.ReDiego0.bastionCore.storage

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.Role
import org.ReDiego0.bastionCore.data.Faction
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
            data.currentRole = Role.fromId(config.getString("role")!!)

            val factionId = config.getString("faction_id", "none")!!
            data.faction = Faction.fromId(factionId)
            data.factionLevel = config.getInt("faction_level", 1)
            data.factionXp = config.getInt("faction_xp", 0)
        }
        return data
    }

    override fun savePlayer(data: PlayerData) {
        val file = File(folder, "${data.uuid}.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        config.set("name", data.name)
        config.set("rank", data.hunterRank)
        config.set("xp", data.guildPoints)
        config.set("role", data.currentRole.id)
        config.set("faction_id", data.faction.id)
        config.set("faction_level", data.factionLevel)
        config.set("faction_xp", data.factionXp)

        try {
            config.save(file)
        } catch (e: Exception) {
            plugin.logger.severe("Error guardando datos YAML para ${data.name}: ${e.message}")
        }
    }
}