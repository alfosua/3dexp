package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.GL20;
import io.github.alfosua.exp3d.Main;

public class CustomModelScreen extends Base3DScreen {
    private Model customModel;
    private ModelInstance instance;

    public CustomModelScreen(Main game) {
        super(game);

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        MeshPartBuilder builder = modelBuilder.part("diamond",
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked | VertexAttributes.Usage.Normal,
                new Material());

        // Mitad superior
        builder.setColor(Color.CYAN);
        builder.triangle(
                new Vector3(0, 5, 0),
                new Vector3(-5, 0, 5),
                new Vector3(5, 0, 5)
        );

        builder.setColor(Color.MAGENTA);
        builder.triangle(
                new Vector3(0, 5, 0),
                new Vector3(5, 0, 5),
                new Vector3(5, 0, -5)
        );

        builder.setColor(Color.YELLOW);
        builder.triangle(
                new Vector3(0, 5, 0),
                new Vector3(5, 0, -5),
                new Vector3(-5, 0, -5)
        );

        builder.setColor(Color.GREEN);
        builder.triangle(
                new Vector3(0, 5, 0),
                new Vector3(-5, 0, -5),
                new Vector3(-5, 0, 5)
        );

        // Mitad inferior
        builder.setColor(Color.RED);
        builder.triangle(
                new Vector3(0, -5, 0),
                new Vector3(5, 0, 5),
                new Vector3(-5, 0, 5)
        );

        builder.setColor(Color.ORANGE);
        builder.triangle(
                new Vector3(0, -5, 0),
                new Vector3(5, 0, -5),
                new Vector3(5, 0, 5)
        );

        builder.setColor(Color.BLUE);
        builder.triangle(
                new Vector3(0, -5, 0),
                new Vector3(-5, 0, -5),
                new Vector3(5, 0, -5)
        );

        builder.setColor(Color.PURPLE);
        builder.triangle(
                new Vector3(0, -5, 0),
                new Vector3(-5, 0, 5),
                new Vector3(-5, 0, -5)
        );

        customModel = modelBuilder.end();
        instance = new ModelInstance(customModel);
        
        cam.position.set(12f, 8f, 12f);
        cam.lookAt(0, 0, 0);
        cam.update();
    }

    @Override
    public void render(float delta) {
        super.render(delta); // limpia la pantalla

        // Rota lentamente el diamante
        instance.transform.rotate(0, 1, 0, 30f * delta);

        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (customModel != null) {
            customModel.dispose();
            customModel = null;
        }
        super.dispose();
    }
}
