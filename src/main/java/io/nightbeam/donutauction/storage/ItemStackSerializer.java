package io.nightbeam.donutauction.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public final class ItemStackSerializer {

    private ItemStackSerializer() {
    }

    public static String serialize(ItemStack itemStack) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(itemStack);
            objectOutputStream.flush();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to serialize ItemStack.", exception);
        }
    }

    public static ItemStack deserialize(String encoded) {
        byte[] data = Base64.getDecoder().decode(encoded);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(inputStream)) {
            Object object = objectInputStream.readObject();
            if (object instanceof ItemStack itemStack) {
                return itemStack;
            }
            throw new IllegalStateException("Serialized value is not an ItemStack.");
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to deserialize ItemStack.", exception);
        }
    }
}