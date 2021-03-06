package factionsystem.Commands;

import factionsystem.Main;
import factionsystem.Objects.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static factionsystem.Subsystems.UtilitySubsystem.findUUIDBasedOnPlayerName;
import static factionsystem.Subsystems.UtilitySubsystem.isInFaction;
import static org.bukkit.Bukkit.getServer;

public class InviteCommand {

    Main main = null;

    public InviteCommand(Main plugin) {
        main = plugin;
    }

    public boolean invitePlayer(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (isInFaction(player.getUniqueId(), main.factions)) {
                for (Faction faction : main.factions) {
                    if (faction.isOwner(player.getUniqueId()) || faction.isOfficer(player.getUniqueId())) {
                        if (args.length > 1) {
                            UUID playerUUID = findUUIDBasedOnPlayerName(args[1]);
                            // invite if player isn't in a faction already
                            if (!(isInFaction(playerUUID, main.factions))) {
                                faction.invite(playerUUID);
                                try {
                                    Player target = Bukkit.getServer().getPlayer(args[1]);
                                    target.sendMessage(ChatColor.GREEN + "You've been invited to " + faction.getName() + "! Type /mf join " + faction.getName() + " to join.");
                                } catch (Exception ignored) {

                                }
                                player.sendMessage(ChatColor.GREEN + "Invitation sent!");

                                int seconds = 60 * 60 * 24;

                                // make invitation expire in 24 hours, if server restarts it also expires since invites aren't saved
                                getServer().getScheduler().runTaskLater(main, new Runnable() {
                                    @Override
                                    public void run() {
                                        faction.uninvite(playerUUID);
                                        try {
                                            Player target = Bukkit.getServer().getPlayer(args[1]);
                                            target.sendMessage(ChatColor.RED + "Your invitation to " + faction.getName() + " has expired!.");
                                        } catch (Exception ignored) {
                                            // player offline
                                        }
                                    }
                                }, seconds * 20);

                                return true;
                            }
                            else {
                                player.sendMessage(ChatColor.RED + "That player is already in a faction, sorry!");
                                return false;
                            }


                        }
                        else {
                            player.sendMessage(ChatColor.RED + "Usage: /mf invite (player-name)");
                            return false;
                        }
                    }
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "You need to be in a faction to use this command.");
            }
        }
        return false;
    }

}
