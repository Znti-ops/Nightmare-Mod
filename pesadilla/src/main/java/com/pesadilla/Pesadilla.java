package com.pesadilla;

import com.pesadilla.item.CursedDollItem;
import com.pesadilla.item.SaltCharmItem;
import java.util.function.Function;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

public class Pesadilla implements ModInitializer {
    public static final String MOD_ID = "pesadilla";

    public static final EntityType<WatcherEntity> WATCHER = registerEntity(
            "watcher",
            EntityType.Builder.<WatcherEntity>of(WatcherEntity::new, MobCategory.MONSTER).sized(0.6f, 2.4f));

    public static final Item CURSED_DOLL = registerItem("cursed_doll",
            props -> new CursedDollItem(props.stacksTo(16)));
    public static final Item SALT_CHARM = registerItem("salt_charm",
            props -> new SaltCharmItem(props.stacksTo(16)));

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createAttributes());

        // Cada 30 s, de noche o en la oscuridad, el Acechador puede aparecer cerca de un jugador.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 600 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerLevel level = (ServerLevel) player.level();
                if (level.getMaxLocalRawBrightness(player.blockPosition()) >= 7) continue;
                if (level.getRandom().nextFloat() > 0.3f) continue;
                if (!level.getEntitiesOfClass(WatcherEntity.class, player.getBoundingBox().inflate(48)).isEmpty()) continue;
                spawnNear(level, player, 20);
            }
        });
    }

    public static boolean spawnNear(ServerLevel level, Entity player, int distance) {
        for (int i = 0; i < 10; i++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos pos = new BlockPos(x, player.getBlockY(), z);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                    && !level.getBlockState(pos.below()).isAir()) {
                WatcherEntity w = new WatcherEntity(WATCHER, level);
                w.setPos(x + 0.5, pos.getY(), z + 0.5);
                level.addFreshEntity(w);
                return true;
            }
        }
        return false;
    }
}
