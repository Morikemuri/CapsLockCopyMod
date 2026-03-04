package gg.nade.capslockcopy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

// ================================================================
// [CAPSLOCKCOPY::HANDLER] :: key listener + target resolver
// ray-trace player lookup -> clipboard write on CapsLock press
// ================================================================
@Mod.EventBusSubscriber(modid = CapsLockCopyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final double RANGE        = 15.0; // max targeting distance (blocks)
    private static final double HITBOX_EXPAND = 0.6; // hitbox inflation for ray test

    // INPUT: fire on CapsLock press (key 280), bail if screen open
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (event.getKey() != GLFW.GLFW_KEY_CAPS_LOCK) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return;

        PlayerEntity target = findTarget(mc);
        if (target == null) return;

        String name = target.getDisplayName().getString();
        mc.keyboardHandler.setClipboard(name); // COPY: write to system clipboard
        // NOTIFY: send confirmation message to local chat
        mc.player.sendMessage(
            new StringTextComponent("§8[§aCapsLockCopy§8] §fСкопировано: §e" + name),
            mc.player.getUUID()
        );
    }

    // RESOLVE: find closest player in look direction, ray-trace first, fallback to distance
    private static PlayerEntity findTarget(Minecraft mc) {
        PlayerEntity player = mc.player;
        Vector3d eyePos  = player.getEyePosition(1.0f);
        Vector3d lookVec = player.getLookAngle();
        Vector3d lookEnd = eyePos.add(lookVec.scale(RANGE));

        // QUERY: get all players in bounding volume around look ray
        AxisAlignedBB queryBox = new AxisAlignedBB(eyePos, lookEnd).inflate(3.0);
        List<PlayerEntity> candidates = mc.level.getEntitiesOfClass(
            PlayerEntity.class, queryBox, p -> p != player);
        if (candidates.isEmpty()) return null;

        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;

        // PASS 1: ray-vs-hitbox intersection -> pick closest hit point
        for (PlayerEntity e : candidates) {
            AxisAlignedBB hitbox = e.getBoundingBox().inflate(HITBOX_EXPAND);
            Optional<Vector3d> hit = hitbox.clip(eyePos, lookEnd);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < bestDist) { bestDist = dist; best = e; }
            }
        }
        if (best != null) return best;

        // PASS 2: fallback - nearest entity within RANGE by direct distance
        for (PlayerEntity e : candidates) {
            double dist = e.distanceTo(player);
            if (dist < RANGE && dist < bestDist) { bestDist = dist; best = e; }
        }
        return best;
    }
}
