package dev.by1337.core;

import dev.by1337.core.bridge.registry.LegacyRegistryBridge;
import dev.by1337.core.impl.bridge.command.BukkitCommandRegisterImpl;
import dev.by1337.core.impl.bridge.inventory.InventoryUtilImpl;
import dev.by1337.core.impl.bridge.inventory.ItemStackSerializerImpl;
import dev.by1337.core.impl.bridge.nbt.NbtBridgeImpl;
import dev.by1337.core.impl.bridge.world.BlockEntityUtilImpl;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

public class BridgeBootstrapper {

    public static void bootstrap() {
        bootRegistryBridge();
        BCore.bukkitCommandRegister = new BukkitCommandRegisterImpl();
        BCore.inventoryUtil = new InventoryUtilImpl();
        BCore.blockEntityUtil = new BlockEntityUtilImpl();
        BCore.itemStackSerializer = new ItemStackSerializerImpl();
        BCore.nbtBridge = new NbtBridgeImpl();
    }

    private static void bootRegistryBridge() {
        ((LegacyRegistryBridge.RegistryImpl<Particle>) LegacyRegistryBridge.PARTICLE_TYPE).importData(
                Registry.PARTICLE_TYPE.iterator(),
                v -> v,
                Particle::getKey
        );
        ((LegacyRegistryBridge.RegistryImpl<PotionEffectType>) LegacyRegistryBridge.MOB_EFFECT).importData(
                Registry.POTION_EFFECT_TYPE.iterator(),
                v -> v,
                PotionEffectType::getKey);

    }
}
