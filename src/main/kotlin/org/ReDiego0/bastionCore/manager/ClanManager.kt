package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Clan
import org.ReDiego0.bastionCore.data.ClanRank
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ClanManager(private val plugin: BastionCore) {

    private val clans = ConcurrentHashMap<String, Clan>()
    private val playerClanMap = ConcurrentHashMap<UUID, String>()

    private val creationCost = 100000.0

    fun getClan(id: String): Clan? = clans[id.lowercase()]

    fun getClanByPlayer(uuid: UUID): Clan? {
        val clanId = playerClanMap[uuid] ?: return null
        return clans[clanId]
    }

    fun adminCreateClan(staff: Player, leader: Player, clanId: String, displayName: String): Boolean {
        if (clans.containsKey(clanId.lowercase())) {
            staff.sendMessage("§cError: Ya existe el ID '$clanId'.")
            return false
        }
        if (getClanByPlayer(leader.uniqueId) != null) {
            staff.sendMessage("§cError: ${leader.name} ya tiene un clan.")
            return false
        }

        val economy = plugin.economy
        if (economy == null) {
            staff.sendMessage("§cError Crítico: Vault no detectado.")
            return false
        }

        if (!economy.has(leader, creationCost)) {
            staff.sendMessage("§cFondos insuficientes en la cuenta del líder.")
            staff.sendMessage("§7Tiene: ${economy.getBalance(leader)} / Requiere: $creationCost")
            return false
        }

        economy.withdrawPlayer(leader, creationCost)
        leader.sendMessage("§e[Finanzas] §fSe han descontado §6$creationCost Oro§f por la fundación del clan.")
        staff.sendMessage("§a[Sistema] §fCobro realizado a ${leader.name}.")

        val newClan = Clan(
            id = clanId.lowercase(),
            displayName = displayName.replace("&", "§"),
            leaderUuid = leader.uniqueId,
            homeLocation = staff.location // importantisimo: el home se establece donde está el Staff parado
        )

        registerClan(newClan)

        // TODO: Guardar en DB/YAML
        // saveClan(newClan)

        staff.sendMessage("§a¡Clan $displayName fundado correctamente!")
        staff.sendMessage("§7Líder: ${leader.name} (Taisho)")

        return true
    }

    fun disbandClan(clan: Clan) {
        for (memberUuid in clan.members.keys) {
            val p = Bukkit.getPlayer(memberUuid)
            p?.sendMessage("§c[Alerta] El clan ${clan.displayName} ha sido disuelto.")
            playerClanMap.remove(memberUuid)
        }
        clans.remove(clan.id)
        // TODO: Borrar de DB
    }

    fun transferLeadership(clan: Clan, newLeaderUuid: UUID): Boolean {
        if (!clan.isMember(newLeaderUuid)) return false
        val oldLeaderUuid = clan.leaderUuid
        clan.setMemberRank(oldLeaderUuid, ClanRank.SANBO)
        clan.setMemberRank(newLeaderUuid, ClanRank.TAISHO)
        clan.leaderUuid = newLeaderUuid

        val newName = Bukkit.getOfflinePlayer(newLeaderUuid).name ?: "Desconocido"
        notifyClan(clan, "§e[Clan] ¡El mando ha pasado a manos de $newName!")

        return true
    }

    fun joinClan(player: Player, clan: Clan) {
        if (getClanByPlayer(player.uniqueId) != null) return

        clan.addMember(player.uniqueId, ClanRank.ASHIGARU)
        playerClanMap[player.uniqueId] = clan.id

        player.sendMessage("§aTe has unido a ${clan.displayName}")
        notifyClan(clan, "§7${player.name} se ha unido al clan.")
    }

    fun leaveClan(player: Player) {
        val clan = getClanByPlayer(player.uniqueId) ?: return

        if (clan.getMemberRank(player.uniqueId) == ClanRank.TAISHO) {
            player.sendMessage("§cEl Taisho no puede abandonar. Promueve a otro o disuelve el clan.")
            return
        }

        clan.removeMember(player.uniqueId)
        playerClanMap.remove(player.uniqueId)

        player.sendMessage("§eHas abandonado ${clan.displayName}.")
        notifyClan(clan, "§7${player.name} ha abandonado el clan.")
    }

    private fun registerClan(clan: Clan) {
        clans[clan.id] = clan
        for (uuid in clan.members.keys) {
            playerClanMap[uuid] = clan.id
        }
    }

    fun notifyClan(clan: Clan, message: String) {
        for (uuid in clan.members.keys) {
            val player = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                player.sendMessage(message)
            }
        }
    }

    fun loadClans() {
        // TODO: Cargar archivo/SQL
        plugin.logger.info("Sistema de Clanes inicializado.")
    }
}