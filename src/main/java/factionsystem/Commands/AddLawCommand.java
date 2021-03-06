package factionsystem.Commands;

import factionsystem.Main;
import factionsystem.Objects.Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static factionsystem.Subsystems.UtilitySubsystem.*;

public class AddLawCommand {

    Main main = null;

    public AddLawCommand(Main plugin) {
        main = plugin;
    }

    public void addLaw(CommandSender sender, String[] args) {
        // player & perm check
        if (sender instanceof Player && ( ((Player) sender).hasPermission("mf.addlaw") || ((Player) sender).hasPermission("mf.default")) ) {

            Player player = (Player) sender;

            if (isInFaction(player.getUniqueId(), main.factions)) {
                Faction playersFaction = getPlayersFaction(player.getUniqueId(), main.factions);

                if (playersFaction.isOwner(player.getUniqueId())) {
                    if (args.length > 1) {
                        String newLaw = createStringFromFirstArgOnwards(args);

                        playersFaction.addLaw(newLaw);

                        player.sendMessage(ChatColor.GREEN + "Law added!");
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Usage: /mf addlaw (new law)");
                    }

                }
                else {
                    player.sendMessage(ChatColor.RED + "You need to be the owner of your faction to use this command!");
                }

            }
            else {
                player.sendMessage(ChatColor.RED + "You need to be in a faction to use this command!");
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need the following permission: 'mf.addlaw'");
        }

    }
}
