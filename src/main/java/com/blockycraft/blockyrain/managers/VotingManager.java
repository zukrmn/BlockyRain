package com.blockycraft.blockyrain.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.List;

public class VotingManager {

    private boolean votingActive = false;
    private Set<String> votedPlayers = new HashSet<>();
    private int yesVotes = 0;
    private final double PERCENT_REQUIRED;
    private final Properties props;
    private final int CHAT_WRAP = 50;

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

    // Word wrap com cor para todos os broadcasts
    private List<String> wrapTextWithColor(String text, int maxLength, String baseColor) {
        List<String> lines = new java.util.ArrayList<String>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        String color = baseColor;
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxLength) {
                lines.add(color + line.toString());
                color = getLastColor(line.toString().isEmpty() ? baseColor : line.toString());
                line = new StringBuilder();
            }
            if (line.length() > 0) line.append(" ");
            line.append(word);
        }
        if (line.length() > 0) lines.add(color + line.toString());
        return lines;
    }
    private String getLastColor(String text) {
        String color = "";
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                color = text.substring(i, i+2);
                break;
            }
        }
        return color;
    }

    // Mensagens multi-linha com word wrap e cor do chat
    private void broadcastMultiLine(String msg) {
        String[] original = msg.split("\\n");
        for (String linha : original) {
            String baseColor = getLastColor(linha.length() > 0 ? linha.substring(0,2) : "§e");
            for (String wrap : wrapTextWithColor(ChatColor.translateAlternateColorCodes('&', linha), CHAT_WRAP, baseColor.isEmpty() ? "§e" : baseColor)) {
                Bukkit.broadcastMessage(wrap);
            }
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
            for (String linha : wrapTextWithColor(ChatColor.translateAlternateColorCodes('&', getCfg("vote_not_active")), CHAT_WRAP, "§c"))
                player.sendMessage(linha);
            return;
        }
        String playerName = player.getName().toLowerCase();
        if (votedPlayers.contains(playerName)) {
            for (String linha : wrapTextWithColor(ChatColor.translateAlternateColorCodes('&', getCfg("already_voted")), CHAT_WRAP, "§e"))
                player.sendMessage(linha);
            return;
        }
        votedPlayers.add(playerName);
        if (yes) {
            yesVotes++;
            for (String linha : wrapTextWithColor(ChatColor.translateAlternateColorCodes('&', getCfg("vote_yes_received")), CHAT_WRAP, "§a"))
                player.sendMessage(linha);
            checkVotes();
        } else {
            for (String linha : wrapTextWithColor(ChatColor.translateAlternateColorCodes('&', getCfg("vote_no_received")), CHAT_WRAP, "§c"))
                player.sendMessage(linha);
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
