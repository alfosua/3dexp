package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import io.github.alfosua.exp3d.Main;

public class FpsCameraScreen extends Base3DScreen {
    private Array<Model> models = new Array<>();
    private Array<ModelInstance> instances = new Array<>();
    private FirstPersonCameraController camController;

    public FpsCameraScreen(Main game) {
        super(game);

        ModelBuilder modelBuilder = new ModelBuilder();
        
        // Suelo
        Model floorModel = modelBuilder.createBox(100f, 1f, 100f, 
                new Material(ColorAttribute.createDiffuse(Color.GRAY)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        models.add(floorModel);
        ModelInstance floor = new ModelInstance(floorModel);
        floor.transform.setToTranslation(0, -0.5f, 0);
        instances.add(floor);

        // Pilares
        Material pillarMaterial = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Model pillarModel = modelBuilder.createCylinder(2f, 10f, 2f, 16, pillarMaterial, 
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        models.add(pillarModel);

        for (int x = -40; x <= 40; x += 20) {
            for (int z = -40; z <= 40; z += 20) {
                if (x == 0 && z == 0) continue; // saltar el centro
                ModelInstance pillar = new ModelInstance(pillarModel);
                pillar.transform.setToTranslation(x, 5f, z);
                instances.add(pillar);
            }
        }

        cam.position.set(0f, 3f, 0f);
        cam.lookAt(0, 3f, -1f);
        cam.up.set(0, 1, 0);
        cam.update();

        camController = new FirstPersonCameraController(cam);
        camController.setDegreesPerPixel(0.5f);
        camController.setVelocity(10f); // velocidad de movimiento
    }

    @Override
    public void show() {
        super.show(); // Configura el multiplexor con la tecla escape
        multiplexer.addProcessor(camController);
        
        // Ocultar el cursor del ratón para sensación de FPS
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {
        super.render(delta); // limpia la pantalla

        camController.update(delta);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        for (Model model : models) {
            model.dispose();
        }
        models.clear();
        super.dispose();
    }
}
