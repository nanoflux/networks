package de.kwantux.networks.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

import static de.kwantux.networks.Main.lang;


public class BlockLocation extends Origin {
    private final int x;
    private final int y;
    private final int z;

    private final UUID world;


    public BlockLocation(int x, int y, int z, UUID world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public BlockLocation(@Nonnull org.bukkit.Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getUID();
    }

    public BlockLocation(@Nonnull org.bukkit.block.Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.world = block.getWorld().getUID();
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }
    public UUID getWorld() {
        return world;
    }

    public @Nullable org.bukkit.Location getBukkitLocation() {
        World world = Bukkit.getWorld(getWorld());
        if (world == null) return null;
        return new org.bukkit.Location(world, x, y, z);
    }

    public @Nullable Block getBlock() {
        Location bukkitLocation = this.getBukkitLocation();
        if (bukkitLocation == null) return null;
        return bukkitLocation.getBlock();
    }

    public boolean isLoaded() {
        World world = Bukkit.getWorld(this.world);
        return world != null && world.isChunkLoaded(x >> 4, z >> 4);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public Component displayText() {
        return Component.text(toString()).clickEvent(ClickEvent.runCommand("/runcmd teleporttoworld " + world.toString() + " " + x + " " + y + " " + z)).hoverEvent(HoverEvent.showText(lang.getFinal("click-to-tp")));
    }


    @Override
    public boolean equals(Object otherObject) {
        if (otherObject instanceof BlockLocation other) {
            return (other.getX() == this.getX() && other.getY() == this.getY() && other.getZ() == this.getZ() && other.getWorld().equals(this.getWorld()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (world.toString() + "," + x + "," + y + "," + z).hashCode();
    }

    public double getDistance(BlockLocation second) {
        if (!second.getWorld().equals(this.getWorld())) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.sqrt(Math.pow(this.x-second.getX(),2)+Math.pow(this.y-second.getY(),2)+Math.pow(this.z-second.getZ(),2));
    }

    public static void register() {
        Origin.register(BlockLocation.class, "block");
    }
}
