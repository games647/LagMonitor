package com.github.games647.lagmonitor.graphs;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public abstract class GraphRenderer extends MapRenderer {

    protected static final int TEXT_HEIGHT = MinecraftFont.Font.getHeight();

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

            clearBar(canvas, nextPosX);
            //make it more visual where the renderer is at the moment
            clearBar(canvas, nextPosX + 1);
            int maxValue = renderGraphTick(canvas, nextPosX);

            //override the color
            drawText(canvas, MAX_WIDTH / 2, MAX_HEIGHT / 2, title);

            //count indicators
            drawText(canvas, MAX_WIDTH, TEXT_HEIGHT, Integer.toString(maxValue));
            drawText(canvas, MAX_WIDTH, MAX_HEIGHT / 2, Integer.toString(maxValue / 2));
            drawText(canvas, MAX_WIDTH, MAX_HEIGHT, Integer.toString(0));

            nextPosX++;
        }

        nextUpdate--;
    }

    public abstract int renderGraphTick(MapCanvas canvas, int nextPosX);

    protected int getHeightScaled(int maxValue, int value) {
        return MAX_HEIGHT * value / maxValue;
    }

    protected void clearBar(MapCanvas canvas, int posX) {
        //resets the complete y coordinates on this x in order to free unused
        for (int yPos = 0; yPos < MAX_HEIGHT; yPos++) {
            canvas.setPixel(posX, yPos, (byte) 0);
        }
    }

    protected void clearMap(MapCanvas canvas) {
        for (int xPos = 0; xPos < MAX_WIDTH; xPos++) {
            fillBar(canvas, xPos, 0, (byte) 0);
        }
    }

    protected void fillBar(MapCanvas canvas, int xPos, int yStart, byte color) {
        for (int yPos = yStart; yPos < MAX_HEIGHT; yPos++) {
            canvas.setPixel(xPos, yPos, color);
        }
    }

    protected void drawText(MapCanvas canvas, int midX, int midY, String text) {
        int textWidth = MinecraftFont.Font.getWidth(text);
        canvas.drawText(midX - (textWidth / 2), midY - (TEXT_HEIGHT / 2), MinecraftFont.Font, text);
    }
}
