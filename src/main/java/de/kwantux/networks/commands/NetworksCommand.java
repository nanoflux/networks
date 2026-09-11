package de.kwantux.networks.commands;

import de.kwantux.config.util.exceptions.InvalidNodeException;
import de.kwantux.networks.Main;
import de.kwantux.networks.Network;
import de.kwantux.networks.component.BasicComponent;
import de.kwantux.networks.component.InstallableComponent;
import de.kwantux.networks.component.component.InputContainer;
import de.kwantux.networks.component.component.FallbackContainer;
import de.kwantux.networks.component.component.SortedContainer;
import de.kwantux.networks.component.util.ComponentType;
import de.kwantux.networks.component.util.FilterTranslator;
import de.kwantux.networks.config.Config;
import de.kwantux.networks.utils.BlockLocation;
import de.kwantux.networks.utils.ItemHash;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.setting.ManagerSetting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static de.kwantux.networks.Main.*;
import static de.kwantux.networks.commands.ComponentTypeParser.componentTypeParser;
import static de.kwantux.networks.commands.NetworkParser.networkParser;
import static de.kwantux.networks.config.Config.resetContainerNamesOnDelete;
import static de.kwantux.networks.utils.DevelopmentUtils.runInDevelopment;
import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.bukkit.parser.location.LocationParser.locationParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class NetworksCommand extends CommandHandler {

    public NetworksCommand(Main plugin, CommandManager<Source> commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        cmd.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);

        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .handler(this::help)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("help")
                .handler(this::help)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("manual")
                .handler(this::manual)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("version")
                .handler(this::version)
        );
        //TODO: Help to specific commands
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("force")
                .permission("networks.force")
                .senderType(PlayerSource.class)
                .handler(this::force)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("create")
                .required("id", stringParser())
                .senderType(PlayerSource.class)
                .handler(this::create)
                .permission("networks.create")
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("delete")
                .required("network", networkParser())
                .senderType(PlayerSource.class)
                .handler(this::deleteAsk)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("delete")
                .required("network", networkParser())
                .literal("confirm")
                .senderType(PlayerSource.class)
                .handler(this::delete)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("select")
                .required("network", networkParser())
                .senderType(PlayerSource.class)
                .handler(this::select)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("rename")
                .required("network", networkParser())
                .required("newID", stringParser())
                .handler(this::rename)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("list")
                .senderType(PlayerSource.class)
                .handler(this::list)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("list")
                .required("player", playerParser())
                .permission("networks.listforeign")
                .handler(this::listForeign)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("listall")
                .permission("networks.listforeign")
                .handler(this::getNetworks)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("info")
                .handler(this::info)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("component")
                .literal("list")
                .handler(this::components)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("component")
                .literal("info")
                .required("location", locationParser())
                .handler(this::componentInfo)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("user")
                .literal("add")
                .required("player", playerParser())
                .handler(this::addUser)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("user")
                .literal("remove")
                .required("player", playerParser())
                .handler(this::removeUser)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("owner")
                .required("player", playerParser())
                .senderType(PlayerSource.class)
                .handler(this::transferOwnership)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("accept")
                .required("network", networkParser())
                .senderType(PlayerSource.class)
                .handler(this::acceptTransfer)
        );
        if (Config.allowMerge)
            cmd.command(cmd.commandBuilder("networks", Config.commands)
                    .literal("merge")
                    .required("final", networkParser())
                    .required("other", networkParser())
                    .handler(this::merge)
            );
        else
            cmd.command(cmd.commandBuilder("networks", Config.commands)
                    .literal("merge")
                    .required("final", networkParser())
                    .required("other", networkParser())
                    .permission("networks.data")
                    .handler(this::merge)
            );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("items")
                .handler(this::items)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("data")
                .literal("save")
                .permission("networks.data")
                .handler(this::saveNetworks)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("data")
                .literal("fixcache")
                .permission("networks.data")
                .handler(this::fixCacheWarning)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("data")
                .literal("fixcache")
                .literal("confirm")
                .permission("networks.data")
                .handler(this::fixCache)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("give")
                .literal("wand")
                .permission("networks.give")
                .senderType(PlayerSource.class)
                .handler(this::giveWand)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("give")
                .literal("component")
                .required("type", componentTypeParser(true))
                .permission("networks.give")
                .senderType(PlayerSource.class)
                .handler(this::giveComponent)
        );
        cmd.command(cmd.commandBuilder("networks", Config.commands)
                .literal("give")
                .literal("upgrade")
                .literal("range")
                .required("tier", integerParser(1, Config.ranges.length))
                .permission("networks.give")
                .senderType(PlayerSource.class)
                .handler(this::giveUpgradeRange)
        );
        runInDevelopment(() -> {
            cmd.command(cmd.commandBuilder("networks", Config.commands)
                    .literal("debug")
                    .literal("itemhash")
                    .permission("networks.debug")
                    .senderType(PlayerSource.class)
                    .handler(this::debugItemHash)
            );
            cmd.command(cmd.commandBuilder("networks", Config.commands)
                    .literal("debug")
                    .literal("itemname")
                    .permission("networks.debug")
                    .senderType(PlayerSource.class)
                    .handler(this::itemNameWithHover)
            );
            cmd.command(cmd.commandBuilder("networks", Config.commands)
                    .literal("debug")
                    .literal("placecore")
                    .permission("networks.debug")
                    .senderType(PlayerSource.class)
                    .handler(this::placeCore)
            );
        });
    }


    private @Nullable Network selection(CommandSender sender) {
        Network network = mgr.selection(sender);
        if (network == null) {
            lang.message(sender, "select.noselection");
            return null;
        }
        return network;
    }

    private @Nullable Network selection(Source source) {
        return selection(source.source());
    }

    private void help(CommandContext<Source> context) {
        Source sender = context.sender();
        boolean admin = true;
        if (sender instanceof Player player) {
            admin = player.hasPermission("networks.admin");
        }
        if (admin) lang.message(sender, "help.admin", plugin.getPluginMeta().getVersion());
        else lang.message(sender, "help", plugin.getPluginMeta().getVersion());
    }

    private void manual(CommandContext<Source> context) {
        lang.message(context.sender(), "manual");
    }

    private void version(CommandContext<Source> context) {
        lang.message(context.sender(), "version", plugin.getPluginMeta().getVersion());
    }

    private void force(CommandContext<PlayerSource> context) {
        boolean active = mgr.forceToggle(context.sender().source());
        lang.message(context.sender().source(), "force." + (active ? "enable" : "disable"));
    }

    private void saveNetworks(CommandContext<Source> context) {
        mgr.saveData();
        lang.message(context.sender(), "data.save.networks");
    }

    private void fixCacheWarning(CommandContext<Source> context) {
        lang.message(context.sender(), "data.fixcache.warning");
    }

    private void fixCache(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        final int total = mgr.getNetworks().size();
        int counter = 0;
        sender.sendMessage("Rebuilding cache for " + total + " networks.");
        context.sender().source().sendMessage("This action might take a while...");
        for (Network network : mgr.getNetworks()) {
            network.rebuildBlockDataCache();
            counter++;
            if (counter % 20 == 0) {
                context.sender().source().sendMessage(counter + "/" + total + " (" + Math.floorDiv(counter * 100, total) + "%) of networks processed.");
            }
        }
        context.sender().source().sendMessage("Cache rebuild complete.");
    }

    private void create(CommandContext<PlayerSource> context) {

        String id = context.get("id");
        if (!Network.validName(id)) {
            lang.message(context.sender(), "create.illegal_name");
            return;
        }
        Player player = context.sender().source();

        if (!(mgr.listNetworksWithOwner(player.getUniqueId()).size() < Config.maxNetworks || player.hasPermission("networks.bypass_limit"))) {
            lang.message(context.sender(), "create.limit", player.displayName());
            return;
        }

        if (mgr.getFromName(id) != null) {
            lang.message(player, "create.exists");
            return;
        }

        if (!mgr.create(id, player.getUniqueId())) {
            lang.message(player, "create.unknown_fail");
            return;
        }
        lang.message(player, "create.success", id);
        mgr.select(player, mgr.getFromName(id));
        lang.message(player, "select.success", id);
    }

    private void deleteAsk(CommandContext<PlayerSource> context) {
        Network network = context.get("network");

        if (!mgr.permissionOwner(context.sender().source() ,network)) {
            lang.message(context.sender(), "permission.owner");
            return;
        }

        lang.message(context.sender(), "delete.confirm", network.name());

    }

    private void delete(CommandContext<PlayerSource> context) {
        Network network = context.get("network");
        Player player = context.sender().source();

        if (!mgr.permissionOwner(player,network)) {
            lang.message(player, "permission.owner");
            return;
        }


        for (BasicComponent component : network.components()) {
            if (component instanceof InstallableComponent installable) {
                ItemStack drop = installable.item();
                if (player.getInventory().firstEmpty() >= 0)
                    player.getInventory().addItem(drop);
                else
                    player.getWorld().dropItem(player.getLocation(), drop);
            }
            if (resetContainerNamesOnDelete) component.resetBlockData();
        }

        String name = network.name();
        mgr.delete(name);
        lang.message(player, "delete.success", name);
    }

    private void select(CommandContext<PlayerSource> context) {
        Network network = context.get("network");
        Player player = context.sender().source();

        if (mgr.permissionUser(player, network)) {
            mgr.select(player, network);
            lang.message(player, "select.success", network.name());
        }

        else {
            lang.message(player, "permission.user");
        }
    }

    private void rename(CommandContext<Source> context) {
        Network network = context.get("network");
        String newID = context.get("newID");
        if (!Network.validName(newID)) {
            lang.message(context.sender(), "create.illegal_name");
            return;
        }
        String oldID = network.name();
        Source sender = context.sender();

        if (mgr.permissionOwner(sender.source(), network)) {
            if (mgr.rename(oldID, newID)) lang.message(sender, "rename.success", network.name());
            else lang.message(sender, "rename.taken", newID);
        }

        else {
            lang.message(sender, "permission.owner");
        }
    }

    private void list(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        List<Network> list = mgr.listNetworksWithUser(player.getUniqueId());
        List<Network> owned = mgr.listNetworksWithOwner(player.getUniqueId());



        if (list.isEmpty()) {
            lang.message(player, "list.empty");
        }

        else {
            if (owned.size() > 1) {
                lang.message(player, "list.owned");
                for (Network network : owned) {
                    player.sendMessage(network.displayText());
                    list.remove(network);
                }
            }
            if (list.size() > 1) lang.message(player, "list");
            for (Network network : list) {
                player.sendMessage(network.displayText());
            }
        }

    }

    private void getNetworks(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        if (mgr.getNetworks().isEmpty()) {
            lang.message(sender, "list.all.empty");
            return;
        }
        lang.message(sender, "list.all");
        for (Network network : mgr.getNetworks()) {
            sender.sendMessage(network.displayText());
        }
    }

    private void listForeign(CommandContext<Source> context) {

        Player player = context.get("player");
        CommandSender sender = context.sender().source();
        List<Network> list = mgr.listNetworksWithUser(player.getUniqueId());
        List<Network> owned = mgr.listNetworksWithOwner(player.getUniqueId());


        if (list.isEmpty()) {
            lang.message(sender, "list.foreign.empty", player.displayName());
        }

        else {
            if (owned.size() > 1) {
                lang.message(sender, "list.foreign.owned", player.displayName());
                for (Network network : owned) {
                    sender.sendMessage(network.displayText());
                    list.remove(network);
                }
            }
            lang.message(sender, "list.foreign", player.displayName());
            for (Network network : list) {
                sender.sendMessage(network.displayText());
            }
        }
    }

    private void info(CommandContext<Source> context) {
        Source sender = context.sender();
        Network network = selection(sender);
        if (network == null) return;
        lang.message(sender, "info.title");
        lang.message(sender, "info.name", network.name());
        lang.message(sender, "info.owner", Bukkit.getOfflinePlayer(network.owner()).getName());

        StringBuilder users = new StringBuilder();

        for (UUID uid : network.users()) {
            users.append(Bukkit.getOfflinePlayer(uid).getName()).append(",  ");
        }

        if (!users.isEmpty()) {
            users = new StringBuilder(users.substring(0, users.length() - 3));
        }

        lang.message(sender, "info.users", users.toString());

        lang.message(sender, "info.components.total", String.valueOf(network.components().size()));
        lang.message(sender, "info.range", String.valueOf(network.properties().baseRange()));
    }

    private void components(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        Network network = selection(sender);
        if (network == null) return;

        if (network.components().isEmpty()) {
            lang.message(sender, "component.list.empty", network.name());
            return;
        }

        for (BasicComponent component : network.components()) {
            sender.sendMessage(
                    Objects.requireNonNullElse(lang.getFinal("item.name.component."+component.type().tag()), Component.text("Unknown Component type: " +  component.type().tag()))
                    .append(Component.text(":  ")
                    .append(component.origin().displayText()))
                    .hoverEvent(HoverEvent.showText(componentInfo(network, component, false)))
            );
        }
    }

    private void componentInfo(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        BlockLocation location = new BlockLocation((Location) context.get("location"));
        BasicComponent component = dcu.componentAt(location);
        if (component == null) {
            lang.message(sender, "component.nocomponent", location.toString());
            return;
        }
        Network network = component.network();
        boolean isProxy = network.getComponent(location) == null;
        sender.sendMessage(componentInfo(network, component, isProxy));
    }

    /**
     * Generate a {@link Component} containing information about the component at the given {@link BlockLocation} in the given {@link Network}.
     * <p>
     * If the component is a isProxy, it will be indicated.
     * <p>
     * The generated component will be translated into the language that the {@link Source} is currently using.
     *
     * @param network The network containing the component
     * @param component The component to generate information about
     * @param isProxy Whether the component is a isProxy
     * @return A {@link Component} containing information about the component
     */
    public static Component componentInfo(Network network, @Nullable BasicComponent component, boolean isProxy) {
        try {
            return Component.newline().append(
                switch (component) {
                    case InputContainer container ->
                            lang.get("wand.info.input", network.displayText(), component.origin().displayText(), Component.text(String.valueOf(container.range())));
                    case SortedContainer container -> {
                        Component filters = Component.text("[");
                        Set<Component> filterSet = Arrays.stream(container.filters()).mapToObj(
                                FilterTranslator::translate
                        ).collect(Collectors.toSet());
                        for (Component filter : filterSet) {
                            filters = filters.append(filter);
                            filters = filters.append(Component.text(", "));
                        }
                        filters = filters.append(Component.text("]"));
                        yield lang.get("wand.info.sorted", network.displayText(), component.origin().displayText(), Component.text(String.valueOf(container.acceptorPriority())), filters);
                    }
                    case FallbackContainer container ->
                            lang.get("wand.info.fallback", network.displayText(), component.origin().displayText(), Component.text(String.valueOf(container.acceptorPriority())));
                    case null, default -> Component.empty();
                }
            ).append(isProxy ? Component.newline().append(lang.get("wand.info.proxy")) : Component.empty());

        } catch (InvalidNodeException e) {
            return Component.empty();
        }
    }

    private void addUser(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        Player target = context.get("player");
        Network network = selection(sender);
        if (network == null) return;

        if (!mgr.permissionOwner(sender, selection(sender))) {
            lang.message(sender, "permission.owner");
            return;
        }

        if (network.users().contains(target.getUniqueId())) {
            lang.message(sender, "user.add.nochange", Component.text(network.name()), target.displayName());
            return;
        }

        network.addUser(target.getUniqueId());
        lang.message(sender, "user.add", Component.text(network.name()), target.displayName());

    }

    private void removeUser(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        Player target = context.get("player");
        Network network = selection(sender);
        if (network == null) return;

        if (!mgr.permissionOwner(sender, selection(sender))) {
            lang.message(sender, "permission.owner");
            return;
        }

        if (!network.users().contains(target.getUniqueId())) {
            lang.message(sender, "user.remove.nochange", Component.text(network.name()), target.displayName());
            return;
        }

        network.removeUser(target.getUniqueId());
        lang.message(sender, "user.remove", Component.text(network.name()), target.displayName());

    }

    private void transferOwnership(CommandContext<PlayerSource> context) {
        Player sender = context.sender().source();
        boolean request = Config.requestOwnershipTransfers && !sender.hasPermission("networks.transfer_without_request");
        Player target = context.get("player");
        Network network = selection(sender);
        if (network == null) return;

        if (!(mgr.listNetworksWithOwner(target.getUniqueId()).size() < Config.maxNetworks || target.hasPermission("networks.bypass_limit") || mgr.force(sender))) {
            lang.message(context.sender(), "create.limit", target.displayName());
            return;
        }

        if (!mgr.permissionOwner(sender, selection(sender))) {
            lang.message(sender, "permission.owner");
            return;
        }

        if (target.getUniqueId().equals(network.owner())) {
            lang.message(sender, "user.owner.nochange", Component.text(network.name()), target.displayName());
            return;
        }

        if (request) {
            lang.message(sender, "user.owner.request.donator", Component.text(network.name()), target.displayName());
            lang.message(target, "user.owner.request.acceptor", network.name(), Bukkit.getOfflinePlayer(network.owner()).getName());
            target.sendMessage(lang.getFinal("user.owner.request.acceptor.clickhere").clickEvent(ClickEvent.runCommand("/runcmd networks accept " + network.name())));
            mgr.requestTransfer(network, target);
        }

        else {
            network.removeUser(target.getUniqueId());
            network.addUser(network.owner());
            network.owner(target.getUniqueId());
            lang.message(sender, "user.owner.transfer.donator", Component.text(network.name()), target.displayName());
            lang.message(target, "user.owner.transfer.acceptor", network.name(), Bukkit.getOfflinePlayer(network.owner()).getName());
        }
    }


    private void acceptTransfer(CommandContext<PlayerSource> context) {
        Player sender = context.sender().source();
        Network network = context.get("network");

        if (!mgr.canTransfer(network, sender)) {
            lang.message(sender, "user.owner.norequest");
            return;
        }

        lang.message(sender, "user.owner.accept.acceptor", Component.text(network.name()));
        Player owner = Bukkit.getPlayer(network.owner());
        if (owner != null) lang.message(owner, "user.owner.accept.donator", Component.text(network.name()), sender.displayName());

        mgr.acceptTransfer(network);

        network.removeUser(sender.getUniqueId());
        network.addUser(network.owner());
        network.owner(sender.getUniqueId());
    }


    private void merge(CommandContext<Source> context) {
        Network finalNetwork = context.get("final");
        Network otherNetwork = context.get("other");
        CommandSender sender = context.sender().source();
        
        if (!mgr.permissionOwner(sender, finalNetwork) || !mgr.permissionOwner(sender, otherNetwork)) {
            lang.message(sender, "merge.nopermission");
            return;
        }

        if (finalNetwork.equals(otherNetwork)) {
            lang.message(sender, "merge.identical");
            return;
        }

        String otherName = otherNetwork.name();
        finalNetwork.addComponents(otherNetwork.components());
        mgr.delete(otherName);
        lang.message(sender, "merge.success", Component.text(finalNetwork.name()), Component.text(otherName));
    }

    private void items(CommandContext<Source> context) {
        CommandSender sender = context.sender().source();
        Network network = selection(sender);
        if (network == null) return;

        if (!mgr.permissionUser(sender, network)) {
            lang.message(sender, "permission.user");
            return;
        }

        HashMap<Material, Integer> materials = network.materials();

        if (materials.isEmpty()) {
            lang.message(sender, "items.noitems");
            return;
        }

        lang.message(sender, "items.message", Component.text(network.name()));

        for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
            sender.sendMessage(Component.translatable(entry.getKey().translationKey()).append(Component.text(":  ").color(TextColor.color(0, 255, 230)).append(Component.text(entry.getValue())).color(TextColor.color(255, 255, 255))).hoverEvent(HoverEvent.showItem(HoverEvent.ShowItem.showItem(entry.getKey().key(), 1))));
        }
    }

    private void giveWand(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        ItemStack wand = crf.getNetworkWand(2);
        player.getInventory().addItem(wand);
    }

    private void giveUpgradeRange(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        ItemStack upgrade = crf.getRangeUpgrade(context.get("tier"));
        player.getInventory().addItem(upgrade);
    }

    private void giveComponent(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        ComponentType type = context.get("type");
        player.getInventory().addItem(type.item());
    }

    private void debugItemHash(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        ItemStack item = player.getInventory().getItemInMainHand();
        lang.message(player, "debug.hash", "" + ItemHash.materialHash(item), "" + ItemHash.strictHash(item), "");
    }

    private void itemNameWithHover(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        ItemStack item = player.getInventory().getItemInMainHand();
        Component component = item.effectiveName().hoverEvent(HoverEvent.showItem(
                HoverEvent.ShowItem.showItem(
                        Key.key(item.getType().name().toLowerCase()), 1
                )
        ));
        player.sendMessage(component);
    }

    private void placeCore(CommandContext<PlayerSource> context) {
        Player player = context.sender().source();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemDisplay display = player.getWorld().spawn(new BlockLocation(player.getLocation()).getBukkitLocation(), ItemDisplay.class);
        display.setItemStack(item);
    }


}
