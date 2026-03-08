package ru.nametag.client.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.nametag.client.NametagClient;

@Mixin(InGameHud.class)
public class ScoreboardMixin {
    @ModifyVariable(method = "renderScoreboardSidebar", at = @At("STORE"), ordinal = 0)
    private Text modifyScoreboardLine(Text text) {
        return NametagClient.replaceName(text);
    }
}
