package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.ActiveMission
import org.ReDiego0.bastionCore.combat.MissionType
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

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

        var timeLimit = 1800
        var lives = 3

        val missionId = pdc.get(ContractUtils.MISSION_ID_KEY, PersistentDataType.STRING)

        val templateId = pdc.get(ContractUtils.DATA_TEMPLATE_ID_KEY, PersistentDataType.STRING)
        val allowedSet = HashSet<String>()

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return

        if (missionId != null) {
            val config = ContractUtils.getConfig()
            timeLimit = config.getInt("special.$missionId.time_limit", 1800)
            lives = config.getInt("special.$missionId.lives", 3)
        } else {
            val config = ContractUtils.getConfig()
            timeLimit = config.getInt("defaults.$typeStr.time_limit", 1800)
            lives = config.getInt("defaults.$typeStr.lives", 3)
        }

        if (templateId != null) {
            val list = ContractUtils.getConfig().getStringList("templates.$templateId.allowed_break")
            allowedSet.addAll(list)
        }

        if (data.hunterRank < threat) {
            player.sendMessage("§c[!] Acceso Denegado.")
            player.sendMessage("§7Esta misión requiere Rango de Cazador: §e$threat")
            player.sendMessage("§7Tu Rango actual es: §c${data.hunterRank}")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        player.sendMessage("§e[Sistema] §fGenerando zona de despliegue: $templateWorld...")

        val world = plugin.instanceManager.createInstance(templateWorld)

        if (world != null) {
            item.amount = item.amount - 1

            val party = plugin.partyManager.getParty(player.uniqueId)
            val teamMembers = ArrayList<UUID>()

            if (party != null && party.isLeader(player.uniqueId)) {
                // Si es líder de Party, llevamos a todos
                for (memberId in party.members) {
                    val member = Bukkit.getPlayer(memberId)
                    if (member != null && member.world.name == plugin.citadelWorldName) {
                        member.teleport(world.spawnLocation)
                        member.playSound(member.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.5f)
                        member.sendTitle("§cMISIÓN INICIADA", "§7Objetivo: $targetId", 10, 70, 20)
                        teamMembers.add(memberId)
                    }
                }
            } else {
                player.teleport(world.spawnLocation)
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.5f)
                player.sendTitle("§cMISIÓN INICIADA", "§7Objetivo: $targetId", 10, 70, 20)
                teamMembers.add(player.uniqueId)
            }

            val activeMission = ActiveMission(
                worldName = world.name,
                leaderId = player.uniqueId,
                type = missionType,
                targetId = targetId,
                requiredAmount = amount,
                rewardGold = reward,
                threatLevel = threat,
                allowedBlocks = allowedSet,
                timeLimitSeconds = timeLimit,
                maxLives = lives,
                currentLives = lives
            )

            plugin.gameManager.startGame(activeMission, radius)
            plugin.logger.info("Juego iniciado: ${player.name} vs $targetId ($typeStr) en ${world.name}")

        } else {
            player.sendMessage("§cError Crítico: No se pudo generar la instancia.")
        }
    }
}