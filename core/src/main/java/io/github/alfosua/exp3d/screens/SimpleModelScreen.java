package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import io.github.alfosua.exp3d.Main;

public class SimpleModelScreen extends Base3DScreen {
    private Model model;
    private ModelInstance instance;

    public SimpleModelScreen(Main game) {
        super(game);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f, 
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        
        instance = new ModelInstance(model);
    }

    @Override
    public void render(float delta) {
        super.render(delta); // limpia la pantalla

        // Rotar lentamente la caja
        instance.transform.rotate(0, 1, 0, 45f * delta);

        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (model != null) {
            model.dispose();
            model = null;
        }
        super.dispose();
    }
}
