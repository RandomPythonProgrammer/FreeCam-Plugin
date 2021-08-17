package com.jchen.freecam;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;

public class PlaceHolder {

	public static void create(Player player, FreeCamData data) {
		EntityPlayer craftPlayer = ((CraftPlayer) player).getHandle();

		Property textures = (Property) craftPlayer.getProfile().getProperties().get("textures").toArray()[0];
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getDisplayName());
		gameProfile.getProperties().put("textures",
				new Property("textures", textures.getValue(), textures.getSignature()));

		EntityPlayer placeholder = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(),
				((CraftWorld) player.getWorld()).getHandle(), gameProfile);

		Location loc = player.getLocation();
		placeholder.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		DataWatcher watcher = placeholder.getDataWatcher();

		byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
		watcher.set(DataWatcherRegistry.a.a(17), b);

		data.watcher = watcher;

		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			PlayerConnection connection = ((CraftPlayer) onlinePlayer).getHandle().b;

			connection.sendPacket(
					new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, placeholder));
			connection.sendPacket(new PacketPlayOutNamedEntitySpawn(placeholder));
			connection.sendPacket(new PacketPlayOutEntityMetadata(placeholder.getId(), watcher, true));
		}

		data.placeholder = placeholder;
	}

	public static void destroyPlaceholder(EntityPlayer placeholder) {
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			PlayerConnection connection = ((CraftPlayer) onlinePlayer).getHandle().b;

			connection.sendPacket(new PacketPlayOutEntityDestroy(placeholder.getId()));
		}
	}
}
