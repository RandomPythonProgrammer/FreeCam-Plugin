package com.jchen.freecam;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.level.EntityPlayer;

public class FreeCam extends JavaPlugin implements Listener {

	private HashMap<Player, FreeCamData> coordinateHashMap;

	@Override
	public void onEnable() {
		coordinateHashMap = new HashMap<Player, FreeCamData>();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		for (Player player : coordinateHashMap.keySet()) {
			toggleFreeCam(player);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("freecam")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				toggleFreeCam(player);
				return true;
			}
		}

		if (label.equals("disable")) {
			onDisable();
			return true;
		}
		return false;
	}

	@EventHandler()
	public void onLeave(PlayerQuitEvent event) {
		if (isFreeCam(event.getPlayer())) {
			toggleFreeCam(event.getPlayer());
		}
	}

	@EventHandler()
	public void onInteract(PlayerInteractEvent event) {
		if (coordinateHashMap.keySet().contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler()
	public void onGamemode(PlayerGameModeChangeEvent event) {
		for (Player player : coordinateHashMap.keySet()) {
			if (player.equals(event.getPlayer()) && event.getNewGameMode() != GameMode.SPECTATOR) {
				toggleFreeCam(player);
			}
		}
	}

	public void toggleFreeCam(Player player) {
		if (player.getGameMode() != GameMode.SPECTATOR) {
			coordinateHashMap.put(player, new FreeCamData(player));
			player.setGameMode(GameMode.SPECTATOR);
		} else {
			if (coordinateHashMap.containsKey(player)) {
				FreeCamData data = coordinateHashMap.get(player);
				coordinateHashMap.remove(player);
				player.teleport(data.location);
				player.setGameMode(data.gamemode);
				player.setFallDistance(data.blocksFallen);
				PlaceHolder.destroyPlaceholder(data.placeholder);
			}
		}
	}

	public boolean isFreeCam(Player player) {
		return coordinateHashMap.containsKey(player);
	}

	public static ItemStack getPlayerHead(Player player) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
		skullMeta.setOwningPlayer(player);
		head.setItemMeta(skullMeta);
		return head;
	}

}

class FreeCamData {
	public GameMode gamemode;
	public float blocksFallen;
	public EntityPlayer placeholder;
	public Location location;

	public FreeCamData(Player player) {
		location = player.getLocation();
		gamemode = player.getGameMode();
		blocksFallen = player.getFallDistance();
		placeholder = PlaceHolder.create(player);
	}
}
