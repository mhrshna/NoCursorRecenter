package dev.pausecursorfix.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 26.3 (SDL3) normally receives a center position when the mouse
 * is released. Instead, let SDL leave relative mouse mode without any
 * explicit request to warp the physical pointer.
 *
 * This intentionally affects ALL normal mouse releases (Esc, inventories,
 * losing focus) while leaving the game's actual pause/menu logic intact.
 */
@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Inject(
        method = "releaseMouse(Lcom/mojang/blaze3d/platform/Window;DD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void pausecursorfix$releaseWithoutWarp(
        Window window, double x, double y, CallbackInfo ci
    ) {
        // If SDL fails to disable relative mouse mode, allow vanilla to run,
        // so the user does not get stuck with a hidden/captured cursor.
        if (SDLMouse.SDL_SetWindowRelativeMouseMode(window.handle(), false)) {
            SDLMouse.SDL_ShowCursor();
            ci.cancel();
        }
    }
}
