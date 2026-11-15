package com.pesadilla.client;

import com.pesadilla.Pesadilla;
import com.pesadilla.WatcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class WatcherRenderer extends MobRenderer<WatcherEntity, WatcherRenderState, WatcherModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Pesadilla.MOD_ID, "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new WatcherModel(context.bakeLayer(PesadillaClient.WATCHER_LAYER)), 0.4f);
    }

    @Override
    public WatcherRenderState createRenderState() {
        return new WatcherRenderState();
    }

    @Override
    public Identifier getTextureLocation(WatcherRenderState state) {
        return TEXTURE;
    }
}
