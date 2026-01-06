package tech.pmman.csutility.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

public class MinecraftTool {
    public static boolean isServerSide(Level level){
        return !level.isClientSide();
    }

    public static LocalPlayer getLocalPLayer(){
        return Minecraft.getInstance().player;
    }

    public static String getLocalPlayerStringUUID(){
        LocalPlayer localPLayer = getLocalPLayer();
        if (localPLayer == null) return null;
        return localPLayer.getStringUUID();
    }
}
