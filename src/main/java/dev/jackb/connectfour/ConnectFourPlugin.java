package dev.jackb.connectfour;

import java.util.logging.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper plugin entry point for the configurable Connect Four board.
 */
public final class ConnectFourPlugin extends JavaPlugin {
    private ConnectFourGame game;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            loadGame();
        } catch (IllegalArgumentException exception) {
            getLogger().log(Level.SEVERE, "Connect Four config is invalid. Disabling plugin.", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConnectFourCommand command = new ConnectFourCommand(this);
        PluginCommand pluginCommand = getCommand("connectfour");
        if (pluginCommand == null) {
            getLogger().severe("Command 'connectfour' is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
        getLogger().info("Connect Four enabled.");
    }

    ConnectFourGame game() {
        return game;
    }

    void reloadGame() {
        reloadConfig();
        loadGame();
    }

    private void loadGame() {
        GameConfig config = GameConfig.load(getConfig());
        ConnectFourGame loadedGame = new ConnectFourGame(this, config);
        loadedGame.scanFromWorld();
        this.game = loadedGame;
    }
}
