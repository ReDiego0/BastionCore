package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ThreadLocalRandom

class MissionGenerator(private val plugin: BastionCore) {

    fun generateMission(templateId: String): ItemStack? {
        val config = plugin.config
        val path = "templates.$templateId"

        if (!config.contains(path)) return null

        val bosses = config.getMapList("$path.possible_bosses")
        if (bosses.isEmpty()) return null

        // TODO: idealmente usar un sistema de pesos ponderados
        val selectedBoss = bosses[ThreadLocalRandom.current().nextInt(bosses.size)]

        val mobId = selectedBoss["mob"] as String
        val baseReward = (selectedBoss["base_reward"] as Int).toDouble()
        val threatLevel = selectedBoss["threat_level"] as Int

        val variance = ThreadLocalRandom.current().nextDouble(0.9, 1.1)
        val finalReward = (baseReward * variance).toInt()

        val item = ItemStack(Material.PAPER)
        val meta = item.itemMeta

        val displayName = config.getString("$path.display_name") ?: "Contrato de Caza"
        meta.setDisplayName("§e$displayName §7(Nv. $threatLevel)")

        val lore = ArrayList<String>()
        lore.add("§7Objetivo: §c$mobId")
        lore.add("§7Zona: §f${config.getString("$path.world_template")}")
        lore.add("§7Recompensa: §6$finalReward Oro")
        lore.add("")
        lore.add("§e[Click en ${config.getString("$path.trigger_block")} para iniciar]")
        meta.lore = lore

        val pdc = meta.persistentDataContainer
        pdc.set(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING, "PROCEDURAL")
        pdc.set(ContractUtils.DATA_WORLD_KEY, PersistentDataType.STRING, config.getString("$path.world_template")!!)
        pdc.set(ContractUtils.DATA_BOSS_KEY, PersistentDataType.STRING, mobId)
        pdc.set(ContractUtils.DATA_REWARD_KEY, PersistentDataType.DOUBLE, finalReward.toDouble())
        pdc.set(ContractUtils.DATA_THREAT_KEY, PersistentDataType.INTEGER, threatLevel)

        val triggerBlock = config.getString("$path.trigger_block", "OAK_FENCE")!!
        pdc.set(ContractUtils.DATA_TRIGGER_KEY, PersistentDataType.STRING, triggerBlock)

        item.itemMeta = meta
        return item
    }
}