package com.jchen.freecam;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.network.PlayerConnection;

public class FreeCam extends JavaPlugin implements Listener {

	public HashMap<Player, FreeCamData> coordinateHashMap;

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
	public void onJoin(PlayerJoinEvent event) {
		for (FreeCamData data : coordinateHashMap.values()) {
			PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().b;

			connection.sendPacket(
					new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, data.placeholder));
			connection.sendPacket(new PacketPlayOutNamedEntitySpawn(data.placeholder));
			connection.sendPacket(new PacketPlayOutEntityMetadata(data.placeholder.getId(), data.watcher, true));
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
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 255));
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
