package dev.by1337.core;

import dev.by1337.core.bridge.registry.LegacyRegistryBridge;
import dev.by1337.core.impl.bridge.command.BukkitCommandRegisterImpl;
import dev.by1337.core.impl.bridge.inventory.InventoryUtilImpl;
import dev.by1337.core.impl.bridge.inventory.ItemStackSerializerImpl;
import dev.by1337.core.impl.bridge.nbt.NbtBridgeImpl;
import dev.by1337.core.impl.bridge.world.BlockEntityUtilImpl;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R2.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftNamespacedKey;
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
                net.minecraft.core.Registry.PARTICLE_TYPE.iterator(),
                CraftParticle::toBukkit,
                particleType -> CraftNamespacedKey.fromMinecraft(net.minecraft.core.Registry.PARTICLE_TYPE.getKey(particleType))
        );
        ((LegacyRegistryBridge.RegistryImpl<PotionEffectType>) LegacyRegistryBridge.MOB_EFFECT).importData(
                net.minecraft.core.Registry.MOB_EFFECT.iterator(),
                CraftPotionEffectType::new,
                mobEffect -> CraftNamespacedKey.fromMinecraft(net.minecraft.core.Registry.MOB_EFFECT.getKey(mobEffect)));

    }
}
