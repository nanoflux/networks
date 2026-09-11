package de.kwantux.networks.component;


import de.kwantux.networks.Main;
import de.kwantux.networks.Network;
import de.kwantux.networks.component.util.ComponentType;
import de.kwantux.networks.config.Config;
import de.kwantux.networks.utils.BlockLocation;
import de.kwantux.networks.utils.Origin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static de.kwantux.networks.Main.mgr;
import static de.kwantux.networks.utils.NamespaceUtils.NETWORK;


public abstract class BlockComponent extends InstallableComponent {

    public abstract ComponentType type();

    protected BlockLocation pos;

    protected BlockComponent(BlockLocation pos, Network network) {
        this.pos = pos;
        this.network(network);
    }

    public BlockLocation pos() {
        return pos;
    }
    public Origin origin() {
        return pos;
    }

    public boolean isLoaded() {
        return pos.isLoaded();
    }

    public boolean ready() {
        if (!(Config.loadChunks || isLoaded())) return false;
        Location location = pos.getBukkitLocation();
        if (location == null) return false;
        return Main.instance.getServer().isOwnedByCurrentRegion(location);
    }

    @Override
    public void setBlockData() {
        Block block = pos.getBlock();
        if (block == null) {
            network().removeComponent(origin());
            return;
        }
        BlockState state = block.getState();
        if (state instanceof TileState tileState) {
            tileState.getPersistentDataContainer().set(NETWORK.key, PersistentDataType.STRING, network().name());
        }
        if (state instanceof Nameable nameable) {
            nameable.customName(type().uiName().append(Component.text(" - ")).append(Component.text(network().name())));
        }
        state.update();
    }

    @Override
    public void resetBlockData() {
        Block block = pos.getBlock();
        if (block == null) {
            network().removeComponent(origin());
            return;
        }
        BlockState state = block.getState();
        if (state instanceof TileState tileState) {
            tileState.getPersistentDataContainer().remove(NETWORK.key);
        }
        if (state instanceof Nameable nameable) {
            nameable.customName(null);
        }
        state.update();
    }

    public static @Nullable BlockComponent getComponentAtBlock(Block block) {
        if (block.getState() instanceof TileState state) {
            Network network = mgr.getNetwork(
                    state.getPersistentDataContainer().get(NETWORK.key, PersistentDataType.STRING)
            );
            if (network != null) {
                return (BlockComponent) network.getComponent(new BlockLocation(block));
            }
        }
        return null;
    }

    public @Nullable Inventory inventory() {

        if (!ready()) return null;

        Block block = Objects.requireNonNull(Bukkit.getWorld(pos.getWorld())).getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if (block.getState() instanceof InventoryHolder) {
            return ((InventoryHolder) block.getState()).getInventory();
        }
        // Remove component from database if it has no inventory
        mgr.removeComponent(origin());
        return null;
    }
}
