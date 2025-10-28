package com.blockycraft.blockyrain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;
import com.blockycraft.blockyrain.managers.VotingManager;

public class BlockyRain extends JavaPlugin {

    private VotingManager votingManager;
    private Properties configProperties;

    @Override
    public void onEnable() {
        loadConfigProperties();
        votingManager = new VotingManager(this);
        getServer().getPluginManager().registerEvents(
            new com.blockycraft.blockyrain.listeners.WeatherListener(votingManager), this
        );
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

    private void loadConfigProperties() {
        try {
            File configFile = new File(getDataFolder(), "config.properties");
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            configProperties = new Properties();
            if (!configFile.exists()) {
                configFile.createNewFile();
                // Configuração padrão
                configProperties.setProperty("percent_required", "50");
                configProperties.setProperty("vote_start", "&9Esta chovendo!\\n&9Deseja votar para que a chuva pare?\\nDigite &a/sim &9ou &c/nao");
                configProperties.setProperty("vote_not_active", "&cNão há votação ativa. Aguarde a próxima chuva.");
                configProperties.setProperty("already_voted", "&eVocê já votou nesta sessão de chuva.");
                configProperties.setProperty("vote_yes_received", "&aSeu voto para parar a chuva foi registrado!");
                configProperties.setProperty("vote_no_received", "&cSeu voto para continuar a chuva foi registrado!");
                configProperties.setProperty("stopping_rain", "&bParando de chover...");
                FileOutputStream out = new FileOutputStream(configFile);
                configProperties.store(out, "BlockyRain Config");
                out.close();
            } else {
                FileInputStream in = new FileInputStream(configFile);
                configProperties.load(in);
                in.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar config.properties: " + e.getMessage());
        }
    }

    public Properties getPluginProperties() {
        return configProperties;
    }
}
