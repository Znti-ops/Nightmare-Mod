package com.pesadilla.item;

import com.pesadilla.Pesadilla;
import com.pesadilla.Sanity;
import com.pesadilla.WatcherEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** El amuleto de sal espanta a los Acechadores cercanos y restaura la cordura. */
public class SaltCharmItem extends Item {
    public SaltCharmItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            Sanity.value = 100f;
        } else {
            level.getEntitiesOfClass(WatcherEntity.class, player.getBoundingBox().inflate(16))
                    .forEach(Entity::discard);
            player.displayClientMessage(Component.literal("La sal los aleja..."), true);
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
