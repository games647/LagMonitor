package com.github.games647.lagmonitor.graphs;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public abstract class GraphRenderer extends MapRenderer {

    //max height and width = 128 (index from 0-127)
    protected static final int MAX_WIDTH = 128;
    protected static final int MAX_HEIGHT = 128;

    //orange
    protected static final byte MAX_COLOR = MapPalette.matchColor(235, 171, 96);

    //blue
    protected static final byte USED_COLOR = MapPalette.matchColor(105, 182, 212);

    private int nextUpdate;
    private int nextPosX;

    private final String title;

    public GraphRenderer(String title) {
        this.title = title;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (nextUpdate <= 0) {
            //paint only every half seconds (20 Ticks / 2)
            nextUpdate = 10;

            if (nextPosX >= MAX_WIDTH) {
                //start again from the beginning
                nextPosX = 0;
            }

            clearMap(canvas, nextPosX);
            //make it more visual where the renderer is at the moment
            clearMap(canvas, nextPosX + 1);
            int maxValue = renderGraphTick(canvas, nextPosX);

            //override the color
            canvas.drawText(0, 0, MinecraftFont.Font, title);

            //count indicators
            canvas.drawText(110, 0, MinecraftFont.Font, Integer.toString(maxValue));
            canvas.drawText(110, MAX_HEIGHT / 2, MinecraftFont.Font, Integer.toString(maxValue / 2));
            canvas.drawText(MAX_WIDTH - MinecraftFont.Font.getWidth("0"), 120, MinecraftFont.Font, "0");
            nextPosX++;
        }

        nextUpdate--;
    }

    public abstract int renderGraphTick(MapCanvas canvas, int nextPosX);

    protected int getHeightScaled(int maxValue, int value) {
        return MAX_HEIGHT * value / maxValue;
    }

    protected void clearMap(MapCanvas canvas, int posX) {
        //resets the complete y coords on this x in order to free unused
        for (int yPos = 0; yPos < MAX_HEIGHT; yPos++) {
            canvas.setPixel(posX, yPos, (byte) 0);
        }
    }

    protected void clearMap(MapCanvas canvas) {
        for (int xPos = 0; xPos < MAX_WIDTH; xPos++) {
            for (int yPos = 0; yPos < 128; yPos++) {
                canvas.setPixel(xPos, yPos, (byte) 0);
            }
        }
    }
}
