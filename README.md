# Shrink Ray ⚡

A high-production-value, mechanic-shifting Paper/Spigot Minecraft plugin that introduces a dynamic **Shrink Ray** weapon into the game. Change player scales on the fly, execute stealth missions, or smash into battle as a heavy giant.

## 👤 Author
* **Mighty**

## 🚀 Features

* **3 Dynamic Modes (Sneak + Scroll to Cycle):**
  * **SHRINK (25% Size):** Grants a significant movement speed boost, activates *Stealth Mode* (hides your name tag and makes your movements entirely silent), and makes monsters completely ignore you.
  * **GROW (100% Size):** Safely restores a player's physical scale, stealth status, and movement speed back to vanilla defaults.
  * **GIANT (150% Size):** Increases your size by 50% at the cost of a slowness movement penalty. Falling or jumping from high places triggers a massive **Ground Stomp Shockwave** that blasts nearby entities into the air with vertical knockback.
* **Self-Targeting:** Look straight down (75° pitch or lower) and right-click to blast yourself with the active mode.
* **Cinematic Visuals & Audio:** Features bright, color-coded particle laser beams (Aqua for Shrink, Red for Giant, Yellow for Restore) paired with custom high-tech sound design for weapon triggers, mode switches, and target impacts.

---

## 🛠️ Custom Crafting Recipe

The Shrink Ray can be crafted in survival mode using a standard Crafting Table with the following grid layout:
<img width="510" height="240" alt="image" src="https://github.com/user-attachments/assets/9a2df776-537d-4cf0-a267-faa58888b5b3" />


```text
[ Diamond ]    [ Eye of Ender ]    [ Diamond ]
[Redstone ]    [  Blaze Rod   ]    [Redstone ]
[IronIngot]    [ Nether Star  ]    [IronIngot]

💻 Commands & Permissions
/shrinkray give - Gives the executive Shrink Ray item to the user.

Permission: shrinkray.admin

🔧 Build Requirements
Server Software: Paper, Purpur, or Spigot (Minecraft version 1.20.5+ required for native attribute scaling).

Java Version: Java 17 or higher.

Build Tool: Maven.

Compiling via CLI
To compile the plugin into a deployment-ready .jar, open your terminal and run:
mvn clean package

The compiled file will be located inside the /target directory as ShrinkRay-1.1.0.jar.
EOF

Stage, commit, and push the README straight to your repository!
git add README.md
git commit -m "docs: Added a comprehensive README.md file"
git push origin main

---

## 🐛 Bug Reports & Feature Requests

Found a bug or have a suggestion for the next update? Please report it on our official [GitHub Issue Tracker](https://github.com/Mighty-Skull-1/ShrinkRay/issues). 

Before submitting, check the open issues to see if someone else has already reported it!
