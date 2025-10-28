package com.blockycraft.blockyrain.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class VotingManager {

    private boolean votingActive = false;
    private Set<String> votedPlayers = new HashSet<>();
    private int yesVotes = 0;

    private final double PERCENT_REQUIRED;
    private final Properties props;

    public VotingManager(com.blockycraft.blockyrain.BlockyRain plugin) {
        this.props = plugin.getPluginProperties();
        this.PERCENT_REQUIRED = getPercentRequiredFromConfig();
    }

    private double getPercentRequiredFromConfig() {
        try {
            return Integer.parseInt(props.getProperty("percent_required", "50")) / 100.0;
        } catch (Exception e) {
            return 0.5;
        }
    }

    private void broadcastMultiLine(String msg) {
        for (String linha : msg.split("\n")) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', linha));
        }
    }

    private String getCfg(String key) {
        String value = props.getProperty(key);
        return value != null ? value : key;
    }

    public void startVoting() {
        votingActive = true;
        votedPlayers.clear();
        yesVotes = 0;
        broadcastMultiLine(getCfg("vote_start"));
    }

    public void stopVoting() {
        votingActive = false;
        votedPlayers.clear();
        yesVotes = 0;
    }

    public boolean isVotingActive() {
        return votingActive;
    }

    public void handleVote(CommandSender sender, boolean yes) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem votar.");
            return;
        }
        Player player = (Player) sender;

        if (!votingActive) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getCfg("vote_not_active")));
            return;
        }

        String playerName = player.getName().toLowerCase();
        if (votedPlayers.contains(playerName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getCfg("already_voted")));
            return;
        }
        votedPlayers.add(playerName);

        if (yes) {
            yesVotes++;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getCfg("vote_yes_received")));
            checkVotes();
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getCfg("vote_no_received")));
        }
    }

    private void checkVotes() {
        Player[] online = Bukkit.getServer().getOnlinePlayers();
        int onlinePlayers = online.length;
        if (onlinePlayers == 1 && yesVotes == 1) {
            stopRain();
            return;
        }
        int votesNeeded = (int) Math.ceil(PERCENT_REQUIRED * onlinePlayers);

        if (yesVotes >= votesNeeded) {
            stopRain();
        }
    }

    private void stopRain() {
        Bukkit.getServer().getWorlds().get(0).setStorm(false);
        broadcastMultiLine(getCfg("stopping_rain"));
        stopVoting();
    }
}
