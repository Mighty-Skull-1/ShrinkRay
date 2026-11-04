# Shrink Ray ⚡

A high-production-value, mechanic-shifting Paper/Spigot Minecraft plugin that introduces a dynamic **Shrink Ray** weapon into the game. Change player scales on the fly, execute stealth missions, or smash into battle as a heavy giant.

## 👤 Author
* **Mighty**

## 🚀 Features

* **3 Dynamic Modes:**
  * **SHRINK (25% Size):** Grants a significant movement speed boost, activates *Stealth Mode* (hides your name tag and makes your movements entirely silent), and makes monsters completely ignore you.
  * **GROW (100% Size):** Safely restores a player's physical scale, stealth status, and movement speed back to vanilla defaults.
  * **GIANT (150% Size):** Increases your size by 50% at the cost of a slowness movement penalty. Falling or jumping from high places triggers a massive **Ground Stomp Shockwave** that blasts nearby entities into the air with vertical knockback.
* **Simplified Controls:**
  * **Left-Click:** Cycle modes (`SHRINK` → `GIANT` → `GROW / RESTORE`).
  * **Right-Click:** Fire laser beam at targeted player (precision crosshair raytracing).
  * **Sneak + Right-Click:** Blast yourself instantly with the active mode (looking down with pitch $\ge 75^\circ$ + Right-Click also supported).
* **Cinematic Visuals & Audio:** Features bright, color-coded particle laser beams (Aqua for Shrink, Red for Giant, Yellow for Restore) paired with custom high-tech sound design for weapon triggers, mode switches, and target impacts.

## 🏆 Advancements

The plugin adds its own **Shrink Ray** advancement tab:

| Advancement | How to get it |
|---|---|
| **Shrink Ray** | Craft, receive, or use a Shrink Ray |
| **Honey, I Shrunk the Player** | Shrink another player down to 25% |
| **Fee-Fi-Fo-Fum** | Turn another player into a Giant |
| **Lab Rat** | Blast yourself with your own Shrink Ray |
| **Ground Pounder** *(goal)* | Launch a player into the air with a Giant Stomp |

With the Shrink Ray resource pack, the icons show the ray gun in its cyan, red and yellow mode colours.

---

## 🛠️ Custom Crafting Recipe

The Shrink Ray can be crafted in survival mode using a standard Crafting Table with the following grid layout:

```text
[ Diamond ]    [ Eye of Ender ]    [ Diamond ]
[ Redstone ]   [  Blaze Rod   ]    [ Redstone ]
[Iron Ingot]   [ Nether Star  ]    [Iron Ingot]
```

## 💻 Commands & Permissions
* `/shrinkray give` - Gives the executive Shrink Ray item to the user.
* **Permission:** `shrinkray.admin`

## 🔧 Build Requirements
* **Server Software:** Paper, Purpur, or Spigot (Minecraft version 1.20.5+ required for native attribute scaling).
* **Java Version:** Java 17 or higher.
* **Build Tool:** Maven.

### Compiling via CLI
To compile the plugin into a deployment-ready `.jar`, open your terminal and run:
```bash
mvn clean package
```
The compiled file will be located inside the `/target` directory as `ShrinkRay-1.2.0.jar`.

---

## 🐛 Bug Reports & Feature Requests

Found a bug or have a suggestion for the next update? Please report it on our official [GitHub Issue Tracker](https://github.com/Mighty-Skull-1/ShrinkRay/issues).
