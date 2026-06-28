package com.shrinkray;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class ShrinkRay extends JavaPlugin implements CommandExecutor, Listener {

    private NamespacedKey rayKey;
    private NamespacedKey modeKey;

    @Override
    public void onEnable() {
        this.rayKey = new NamespacedKey(this, "shrink_ray_weapon");
        this.modeKey = new NamespacedKey(this, "shrink_ray_mode");
        
        if (this.getCommand("shrinkray") != null) {
            this.getCommand("shrinkray").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
        
        registerShrinkRayRecipe();
        
        getLogger().info("ShrinkRay Ultimate Weapon System Enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("give")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can run this command.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("shrinkray.admin")) {
                player.sendMessage("§cYou don't have permission!");
                return true;
            }
            
            player.getInventory().addItem(createShrinkRayWeapon());
            player.sendMessage("§b§l[ShrinkRay] §aYou have been given the Shrink Ray!");
            return true;
        }
        sender.sendMessage("§cUsage: /shrinkray give");
        return true;
    }

    private ItemStack createShrinkRayWeapon() {
        ItemStack ray = new ItemStack(Material.BLAZE_ROD);
        updateWeaponMeta(ray, "SHRINK");
        return ray;
    }

    private void updateWeaponMeta(ItemStack item, String mode) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String colorMode;
            if (mode.equalsIgnoreCase("SHRINK")) {
                colorMode = "§b§lSHRINK (25%)";
            } else if (mode.equalsIgnoreCase("GIANT")) {
                colorMode = "§c§lGIANT (150%)";
            } else {
                colorMode = "§e§lGROW (100%)";
            }
            
            meta.setDisplayName("§d§lSHRINK RAY §7«" + colorMode + "§7»");
            meta.setLore(Arrays.asList(
                "§7§oA highly dangerous prototype device.",
                "",
                "§7Current Mode: " + colorMode,
                "§fSneak + Scroll §7to cycle modes.",
                "",
                "§dRight-Click §7to blast a player.",
                "§dLook Straight Down + Right-Click §7to blast yourself."
            ));
            
            meta.getPersistentDataContainer().set(rayKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, mode.toUpperCase());
            
            meta.setCustomModelData(9901);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
    }

    private void registerShrinkRayRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(this, "shrink_ray_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createShrinkRayWeapon());
        
        recipe.shape("DED", "RBR", "INI");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('N', Material.NETHER_STAR);
        
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onHotbarScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getPreviousSlot());

        if (item == null || item.getItemMeta() == null) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(rayKey, PersistentDataType.BOOLEAN)) return;

        if (player.isSneaking()) {
            event.setCancelled(true);
            
            String currentMode = item.getItemMeta().getPersistentDataContainer().get(modeKey, PersistentDataType.STRING);
            if (currentMode == null) currentMode = "SHRINK";
            
            String newMode = currentMode.equalsIgnoreCase("SHRINK") ? "GIANT" : currentMode.equalsIgnoreCase("GIANT") ? "GROW" : "SHRINK";
            
            updateWeaponMeta(item, newMode);
            player.getInventory().setItem(event.getPreviousSlot(), item);
            
            String display = newMode.equals("SHRINK") ? "§b§l« MODE: SHRINK (25%) »" : newMode.equals("GIANT") ? "§c§l« MODE: GIANT (150%) »" : "§e§l« MODE: RESTORE (100%) »";
            player.sendActionBar(display);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.5f);
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player shooter = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getItemMeta() == null) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(rayKey, PersistentDataType.BOOLEAN)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String currentMode = item.getItemMeta().getPersistentDataContainer().get(modeKey, PersistentDataType.STRING);
            if (currentMode == null) currentMode = "SHRINK";

            Player target;
            double targetDistance;
            
            if (shooter.getLocation().getPitch() >= 75.0F) {
                target = shooter;
                targetDistance = 2.0;
            } else {
                Entity entity = getTargetEntity(shooter, 35);
                target = (entity instanceof Player) ? (Player) entity : null;
                targetDistance = target != null ? shooter.getLocation().distance(target.getLocation()) : 25.0;
            }

            drawLaserBeam(shooter, currentMode, targetDistance);

            if (target != null) {
                executeRayEffect(shooter, target, currentMode);
            } else {
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 1.8f);
            }
        }
    }

    private void executeRayEffect(Player shooter, Player victim, String mode) {
        AttributeInstance scaleAttr = victim.getAttribute(Attribute.GENERIC_SCALE);
        AttributeInstance speedAttr = victim.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        
        if (scaleAttr == null || speedAttr == null) return;

        boolean hasModifier = true;
        while (hasModifier) {
            hasModifier = false;
            for (AttributeModifier modifier : speedAttr.getModifiers()) {
                if (modifier.getName().equalsIgnoreCase("shrink_ray_speed")) {
                    speedAttr.removeModifier(modifier);
                    hasModifier = true;
                    break; 
                }
            }
        }

        Location loc = victim.getLocation();

        if (mode.equalsIgnoreCase("SHRINK")) {
            scaleAttr.setBaseValue(0.25);
            
            AttributeModifier speedBoost = new AttributeModifier("shrink_ray_speed", 0.06, AttributeModifier.Operation.ADD_NUMBER);
            speedAttr.addModifier(speedBoost);
            
            // Stealth Activation
            victim.setCustomNameVisible(false);
            victim.setSilent(true);
            
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 30, 0.3, 0.5, 0.3, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_BAT_DEATH, 1.2f, 2.0f);
            
            sendMessagePair(shooter, victim, "§bshrunk §f" + victim.getName() + " §binto Stealth Mode!", "§c§lZAP! You are tiny, silent, and fast!");
        } else if (mode.equalsIgnoreCase("GIANT")) {
            scaleAttr.setBaseValue(1.50);
            
            AttributeModifier heavySlowness = new AttributeModifier("shrink_ray_speed", -0.04, AttributeModifier.Operation.ADD_NUMBER);
            speedAttr.addModifier(heavySlowness);
            
            // Normal Visibility
            victim.setCustomNameVisible(true);
            victim.setSilent(false);
            
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 0.5, 1.0, 0.5, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
            
            sendMessagePair(shooter, victim, "§cenlarged §f" + victim.getName() + " §cto a Crushing Giant!", "§4§lZAP! You are now a heavy Giant! Jump to trigger Stomp shockwaves!");
        } else {
            scaleAttr.setBaseValue(1.0);
            
            victim.setCustomNameVisible(true);
            victim.setSilent(false);
            
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 20, 0.4, 0.8, 0.4, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.2f);
            
            sendMessagePair(shooter, victim, "§erestored §f" + victim.getName() + " §eto normal scale.", "§aYour structural scale and speed have been normalized.");
        }
    }

    // 1. Mechanic: Giant Heavy Landing Shockwave Stomp Detector
    @EventHandler
    public void onGiantMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr == null || scaleAttr.getBaseValue() < 1.4) return; // Must be Giant

        // Check if player just slammed into the ground after being airborne
        if (player.getFallDistance() > 1.5 && entityIsOnGround(player)) {
            Location loc = player.getLocation();
            loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 1.0, 0.1, 1.0, 0.2);
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.5f, 0.5f);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);

            // Blast all nearby entities into the air
            for (Entity entity : player.getNearbyEntities(6.0, 3.0, 6.0)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setVelocity(new Vector(0, 0.65, 0)); // Pop them up
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c§lCRASH! You were blown back by " + player.getName() + "'s Giant Stomp!");
                    }
                }
            }
        }
    }

    // 2. Mechanic: Stealth Aggro Ignorer (Mobs ignore tiny players)
    @EventHandler
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            AttributeInstance scaleAttr = target.getAttribute(Attribute.GENERIC_SCALE);
            if (scaleAttr != null && scaleAttr.getBaseValue() <= 0.3) {
                event.setCancelled(true); // Mob ignores you completely
            }
        }
    }

    private boolean entityIsOnGround(Player p) {
        return ((Entity) p).isOnGround();
    }

    private void drawLaserBeam(Player shooter, String mode, double maxDistance) {
        Location start = shooter.getEyeLocation().subtract(0, 0.2, 0);
        Vector direction = shooter.getLocation().getDirection().normalize();
        
        Color laserColor = mode.equalsIgnoreCase("SHRINK") ? Color.AQUA : mode.equalsIgnoreCase("GIANT") ? Color.RED : Color.YELLOW;
        Particle.DustOptions dust = new Particle.DustOptions(laserColor, 1.25f);
        start.getWorld().playSound(start, Sound.BLOCK_CONDUIT_ACTIVATE, 1.2f, 1.6f);

        for (double d = 0.5; d < maxDistance; d += 0.3) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(Particle.DUST, point, 1, dust);
        }
    }

    private void sendMessagePair(Player shooter, Player victim, String shooterMsg, String victimMsg) {
        if (shooter == victim) {
            shooter.sendMessage("§b§l[ShrinkRay] §aYou blasted yourself! " + victimMsg);
        } else {
            shooter.sendMessage("§b§l[ShrinkRay] §aYou " + shooterMsg);
            victim.sendMessage(victimMsg);
        }
    }

    private Entity getTargetEntity(Player player, int range) {
        return player.getWorld().getNearbyEntities(player.getEyeLocation(), range, range, range).stream()
                .filter(entity -> entity instanceof Player && entity != player)
                .filter(entity -> player.hasLineOfSight(entity))
                .findFirst().orElse(null);
    }
}
