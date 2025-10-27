package com.blockycraft.blockyrain.listeners;

import com.blockycraft.blockyrain.managers.VotingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {

    private final VotingManager votingManager;

    public WeatherListener(VotingManager votingManager) {
        this.votingManager = votingManager;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) { // Começou a chover
            if (!votingManager.isVotingActive()) {
                votingManager.startVoting();
            }
        } else { // Parou de chover naturalmente
            if (votingManager.isVotingActive()) {
                votingManager.stopVoting();
            }
        }
    }
}
