package org.ReDiego0.bastionCore.listener

import dev.lone.itemsadder.api.CustomStack
import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CombatListener(private val plugin: BastionCore) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDefend(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        if (victim !is Player) return
        if (!victim.world.name.startsWith("inst_")) return

        if (plugin.combatManager.isBlocking(victim.uniqueId)) {
            event.isCancelled = true
            event.damage = 0.0
            plugin.combatManager.spearHandler.triggerExplosiveCounter(victim)
            return
        }

        if (plugin.combatManager.isParrying(victim.uniqueId)) {
            event.isCancelled = true
            event.damage = 0.0
            plugin.combatManager.katanaHandler.triggerParryCounter(victim)
            return
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.damager !is Player) return
        if (event.entity !is LivingEntity) return

        val player = event.damager as Player

        if (!player.world.name.startsWith("inst_")) return

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        val weaponType = ItemTags.getWeaponType(player.inventory.itemInMainHand)

        val gain = if (weaponType != WeaponType.NONE) 2.0 else 0.5

        if (data.ultimateCharge < 100.0) {
            data.addCharge(player, gain)
            updateMissionBar(player, data.ultimateCharge)
        }
    }

    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (!killer.world.name.startsWith("inst_")) return

        val data = plugin.playerDataManager.getData(killer.uniqueId) ?: return
        val gainOnKill = plugin.config.getDouble("combat.kill_ultimate_gain", 10.0)

        if (data.ultimateCharge < 100.0) {
            data.addCharge(killer, gainOnKill)
            updateMissionBar(killer, data.ultimateCharge)
        }
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectile = event.entity
        val key = NamespacedKey(plugin, "is_explosive_arrow")
        if (!projectile.persistentDataContainer.has(key, PersistentDataType.BYTE)) return

        val loc = projectile.location
        val world = projectile.world
        val shooter = projectile.shooter as? Player

        world.spawnParticle(Particle.EXPLOSION, loc, 1)
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f)

        projectile.remove()

        val radius = 4.0

        for (entity in world.getNearbyEntities(loc, radius, radius, radius)) {
            if (entity is LivingEntity && entity != shooter) {
                entity.damage(15.0, shooter)

                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 2))
                val dir = entity.location.toVector().subtract(loc.toVector()).normalize()
                if (dir.lengthSquared() < 0.01) dir.setY(1.0)

                entity.velocity = dir.multiply(1.5).setY(0.5)
            }
        }
    }

    @EventHandler
    fun onSlotChange(e: PlayerItemHeldEvent) {
        val player = e.player
        val newStack = player.inventory.getItem(e.newSlot)
        updateOffhand(player, newStack)
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return

        if (e.slotType == InventoryType.SlotType.QUICKBAR && e.slot == 40) {
            if (isOurGhostItem(e.currentItem)) {
                e.isCancelled = true

                plugin.server.scheduler.runTask(plugin, Runnable {
                    updateOffhand(player, player.inventory.itemInMainHand)
                })
            }
        }
        if (isOurGhostItem(e.cursor)) {
            e.isCancelled = true
            e.view.setCursor(ItemStack(Material.AIR))

            plugin.server.scheduler.runTask(plugin, Runnable {
                updateOffhand(player, player.inventory.itemInMainHand)
            })
        }

        if (e.clickedInventory == player.inventory) {
            plugin.server.scheduler.runTask(plugin, Runnable {
                updateOffhand(player, player.inventory.itemInMainHand)
            })
        }
    }

    private fun updateMissionBar(player: Player, charge: Double) {
        player.exp = (charge / 100.0).toFloat().coerceIn(0f, 1f)
        player.level = charge.toInt()
    }

    private fun updateOffhand(player: Player, mainHandItem: ItemStack?) {
        val weaponType = ItemTags.getWeaponType(mainHandItem)

        if (weaponType == WeaponType.DUAL_BLADES) {
            val customStack = CustomStack.byItemStack(mainHandItem) ?: return
            val id = customStack.namespacedID

            val offhandId = id.replace("dagger", "dagger_offhand")

            val offhandStack = CustomStack.getInstance(offhandId)?.itemStack

            if (offhandStack != null) {
                val meta = offhandStack.itemMeta
                meta.setDisplayName("§7")
                offhandStack.itemMeta = meta
                player.inventory.setItemInOffHand(offhandStack)
            }
        } else {
            val currentOffhand = player.inventory.itemInOffHand
            if (isOurGhostItem(currentOffhand)) {
                player.inventory.setItemInOffHand(ItemStack(Material.AIR))
            }
        }
    }

    private fun isOurGhostItem(item: ItemStack?): Boolean {
        if (item == null || item.type == Material.AIR) return false
        val customStack = CustomStack.byItemStack(item) ?: return false
        return customStack.namespacedID.endsWith("_offhand")
    }
}