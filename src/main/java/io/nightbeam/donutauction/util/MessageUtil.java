package io.nightbeam.donutauction.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class MessageUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final String prefix;

    public MessageUtil(String prefix) {
        this.prefix = prefix;
    }

    public void send(CommandSender sender, String message) {
        sender.sendMessage(component(prefix + message));
    }

    public Component component(String message) {
        return SERIALIZER.deserialize(message);
    }
}