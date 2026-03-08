package ru.nametag.client.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.nametag.client.NametagClient;

@Mixin(InGameHud.class)
public class ScoreboardMixin {
    
    // Перехватываем отрисовку текста (когда сервер использует форматированный Text)
    @ModifyArg(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"
        ),
        index = 1
    )
    private Text modifyScoreboardText(Text text) {
        return NametagClient.replaceName(text);
    }

    // Перехватываем отрисовку обычных строк (на случай, если HolyWorld шлет текст сырой строкой)
    @ModifyArg(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
        ),
        index = 1
    )
    private String modifyScoreboardString(String string) {
        if (NametagClient.fakeName != null && string != null) {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client != null && client.getSession() != null) {
                String realName = client.getSession().getUsername();
                if (realName != null && !realName.isEmpty() && string.contains(realName)) {
                    return string.replace(realName, NametagClient.fakeName);
                }
            }
        }
        return string;
    }
}
