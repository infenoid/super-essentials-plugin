package org.local.test;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SEssentials extends JavaPlugin implements CommandExecutor, TabCompleter {

    private HashMap<UUID, UUID> teleportRequests = new HashMap<>();
    private HashMap<UUID, UUID> teleportHereRequests = new HashMap<>();
    private HashMap<UUID, Boolean> privateChatMode = new HashMap<>();
    private HashMap<UUID, Player> privateChatTarget = new HashMap<>();
    private HashMap<UUID, Boolean> afkStatus = new HashMap<>();
    private HashMap<UUID, Location> homes = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("SEssentials Plugin has been enabled");

        // Register commands and tab completers
        getCommand("traverse").setExecutor(this);
        getCommand("traverse").setTabCompleter(this);
        getCommand("taccept").setExecutor(this);
        getCommand("tdeny").setExecutor(this);
        getCommand("there").setExecutor(this);
        getCommand("there").setTabCompleter(this);
        getCommand("chat").setExecutor(this);
        getCommand("chat").setTabCompleter(this);
        getCommand("msg").setExecutor(this);
        getCommand("msg").setTabCompleter(this);
        getCommand("afk").setExecutor(this);
        getCommand("fly").setExecutor(this);
        getCommand("linkt").setExecutor(this); // Register the new command
        getCommand("delhome").setExecutor(this);
        getCommand("delhome").setTabCompleter(this);
        getCommand("home").setExecutor(this);
        getCommand("home").setTabCompleter(this);
        getCommand("sethome").setExecutor(this);
        getCommand("sethome").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("SEssentials Plugin has been disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Command: /sethome
        if (cmd.getName().equalsIgnoreCase("sethome")) {
            homes.put(player.getUniqueId(), player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Home set!");
        }

        // Command: /home
        if (cmd.getName().equalsIgnoreCase("home")) {
            Location home = homes.get(player.getUniqueId());
            if (home != null) {
                player.teleport(home);
                player.sendMessage(ChatColor.GREEN + "Teleported to home!");
            } else {
                player.sendMessage(ChatColor.RED + "Home not set. Use /sethome to set your home.");
            }
            return true;
        }

        // Command: /delhome
        if (cmd.getName().equalsIgnoreCase("delhome")) {
            if (homes.remove(player.getUniqueId()) != null) {
                player.sendMessage(ChatColor.GREEN + "Home deleted!");
            } else {
                player.sendMessage(ChatColor.RED + "Home not set.");
            }
            return true;
        }

        // Command: /fly
        if (cmd.getName().equalsIgnoreCase("fly")) {
            // Toggle flight mode
            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.sendMessage(ChatColor.RED + "Flight mode disabled.");
            } else {
                player.setAllowFlight(true);
                player.sendMessage(ChatColor.GREEN + "Flight mode enabled. Use double jump to fly.");
            }
            return true;
        }

        // Command: /tpa <player>
        if (cmd.getName().equalsIgnoreCase("traverse")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: /traverse <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                teleportRequests.put(target.getUniqueId(), player.getUniqueId());
                TextComponent message = new TextComponent(player.getName() + " has requested to teleport to you. ");
                TextComponent accept = new TextComponent("[Accept]");
                accept.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/taccept"));
                TextComponent deny = new TextComponent("[Deny]");
                deny.setColor(net.md_5.bungee.api.ChatColor.RED);
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tdeny"));
                message.addExtra(accept);
                message.addExtra(" ");
                message.addExtra(deny);
                target.spigot().sendMessage(message);
                player.sendMessage(ChatColor.YELLOW + "Teleport request sent to " + target.getName());

                // Schedule a task to remove the request after 120 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (teleportRequests.containsKey(target.getUniqueId()) && teleportRequests.get(target.getUniqueId()).equals(player.getUniqueId())) {
                            teleportRequests.remove(target.getUniqueId());
                            player.sendMessage(ChatColor.RED + "Teleport request to " + target.getName() + " has timed out.");
                            target.sendMessage(ChatColor.RED + "Teleport request from " + player.getName() + " has timed out.");
                        }
                    }
                }.runTaskLater(SEssentials.this, 2400L); // 2400 ticks = 120 seconds
            } else {
                player.sendMessage(ChatColor.RED + "Player not found or offline.");
            }
            return true;
        }

        // Command: /tpahere <player>
        if (cmd.getName().equalsIgnoreCase("there")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: /there <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                teleportHereRequests.put(target.getUniqueId(), player.getUniqueId());
                TextComponent message = new TextComponent(player.getName() + " has requested you to teleport to them. ");
                TextComponent accept = new TextComponent("[Accept]");
                accept.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/taccept"));
                TextComponent deny = new TextComponent("[Deny]");
                deny.setColor(net.md_5.bungee.api.ChatColor.RED);
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tdeny"));
                message.addExtra(accept);
                message.addExtra(" ");
                message.addExtra(deny);
                target.spigot().sendMessage(message);
                player.sendMessage(ChatColor.YELLOW + "Teleport request sent to " + target.getName() + " to teleport here.");

                // Schedule a task to remove the request after 120 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (teleportHereRequests.containsKey(target.getUniqueId()) && teleportHereRequests.get(target.getUniqueId()).equals(player.getUniqueId())) {
                            teleportHereRequests.remove(target.getUniqueId());
                            player.sendMessage(ChatColor.RED + "Teleport request to " + target.getName() + " has timed out.");
                            target.sendMessage(ChatColor.RED + "Teleport request from " + player.getName() + " has timed out.");
                        }
                    }
                }.runTaskLater(SEssentials.this, 2400L); // 2400 ticks = 120 seconds
            } else {
                player.sendMessage(ChatColor.RED + "Player not found or offline.");
            }
            return true;
        }

        // Command: /tpaccept
        if (cmd.getName().equalsIgnoreCase("taccept")) {
            UUID requesterUUID = teleportRequests.get(player.getUniqueId());
            UUID hereRequesterUUID = teleportHereRequests.get(player.getUniqueId());
            if (requesterUUID != null) {
                Player requester = Bukkit.getPlayer(requesterUUID);
                if (requester != null) {
                    requester.teleport(player.getLocation());
                    requester.sendMessage(ChatColor.GREEN + "Teleport request accepted.");
                    player.sendMessage(ChatColor.GREEN + requester.getName() + " has been teleported to you.");
                    teleportRequests.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "Teleport requester is offline.");
                }
            } else if (hereRequesterUUID != null) {
                Player hereRequester = Bukkit.getPlayer(hereRequesterUUID);
                if (hereRequester != null) {
                    player.teleport(hereRequester.getLocation());
                    player.sendMessage(ChatColor.GREEN + "You have been teleported to " + hereRequester.getName() + ".");
                    hereRequester.sendMessage(ChatColor.GREEN + player.getName() + " has accepted your request to teleport.");
                    teleportHereRequests.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "Requesting player is offline.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "No pending teleport requests.");
            }
            return true;
        }

        // Command: /tpdeny
        if (cmd.getName().equalsIgnoreCase("tdeny")) {
            UUID requesterUUID = teleportRequests.get(player.getUniqueId());
            UUID hereRequesterUUID = teleportHereRequests.get(player.getUniqueId());
            if (requesterUUID != null) {
                Player requester = Bukkit.getPlayer(requesterUUID);
                if (requester != null) {
                    requester.sendMessage(ChatColor.RED + "Teleport request denied.");
                    player.sendMessage(ChatColor.GREEN + "You have denied the teleport request.");
                    teleportRequests.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "Teleport requester is offline.");
                }
            } else if (hereRequesterUUID != null) {
                Player hereRequester = Bukkit.getPlayer(hereRequesterUUID);
                if (hereRequester != null) {
                    hereRequester.sendMessage(ChatColor.RED + player.getName() + " has denied your request to teleport.");
                    player.sendMessage(ChatColor.GREEN + "You have denied the teleport request to teleport here.");
                    teleportHereRequests.remove(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "Requesting player is offline.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "No pending teleport requests.");
            }
            return true;
        }

        // Command: /chat [toggle/set] [player]
        if (cmd.getName().equalsIgnoreCase("chat")) {
            if (args.length == 0) {
                // Show current chat mode
                boolean isPrivate = privateChatMode.getOrDefault(player.getUniqueId(), false);
                player.sendMessage(ChatColor.GREEN + "You are currently in " + (isPrivate ? "private" : "global") + " chat mode.");
                return true;
            }

            if (args[0].equalsIgnoreCase("toggle")) {
                // Toggle chat mode
                boolean isPrivate = privateChatMode.getOrDefault(player.getUniqueId(), false);
                privateChatMode.put(player.getUniqueId(), !isPrivate);
                player.sendMessage(ChatColor.YELLOW + "Chat mode switched to " + (!isPrivate ? "private" : "global") + ".");
                return true;
            }

            if (args[0].equalsIgnoreCase("set") && args.length == 2) {
                // Set private chat target
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null && target.isOnline()) {
                    privateChatTarget.put(player.getUniqueId(), target);
                    player.sendMessage(ChatColor.GREEN + "Now chatting privately with " + target.getName() + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found or offline.");
                }
                return true;
            }
        }

        // Command: /msg <player> <message>
        if (cmd.getName().equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                target.sendMessage(ChatColor.LIGHT_PURPLE + "[MSG] " + player.getName() + ": " + message);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[MSG] To " + target.getName() + ": " + message);
            } else {
                player.sendMessage(ChatColor.RED + "Player not found or offline.");
            }
            return true;
        }

        // Command: /afk
        if (cmd.getName().equalsIgnoreCase("afk")) {
            boolean isAfk = afkStatus.getOrDefault(player.getUniqueId(), false);
            afkStatus.put(player.getUniqueId(), !isAfk);

            if (!isAfk) {
                // Notify other players that this player is now AFK
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!p.equals(player)) {
                        p.sendMessage(ChatColor.YELLOW + player.getName() + " is now AFK.");
                    }
                });
                player.sendMessage(ChatColor.YELLOW + "You are now AFK.");
            } else {
                // Notify others that the player is no longer AFK
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!p.equals(player)) {
                        p.sendMessage(ChatColor.GREEN + player.getName() + " is no longer AFK.");
                    }
                });
                player.sendMessage(ChatColor.GREEN + "You are no longer AFK.");
            }
            return true;
        }

        // Command: /linkt <player> <link>
        if (cmd.getName().equalsIgnoreCase("linkt")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /linkt <player> <link>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                String link = args[1];
                target.sendMessage(ChatColor.YELLOW + "You have received a link: " + ChatColor.UNDERLINE + ChatColor.BLUE + link);
                sender.sendMessage(ChatColor.GREEN + "Link sent to " + target.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found or offline.");
            }
            return true;
        }

        return false; // If command not recognized, return false
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("traverse") || cmd.getName().equalsIgnoreCase("twhere")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()); // Suggest online players
        }

        if (cmd.getName().equalsIgnoreCase("linkt")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()); // Suggest online players
            }
        }
        
        if (cmd.getName().equalsIgnoreCase("chat")) {
            if (args.length == 1) {
                return Arrays.asList("toggle", "set");
            } else if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()); // Suggest online players
            }
        }

        if (cmd.getName().equalsIgnoreCase("msg")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()); // Suggest online players
            }
        }

        if (cmd.getName().equalsIgnoreCase("afk")) {
            // No tab completion for AFK
            return Arrays.asList();
        }

        return Arrays.asList(); // Default empty suggestions
    }

}