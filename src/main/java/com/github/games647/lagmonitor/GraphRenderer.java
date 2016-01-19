package com.github.games647.lagmonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public class GraphRenderer extends MapRenderer {

    private static final int MAX_WIDTH = 128;
    private static final int MAX_HEIGHT = 128;

    //orange
    private static final byte MAX_HEAP_COLOR = MapPalette.matchColor(235, 171, 96);

    //blue
    private static final byte USED_HEAP_COLOR = MapPalette.matchColor(105, 182, 212);

    private int nextUpdate;
    private int nextPosX;

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        //max height and width = 128 (index from 0-127)
        if (nextUpdate <= 0) {
            nextUpdate = 10;

            if (nextPosX >= MAX_WIDTH) {
                //start again from the beginning
                nextPosX = 0;
            }

            clearMap(canvas, nextPosX);

            MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            //byte -> mega byte
            int max = (int) (heapUsage.getCommitted() / 1024 / 1024);
            int used = (int) (heapUsage.getUsed() / 1024 / 1024);

            //round to the next 100 e.g. 801 -> 900
            int roundedMax = ((max + 99) / 100) * 100;

            int maxHeight = getHeightPercent(roundedMax, max);
            int usedHeight = getHeightPercent(roundedMax, used);

            //x=0 y=0 is the left top point so convert it
            int convertedMaxHeight = MAX_HEIGHT - maxHeight;
            int convertedUsedHeight = MAX_HEIGHT - usedHeight;
            canvas.setPixel(nextPosX, convertedMaxHeight, MAX_HEAP_COLOR);
            for (int yPos = convertedMaxHeight; yPos < 128; yPos++) {
                canvas.setPixel(nextPosX, yPos, MAX_HEAP_COLOR);
            }

            canvas.setPixel(nextPosX, convertedUsedHeight, USED_HEAP_COLOR);
            for (int yPos = convertedUsedHeight; yPos < 128; yPos++) {
                canvas.setPixel(nextPosX, yPos, USED_HEAP_COLOR);
            }

            //override the color
            canvas.drawText(0, 0, MinecraftFont.Font, "Heap Usage (MB)");
            canvas.drawText(110, 0, MinecraftFont.Font, Integer.toString(roundedMax));
            canvas.drawText(110, 64, MinecraftFont.Font, Integer.toString(roundedMax / 2));
            canvas.drawText(MAX_WIDTH - MinecraftFont.Font.getWidth("0"), 120, MinecraftFont.Font, "0");
            nextPosX++;
        }

        nextUpdate--;
    }

    private int getHeightPercent(int maxValue, int value) {
        return MAX_HEIGHT * value / maxValue;
    }

    private void clearMap(MapCanvas canvas, int posX) {
        //resets the complete y coords on this x in order to free unused
        for (int yPos = 0; yPos < MAX_HEIGHT; yPos++) {
            canvas.setPixel(posX, yPos, (byte) 0);
        }
    }

    private void clearMap(MapCanvas canvas) {
        for (int xPos = 0; xPos < MAX_WIDTH; xPos++) {
            for (int yPos = 0; yPos < 128; yPos++) {
                canvas.setPixel(xPos, yPos, (byte) 0);
            }
        }
    }
}
