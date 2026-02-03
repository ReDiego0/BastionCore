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
            plugin.logger.warning("Generator: No existe el path $path")
            return null
        }

        val objectives = config.getMapList("$path.objectives")
        if (objectives.isEmpty()) {
            plugin.logger.warning("Generator: 'objectives' está vacío para $templateId")
            return null
        }

        var totalWeight = 0
        for (obj in objectives) {
            totalWeight += obj["weight"]?.toString()?.toIntOrNull() ?: 10
        }

        val randomValue = ThreadLocalRandom.current().nextInt(totalWeight)
        var currentWeight = 0
        var selectedObj: Map<*, *>? = null

        for (obj in objectives) {
            val weight = obj["weight"]?.toString()?.toIntOrNull() ?: 10
            currentWeight += weight
            if (randomValue < currentWeight) {
                selectedObj = obj
                break
            }
        }
        if (selectedObj == null) selectedObj = objectives[0]

        val targetId = selectedObj["id"].toString()
        val amount = selectedObj["amount"]?.toString()?.toIntOrNull() ?: 1
        val baseReward = selectedObj["base_reward"]?.toString()?.toDoubleOrNull() ?: 100.0
        val threatLevel = config.getInt("$path.threat_level", 1)

        val typeStr = config.getString("$path.mission_type", "HUNT")!!
        val radius = config.getInt("$path.radius", 100)
        val worldTemplate = config.getString("$path.world_template") ?: "world"
        val triggerBlock = config.getString("$path.trigger_block", "OAK_FENCE")!!

        val variance = ThreadLocalRandom.current().nextDouble(0.9, 1.1)
        val finalReward = (baseReward * variance).toInt()

        val item = ItemStack(Material.PAPER)
        val meta = item.itemMeta
        val displayName = config.getString("$path.display_name") ?: "Contrato"
        meta.setDisplayName("§e$displayName §7(Nv. $threatLevel)")

        val lore = ArrayList<String>()
        when (typeStr) {
            "HUNT" -> lore.add("§7Objetivo: §cCazar $targetId")
            "EXTERMINATION" -> lore.add("§7Objetivo: §cEliminar $amount x $targetId")
            "GATHER" -> lore.add("§7Objetivo: §aRecolectar $amount x $targetId")
            else -> lore.add("§7Objetivo: §7$targetId")
        }
        lore.add("§7Zona: §f$worldTemplate")
        lore.add("§7Recompensa: §6$finalReward Oro")
        lore.add("")
        lore.add("§e[Click en $triggerBlock para iniciar]")
        meta.lore = lore

        val pdc = meta.persistentDataContainer
        pdc.set(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING, "PROCEDURAL")
        pdc.set(ContractUtils.DATA_WORLD_KEY, PersistentDataType.STRING, worldTemplate)
        pdc.set(ContractUtils.DATA_BOSS_KEY, PersistentDataType.STRING, targetId)
        pdc.set(ContractUtils.DATA_REWARD_KEY, PersistentDataType.DOUBLE, finalReward.toDouble())
        pdc.set(ContractUtils.DATA_THREAT_KEY, PersistentDataType.INTEGER, threatLevel)
        pdc.set(ContractUtils.DATA_TRIGGER_KEY, PersistentDataType.STRING, triggerBlock)

        pdc.set(ContractUtils.DATA_MISSION_TYPE_ENUM_KEY, PersistentDataType.STRING, typeStr)
        pdc.set(ContractUtils.DATA_REQUIRED_AMOUNT_KEY, PersistentDataType.INTEGER, amount)
        pdc.set(ContractUtils.DATA_SPAWN_RADIUS_KEY, PersistentDataType.INTEGER, radius)
        pdc.set(ContractUtils.DATA_TEMPLATE_ID_KEY, PersistentDataType.STRING, templateId)

        item.itemMeta = meta
        return item
    }
}