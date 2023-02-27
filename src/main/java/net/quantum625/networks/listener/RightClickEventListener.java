package net.quantum625.networks.listener;

import net.quantum625.networks.Network;
import net.quantum625.networks.NetworkManager;
import net.quantum625.networks.commands.LanguageModule;
import net.quantum625.networks.data.Config;
import net.quantum625.networks.utils.DoubleChestDisconnecter;
import net.quantum625.networks.utils.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class RightClickEventListener implements Listener {

    private NetworkManager net;
    private LanguageModule lang;
    private Config config;

    private DoubleChestDisconnecter dcd;

    public RightClickEventListener(NetworkManager networkManager, LanguageModule languageModule, Config config) {
        net = networkManager;
        lang = languageModule;
        this.config = config;

        dcd = new DoubleChestDisconnecter(net);
    }

    @EventHandler(priority= EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerUse(PlayerInteractEvent event){
        if (!event.isCancelled()) {
            Player p = event.getPlayer();
            Location pos = new Location(event.getClickedBlock());
            Network network = net.getSelectedNetwork(p);

            String componentType = net.getSelectedComponentType(p);

            if (componentType != null && net.getSelectedNetwork(p) != null) {

                if (network.getComponentByLocation(pos) != null) {
                    lang.returnMessage(p, "location.occupied");
                    return;
                }

                if (config.checkLocation(pos, "container")) {
                    if (componentType == "input_container") {
                        net.getSelectedNetwork(p).addInputContainer(pos);
                        dcd.checkChest(pos);
                        net.selectComponentType(p, null);
                        lang.returnMessage(p, "component.input.add", network, pos);
                    }

                    if (componentType == "item_container") {
                        net.getSelectedNetwork(p).addItemContainer(pos, net.getSelectedItems(p));
                        dcd.checkChest(pos);
                        net.selectComponentType(p, null);
                        lang.returnMessage(p, "component.item.add", network, pos);
                    }

                    if (componentType == "misc_container") {
                        net.getSelectedNetwork(p).addMiscContainer(pos);
                        dcd.checkChest(pos);
                        net.selectComponentType(p, null);
                        lang.returnMessage(p, "component.misc.add", network, pos);
                    }
                } else {
                    lang.returnMessage(p, "component.invalid_block", pos.getBlock().getType());
                }


            }
        }
    }
}