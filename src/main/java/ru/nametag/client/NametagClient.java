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
    public static String realTitle = null;
    public static String fakeTitle = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            
            // --- Команда /nametag ---
            dispatcher.register(ClientCommandManager.literal("nametag")
                .executes(context -> {
                    context.getSource().sendError(Text.literal("§cИспользование: /nametag <ник> или /nametag off"));
                    return 1;
                })
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        fakeName = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена ника выключена."));
                        return 1;
                    }))
                .then(ClientCommandManager.argument("nick", StringArgumentType.string())
                    .executes(context -> {
                        fakeName = StringArgumentType.getString(context, "nick");
                        context.getSource().sendFeedback(Text.literal("§a[Nametag] Ник изменен на: §e" + fakeName));
                        return 1;
                    }))
            );

            // --- Команда /titultag ---
            dispatcher.register(ClientCommandManager.literal("titultag")
                .executes(context -> {
                    context.getSource().sendError(Text.literal("§cИспользование: /titultag <старый_титул> <новый_титул> или /titultag off"));
                    return 1;
                })
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        realTitle = null;
                        fakeTitle = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена титула выключена."));
                        return 1;
                    }))
                .then(ClientCommandManager.argument("old", StringArgumentType.string())
                    .then(ClientCommandManager.argument("new", StringArgumentType.string())
                        .executes(context -> {
                            realTitle = StringArgumentType.getString(context, "old");
                            fakeTitle = StringArgumentType.getString(context, "new");
                            context.getSource().sendFeedback(Text.literal("§a[Nametag] Титул '§e" + realTitle + "§a' визуально заменен на '§e" + fakeTitle + "§a'"));
                            return 1;
                        })))
            );
        });
    }

    public static Text replaceName(Text original) {
        if (original == null) return null;
        if (fakeName == null && fakeTitle == null) return original;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) return original;
        
        String realName = client.getSession().getUsername();
        if (realName == null || realName.isEmpty()) return original;

        return replaceRecursively(original, realName, fakeName, realTitle, fakeTitle);
    }

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

        // Копируем стили (цвета, жирность)
        newText.setStyle(text.getStyle());

        for (Text sibling : text.getSiblings()) {
            newText.append(replaceRecursively(sibling, tName, rName, tTitle, rTitle));
        }

        return newText;
    }
}
