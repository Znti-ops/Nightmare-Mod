package com.pesadilla.client;

import com.pesadilla.Pesadilla;
import com.pesadilla.Sanity;
import com.pesadilla.WatcherEntity;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class PesadillaClient implements ClientModInitializer {
    public static final ModelLayerLocation WATCHER_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Pesadilla.MOD_ID, "watcher"), "main");
    private static final Identifier FACE =
            Identifier.fromNamespaceAndPath(Pesadilla.MOD_ID, "textures/gui/jumpscare.png");

    private static final SoundEvent[] HALLUCINATIONS = {
            SoundEvents.ENDERMAN_STARE, SoundEvents.WARDEN_HEARTBEAT,
            SoundEvents.WARDEN_NEARBY_CLOSER, SoundEvents.ENDERMAN_SCREAM};

    private static int jumpscareTicks = 0;
    private static int cooldown = 0;

    @Override
    public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(WATCHER_LAYER, WatcherModel::createLayer);
        EntityRenderers.register(Pesadilla.WATCHER, WatcherRenderer::new);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(Pesadilla.MOD_ID, "sanity"), PesadillaClient::drawHud);
        ClientTickEvents.END_CLIENT_TICK.register(PesadillaClient::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.isPaused()) return;
        if (cooldown > 0) cooldown--;
        if (jumpscareTicks > 0) {
            jumpscareTicks--;
            mc.player.setYRot(mc.player.getYRot() + (mc.level.getRandom().nextFloat() - 0.5f) * 8f);
        }

        List<WatcherEntity> watchers = mc.level.getEntitiesOfClass(WatcherEntity.class,
                mc.player.getBoundingBox().inflate(24));
        int light = mc.level.getMaxLocalRawBrightness(mc.player.blockPosition());

        // La cordura baja en la oscuridad y cerca del Acechador; sube con luz.
        if (light < 4) Sanity.value -= 0.12f;
        else if (light >= 8) Sanity.value += 0.08f;
        if (!watchers.isEmpty()) Sanity.value -= 0.1f;
        Sanity.value = Math.max(0f, Math.min(100f, Sanity.value));

        // Alucinaciones sonoras con poca cordura.
        if (Sanity.value < 40f && mc.level.getRandom().nextInt(400) == 0) {
            SoundEvent s = HALLUCINATIONS[mc.level.getRandom().nextInt(HALLUCINATIONS.length)];
            double a = mc.level.getRandom().nextDouble() * Math.PI * 2;
            mc.level.playLocalSound(mc.player.getX() + Math.cos(a) * 6, mc.player.getY(),
                    mc.player.getZ() + Math.sin(a) * 6, s, SoundSource.AMBIENT, 1.0f, 0.7f, false);
        }

        // Jumpscare: mirar al Acechador de cerca, o que se te acerque demasiado.
        if (cooldown == 0) {
            for (WatcherEntity w : watchers) {
                double dist = w.distanceTo(mc.player);
                Vec3 look = mc.player.getLookAngle();
                Vec3 toWatcher = w.getEyePosition().subtract(mc.player.getEyePosition()).normalize();
                if ((dist < 10 && look.dot(toWatcher) > 0.9) || dist < 2.5) {
                    jumpscareTicks = 30;
                    cooldown = 600;
                    Sanity.value = Math.max(0f, Sanity.value - 25f);
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                            SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.5f, 0.5f, false);
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                            SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 1.5f, 0.6f, false);
                    break;
                }
            }
        }
    }

    private static void drawHud(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // Viñeta roja cuando la cordura es baja.
        if (Sanity.value < 40f) {
            int alpha = (int) ((40f - Sanity.value) / 40f * 110f);
            g.fill(0, 0, w, h, (alpha << 24) | 0x300000);
        }

        // Barra de cordura.
        int x = 10, y = h - 42, bar = 80;
        g.text(mc.font, "Cordura", x, y - 11, 0xFFCCCCCC, true);
        g.fill(x - 1, y - 1, x + bar + 1, y + 7, 0xFF000000);
        int color = Sanity.value > 60 ? 0xFF4FC3F7 : Sanity.value > 30 ? 0xFFFFB300 : 0xFFE53935;
        g.fill(x, y, x + (int) (bar * Sanity.value / 100f), y + 6, color);

        // Jumpscare a pantalla completa.
        if (jumpscareTicks > 0) {
            g.fill(0, 0, w, h, 0xFF000000);
            g.blit(RenderPipelines.GUI_TEXTURED, FACE, 0, 0, 0, 0, w, h, 64, 64, 64, 64);
            if ((jumpscareTicks / 3) % 2 == 0) {
                g.fill(0, 0, w, h, 0x70FF0000);
            }
        }
    }
}
