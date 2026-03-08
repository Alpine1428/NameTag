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
    // Переменные для ника
    public static String fakeName = null;
    
    // Переменные для титула
    public static String realTitle = null;
    public static String fakeTitle = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            
            // --- Команда /nametag ---
            dispatcher.register(ClientCommandManager.literal("nametag")
                .then(ClientCommandManager.argument("nick", StringArgumentType.string())
                    .executes(context -> {
                        fakeName = StringArgumentType.getString(context, "nick");
                        context.getSource().sendFeedback(Text.literal("§a[Nametag] Ник изменен на: §e" + fakeName));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        fakeName = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена ника выключена."));
                        return 1;
                    }))
            );

            // --- Команда /titultag ---
            dispatcher.register(ClientCommandManager.literal("titultag")
                .then(ClientCommandManager.argument("old_title", StringArgumentType.string())
                    .then(ClientCommandManager.argument("new_title", StringArgumentType.string())
                        .executes(context -> {
                            realTitle = StringArgumentType.getString(context, "old_title");
                            fakeTitle = StringArgumentType.getString(context, "new_title");
                            context.getSource().sendFeedback(Text.literal("§a[Nametag] Титул '§e" + realTitle + "§a' визуально заменен на '§e" + fakeTitle + "§a'"));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        realTitle = null;
                        fakeTitle = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена титула выключена."));
                        return 1;
                    }))
            );
        });
    }

    // Главный метод: передает текст в фильтр
    public static Text replaceName(Text original) {
        if (original == null) return null;
        
        // Если ничего не включено - сразу отдаем оригинал (чтобы не нагружать игру)
        if (fakeName == null && fakeTitle == null) return original;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) return original;
        
        String realName = client.getSession().getUsername();
        if (realName == null || realName.isEmpty()) return original;

        return replaceRecursively(original, realName, fakeName, realTitle, fakeTitle);
    }

    // Рекурсивный фильтр: теперь заменяет и ник, и титул за один проход!
    private static MutableText replaceRecursively(Text text, String tName, String rName, String tTitle, String rTitle) {
        MutableText newText;
        
        if (text.getContent() instanceof LiteralTextContent) {
            String str = ((LiteralTextContent) text.getContent()).string();
            boolean changed = false;
            
            // Замена ника
            if (tName != null && rName != null && str.contains(tName)) {
                str = str.replace(tName, rName);
                changed = true;
            }
            
            // Замена титула
            if (tTitle != null && rTitle != null && str.contains(tTitle)) {
                str = str.replace(tTitle, rTitle);
                changed = true;
            }
            
            if (changed) {
                newText = Text.literal(str);
            } else {
                newText = Text.literal(((LiteralTextContent) text.getContent()).string());
            }
        } else {
            newText = text.copyContentOnly();
        }

        // Копируем цвета
        newText.setStyle(text.getStyle());

        // Проходимся по всем дочерним элементам
        for (Text sibling : text.getSiblings()) {
            newText.append(replaceRecursively(sibling, tName, rName, tTitle, rTitle));
        }

        return newText;
    }
}
