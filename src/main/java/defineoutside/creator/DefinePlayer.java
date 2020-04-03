package defineoutside.creator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class DefinePlayer implements Serializable {
    private static final long serialVersionUID = 1L;

    private DefineTeam playerDefineTeam;
    private String inGameType = "lobby";
    private String kit = "empty";

    private int lives = 0;
    public double money = 0;

    private boolean canInfiniteRespawn = true;

    private boolean lockInGame = false;
    private boolean isFrozen = false;

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    private boolean isAlive = true;

    UUID playerUUID = null;

    public boolean isFrozen() {
        return isFrozen;
    }

    HashMap<String, String> internalToDisplayName = new HashMap<>();

    // TODO: Prevent damage while frozen
    public void setFreeze(Boolean freeze) {
        if (freeze) {
            isFrozen = true;

            // Get the pig pointing towards 0,0
            Location playerLocation = getBukkitPlayer().getLocation();

            Vector pigDirection = new Vector();
            pigDirection.setX(playerLocation.getX() * -1);
            pigDirection.setY(0);
            pigDirection.setZ(playerLocation.getZ() * -1);

            // Prevent players from moving
            Entity pig = getBukkitPlayer().getLocation().getWorld().spawnEntity(getBukkitPlayer().getLocation().setDirection(pigDirection), EntityType.PIG);
            ((LivingEntity) pig).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false));
            pig.addPassenger(getBukkitPlayer());
            pig.setCustomName("player freeze");
            pig.setCustomNameVisible(false);
            pig.setSilent(true);
            pig.setInvulnerable(true);
            ((LivingEntity) pig).setAI(false);
        } else {
            if (isFrozen) {
                isFrozen = false;

                //getBukkitPlayer().eject();
                //getBukkitPlayer().getVehicle().eject();
                getBukkitPlayer().getVehicle().setInvulnerable(false);
                getBukkitPlayer().getVehicle().remove();
            }
        }
    }

    public void createPlayer(UUID uuid) {
    }

    public void removePlayer() {
    }

    public void reset() {
        lives = 0;
        canInfiniteRespawn = true;
        isAlive = true;
    }

    public boolean playerDeathCanRespawn() {
        if (canInfiniteRespawn) {
            return true;
        } else if (lives > 0) {
            lives--;
            return true;
        }

        isAlive = false;
        return false;
    }

    public void addObjective(String objectiveName, String displayedName, Integer priority) {
        Scoreboard playerScoreboard = getBukkitPlayer().getScoreboard();
        Objective obj = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);

        Score onlineName = obj.getScore(displayedName); // Gets the score of a fake player
        onlineName.setScore(priority);

        internalToDisplayName.put(objectiveName, displayedName);
    }

    public void updateObjective(String objectiveName, String displayedName) {
        try {
            Scoreboard playerScoreboard = getBukkitPlayer().getScoreboard();
            Objective obj = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);

            Integer priority = obj.getScore(internalToDisplayName.get(objectiveName)).getScore();

            playerScoreboard.resetScores(internalToDisplayName.get(objectiveName));

            addObjective(objectiveName, displayedName, priority);
        } catch(IllegalArgumentException exception) {
            //exception.printStackTrace();
        }

    }

    public void removeObjective(String objectiveName) {
        Scoreboard playerScoreboard = getBukkitPlayer().getScoreboard();
        Objective obj = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);

        obj.getScore(internalToDisplayName.get(objectiveName));
    }

    public void createScoreboard(String hiddenName, String name) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(hiddenName, "dummy", name);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        getBukkitPlayer().setScoreboard(board);
    }

    public String getScoreboardName() {
        return getBukkitPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName();
    }

    // All getters and setters below here
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getName() {
        return getBukkitPlayer().getName();
    }

    public void sendMessage(String message) {
        getBukkitPlayer().sendMessage(message);
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    // Assume the player is alone without a team
    public DefineTeam getPlayerDefineTeam() {
        return playerDefineTeam;
    }

    public void setPlayerDefineTeam(DefineTeam playerDefineTeam) {
        playerDefineTeam.addPlayer(this);
        this.playerDefineTeam = playerDefineTeam;
    }


    public int getLives() {
        return lives;
    }


    public void setLives(int lives) {
        this.lives = lives;
    }


    public boolean getCanInfiniteRespawn() {
        return canInfiniteRespawn;
    }


    public void setCanInfiniteRespawn(boolean canRespawn) {
        this.canInfiniteRespawn = canRespawn;
    }

    public String getInGameType() {
        return inGameType;
    }

    public void setInGameType(String inGameType) {
        this.inGameType = inGameType;
    }

    public boolean isLockInGame() {
        return lockInGame;
    }

    public void setLockInGame(boolean lockInGame) {
        this.lockInGame = lockInGame;
    }

    public String getKit() {
        return kit;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
