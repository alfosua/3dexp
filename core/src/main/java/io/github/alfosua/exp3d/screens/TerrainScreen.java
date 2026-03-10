package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import io.github.alfosua.exp3d.Main;

public class TerrainScreen extends Base3DScreen {
    private Model terrainModel;
    private ModelInstance instance;
    private FirstPersonCameraController camController;

    public TerrainScreen(Main game) {
        super(game);

        int width = 50;
        int depth = 50;
        float scale = 2f;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        MeshPartBuilder builder = modelBuilder.part("terrain", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked,
                new Material());

        // Procedural heightmap using sin and cos
        float[][] heights = new float[width][depth];
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                heights[x][z] = (float) (Math.sin(x * 0.2) + Math.cos(z * 0.2)) * 3f;
            }
        }

        // Generate mesh
        for (int x = 0; x < width - 1; x++) {
            for (int z = 0; z < depth - 1; z++) {
                float h = heights[x][z];
                // Color based on height to make it look like terrain
                if (h > 2.5f) {
                    builder.setColor(Color.WHITE); // Snow
                } else if (h > 0f) {
                    builder.setColor(Color.valueOf("8B4513")); // Brown (Dirt/Rock)
                } else {
                    builder.setColor(Color.valueOf("228B22")); // Forest Green
                }

                Vector3 v1 = new Vector3(x * scale, heights[x][z], z * scale);
                Vector3 v2 = new Vector3((x + 1) * scale, heights[x + 1][z], z * scale);
                Vector3 v3 = new Vector3((x + 1) * scale, heights[x + 1][z + 1], (z + 1) * scale);
                Vector3 v4 = new Vector3(x * scale, heights[x][z + 1], (z + 1) * scale);

                builder.triangle(v1, v2, v3);
                builder.triangle(v1, v3, v4);
            }
        }

        terrainModel = modelBuilder.end();
        instance = new ModelInstance(terrainModel);

        // Center the terrain
        instance.transform.setToTranslation(-width * scale / 2f, 0, -depth * scale / 2f);

        cam.position.set(0f, 20f, 30f);
        cam.lookAt(0, 0, 0);
        cam.up.set(0, 1, 0);
        cam.update();

        camController = new FirstPersonCameraController(cam);
        camController.setDegreesPerPixel(0.5f);
        camController.setVelocity(20f);
    }

    @Override
    public void show() {
        super.show();
        multiplexer.addProcessor(camController);
    }

    @Override
    public void render(float delta) {
        super.render(delta); // clears screen

        camController.update(delta);

        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (terrainModel != null) {
            terrainModel.dispose();
            terrainModel = null;
        }
        super.dispose();
    }
}
