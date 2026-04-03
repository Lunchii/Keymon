# Keymon — Typing Adventure Game

**CS2212 Group Project | Group 72 | Winter 2026 | Western University**  
**Team:** Isaac, Miguel, Ryan, Nathaniel, Dehou

---

## Description

Keymon is a Java desktop typing-adventure game inspired by classic RPG games. Players explore tile-based maps using the WASD keys, encounter wild Keymon creatures and NPC trainers in typing battles, and progress through three increasingly difficult stages. Each battle challenges the player to type words quickly and accurately before the timer runs out. Correct typing deals damage to the enemy, while mistakes drain the player's Focus meter. Players manage a party of up to six Keymon creatures, use consumable powerups from their bag, and earn points and achievement badges that are saved to a local profile.

---

## Requirements

| Requirement | Version |
|---|---|
| Java Development Kit (JDK) | 21 or later |
| Gson (included in `lib/`) | 2.10.1 |

No other third-party libraries or tools are required. Gson is bundled in the `lib/` folder and does not need to be installed separately.

---

## Building from Source

> These instructions are written for **Windows 11** using a standard Command Prompt or PowerShell window.

### Step 1 — Install Java

1. Download the latest JDK 21 (or newer) from https://adoptium.net or https://www.oracle.com/java/technologies/downloads/
2. Run the installer and follow the on-screen steps.
3. Verify the installation by opening a Command Prompt and running:
   ```
   java -version
   javac -version
   ```
   Both commands should print a version number of 21 or higher.

### Step 2 — Obtain the Project

Clone the repository or extract the submitted zip archive. The project root should contain a `src/` folder, a `lib/` folder, and asset files (`.png`, `.csv`, `.ttf`, `.json`, `.gif`).

### Step 3 — Compile

Open a Command Prompt in the `group72/` directory (the folder containing `src/` and `lib/`) and run:

```
javac -cp "lib\gson-2.10.1.jar" -d out src\*.java
```

This compiles all source files into the `out/` directory. No errors should appear. Warnings about line endings can be ignored.

---

## Running the Application

From the same `group72/` directory, run:

```
java -cp "out;lib\gson-2.10.1.jar" Main
```

The game window will open maximised. All asset files (images, fonts, maps, save files) must be in the `group72/` working directory, which is the default when launching from there.

> **Note:** Use a semicolon (`;`) as the classpath separator on Windows. On macOS/Linux use a colon (`:`).

---

## User Guide

### Main Menu

Use **W / S** or the mouse to highlight a button, then press **Enter** or click to select.

| Button | Action |
|---|---|
| LOGIN | Log in or create a new trainer account |
| HIGH SCORES | View the global top-10 leaderboard |
| TUTORIAL | Read the instructions or play the guided tutorial |
| PARENTAL CONTROLS | Teacher/parent dashboard (PIN required) |
| EXIT | Close the application |

### Creating an Account

1. Click **LOGIN** from the main menu.
2. Enter a username (3–16 letters, numbers, dashes, or underscores) and a password (minimum 4 characters).
3. Click **CREATE ACCOUNT**. You will be taken to your trainer hub automatically.

### Player Hub

After logging in you will see your trainer hub, which shows your best score, highest unlocked stage, typing statistics, coaching tips, and earned badges.

| Button | Action |
|---|---|
| CONTINUE JOURNEY | Resume from your last saved position |
| SELECT STAGE | Choose which stage to play |
| VIEW FULL STATS | Open the detailed statistics screen |
| LOGOUT | Return to the main menu |

### Exploring the Map

| Key | Action |
|---|---|
| W | Move up |
| S | Move down |
| A | Move left |
| D | Move right |
| ESC | Open / close the pause menu |
| P | View your Keymon party |
| B | Open your item bag |

Walk through **tall grass** tiles to trigger random wild Keymon encounters. Walk into the **trainer's line of sight** (directly in front of them, up to 4 tiles) to start a trainer battle. Reach the **dark exit tile** to complete the stage.

### Battles

1. Press **ATTACK** to start your typing turn.
2. A 3-second countdown begins, then a word appears on screen.
3. Type the word and press **Enter** before the timer runs out.
4. Correct letters are highlighted green as you type; mistakes are shown in red and reduce your Focus bar.
5. Every correctly typed word deals damage to the enemy equal to your active Keymon's attack plus a word-length bonus.
6. After the player turn, the enemy attacks and deals fixed damage to your active Keymon.
7. Win by reducing the enemy's HP to zero. Lose if all your Keymon faint or your Focus bar reaches zero.

Other battle options:

| Button | Action |
|---|---|
| BAG [B] | Use a consumable item (costs your turn) |
| SWITCH | Swap your active Keymon (costs your turn) |
| RUN | Retreat safely back to the map |

### Items

| Item | Effect |
|---|---|
| Potion | Heals active Keymon for 30 HP |
| Full Heal | Fully restores active Keymon's HP |
| Attack Boost | Doubles attack damage for the next typing turn |
| Slow Time | Adds 5 seconds to the next typing turn timer |

### Pause Menu

Press **ESC** during exploration to pause. Use **W / S** to navigate options and **Enter** to select.

| Option | Action |
|---|---|
| RESUME | Return to the game |
| RESTART STAGE | Restart the current stage from the beginning |
| VIEW STATS | Show live typing stats for the current session |
| QUIT TO HUB | Save progress and return to the trainer hub |

### Saving

Progress is saved automatically when you complete a stage, fail a stage, or quit to the hub via the pause menu. Your position on the map, party HP, item counts, and statistics are all persisted locally in a `<username>_profile.json` file.

---

## Demo Accounts

The following accounts are included for testing and can be used immediately without creating a new account:

| Username | Password | Notes |
|---|---|---|
| `ryan` | `1234` | Has completed Stage 1, Stage 2 unlocked, team of 3 Keymon |
| `Miguel` | `Miguel` | Starter account |

---

## Parental Controls

The parental controls screen is accessible from the main menu via the **PARENTAL CONTROLS** button.

**Teacher PIN:** `2212`

From the parental controls dashboard a teacher can:
- View all registered player accounts and their statistics
- Create new student accounts
- Reset a student's password
- Reset a student's statistics
- Delete a student account
- Clear or seed the global high score table

---

## Stages

| Stage | Name | Words | Timer per turn |
|---|---|---|---|
| 1 | Meadow Path | Short, common words | 12 seconds |
| 2 | Shadow Grove | Medium words | 10 seconds |
| 3 | Sky Summit | Long and complex words | 8 seconds |

Stage 2 unlocks after completing Stage 1. Stage 3 unlocks after completing Stage 2.

---

## Generating Javadoc

From the `group72/` directory, run:

```
javadoc -cp "lib\gson-2.10.1.jar" -d docs src\*.java
```

The generated HTML documentation will be placed in the `docs/` folder. Open `docs\index.html` in any browser to browse it.

---

## Project Structure

```
group72/
├── src/              Java source files
├── lib/              Third-party JARs (Gson 2.10.1)
├── out/              Compiled class files
├── keymonsters/      Keymon sprite images
├── *.csv             Tile map definitions for each stage
├── *.png / *.gif     Sprite and background assets
├── *.ttf             Custom font (KA1)
├── *_profile.json    Player save files
├── highscores.json   Global leaderboard data
└── README.md         This file
```

---

## Known Limitations

- Save files are stored locally in the working directory; they are not encrypted.
- The teacher PIN is hardcoded to `2212`.
- The animated menu background (`menuBackground.gif`) is a large file and may take a moment to load on slower machines.
