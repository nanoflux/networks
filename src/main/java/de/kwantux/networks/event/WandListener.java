package de.kwantux.networks.event;

import de.kwantux.config.util.exceptions.InvalidNodeException;
import de.kwantux.networks.Main;
import de.kwantux.networks.Manager;
import de.kwantux.networks.Network;
import de.kwantux.networks.commands.NetworksCommand;
import de.kwantux.networks.component.NetworkComponent;
import de.kwantux.networks.component.component.InputContainer;
import de.kwantux.networks.component.component.SortingContainer;
import de.kwantux.networks.component.module.Acceptor;
import de.kwantux.networks.component.module.Donator;
import de.kwantux.networks.component.module.Requestor;
import de.kwantux.networks.config.Config;
import de.kwantux.networks.config.CraftingManager;
import de.kwantux.networks.utils.BlockLocation;
import de.kwantux.networks.utils.DoubleChestUtils;
import de.kwantux.config.lang.LanguageController;
import de.kwantux.networks.component.component.MiscContainer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class WandListener implements Listener {

    private final Config config;
    private final Manager net;
    private final LanguageController lang;
    private final CraftingManager crafting;
    private final DoubleChestUtils dcu;

    public WandListener(Main plugin, CraftingManager craftingManager, DoubleChestUtils dcu) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.config = plugin.getConfiguration();
        this.net = plugin.getNetworkManager();
        this.lang = plugin.getLanguage();
        this.crafting = craftingManager;
        this.dcu = dcu;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws InvalidNodeException {
        Player p = event.getPlayer();
        BlockLocation l = null;
        if (event.getClickedBlock() != null) l = new BlockLocation(event.getClickedBlock());
        Action action = event.getAction();

        ItemStack wand = p.getInventory().getItemInMainHand();

        if (!wand.getType().equals(Material.AIR)) {
            if (wand.getItemMeta().getPersistentDataContainer().has(new NamespacedKey("networks", "wand"), PersistentDataType.INTEGER)) {

                event.setCancelled(true);

                if (!event.getHand().equals(EquipmentSlot.HAND)) return;

                int mode = wand.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("networks", "wand"), PersistentDataType.INTEGER);
                
                if (!p.isSneaking()) {
                    if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)) {
                        if (!p.isSneaking()) {
                            mode++;
                            if (mode > 1) mode = 0;
                            //p.getInventory().setItemInMainHand(crf.getNetworkWand(mode));
                            event.getItem().setItemMeta(crafting.getNetworkWand(mode).getItemMeta());
                            lang.message(p, "wand.mode", lang.getRaw("wand.mode." + mode));
                            return;
                        }
                    }
                }

                if (l == null) return;
                Network network = net.getNetworkWithComponent(l);
                NetworkComponent component = net.getComponent(l);

                if (!p.isSneaking()) {
                    if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                        p.sendMessage(NetworksCommand.componentInfo(network, component));
                    }
                    return;
                }

                if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_AIR)) {
                    return;
                }

                if (component == null) {
                    lang.message(p, "component.nocomponent");
                    return;
                }

                if (!net.permissionUser(p, network)) {
                    lang.message(p, "permission.user");
                    return;
                }

                if (action.equals(Action.RIGHT_CLICK_BLOCK)) {

                    if (mode == 0 && !p.getInventory().getItemInOffHand().getType().equals(Material.AIR) && net.getComponent(l) instanceof SortingContainer) {
                        NetworkComponent c = net.getComponent(l);
                        if (c instanceof SortingContainer container) {
                            container.addFilter(p.getInventory().getItemInOffHand().getType().toString().toUpperCase());
                            lang.message(p, "component.sorting.setitem", l.toString(), p.getInventory().getItemInOffHand().getType().toString());
                        }
                    }
                    if (mode == 1) {
                        if (component instanceof Acceptor container) {
                            container.incrementAcceptorPriority();
                            lang.message(p, "component.priority", String.valueOf(container.acceptorPriority()));
                        }
                    }
                }

                if (action.equals(Action.LEFT_CLICK_BLOCK)) {

                    if (mode == 0 && net.getComponent(l) instanceof SortingContainer && !p.getInventory().getItemInOffHand().getType().equals(Material.AIR) && p.isSneaking()) {
                        NetworkComponent c = net.getComponent(l);
                        if (c instanceof SortingContainer container) {
                            container.removeFilter(p.getInventory().getItemInOffHand().getType().toString().toUpperCase());
                            lang.message(p, "component.sorting.removeitem", l.toString(), p.getInventory().getItemInOffHand().getType().toString());
                        }
                    }

                    if (mode == 1) {
                        if (component instanceof Acceptor container) {
                            container.decrementAcceptorPriority();
                            lang.message(p, "component.priority", String.valueOf(container.acceptorPriority()));
                        }
                    }
                }
            }

            if (p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey("networks", "upgrade.range"), PersistentDataType.INTEGER)) {
                if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                    event.setCancelled(true);
                    NetworkComponent component = net.getComponent(l);
                    if (component == null) {
                        lang.message(p, "component.nocomponent");
                        return;
                    }
                    if (component instanceof Donator donator) {
                        int tier = donator.range();
                        int upgradeTier = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey("networks", "upgrade.range"), PersistentDataType.INTEGER)-1;

                        if (upgradeTier == tier) {
                            ItemStack item = p.getInventory().getItemInMainHand();
                            item.setAmount(item.getAmount() - 1);
                            donator.rangeUp();
                            lang.message(p, "rangeupgrade.success", String.valueOf(tier+1), component.pos().toString());
                        }
                        if (tier == config.getMaxRanges().length) {
                            lang.message(p, "rangeupgrade.last");
                            return;
                        }
                        if (upgradeTier < tier) {
                            lang.message(p, "rangeupgrade.alreadyupgraded", String.valueOf(tier));
                        }
                        if (upgradeTier > tier) {
                            lang.message(p, "rangeupgrade.unlockfirst", String.valueOf(tier));
                        }
                    }
                    else if (component instanceof Requestor requestor) {
                        int tier = requestor.range();
                        int upgradeTier = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey("networks", "upgrade.range"), PersistentDataType.INTEGER);

                        if (upgradeTier == tier) {
                            ItemStack item = p.getInventory().getItemInMainHand();
                            item.setAmount(item.getAmount() - 1);
                            requestor.rangeUp();
                            lang.message(p, "rangeupgrade.success", String.valueOf(tier), component.pos().toString());
                        }
                        if (tier == config.getMaxRanges().length) {
                            lang.message(p, "rangeupgrade.last");
                            return;
                        }
                        if (upgradeTier < tier) {
                            lang.message(p, "rangeupgrade.alreadyupgraded", String.valueOf(tier));
                        }
                        if (upgradeTier > tier) {
                            lang.message(p, "rangeupgrade.unlockfirst", String.valueOf(tier));
                        }
                    }
                    else {
                        lang.message(p, "rangeupgrade.passivecomponent");
                    }

                    
                }
            }
        }
    }
}