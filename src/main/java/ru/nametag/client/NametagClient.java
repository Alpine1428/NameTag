package ru.nametag.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
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
                        context.getSource().sendFeedback(Text.literal("§a[Nametag] Ваш ник визуально изменен на: §e" + fakeName));
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

    public static Text replaceName(Text original) {
        if (fakeName == null || original == null) return original;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) return original;
        
        String realName = client.getSession().getUsername();
        if (realName == null || realName.isEmpty()) return original;

        try {
            String json = Text.Serializer.toJson(original);
            if (json.contains(realName)) {
                String newJson = json.replace(realName, fakeName);
                return Text.Serializer.fromJson(newJson);
            }
        } catch (Exception e) {
            // Игнорируем ошибки сериализации
        }
        return original;
    }
}
