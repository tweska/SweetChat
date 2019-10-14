package com.tweska.sweetchat.listeners;

import com.tweska.sweetchat.util.ChatMode;
import com.tweska.sweetchat.events.SweetChatEvent;
import com.tweska.sweetchat.events.GlobalChatEvent;
import com.tweska.sweetchat.events.LocalChatEvent;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class ChatListener implements Listener {
    private Plugin plugin;
    private HashMap<Player, ChatMode> playerChatMode;

    public ChatListener(Plugin plugin, HashMap<Player, ChatMode> playerChatMode) {
        this.plugin = plugin;
        this.playerChatMode = playerChatMode;
    }

    /** The vanilla chat events are used to detect a chat event. This event is then translated into a new sweetChatEvent
      * for the right chat channel. */
    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        /* Cancel the event to prevent the message from showing up in the chat as a vanilla message. */
        event.setCancelled(true);

        /* Extract some important information from the chat event. */
        Player sender = event.getPlayer();

        /* A new chat event will be created for the right chat channel. */
        final SweetChatEvent newSweetChatEvent;
        switch (playerChatMode.get(sender)) {
            case GLOBAL_CHAT:
                newSweetChatEvent = new GlobalChatEvent(event.getPlayer(), event.getMessage());
                break;
            case LOCAL_CHAT:
                newSweetChatEvent = new LocalChatEvent(event.getPlayer(), event.getMessage());
                break;
            default:
                newSweetChatEvent = null;
        }

        /* Fire the newly created event in the main server thread. */
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                Bukkit.getPluginManager().callEvent(newSweetChatEvent);
            }
        });
    }

    /** When the plugin is enabled all online players should be added to the list of player chat modes. */
    @EventHandler
    public void onPluginEnableEvent(PluginEnableEvent event) {
        Server server = event.getPlugin().getServer();

        /* Set the chat mode of all players to the default (global). */
        for(Player player : server.getOnlinePlayers()) {
            playerChatMode.put(player, ChatMode.GLOBAL_CHAT);
        }
    }

    /** When players join the server they should have a chat mode by default. */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        /* Check if the player already has a chat mode. */
        if (playerChatMode.containsKey(player)) {
            return;
        }

        /* Set the players chat mode to the default chat mode (global). */
        playerChatMode.put(player, ChatMode.GLOBAL_CHAT);
    }
}