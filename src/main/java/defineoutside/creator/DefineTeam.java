package defineoutside.creator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DefineTeam implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<DefinePlayer> uuidInTeam = new ArrayList<>();
    public boolean allowTeamDamage = false;
    public String name = UUID.randomUUID().toString();
    public Material woolType = Material.WHITE_WOOL;
    public ChatColor chatColor = ChatColor.WHITE;
    public List<Location> spawns = new ArrayList<>();

    Random rand = new Random();

    int spawnIterator = 0;

    public void addPlayer(DefinePlayer uuid) {
        uuidInTeam.add(uuid);
    }

    public void removePlayer(DefinePlayer uuid) {
        uuidInTeam.remove(uuid);
    }

    public int getSizeOfTeam() {
        return uuidInTeam.size();
    }

    public boolean isPlayerOnTeam(DefinePlayer uuid) {
        return uuidInTeam.contains(uuid);
    }

    public ArrayList<DefinePlayer> getUuidInTeam() {
        return uuidInTeam;
    }

    public boolean isAllowTeamDamage() {
        return allowTeamDamage;
    }

    public void setAllowTeamDamage(boolean allowTeamDamage) {
        this.allowTeamDamage = allowTeamDamage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addSpawn(Location location) {
        spawns.add(location);
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public void setSpawns(List<Location> spawns) {
        this.spawns = spawns;
    }

    public Location getNextSpawn() {
        spawnIterator++;

        if (spawnIterator >= spawns.size()) {
            spawnIterator = 0;
        }

        return spawns.get(spawnIterator);
    }

    // Meant to check if a player is at the correct position before the game begins
    public boolean containsSpawn(int x, int z) {
        for (Location location : getSpawns()) {
            if ((int) location.getX() == x && (int) location.getZ() == z) {
                return true;
            }
        }
        return false;
    }

    public Location getRandomSpawn() {
        return spawns.get(rand.nextInt(spawns.size()));
    }

    public Material getWoolType() {
        return woolType;
    }

    public void setWoolType(Material woolType) {
        this.woolType = woolType;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
    }
}
