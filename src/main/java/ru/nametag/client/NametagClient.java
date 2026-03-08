package ru.nametag.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class NametagClient implements ClientModInitializer {
    public static String fakeName = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("nametag")
                .then(ClientCommandManager.argument("nick", StringArgumentType.word())
                    .executes(context -> {
                        fakeName = StringArgumentType.getString(context, "nick");
                        context.getSource().sendFeedback(Text.literal("§a[Nametag] Ваш ник изменен на: §e" + fakeName));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        fakeName = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена ника выключена."));
                        return 1;
                    }))
            );
        });
    }

    // Главный метод: запускает молекулярный разбор текста
    public static Text replaceName(Text original) {
        if (fakeName == null || original == null) return original;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) return original;
        
        String realName = client.getSession().getUsername();
        if (realName == null || realName.isEmpty()) return original;

        return replaceRecursively(original, realName, fakeName);
    }

    // Рекурсивный фильтр: обходит защиту сервера и меняет текст, сохраняя стили
    private static MutableText replaceRecursively(Text text, String target, String replacement) {
        MutableText newText;
        
        // 1. Изменяем содержимое, если это обычный текст
        if (text.getContent() instanceof LiteralTextContent) {
            String str = ((LiteralTextContent) text.getContent()).string();
            if (str.contains(target)) {
                newText = Text.literal(str.replace(target, replacement));
            } else {
                newText = Text.literal(str);
            }
        } else {
            newText = text.copyContentOnly();
        }

        // 2. Копируем оригинальный цвет, жирность и шрифты
        newText.setStyle(text.getStyle());

        // 3. Перебираем все остальные кусочки текста (префиксы, звездочки)
        for (Text sibling : text.getSiblings()) {
            newText.append(replaceRecursively(sibling, target, replacement));
        }

        return newText;
    }
}
