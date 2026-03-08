package ru.nametag.client.mixin;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nametag.client.NametagClient;

@Mixin(Team.class)
public class ScoreboardMixin {
    
    // Перехватываем генерацию префиксов и суффиксов в самом ядре игры
    // Это автоматически изменит ник в Скорборде (боковой панели), Табе и над головой!
    @Inject(
        method = "decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;", 
        at = @At("RETURN"), 
        cancellable = true
    )
    private static void modifyScoreboardCore(AbstractTeam team, Text name, CallbackInfoReturnable<MutableText> cir) {
        Text original = cir.getReturnValue();
        if (original != null) {
            // Прогоняем полностью собранную сервером строчку (со всеми префиксами и цветами) через наш фильтр
            Text replaced = NametagClient.replaceName(original);
            
            if (replaced instanceof MutableText) {
                cir.setReturnValue((MutableText) replaced);
            } else {
                cir.setReturnValue(replaced.copy());
            }
        }
    }
}
