package com.pesadilla.item;

import com.pesadilla.Pesadilla;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** La muñeca llama al Acechador. */
public class CursedDollItem extends Item {
    public CursedDollItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            Pesadilla.spawnNear((ServerLevel) level, player, 8);
            player.displayClientMessage(Component.literal("Algo te ha oído..."), true);
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
