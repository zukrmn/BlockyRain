package com.blockycraft.blockyrain;

import org.bukkit.plugin.java.JavaPlugin;
import com.blockycraft.blockyrain.managers.VotingManager;
import com.blockycraft.blockyrain.listeners.WeatherListener;

public class BlockyRain extends JavaPlugin {

    private VotingManager votingManager;

    @Override
    public void onEnable() {
        votingManager = new VotingManager();

        getServer().getPluginManager().registerEvents(new WeatherListener(votingManager), this);

        getCommand("sim").setExecutor((sender, command, label, args) -> {
            votingManager.handleVote(sender, true);
            return true;
        });

        getCommand("nao").setExecutor((sender, command, label, args) -> {
            votingManager.handleVote(sender, false);
            return true;
        });
    }

    @Override
    public void onDisable() {
        // Nenhuma ação de limpeza por padrão
    }

    public VotingManager getVotingManager() {
        return votingManager;
    }
}
