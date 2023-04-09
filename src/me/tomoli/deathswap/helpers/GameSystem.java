package me.tomoli.deathswap.helpers;

import me.tomoli.deathswap.types.CountdownType;
import me.tomoli.deathswap.DeathSwap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameSystem {
    public static final GameSystem instance = new GameSystem();
    private boolean gameEnabled = false;
    private int betweenTaskId = 0;
    private int randomTime = 0;
    private long countStarted = 0;

    // public methods, these should be exposed
    public void enableGame() {
        gameEnabled = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.GREEN+"Starting game now!");
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setTotalExperience(0);
            player.setFallDistance(0);
            player.setHealth(20);
            player.setFoodLevel(20);
            for(PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0,1,0));
        }
        handleCountdown(10, CountdownType.START);
    }

    public void disableGame() {
        gameEnabled = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.RED+"Game has been stopped!");
            player.setGameMode(GameMode.SPECTATOR);
        }
        Bukkit.getScheduler().cancelTask(betweenTaskId);
    }

    // should not be used usually - exposed only for debugging commands. output should not be parsed
    public String debuggingData() {
        List<Object> list = Arrays.asList(
                gameEnabled,
                (countStarted + randomTime) - (Instant.now().getEpochSecond()),
                randomTime,
                betweenTaskId
        );
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    // check for winners on death
    public void checkWinCondition() {
        int playersAlive = 0;
        Player lastPlayerAlive = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                playersAlive++;
                lastPlayerAlive = player;
            }
        }
        if(playersAlive == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.YELLOW+lastPlayerAlive.getDisplayName()+ChatColor.GREEN+" has won the game!");
            }
            disableGame();
        }
    }

    // private internal methods

    private void handleCountdown(int time, CountdownType type) {
        if(!gameEnabled) return;
        if(time > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DeathSwap.instance, () -> {
                handleCountdown(time - 1, type);
                if(type.equals(CountdownType.SWAP)) {
                    warnSwap(time - 1);
                }
            }, 20L); // 20 game ticks = 1 second
        } else {
            switch(type) {
                case START -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(ChatColor.GREEN+"The game has begun! When the warning starts, get yourself into position so you can trap the person swapping to you!");
                    }
                    randomSwapTime();
                }
                case SWAP -> {
                    checkWinCondition();
                    teleportRandoms();
                    randomSwapTime();
                }
                default -> throw new IllegalArgumentException("invalid countdown type passed - " + type);
            }
        }
    }

    private void randomSwapTime() {
        Bukkit.getScheduler().cancelTask(betweenTaskId);
        randomTime = ThreadLocalRandom.current().nextInt(30, 300 + 1);
        countStarted = Instant.now().getEpochSecond();
        // origin = min, bound = max | between 1 min and 5 mins
        betweenTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(DeathSwap.instance, () -> handleCountdown(11, CountdownType.SWAP), randomTime * 20L);
    }

    private void warnSwap(int seconds) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.RED+"Swapping in "+ChatColor.YELLOW+seconds+ChatColor.RED+" seconds!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1f, 1f);
        }
    }

    private void teleportRandoms() {
        ArrayList<Location> playerLocations = new ArrayList<>();
        HashMap<Location, Player> playerLocationMap = new HashMap<>();
        HashMap<Player, Location> playerLocationMapReverse = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                Location loc = player.getLocation(); // stop funky business
                playerLocations.add(loc);
                playerLocationMap.put(loc, player);
                playerLocationMapReverse.put(player, loc);
            }
        }
        Collections.shuffle(playerLocations);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                Location[] potentials = playerLocations.stream().filter(x -> !x.equals(playerLocationMapReverse.get(player))).toArray(Location[]::new);
                if(potentials.length != 0) {
                    Location loc = potentials[0];
                    playerLocations.remove(loc);
                    player.teleport(loc);
                    player.sendMessage(ChatColor.RED + "Teleported to " + ChatColor.YELLOW + playerLocationMap.get(loc).getDisplayName() + ChatColor.RED + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1f, 1f);
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,20,100));
                    player.sendMessage(ChatColor.RED + "You would have swapped with yourself, so you have been given resistance to avoid your trap.");
                    player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation());
                }
                player.setFallDistance(0);
                player.setFireTicks(0);
            }
        }
    }
}
