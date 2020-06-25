package xyz.michaelzhao.mikeyminigames.games;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

enum TimerDisplay {XP, BOSS, CHAT, NONE}

public class GameTimer {

    int count, id;
    String gameName, callback, fun;
    GameData data;
    boolean countdown;
    BossBar bar;
    TimerDisplay timerDisplay;

    public GameTimer() {
        this.gameName = null;
        this.data = null;
        this.countdown = false;
        this.count = -1;
        this.callback = null;
        this.fun = null;
        this.timerDisplay = null;
        this.id = -1;
    }

    public GameTimer(String gameName, boolean countdown, int seconds, String callback, String fun, TimerDisplay timerDisplay) {
        this.gameName = gameName;
        this.data = Util.getData(gameName);
        this.countdown = countdown;
        this.count = countdown ? seconds : 0;
        this.callback = callback;
        this.fun = fun;
        this.timerDisplay = timerDisplay;
        this.id = MikeyMinigames.instance.getServer().getScheduler().scheduleSyncRepeatingTask(MikeyMinigames.instance, (this::runTimer), 20L, 20L);
    }

    public void displayTimer() {
        switch (timerDisplay) {
            case XP:
                for (Player p : data.gamePlayers.values())
                    p.setLevel(count);
                break;
            case BOSS:
                if (bar == null) bar = Bukkit.createBossBar(Integer.toString(count), BarColor.BLUE, BarStyle.SOLID);
                bar.setTitle(Integer.toString(count));
                for (Player p : data.gamePlayers.values())
                    bar.addPlayer(p);
                break;
            case CHAT:
                for (Player p : data.gamePlayers.values())
                    p.sendMessage(String.valueOf(count));
                break;
        }
    }

    /**
     * Runnable function inside of the timer
     */
    public void runTimer() {
        // Decrement/increment timer
        count += countdown ? -1 : 1;

        // Check for when the timer runs out
        if (count == 0) timerEnd();

        // Show timer
        displayTimer();

        // Run timer functions
        if (fun != null)
            timerFun();
    }

    /**
     * Runs the timer function based on the name
     */
    public void timerFun() {
        // Run function based on name
        switch (fun) {
            case "endgame":
                GameEngine.checkForEndGame(data);
                break;
        }
    }

    /**
     * Ends the timer and runs the callback function
     */
    public void timerEnd() {
        // Stop timer
        MikeyMinigames.instance.getServer().getScheduler().cancelTask(id);

        // Reset timer count
        count = 0;

        // Run callback function
        switch (callback) {
            case "start":
                GameEngine.start(data.name);
                break;
        }
    }
}
