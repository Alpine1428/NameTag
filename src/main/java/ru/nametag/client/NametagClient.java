package ru.nametag.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NametagClient implements ClientModInitializer {
    public static String fakeName = null;
    public static String realTitle = null;
    public static String fakeTitle = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            
            // --- Команда подмены НИКА (/nametag) ---
            dispatcher.register(ClientCommandManager.literal("nametag")
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        fakeName = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена ника выключена."));
                        return 1;
                    }))
                .then(ClientCommandManager.argument("nick", StringArgumentType.greedyString())
                    .executes(context -> {
                        fakeName = StringArgumentType.getString(context, "nick");
                        context.getSource().sendFeedback(Text.literal("§a[Nametag] Ник изменен на: §e" + fakeName));
                        return 1;
                    }))
            );

            // --- Команда подмены ТИТУЛА (/tag) ---
            dispatcher.register(ClientCommandManager.literal("tag")
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        realTitle = null;
                        fakeTitle = null;
                        context.getSource().sendFeedback(Text.literal("§c[Nametag] Подмена титула выключена."));
                        return 1;
                    }))
                .then(ClientCommandManager.argument("old_title", StringArgumentType.string())
                    .then(ClientCommandManager.argument("new_title", StringArgumentType.greedyString())
                        .executes(context -> {
                            realTitle = StringArgumentType.getString(context, "old_title");
                            fakeTitle = StringArgumentType.getString(context, "new_title");
                            context.getSource().sendFeedback(Text.literal("§a[Nametag] Титул '§e" + realTitle + "§a' заменен на '§e" + fakeTitle + "§a'"));
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
            
            // 1. Подмена ника (точное совпадение)
            if (tName != null && rName != null && str.contains(tName)) {
                str = str.replace(tName, rName);
                changed = true;
            }
            
            // 2. Подмена титула (игнорирует большие/маленькие буквы)
            if (tTitle != null && rTitle != null) {
                try {
                    // Используем регулярное выражение для поиска без учета регистра
                    Pattern pattern = Pattern.compile(Pattern.quote(tTitle), Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        str = matcher.replaceAll(Matcher.quoteReplacement(rTitle));
                        changed = true;
                    }
                } catch (Exception ignored) {}
            }
            
            if (changed) {
                newText = Text.literal(str);
            } else {
                newText = Text.literal(((LiteralTextContent) text.getContent()).string());
            }
        } else {
            newText = text.copyContentOnly();
        }

        // Сохраняем цвета, шрифты и значки сервера
        newText.setStyle(text.getStyle());

        // Проходимся по всем кусочкам текста
        for (Text sibling : text.getSiblings()) {
            newText.append(replaceRecursively(sibling, tName, rName, tTitle, rTitle));
        }

        return newText;
    }
}
