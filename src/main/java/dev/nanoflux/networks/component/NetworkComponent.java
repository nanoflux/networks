package dev.nanoflux.networks.component;

import dev.nanoflux.config.util.exceptions.InvalidNodeException;
import dev.nanoflux.networks.Config;
import dev.nanoflux.networks.Main;
import dev.nanoflux.networks.utils.BlockLocation;
import dev.nanoflux.networks.utils.NamespaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class NetworkComponent {

    public abstract ComponentType type();

    protected BlockLocation pos;

    protected NetworkComponent(BlockLocation pos) {
        this.pos = pos;
    }

    public BlockLocation pos() {
        return pos;
    }

    public boolean isLoaded() {
        World world = Bukkit.getWorld(pos.getWorld());
        return world != null && world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public boolean ready() {
        return Config.loadChunks || isLoaded();
    }

    public abstract Map<String, Object> properties();

    public abstract ItemStack item(Material material);

    public @Nullable Inventory inventory() {

        if (!ready()) return null;

        Block block = Bukkit.getWorld(pos.getWorld()).getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if (block.getState() instanceof InventoryHolder) {
            return ((InventoryHolder) block.getState()).getInventory();
        }
        // TODO: Remove Component from database
        return null;
    }
}
