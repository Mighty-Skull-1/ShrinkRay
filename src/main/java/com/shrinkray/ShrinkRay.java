package com.shrinkray;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
        registerAdvancements();

        getLogger().info("ShrinkRay Ultimate Weapon System Enabled!");
    }

    // ------------------------------------------------------------------
    // Custom advancements ("Shrink Ray" tab)
    // ------------------------------------------------------------------

    private static final String ADV_ROOT = "root";
    private static final String ADV_TINY_TERROR = "tiny_terror";
    private static final String ADV_FEE_FI_FO_FUM = "fee_fi_fo_fum";
    private static final String ADV_LAB_RAT = "lab_rat";
    private static final String ADV_GROUND_POUNDER = "ground_pounder";

    private void registerAdvancements() {
        // 1.21.4 changed custom_model_data to {"floats":[...]}, 1.21.5 changed the tab background path
        boolean floatsCmd = isAtLeast(1, 21, 4);
        boolean newBackground = isAtLeast(1, 21, 5);
        String background = newBackground
                ? "minecraft:block/purple_concrete"
                : "minecraft:textures/block/purple_concrete.png";

        loadAdvancement(ADV_ROOT, null,
                rayIcon(9901, floatsCmd),
                "Shrink Ray", "light_purple",
                "Get your hands on a Shrink Ray",
                "task", false, background);

        loadAdvancement(ADV_TINY_TERROR, ADV_ROOT,
                rayIcon(9901, floatsCmd),
                "Honey, I Shrunk the Player", "aqua",
                "Shrink another player down to 25%",
                "task", false, null);

        loadAdvancement(ADV_FEE_FI_FO_FUM, ADV_ROOT,
                rayIcon(9902, floatsCmd),
                "Fee-Fi-Fo-Fum", "red",
                "Turn another player into a Giant",
                "task", false, null);

        loadAdvancement(ADV_LAB_RAT, ADV_ROOT,
                rayIcon(9903, floatsCmd),
                "Lab Rat", "yellow",
                "Blast yourself with your own Shrink Ray",
                "task", false, null);

        loadAdvancement(ADV_GROUND_POUNDER, ADV_FEE_FI_FO_FUM,
                "{\"id\":\"minecraft:anvil\"}",
                "Ground Pounder", "gold",
                "Launch a player into the air with a Giant Stomp",
                "goal", false, null);

        int loaded = 0;
        for (String id : new String[]{ADV_ROOT, ADV_TINY_TERROR, ADV_FEE_FI_FO_FUM, ADV_LAB_RAT, ADV_GROUND_POUNDER}) {
            if (Bukkit.getAdvancement(new NamespacedKey(this, id)) != null) loaded++;
        }
        getLogger().info("Loaded " + loaded + "/5 Shrink Ray advancements.");
    }

    private String rayIcon(int modelData, boolean floatsCmd) {
        String cmd = floatsCmd ? "{\"floats\":[" + modelData + "]}" : String.valueOf(modelData);
        return "{\"id\":\"minecraft:blaze_rod\",\"components\":{\"minecraft:custom_model_data\":" + cmd + "}}";
    }

    @SuppressWarnings("deprecation")
    private void loadAdvancement(String id, String parentId, String iconJson, String title, String color,
                                 String description, String frame, boolean hidden, String background) {
        NamespacedKey key = new NamespacedKey(this, id);
        if (Bukkit.getAdvancement(key) != null) {
            return; // already loaded (Bukkit saves these into the world's "bukkit" datapack)
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        if (parentId != null) {
            json.append("\"parent\":\"").append(new NamespacedKey(this, parentId)).append("\",");
        }
        json.append("\"criteria\":{\"trigger\":{\"trigger\":\"minecraft:impossible\"}},");
        json.append("\"display\":{");
        json.append("\"icon\":").append(iconJson).append(",");
        json.append("\"title\":{\"text\":\"").append(title).append("\",\"color\":\"").append(color).append("\"},");
        json.append("\"description\":{\"text\":\"").append(description).append("\",\"color\":\"gray\"},");
        if (background != null) {
            json.append("\"background\":\"").append(background).append("\",");
        }
        json.append("\"frame\":\"").append(frame).append("\",");
        json.append("\"show_toast\":true,");
        json.append("\"announce_to_chat\":true,");
        json.append("\"hidden\":").append(hidden);
        json.append("}}");

        try {
            Bukkit.getUnsafe().loadAdvancement(key, json.toString());
        } catch (Exception ex) {
            getLogger().warning("Could not load advancement " + key + ": " + ex.getMessage());
        }
    }

    /** Grants a ShrinkRay advancement (does nothing if the player already has it). */
    private void award(Player player, String id) {
        Advancement adv = Bukkit.getAdvancement(new NamespacedKey(this, id));
        if (adv == null) return;
        AdvancementProgress progress = player.getAdvancementProgress(adv);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) {
            progress.awardCriteria(criterion);
        }
    }

    /** Compares the running Minecraft version, e.g. "1.21.4-R0.1-SNAPSHOT" or "26.3.build.239-stable". */
    private boolean isAtLeast(int major, int minor, int patch) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?")
                .matcher(Bukkit.getBukkitVersion());
        if (!m.find()) return true; // unknown format: assume a newer version
        int[] v = {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                m.group(3) == null ? 0 : Integer.parseInt(m.group(3))};
        if (v[0] != major) return v[0] > major;
        if (v[1] != minor) return v[1] > minor;
        return v[2] >= patch;
    }

    // ------------------------------------------------------------------
    // Version-safe helpers (attribute and particle names changed in 1.21.x)
    // ------------------------------------------------------------------

    /** Looks an attribute up by its registry name: "scale" on 1.21.3+, "generic.scale" before that. */
    private AttributeInstance attribute(Player player, String name) {
        Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(name));
        if (attr == null) {
            attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + name));
        }
        return attr == null ? null : player.getAttribute(attr);
    }

    private AttributeInstance scaleOf(Player player) {
        return attribute(player, "scale");
    }

    /** Spawns a particle, adding default data for particles that require it on newer versions. */
    private void particle(Location loc, Particle particle, int count, double dx, double dy, double dz, double speed) {
        Class<?> type = particle.getDataType();
        try {
            if (type == Void.class) {
                loc.getWorld().spawnParticle(particle, loc, count, dx, dy, dz, speed);
            } else if (type == Float.class) {
                loc.getWorld().spawnParticle(particle, loc, count, dx, dy, dz, speed, 1.0f);
            }
        } catch (RuntimeException ignored) {
            // purely cosmetic; never let a particle break the weapon
        }
    }

    @EventHandler
    public void onCraftShrinkRay(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (isShrinkRay(event.getRecipe().getResult())) {
            award((Player) event.getWhoClicked(), ADV_ROOT);
        }
    }

    private boolean isShrinkRay(ItemStack item) {
        return item != null && item.getItemMeta() != null
                && item.getItemMeta().getPersistentDataContainer().has(rayKey, PersistentDataType.BOOLEAN);
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
            award(player, ADV_ROOT);
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
                "",
                "§eLeft-Click §7to cycle modes.",
                "§dRight-Click §7to blast a player.",
                "§bSneak + Right-Click §7to blast yourself."
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

    /**
     * Cycles the Shrink Ray mode: SHRINK -> GIANT -> GROW -> SHRINK
     */
    private void cycleWeaponMode(Player player, ItemStack item) {
        if (item == null || item.getItemMeta() == null) return;

        String currentMode = item.getItemMeta().getPersistentDataContainer().get(modeKey, PersistentDataType.STRING);
        if (currentMode == null) currentMode = "SHRINK";

        String newMode = currentMode.equalsIgnoreCase("SHRINK") ? "GIANT" : currentMode.equalsIgnoreCase("GIANT") ? "GROW" : "SHRINK";

        updateWeaponMeta(item, newMode);

        String display = newMode.equals("SHRINK") ? "§b§l« MODE: SHRINK (25%) »" :
                         newMode.equals("GIANT") ? "§c§l« MODE: GIANT (150%) »" :
                         "§e§l« MODE: RESTORE (100%) »";

        player.sendActionBar(display);
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.6f);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getItemMeta() == null) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(rayKey, PersistentDataType.BOOLEAN)) return;

        // Ensure we only handle main hand to prevent double triggers
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Covers rays picked up or traded from other players
        award(player, ADV_ROOT);

        Action action = event.getAction();

        // 1. LEFT-CLICK: Simplified mode cycling
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            cycleWeaponMode(player, item);
            return;
        }

        // 2. RIGHT-CLICK: Fire Ray or Self-Zap
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            // Cooldown check: prevent click spam / double firing
            if (player.hasCooldown(item.getType())) {
                return;
            }

            handleWeaponFire(player, item);
        }
    }

    /**
     * Optional backward-compatible Sneak + Scroll mode cycling
     */
    @EventHandler
    public void onHotbarScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getPreviousSlot());

        if (item == null || item.getItemMeta() == null) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(rayKey, PersistentDataType.BOOLEAN)) return;

        if (player.isSneaking()) {
            event.setCancelled(true);
            cycleWeaponMode(player, item);
        }
    }

    private void handleWeaponFire(Player shooter, ItemStack item) {
        String currentMode = item.getItemMeta().getPersistentDataContainer().get(modeKey, PersistentDataType.STRING);
        if (currentMode == null) currentMode = "SHRINK";

        // Apply a 10-tick (0.5s) cooldown
        shooter.setCooldown(item.getType(), 10);

        Player target = null;
        double targetDistance;

        // Self-targeting: Sneaking OR Looking down (pitch >= 75 degrees)
        if (shooter.isSneaking() || shooter.getLocation().getPitch() >= 75.0F) {
            target = shooter;
            targetDistance = 2.0;
        } else {
            Location eye = shooter.getEyeLocation();
            Vector dir = eye.getDirection().normalize();

            // Precise crosshair raytracing up to 35 blocks with bounding-box collision
            RayTraceResult entityHit = shooter.getWorld().rayTraceEntities(
                eye, dir, 35.0, 0.5,
                e -> e instanceof Player && e != shooter && !e.isDead()
            );

            if (entityHit != null && entityHit.getHitEntity() instanceof Player) {
                target = (Player) entityHit.getHitEntity();
                targetDistance = eye.distance(entityHit.getHitPosition().toLocation(shooter.getWorld()));
            } else {
                // Raytrace blocks to terminate the visual beam cleanly at obstacles
                RayTraceResult blockHit = shooter.getWorld().rayTraceBlocks(eye, dir, 35.0);
                if (blockHit != null && blockHit.getHitPosition() != null) {
                    targetDistance = eye.distance(blockHit.getHitPosition().toLocation(shooter.getWorld()));
                } else {
                    targetDistance = 25.0;
                }
            }
        }

        drawLaserBeam(shooter, currentMode, targetDistance);

        if (target != null) {
            executeRayEffect(shooter, target, currentMode);
        } else {
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 1.8f);
        }
    }

    private void executeRayEffect(Player shooter, Player victim, String mode) {
        AttributeInstance scaleAttr = scaleOf(victim);
        if (scaleAttr == null) return;

        // Purge any legacy/stuck shrink_ray attribute modifiers from the player
        purgeSpeedModifiers(victim);

        // Advancements
        if (shooter == victim) {
            award(shooter, ADV_LAB_RAT);
        } else if (mode.equalsIgnoreCase("SHRINK")) {
            award(shooter, ADV_TINY_TERROR);
        } else if (mode.equalsIgnoreCase("GIANT")) {
            award(shooter, ADV_FEE_FI_FO_FUM);
        }

        Location loc = victim.getLocation();

        if (mode.equalsIgnoreCase("SHRINK")) {
            scaleAttr.setBaseValue(0.25);
            // Native walk speed: +40% boost (default is 0.20f)
            victim.setWalkSpeed(0.28f);

            // Stealth Activation
            victim.setCustomNameVisible(false);
            victim.setSilent(true);

            particle(loc, Particle.DRAGON_BREATH, 30, 0.3, 0.5, 0.3, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_BAT_DEATH, 1.2f, 2.0f);

            sendMessagePair(shooter, victim, "§bshrunk §f" + victim.getName() + " §binto Stealth Mode!", "§c§lZAP! You are tiny, silent, and fast!");
        } else if (mode.equalsIgnoreCase("GIANT")) {
            scaleAttr.setBaseValue(1.50);
            // Native walk speed: -30% slowness penalty (default is 0.20f)
            victim.setWalkSpeed(0.14f);

            // Normal Visibility
            victim.setCustomNameVisible(true);
            victim.setSilent(false);

            particle(loc, Particle.EXPLOSION, 5, 0.5, 1.0, 0.5, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);

            sendMessagePair(shooter, victim, "§cenlarged §f" + victim.getName() + " §cto a Crushing Giant!", "§4§lZAP! You are now a heavy Giant! Jump to trigger Stomp shockwaves!");
        } else {
            // GROW / RESTORE: Clean reset to vanilla defaults
            scaleAttr.setBaseValue(1.0);
            victim.setWalkSpeed(0.20f);

            victim.setCustomNameVisible(true);
            victim.setSilent(false);

            particle(loc, Particle.HAPPY_VILLAGER, 20, 0.4, 0.8, 0.4, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.2f);

            sendMessagePair(shooter, victim, "§erestored §f" + victim.getName() + " §eto normal scale.", "§aYour structural scale and speed have been normalized.");
        }
    }

    /**
     * Completely purges any lingering shrink_ray attribute modifiers.
     */
    private void purgeSpeedModifiers(Player player) {
        AttributeInstance speedAttr = attribute(player, "movement_speed");
        if (speedAttr != null) {
            for (AttributeModifier modifier : new ArrayList<>(speedAttr.getModifiers())) {
                // toString() includes the modifier's name/key on every server version
                if (modifier.toString().toLowerCase().contains("shrink_ray")) {
                    speedAttr.removeModifier(modifier);
                }
            }
        }
    }

    /**
     * Resets a player cleanly back to standard vanilla defaults.
     */
    private void resetPlayer(Player player) {
        AttributeInstance scaleAttr = scaleOf(player);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0);
        }
        purgeSpeedModifiers(player);
        player.setWalkSpeed(0.20f);
        player.setCustomNameVisible(true);
        player.setSilent(false);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Next-tick reset to guarantee newly respawned player entity is fully initialized
        Bukkit.getScheduler().runTask(this, () -> resetPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        resetPlayer(event.getPlayer());
    }

    // 1. Mechanic: Giant Heavy Landing Shockwave Stomp Detector
    @EventHandler
    public void onGiantMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        AttributeInstance scaleAttr = scaleOf(player);
        if (scaleAttr == null || scaleAttr.getBaseValue() < 1.4) return; // Must be Giant

        // Check if player just slammed into the ground after being airborne
        if (player.getFallDistance() > 1.5 && entityIsOnGround(player)) {
            Location loc = player.getLocation();
            particle(loc, Particle.EXPLOSION_EMITTER, 2, 1.0, 0.1, 1.0, 0.2);
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.5f, 0.5f);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);

            // Blast all nearby entities into the air
            for (Entity entity : player.getNearbyEntities(6.0, 3.0, 6.0)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setVelocity(new Vector(0, 0.65, 0)); // Pop them up
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c§lCRASH! You were blown back by " + player.getName() + "'s Giant Stomp!");
                        award(player, ADV_GROUND_POUNDER);
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
            AttributeInstance scaleAttr = scaleOf(target);
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

        Color laserColor = mode.equalsIgnoreCase("SHRINK") ? Color.AQUA :
                           mode.equalsIgnoreCase("GIANT") ? Color.RED : Color.YELLOW;
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
}
