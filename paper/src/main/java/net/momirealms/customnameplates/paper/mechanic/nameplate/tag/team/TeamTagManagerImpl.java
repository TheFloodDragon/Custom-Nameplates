package net.momirealms.customnameplates.paper.mechanic.nameplate.tag.team;

import net.momirealms.customnameplates.api.CustomNameplatesPlugin;
import net.momirealms.customnameplates.api.manager.NameplateManager;
import net.momirealms.customnameplates.api.manager.TeamTagManager;
import net.momirealms.customnameplates.api.mechanic.tag.team.TeamPlayer;
import net.momirealms.customnameplates.api.scheduler.CancellableTask;
import net.momirealms.customnameplates.api.util.LogUtils;
import net.momirealms.customnameplates.paper.util.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TeamTagManagerImpl implements TeamTagManager {

    private final NameplateManager manager;
    private final ConcurrentHashMap<UUID, TeamPlayer> teamPlayerMap;
    private CancellableTask refreshTask;

    public TeamTagManagerImpl(NameplateManager manager) {
        this.manager = manager;
        this.teamPlayerMap = new ConcurrentHashMap<>();
    }

    public void load(long refreshFrequency, boolean fixTab) {
        this.refreshTask = CustomNameplatesPlugin.get().getScheduler().runTaskAsyncTimer(
                () -> {
                    try {
                        for (TeamPlayer teamPlayer : teamPlayerMap.values()) {
                            teamPlayer.updateForNearbyPlayers(false);
                        }
                    } catch (Exception e) {
                        LogUtils.severe(
                                "Error occurred when updating team tags. " +
                                "This might not be a bug in CustomNameplates. Please report " +
                                "to the Plugin on the top of the following " +
                                "stack trace."
                        );
                        e.printStackTrace();
                    }
                },
                refreshFrequency * 50L,
                refreshFrequency * 50L,
                TimeUnit.MILLISECONDS
        );
        if (fixTab) {
            //TODO
        }
    }

    public void unload() {
        if (this.refreshTask != null && !this.refreshTask.isCancelled()) {
            this.refreshTask.cancel();
        }
        for (TeamPlayer entry : teamPlayerMap.values()) {
            entry.destroy();
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public TeamPlayer createTagForPlayer(Player player, String prefix, String suffix) {
        if (this.teamPlayerMap.containsKey(player.getUniqueId())) {
            return null;
        }

        var teamPlayer = new TeamPlayer(this, player, prefix, suffix);
        this.teamPlayerMap.put(
                player.getUniqueId(),
                teamPlayer
        );
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (   online == player
                || !online.canSee(player)
                || LocationUtils.getDistance(online, player) > 48
                || online.getWorld() != player.getWorld()
                || online.isDead()
            ) continue;
            teamPlayer.addNearbyPlayer(online);
        }
        return teamPlayer;
    }

    @Override
    public TeamPlayer removeTeamPlayerFromMap(UUID uuid) {
        return teamPlayerMap.remove(uuid);
    }

    @Nullable
    public TeamPlayer getTeamPlayer(UUID uuid) {
        return teamPlayerMap.get(uuid);
    }

    public void handleEntitySpawnPacket(Player receiver, int entityId) {
        Entity spawned = manager.getEntityByEntityID(entityId);
        if (spawned == null) return;
        TeamPlayer teamPlayer = getTeamPlayer(spawned.getUniqueId());
        if (teamPlayer == null) return;
        teamPlayer.addNearbyPlayer(receiver);
    }

    public void handleEntityDestroyPacket(Player receiver, List<Integer> list) {
        for (int id : list) {
            handleSingleEntityDestroy(receiver, id);
        }
    }

    public void handlePlayerQuit(Player quit) {
        teamPlayerMap.remove(quit.getUniqueId());
        for (TeamPlayer teamPlayer : teamPlayerMap.values()) {
            teamPlayer.removeNearbyPlayer(quit);
        }
    }

    private void handleSingleEntityDestroy(Player receiver, int entityID) {
        Entity deSpawned = manager.getEntityByEntityID(entityID);
        if (deSpawned == null) return;
        TeamPlayer teamPlayer = getTeamPlayer(deSpawned.getUniqueId());
        if (teamPlayer == null) return;
        teamPlayer.removeNearbyPlayer(receiver);
    }
}
