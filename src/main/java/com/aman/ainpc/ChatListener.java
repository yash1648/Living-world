package com.aman.ainpc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Mod.EventBusSubscriber
public class ChatListener {

    public static boolean talkingToNPC = false;
    public static boolean isThinking = false;
    public static ServerPlayer currentNPC = null;

    // ================= AI =================
    private static String sendToAI(String message) {
        try {
            URL url = new URL("http://127.0.0.1:5000/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{\"message\":\"" + message + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            String json = response.toString();
            return json.replace("{\"reply\":\"", "").replace("\"}", "");

        } catch (Exception e) {
            e.printStackTrace();
            return "AI ERROR";
        }
    }

    // ================= EMOTION =================
    private static String extractEmotion(String text) {
        text = text.toLowerCase();

        if (text.contains("smile")) return "smile";
        if (text.contains("laugh") || text.contains("giggle")) return "laugh";
        if (text.contains("wink")) return "wink";

        return "none";
    }

    private static String cleanText(String text) {
        return text.replaceAll("\\*.*?\\*", "").trim();
    }

    // ================= NPC CONTROL =================
    public static void freezeNPC() {
        if (currentNPC != null) {
            currentNPC.setDeltaMovement(0, 0, 0);
            currentNPC.setSprinting(false);
            currentNPC.setShiftKeyDown(false);
        }
    }

    public static void keepNPCStill() {
        if (currentNPC != null) {
            currentNPC.setDeltaMovement(0, 0, 0);
            currentNPC.hurtMarked = true;
        }
    }

    private static void makeNPCLookAtPlayer() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && currentNPC != null) {

            double dx = mc.player.getX() - currentNPC.getX();
            double dz = mc.player.getZ() - currentNPC.getZ();

            float yaw = (float)(Math.atan2(dz, dx) * (180 / Math.PI)) - 90;

            currentNPC.setYRot(yaw);
            currentNPC.setYHeadRot(yaw);
            currentNPC.yBodyRot = yaw;
        }
    }

    // ================= CHAT EVENT =================
    @SubscribeEvent
    public static void onChat(ClientChatEvent event) {

        if (!talkingToNPC || isThinking) return;

        String message = event.getMessage();

        new Thread(() -> {

            isThinking = true;

            try {
                int delay = Math.min(3000, 500 + message.length() * 50);
                int steps = delay / 100;

                for (int i = 0; i < steps; i++) {
                    Thread.sleep(100);

                    Minecraft.getInstance().execute(() -> {
                        makeNPCLookAtPlayer();
                        keepNPCStill();
                    });
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String aiResponse = sendToAI(message);

            String emotion = extractEmotion(aiResponse);
            String cleanReply = cleanText(aiResponse);

            Minecraft.getInstance().execute(() -> {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player != null && mc.level != null && currentNPC != null) {

                    makeNPCLookAtPlayer();
                    keepNPCStill();

                    mc.player.sendSystemMessage(
                            Component.literal("AI NPC: " + cleanReply)
                    );

                    double x = currentNPC.getX();
                    double y = currentNPC.getY() + 2;
                    double z = currentNPC.getZ();

                    if (emotion.equals("smile")) {
                        mc.level.addParticle(ParticleTypes.HEART, x, y, z, 0, 0.1, 0);
                    }

                    if (emotion.equals("laugh")) {
                        mc.level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.1, 0);
                    }

                    if (emotion.equals("wink")) {
                        mc.level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0, 0.1, 0);
                    }
                }
            });

            isThinking = false;

        }).start();
    }
}