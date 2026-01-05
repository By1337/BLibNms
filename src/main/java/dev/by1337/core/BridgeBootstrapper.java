package dev.by1337.core;

import dev.by1337.core.impl.util.command.BukkitCommandRegisterImpl;
import dev.by1337.core.impl.util.inventory.InventoryUtilImpl;
import dev.by1337.core.impl.util.inventory.ItemStackSerializerImpl;
import dev.by1337.core.impl.util.world.BlockEntityUtilImpl;
import dev.by1337.core.util.registry.LegacyRegistryBridge;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_17_R1.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.potion.PotionEffectType;

public class BridgeBootstrapper {

    public static void bootstrap() {
        bootRegistryBridge();
        BCore.bukkitCommandRegister = new BukkitCommandRegisterImpl();
        BCore.inventoryUtil = new InventoryUtilImpl();
        BCore.blockEntityUtil = new BlockEntityUtilImpl();
        BCore.itemStackSerializer = new ItemStackSerializerImpl();
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
