package com.blockycraft.blockyrain.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class VotingManager {

    private boolean votingActive = false;
    private Set<String> votedPlayers = new HashSet<>();
    private int yesVotes = 0;

    // Mensagens customizáveis
    private final String MSG_VOTE_START = ChatColor.AQUA + "Está chovendo!\n" + ChatColor.AQUA + "Deseja votar para que a chuva pare?\nDigite " + ChatColor.GREEN + "/sim " + ChatColor.AQUA + "ou " + ChatColor.RED + "/nao";
    private final String MSG_VOTE_NOT_ACTIVE = ChatColor.RED + "Não há votação ativa. Aguarde a próxima chuva.";
    private final String MSG_ALREADY_VOTED = ChatColor.YELLOW + "Você já votou nesta sessão de chuva.";
    private final String MSG_VOTE_YES = ChatColor.GREEN + "Seu voto para parar a chuva foi registrado!";
    private final String MSG_VOTE_NO = ChatColor.RED + "Seu voto para continuar a chuva foi registrado!";
    private final String MSG_STOPPING_RAIN = ChatColor.AQUA + "Parando de chover...";

    // Porcentagem fixa (pode adaptar posteriormente para configuração manual)
    private final double PERCENT_REQUIRED = 0.5; // 50%

    public VotingManager() {}

    public void startVoting() {
        votingActive = true;
        votedPlayers.clear();
        yesVotes = 0;
        Bukkit.broadcastMessage(MSG_VOTE_START);
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
            player.sendMessage(MSG_VOTE_NOT_ACTIVE);
            return;
        }
        String playerName = player.getName().toLowerCase();
        if (votedPlayers.contains(playerName)) {
            player.sendMessage(MSG_ALREADY_VOTED);
            return;
        }
        votedPlayers.add(playerName);

        if (yes) {
            yesVotes++;
            player.sendMessage(MSG_VOTE_YES);
            checkVotes();
        } else {
            player.sendMessage(MSG_VOTE_NO);
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
        Bukkit.broadcastMessage(MSG_STOPPING_RAIN);
        stopVoting();
    }
}
