package com.jchen.freecam;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;

public class FreeCamData {
	public GameMode gamemode;
	public float blocksFallen;
	public EntityPlayer placeholder;
	public Location location;
	public DataWatcher watcher;

	public FreeCamData(Player player) {
		location = player.getLocation();
		gamemode = player.getGameMode();
		blocksFallen = player.getFallDistance();
		PlaceHolder.create(player, this);
	}
}
