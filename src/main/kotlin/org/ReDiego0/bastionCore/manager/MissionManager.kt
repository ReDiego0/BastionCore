package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.ActiveMission
import org.ReDiego0.bastionCore.combat.MissionType
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MissionManager(private val plugin: BastionCore) {

    fun startMission(player: Player, item: ItemStack) {
        val pdc = item.itemMeta.persistentDataContainer

        if (!LoadoutValidator.canDeploy(player)) {
            player.playSound(player.location, Sound.BLOCK_CHEST_CLOSE, 1f, 0.5f)
            return
        }

        val templateWorld = pdc.get(ContractUtils.DATA_WORLD_KEY, PersistentDataType.STRING) ?: "world"
        val targetId = pdc.get(ContractUtils.DATA_BOSS_KEY, PersistentDataType.STRING) ?: "ZOMBIE"
        val reward = pdc.get(ContractUtils.DATA_REWARD_KEY, PersistentDataType.DOUBLE) ?: 0.0
        val threat = pdc.get(ContractUtils.DATA_THREAT_KEY, PersistentDataType.INTEGER) ?: 1

        val typeStr = pdc.get(ContractUtils.DATA_MISSION_TYPE_ENUM_KEY, PersistentDataType.STRING) ?: "HUNT"
        val amount = pdc.get(ContractUtils.DATA_REQUIRED_AMOUNT_KEY, PersistentDataType.INTEGER) ?: 1
        val radius = pdc.get(ContractUtils.DATA_SPAWN_RADIUS_KEY, PersistentDataType.INTEGER) ?: 50

        val missionType = try { MissionType.valueOf(typeStr) } catch (e: Exception) { MissionType.HUNT }

        player.sendMessage("§e[Sistema] §fGenerando zona de despliegue: $templateWorld...")

        val world = plugin.instanceManager.createInstance(templateWorld)

        if (world != null) {
            item.amount = item.amount - 1

            player.teleport(world.spawnLocation)
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.5f)
            player.sendTitle("§cMISIÓN INICIADA", "§7Objetivo: $targetId", 10, 70, 20)

            val activeMission = ActiveMission(
                worldName = world.name,
                leaderId = player.uniqueId,
                type = missionType,
                targetId = targetId,
                requiredAmount = amount,
                rewardGold = reward,
                threatLevel = threat
            )

            plugin.gameManager.startGame(activeMission, radius)
            plugin.logger.info("Juego iniciado: ${player.name} vs $targetId ($typeStr) en ${world.name}")

        } else {
            player.sendMessage("§cError Crítico: No se pudo generar la instancia.")
        }
    }
}