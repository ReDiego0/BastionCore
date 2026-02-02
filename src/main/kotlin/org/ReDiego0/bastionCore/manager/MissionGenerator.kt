package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ThreadLocalRandom

class MissionGenerator(private val plugin: BastionCore) {

    fun generateMission(templateId: String): ItemStack? {
        val config = ContractUtils.getConfig()
        val path = "templates.$templateId"

        if (!config.contains(path)) {
            plugin.logger.warning("Generator: No existe el path $path en missions.yml")
            return null
        }

        val bosses = config.getMapList("$path.possible_bosses")
        if (bosses.isEmpty()) {
            plugin.logger.warning("Generator: 'possible_bosses' está vacío para $templateId")
            return null
        }

        var totalWeight = 0
        for (bossMap in bosses) {
            val weight = bossMap["weight"]?.toString()?.toIntOrNull() ?: 10
            totalWeight += weight
        }

        val randomValue = ThreadLocalRandom.current().nextInt(totalWeight)
        var currentWeight = 0
        var selectedBoss: Map<*, *>? = null

        for (bossMap in bosses) {
            val weight = bossMap["weight"]?.toString()?.toIntOrNull() ?: 10
            currentWeight += weight

            if (randomValue < currentWeight) {
                selectedBoss = bossMap
                break
            }
        }

        if (selectedBoss == null) selectedBoss = bosses[0]

        val mobId = selectedBoss["mob"].toString()
        val baseReward = selectedBoss["base_reward"]?.toString()?.toDoubleOrNull() ?: 100.0
        val threatLevel = selectedBoss["threat_level"]?.toString()?.toIntOrNull() ?: 1

        val variance = ThreadLocalRandom.current().nextDouble(0.9, 1.1)
        val finalReward = (baseReward * variance).toInt()

        val item = ItemStack(Material.PAPER)
        val meta = item.itemMeta

        val displayName = config.getString("$path.display_name") ?: "Contrato de Caza"
        meta.setDisplayName("§e$displayName §7(Nv. $threatLevel)")

        val worldTemplate = config.getString("$path.world_template") ?: "world"
        val triggerBlock = config.getString("$path.trigger_block", "OAK_FENCE")!!

        val lore = ArrayList<String>()
        lore.add("§7Objetivo: §c$mobId")
        lore.add("§7Zona: §f$worldTemplate")
        lore.add("§7Recompensa: §6$finalReward Oro")
        lore.add("")
        lore.add("§e[Click en $triggerBlock para iniciar]")
        meta.lore = lore

        val pdc = meta.persistentDataContainer
        pdc.set(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING, "PROCEDURAL")
        pdc.set(ContractUtils.DATA_WORLD_KEY, PersistentDataType.STRING, worldTemplate)
        pdc.set(ContractUtils.DATA_BOSS_KEY, PersistentDataType.STRING, mobId)
        pdc.set(ContractUtils.DATA_REWARD_KEY, PersistentDataType.DOUBLE, finalReward.toDouble())
        pdc.set(ContractUtils.DATA_THREAT_KEY, PersistentDataType.INTEGER, threatLevel)
        pdc.set(ContractUtils.DATA_TRIGGER_KEY, PersistentDataType.STRING, triggerBlock)

        item.itemMeta = meta
        return item
    }
}