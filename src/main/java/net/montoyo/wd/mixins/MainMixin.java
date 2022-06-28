package net.montoyo.wd.mixins;

import net.minecraft.client.main.Main;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.Configuration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Paths;

@Mixin(value = Main.class, remap = false)
public class MainMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "main", at = @At("HEAD"))
    private static void generations_initRenderdoc(CallbackInfo ci) {
        if (true) {
            Configuration.DEBUG_STREAM.set(System.err);
            Configuration.DEBUG.set(true);
            LOGGER.warn("Enabled LWJGL Debugging");
            if (Files.exists(Paths.get("C:/Program Files/RenderDoc/renderdoc.dll"))) {
                System.load("C:/Program Files/RenderDoc/renderdoc.dll");
                LOGGER.warn("Loaded Render Debugging");
            }
        }
    }
}