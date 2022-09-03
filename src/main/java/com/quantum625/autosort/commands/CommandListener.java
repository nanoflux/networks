package com.quantum625.autosort.commands;

import com.quantum625.autosort.NetworkManager;
import com.quantum625.autosort.StorageNetwork;
import com.quantum625.autosort.container.BaseContainer;
import com.quantum625.autosort.container.InputContainer;
import com.quantum625.autosort.container.ItemContainer;
import com.quantum625.autosort.container.MiscContainer;
import com.quantum625.autosort.data.Language;
import com.quantum625.autosort.utils.Location;
import org.bukkit.command.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class CommandListener implements CommandExecutor{

    private File dataFolder;
    private NetworkManager net;

    private LanguageModule lang;


    public CommandListener(File dataFolder, String lang_id) {
        this.dataFolder = dataFolder;
        this.net = new NetworkManager(dataFolder);
        this.lang = new LanguageModule(dataFolder, lang_id);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof BlockCommandSender) {
            Bukkit.getLogger().warning("Command Blocks are not allowed to use the autosort command!");
            return true;
        }


        if (args.length == 0) {
            sendHelp(sender);

        }
        
        else {
            if (args[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
                return true;
            }

            else if (args[0].equalsIgnoreCase("data")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("reload")) {
                        loadData();
                        lang.returnMessage(sender,"data.load");
                    }
                    if (args[1].equalsIgnoreCase("save")) {
                        saveData();
                        lang.returnMessage(sender, "data.save");
                    }
                }
            }


            else if (args[0].equalsIgnoreCase("create")) {

                UUID owner;

                if (sender instanceof Player) {
                    owner = ((Player) sender).getUniqueId();
                }

                else {
                    if (args.length == 3) {
                        owner = UUID.fromString(args[2]);
                    }
                    else {
                        lang.returnMessage(sender, "create.noowner");
                        return true;
                    }
                }

                if (args[1] != null) {
                    net.add(args[1], owner);
                    if (net.getFromID(args[1]) != null) {
                        lang.returnMessage(sender, "create.success", net.getFromID(args[1]));
                    }
                    else {
                        lang.returnMessage(sender, "create.fail");
                    }

                }
                return true;
            }

            else if (args[0].equalsIgnoreCase("delete")) {
                if (args[1] != null) {
                    if (sender instanceof Player) {
                        if (!net.getFromID(args[1]).getOwner().equals((Player) sender) && !((Player) sender).hasPermission("autosort.admin")) {
                            lang.returnMessage(sender, "nopermission");
                            return true;
                        }
                    }
                    net.delete(args[1]);
                    lang.returnMessage(sender, "delete.success", net.getFromID(args[1]));
                    return true;
                }
                lang.returnMessage(sender, "delete.nonetwork");
                return true;
            }

            else if (args[0].equalsIgnoreCase("select")) {

                if (args.length < 2) {
                    returnMessage(sender, "select.nonetwork");
                    return true;
                }

                if (sender instanceof Player) {
                    net.selectNetwork((Player) sender, net.getFromID(args[1]));
                }

                if (sender instanceof ConsoleCommandSender) {
                    net.consoleSelectNetwork(net.getFromID(args[1]));
                }

                lang.returnMessage(sender, "select.success", net.getFromID(args[1]));

                return true;
            }

            else if (args[0].equalsIgnoreCase("info")) {
                StorageNetwork network = getSelected(sender);
                if (network == null) {
                    lang.returnMessage(sender, "select.noselected");
                    return true;
                }

                returnMessage(sender, "Name: " + network.getID());
                returnMessage(sender, "Owner: " + network.getOwner());
                returnMessage(sender, "Input Containers: ");
                for (InputContainer inputContainer : network.getInputChests()) {
                    returnMessage(sender, "X: " + inputContainer.getPos().getX() + " Y: " + inputContainer.getPos().getY() + " Z: " + inputContainer.getPos().getZ() + " World: " + inputContainer.getPos().getDim());
                }
                returnMessage(sender, "Item Containers: ");
                for (ItemContainer itemContainer : network.getSortingChests()) {
                    returnMessage(sender, "X: " + itemContainer.getPos().getX() + " Y: " + itemContainer.getPos().getY() + " Z: " + itemContainer.getPos().getZ() + " World: " + itemContainer.getPos().getDim());
                }
                returnMessage(sender, "Miscellaneous Containers: ");
                for (MiscContainer miscContainer : network.getMiscChests()) {
                    returnMessage(sender, "X: " + miscContainer.getPos().getX() + " Y: " + miscContainer.getPos().getY() + " Z: " + miscContainer.getPos().getZ() + " World: " + miscContainer.getPos().getDim());
                }
                returnMessage(sender, "");

                return true;
            }

            else if (args[0].equalsIgnoreCase("list")) {

                UUID owner;

                if (sender instanceof Player) {
                    owner = ((Player) sender).getUniqueId();
                }

                else {
                    if (args.length == 3) {
                        owner = Bukkit.getPlayer(args[2]).getUniqueId();
                    }
                    else {
                        lang.returnMessage(sender, "list.noplayer");
                        return true;
                    }
                }

                Bukkit.getLogger().info(net.listFromOwner(owner).toString());
                if (net.listFromOwner(owner).isEmpty()) {
                    lang.returnMessage(sender, "list.empty");
                }
                else {
                    lang.returnMessage(sender, "list");
                    for (int i = 0; i < net.listFromOwner(owner).size(); i++) {
                        returnMessage(sender, net.listFromOwner(owner).get(i).getID().toString());
                    }
                }

                return true;

            }

            else if (args[0].equalsIgnoreCase("listall")) {
                Bukkit.getLogger().info(net.listAll().toString());
                if (net.listAll().isEmpty()) {
                    lang.returnMessage(sender, "listall.empty");
                }
                else {
                    lang.returnMessage(sender, "listall");
                    for (int i = 0; i < net.listAll().size(); i++) {
                        returnMessage(sender, net.listAll().get(i).getID().toString());
                    }
                }
                return true;
            }

            else if (args[0].equalsIgnoreCase("container")) {

                Location pos = new Location(0, 0, 0, "world");
                StorageNetwork network;

                if (args.length < 2) {
                    lang.returnMessage(sender, "component.notype");
                    return true;
                }

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    pos = new Location(player.getTargetBlock(null, 5));
                    network = net.getSelectedNetwork(player);

                    if (network == null) {
                        lang.returnMessage(sender, "select.noselected");
                    }

                    if (args[1].equalsIgnoreCase("input")) {
                        network.addInputChest(pos);
                        lang.returnMessage(sender, "component.input.add");
                    }

                    else if (args[1].equalsIgnoreCase("sorting")) {
                        if (args.length < 3) {
                            lang.returnMessage(sender, "component.item.noitem");
                        }
                        network.addItemChest(pos, args[2].toUpperCase());
                        lang.returnMessage(sender, "component.item.add");
                    }

                    else if (args[1].equalsIgnoreCase("misc")) {
                        network.addMiscChest(pos, false);
                        lang.returnMessage(sender, "component.misc.add");
                    }
                }

                else {
                    if (args.length < 5) {
                        lang.returnMessage(sender, "component.nolocation");
                    }
                    pos.setX(Integer.parseInt(args[2]));
                    pos.setY(Integer.parseInt(args[3]));
                    pos.setZ(Integer.parseInt(args[4]));
                    pos.setDim(args[5]);

                    network = net.getConsoleSelection();

                    if (network == null) {
                        lang.returnMessage(sender, "select.noselection");
                    }

                    if (args[1].equalsIgnoreCase("input")) {
                        network.addInputChest(pos);
                        lang.returnMessage(sender, "component.input.add", network, pos);
                        return true;
                    }

                    else if (args[1].equalsIgnoreCase("sorting")) {
                        if (args.length < 6) {
                            lang.returnMessage(sender, "component.item.noitem");
                        }
                        network.addItemChest(pos, args[6].toUpperCase());
                        lang.returnMessage(sender, "component.item.add", network, pos);
                        return true;
                    }

                    else if (args[1].equalsIgnoreCase("misc")) {
                        network.addMiscChest(pos, false);
                        lang.returnMessage(sender, "component.misc.add", network, pos);
                        return true;

                    }
                }
                return true;
            }

            else if (args[0].equalsIgnoreCase("sort")) {
                getSelected(sender).sortAll();
                return true;
            }

            else if (args[0].equalsIgnoreCase("checkinv")) {
                if (args.length > 4) {
                    BaseContainer container = new BaseContainer(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]),args[4]);
                    returnMessage(sender, String.valueOf(container.getInventory().getSize()));
                }
                return true;
            }

            else {
                lang.returnMessage(sender, "invalid");
            }
        }


        return true;
    }

    private void returnMessage(CommandSender sender, String text) {
        if (sender instanceof Player) {

            Player player = (Player) sender;
            player.sendMessage(text);

        }

        if (sender instanceof ConsoleCommandSender) {
            Bukkit.getLogger().info(text);
        }
    }

    private void sendHelp(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            for (int i = 0; i < playerHelpMessage.toArray().length; i++) {
                sendJSONMessage(player, playerHelpMessage.get(i).toString());
            }
        }

        if (sender instanceof BlockCommandSender) {
            System.out.println(helpMessage);
        }

        if (sender instanceof ConsoleCommandSender) {
            System.out.println(helpMessage);
        }

    }

    private void sendJSONMessage(Player player, String message) {
        player.performCommand("execute as " + player.getUniqueId() + " run tellraw @s " + message);

    }


    private StorageNetwork getSelected(CommandSender sender) {
        StorageNetwork result;
        if (sender instanceof Player) {
            result =  net.getSelectedNetwork((Player) sender);
        }
        else {
            result =  net.getConsoleSelection();
        }
        return result;
    }

    private List playerHelpMessage = Arrays.asList(
            "[\"\",{\"text\":\"       Autosort Plugin - Version 1.0.0 ========================================\",\"bold\":true,\"color\":\"dark_green\"}]",
            "\"\"",
            "[\"\",{\"text\":\"/as help <command>\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as help\"}},{\"text\":\" - \"},{\"text\":\"Help for a command\",\"color\":\"yellow\"}]",
            "[\"\",{\"text\":\"/as help <page>\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as help\"}},{\"text\":\" - \"},{\"text\":\"Show this menu\",\"color\":\"yellow\"}]",
            "\"\"",
            "[\"\",{\"text\":\"/as create <network>\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as create\"}},{\"text\":\" - \"},{\"text\":\"Create a storage network\",\"color\":\"yellow\"}]",
            "[\"\",{\"text\":\"/as delete <network>\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as delete\"}},{\"text\":\" - \"},{\"text\":\"Delete a storage network\",\"color\":\"yellow\"}]",
            "\"\"",
            "[\"\",{\"text\":\"/as select <network>\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as select\"}},{\"text\":\" - \"},{\"text\":\"Select a storage network\",\"color\":\"yellow\"}]",
            "\"\"",
            "[\"\",{\"text\":\"/as info\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as info\"}},{\"text\":\" - \"},{\"text\":\"Show the stats of your storage network\",\"color\":\"yellow\"}]",
            "[\"\",{\"text\":\"/as list\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/as list\"}},{\"text\":\" - \"},{\"text\":\"List all your storage networks\",\"color\":\"yellow\"}]"
    );

    private String helpMessage = """

       Autosort Plugin - Version 1.0.0 
==========================================

/as help <command> - Help for a command
/as help <page> - Show this menu

/as create <network> - Create a storage network
/as delete <network> - Delete a storage network

/as select <network> - Select a storage network

/as info - Show the stats of your storage network
/as list - List all your storage networks

""";

    public void saveData() {
        net.saveData();
    }

    public void loadData() {
        net.loadData();
    }

}
