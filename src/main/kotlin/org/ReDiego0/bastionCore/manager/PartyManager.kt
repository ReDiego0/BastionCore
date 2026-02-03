package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Party
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PartyManager(private val plugin: BastionCore) {
    private val playerPartyMap = ConcurrentHashMap<UUID, Party>()
    private val pendingInvites = ConcurrentHashMap<UUID, UUID>()

    fun createParty(leader: Player) {
        if (hasParty(leader.uniqueId)) {
            leader.sendMessage("§cYa estás en una escuadra.")
            return
        }
        val party = Party(leaderId = leader.uniqueId)
        playerPartyMap[leader.uniqueId] = party

        leader.sendMessage("§aEscuadra creada. §7Usa /party invite <nombre> para reclutar.")
        leader.playSound(leader.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
    }

    fun getParty(playerUuid: UUID): Party? {
        return playerPartyMap[playerUuid]
    }

    fun hasParty(playerUuid: UUID): Boolean {
        return playerPartyMap.containsKey(playerUuid)
    }

    fun sendInvite(sender: Player, target: Player) {
        val party = getParty(sender.uniqueId)
        if (party == null) {
            createParty(sender)
            sendInvite(sender, target)
            return
        }

        if (party.isFull()) {
            sender.sendMessage("§cLa escuadra está llena (Máx ${party.maxSize}).")
            return
        }

        if (hasParty(target.uniqueId)) {
            sender.sendMessage("§c${target.name} ya tiene una escuadra.")
            return
        }

        pendingInvites[target.uniqueId] = sender.uniqueId
        sender.sendMessage("§eInvitación enviada a ${target.name}.")

        target.sendMessage("§8§m--------------------------------")
        target.sendMessage("§a${sender.name} §7te ha invitado a su escuadra.")
        target.sendMessage("§eClic aquí o escribe §f/party accept")
        target.sendMessage("§8§m--------------------------------")
        target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (pendingInvites[target.uniqueId] == sender.uniqueId) {
                pendingInvites.remove(target.uniqueId)
                if (target.isOnline) target.sendMessage("§cLa invitación de ${sender.name} ha expirado.")
            }
        }, 20L * 60)
    }

    fun acceptInvite(player: Player) {
        val inviterId = pendingInvites.remove(player.uniqueId)
        if (inviterId == null) {
            player.sendMessage("§cNo tienes invitaciones pendientes.")
            return
        }

        val party = getParty(inviterId)
        if (party == null) {
            player.sendMessage("§cLa escuadra ya no existe.")
            return
        }

        if (party.isFull()) {
            player.sendMessage("§cLa escuadra se llenó antes de que aceptaras.")
            return
        }

        party.members.add(player.uniqueId)
        playerPartyMap[player.uniqueId] = party

        broadcastToParty(party, "§a${player.name} §7se ha unido a la caza.")
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }

    fun leaveParty(player: Player) {
        val party = getParty(player.uniqueId) ?: return

        if (party.isLeader(player.uniqueId)) {
            disbandParty(party)
        } else {
            party.members.remove(player.uniqueId)
            playerPartyMap.remove(player.uniqueId)
            player.sendMessage("§eHas abandonado la escuadra.")
            broadcastToParty(party, "§c${player.name} §7ha abandonado la escuadra.")
        }
    }

    fun disbandParty(party: Party) {
        broadcastToParty(party, "§cLa escuadra ha sido disuelta.")
        for (memberId in party.members) {
            playerPartyMap.remove(memberId)
        }
    }

    fun broadcastToParty(party: Party, message: String) {
        for (uuid in party.members) {
            Bukkit.getPlayer(uuid)?.sendMessage(message)
        }
    }
}