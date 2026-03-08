package ru.nametag.client.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.nametag.client.NametagClient;

@Mixin(InGameHud.class)
public class ScoreboardMixin {
    
    // Перехватываем метод создания строчек боковой панели ПРЯМО ПЕРЕД рендером
    @Redirect(
        method = "renderScoreboardSidebar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"
        )
    )
    private MutableText redirectScoreboardLine(AbstractTeam team, Text name) {
        // Сервер наложил цвета -> мы перехватили -> отправили в наш бронебойный фильтр
        MutableText originalFormatted = Team.decorateName(team, name);
        return (MutableText) NametagClient.replaceName(originalFormatted);
    }
}
