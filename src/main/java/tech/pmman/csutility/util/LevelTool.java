package tech.pmman.csutility.util;

import net.minecraft.world.level.Level;

public class LevelTool {
    public static boolean isServerSide(Level level){
        return !level.isClientSide();
    }
}
