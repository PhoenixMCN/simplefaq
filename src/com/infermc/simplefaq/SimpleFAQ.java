package com.infermc.simplefaq;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SimpleFAQ extends JavaPlugin implements Listener {
    public HashMap<String,String> topics = new HashMap<String, String>();
    public FileConfiguration topicConfig = null;

    @Override
    public void onEnable() {
        getLogger().info("SimpleFAQ Enabled");

        // Register listener and save default config.
        getServer().getPluginManager().registerEvents(this,this);
        saveDefaultConfig();

        // Save a default copy of the topics file if it doesnt exist.
        saveResource("topics.yml",false);

        // Load topics.
        getLogger().info("Loading topics from configuration.");
        reloadTopics();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String command = cmd.getName();

        getLogger().info(command);
        if (command.equalsIgnoreCase("bfaq")) {
            if (sender.hasPermission("simplefaq.broadcast")) {
                if (args.length >= 1) {
                    String topic = getTopic(args[0]);
                    if (topic != null) {
                        getServer().broadcastMessage(format("&b"+topic));
                    } else {
                        sender.sendMessage(format("&cNo such topic '" + args[0].toLowerCase() + "'!"));
                    }
                } else {
                    List<String> topicList = new ArrayList<String>();
                    for (String key : topics.keySet()) {
                        topicList.add(key.toLowerCase());
                    }
                    sender.sendMessage(format("&bKnown topics: "+joinStrings(", ",(String[]) topicList.toArray())));
                }
            } else {
                sender.sendMessage(format("&cNo permissions!"));
            }
            return true;
        } else if (command.equalsIgnoreCase("faq")) {
            if (sender.hasPermission("simplefaq.faq")) {
                if (args.length >= 1) {
                    String topic = getTopic(args[0]);
                    if (topic != null) {
                        sender.sendMessage(format("&b"+topic));
                    } else {
                        sender.sendMessage(format("&cNo such topic '" + args[0].toLowerCase() + "'!"));
                    }
                } else {
                    List<String> topicList = new ArrayList<String>();
                    for (String key : topics.keySet()) {
                        topicList.add(key.toLowerCase());
                    }
                    sender.sendMessage(format("&bKnown topics: "+joinStrings(", ",(String[]) topicList.toArray())));
                }
            } else {
                sender.sendMessage(format("&cNo permissions!"));
            }
            return true;
        } else if (command.equalsIgnoreCase("addfaq")) {
            if (sender.hasPermission("simplefaq.add")) {
                if (args.length >= 2) {
                    if (topics.containsKey(args[0].toLowerCase())) {
                        sender.sendMessage(format("&cTopic already exists!"));
                    } else {
                        List<String> list = new ArrayList<String>();
                        Collections.addAll(list, args);
                        list.remove(0);

                        String text = joinStrings(" ",(String[]) list.toArray());
                        topics.put(args[0].toLowerCase(),text);
                        saveTopics();
                        sender.sendMessage(format("&aAdded FAQ Topic"));
                    }
                } else {
                    return false;
                }
            } else {
                sender.sendMessage(format("&cNo permissions!"));
            }
            return true;
        } else if (command.equalsIgnoreCase("removefaq")) {
            if (sender.hasPermission("simplefaq.remove")) {
                if (args.length > 0) {
                    if (!topics.containsKey(args[0].toLowerCase())) {
                        sender.sendMessage(format("No such topic!"));
                    } else {
                        topics.remove(args[0].toLowerCase());
                        saveTopics();
                        sender.sendMessage(format("&aRemoved FAQ Topic"));
                    }
                } else {
                    sender.sendMessage(format("&aUsage: /removefaq (topic)"));
                }
            } else {
                sender.sendMessage(format("&cNo permissions!"));
            }
            return true;
        } else if (command.equalsIgnoreCase("reloadfaq")) {
            if (sender.hasPermission("simplefaq.reload")) {
                sender.sendMessage(format("&bReloading Configuration and Topics"));
                reloadConfig();
                reloadTopics();
                sender.sendMessage(format("&aReloaded Configuration and Topics"));
            } else {
                sender.sendMessage(format("&cNo permissions!"));
            }
            return true;
        }

        return false;
    }

    public String format(String input) {
        return ChatColor.translateAlternateColorCodes('&',input);
    }

    public String getTopic(String topic) {
        String message = null;

        if (topics.containsKey(topic.toLowerCase())) {
            message = topics.get(topic.toLowerCase());
        }

        return message;
    }

    public void reloadTopics() {
        topics.clear(); /* Clear all topics */
        File topicFile = new File(getDataFolder(),"topics.yml");
        if (!topicFile.exists()) {
            getLogger().warning("Unable to reload topics, 'topics.yml' doesn't exist!");
            return;
        }
        topicConfig = new YamlConfiguration();

        try {
            topicConfig.load(topicFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }


        Configuration topicSection = topicConfig.getRoot();

        if (topicSection != null) {
            for (String section : topicSection.getKeys(false)) {
                if (getConfig().getBoolean("debug",false)) getLogger().info("Loaded topic '" + section.toLowerCase() + "'");
                topics.put(section.toLowerCase(), topicSection.getString(section));
            }
        } else {
            getLogger().info("Unable to read support topics, topics configuration lacks them?");
        }

    }
    public void saveTopics() {
        File topicFile = new File(getDataFolder(),"topics.yml");
        FileConfiguration cfg = new YamlConfiguration();
        for (String key : topics.keySet()) {
            cfg.set(key,topics.get(key));
        }

        try {
            cfg.save(topicFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String joinStrings(String delimeter, String[] strings) {
        String str = "";
        int i = 0;
        for (String s : strings) {
            if (i == 0) {
                str = s;
            } else {
                str = str + delimeter + s;
            }
        }
        return str;
    }
}
