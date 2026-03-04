package gg.nade.capslockcopy;

import net.minecraftforge.fml.common.Mod;

// ================================================================
// [CAPSLOCKCOPY] :: Forge 1.16.5 client mod
// press CapsLock -> copy crosshair player nickname to clipboard
// ================================================================
@Mod(CapsLockCopyMod.MOD_ID)
public class CapsLockCopyMod {
    public static final String MOD_ID = "capslockcopy";

    public CapsLockCopyMod() {
        // EVENT: ClientEventHandler auto-registered via @Mod.EventBusSubscriber
    }
}
