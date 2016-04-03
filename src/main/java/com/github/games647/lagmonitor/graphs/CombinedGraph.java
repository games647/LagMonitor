package com.github.games647.lagmonitor.graphs;

import org.bukkit.map.MapCanvas;

public class CombinedGraph extends GraphRenderer {

    private static final int SPACES = 2;

    private final GraphRenderer[] graphRenderers;

    private final int componentWidth;
    private final int[] componentLastPos;

    public CombinedGraph(GraphRenderer... renderers) {
        super("Combined");

        this.graphRenderers = renderers;
        this.componentLastPos = new int[graphRenderers.length];

        //MAX width - spaces between (length - 1) the components
        componentWidth = MAX_WIDTH - (SPACES * (graphRenderers.length - 1)) / graphRenderers.length;
        for (int i = 0; i < componentLastPos.length; i++) {
            componentLastPos[i] = i * componentWidth + i * SPACES;
        }
    }

    @Override
    public int renderGraphTick(MapCanvas canvas, int nextPosX) {
        for (int i = 0; i < graphRenderers.length; i++) {
            GraphRenderer graphRenderer = graphRenderers[i];
            int position = this.componentLastPos[i];
            position++;

            //index starts with 0 so in the end - 1
            int maxComponentWidth = (i + 1) * componentWidth + i * SPACES - 1;
            if (position > maxComponentWidth) {
                //reset it to the start pos
                position = i * componentWidth + i * SPACES;
            }

            graphRenderer.renderGraphTick(canvas, position);
            this.componentLastPos[i] = position;
        }

        return 100;
    }
}
