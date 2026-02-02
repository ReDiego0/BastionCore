package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MissionManager(private val plugin: BastionCore) {
    fun startMission(player: Player, missionId: ItemStack) {
        val config = ContractUtils.getConfig()
        val path = "missions.$missionId"

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        val minRank = config.getInt("$path.requirements.min_rank", 1)

        if (data.hunterRank < minRank) {
            player.sendMessage("§c[!] Acceso Denegado.")
            player.sendMessage("§7Tu Rango de Contratista (${data.hunterRank}) es insuficiente.")
            player.sendMessage("§7Rango requerido: $minRank")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        if (!LoadoutValidator.canDeploy(player)) {
            player.playSound(player.location, Sound.BLOCK_CHEST_CLOSE, 1f, 0.5f)
            return
        }

        val templateName = config.getString("$path.template_world") ?: "world"

        player.sendMessage("§e[Sistema] §fProcesando contrato...")
        player.sendMessage("§e[Sistema] §fGenerando zona de despliegue: $templateName...")

        // Idealmente esto debería ser con callback para no congelar si el mundo es grande
        val world = plugin.instanceManager.createInstance(templateName)

        if (world != null) {
            val handItem = player.inventory.itemInMainHand
            handItem.amount -= 1

            player.teleport(world.spawnLocation)

            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.5f)
            player.sendTitle("§cMISIÓN INICIADA", config.getString("$path.display_name")?.replace("&", "§"), 10, 70, 20)

            // TODO: llamar a MythicMobs para spawnear bichos en el futuro
            plugin.logger.info("Misión iniciada para ${player.name} en ${world.name}")

        } else {
            player.sendMessage("§cError Crítico: No se pudo generar la instancia. Reporta esto al staff.")
        }
    }
}