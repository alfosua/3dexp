package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import io.github.alfosua.exp3d.Main;

public class LightingScreen extends Base3DScreen {

    private Array<Model> models = new Array<Model>();
    private Array<ModelInstance> instances = new Array<ModelInstance>();

    public LightingScreen(Main game) {
        super(game);

        cam.position.set(0f, 5f, 15f);
        cam.lookAt(0, 0, 0);
        cam.update();

        // Sobrescribir el entorno de Base3DScreen para probar las luces
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));

        // 1. Luz Direccional (Como el sol, luz mayormente azulada)
        environment.add(new DirectionalLight().set(0.2f, 0.2f, 0.5f, -1f, -0.8f, -0.2f));

        // 2. Luz Puntual (Omnidireccional, como una bombilla) - Rojiza
        environment.add(new PointLight().set(1f, 0f, 0f, -4f, 2f, 4f, 20f));

        // 3. Luz Foco (En forma de cono) - Verdosa
        environment.add(new SpotLight().set(0f, 1f, 0f, 4f, 5f, 0f, 0f, -1f, 0f, 20f, 10f, 25f));

        ModelBuilder modelBuilder = new ModelBuilder();

        // Suelo
        Material floorMaterial = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        Model floor = modelBuilder.createBox(20f, 0.5f, 20f, floorMaterial, Usage.Position | Usage.Normal);
        ModelInstance floorInstance = new ModelInstance(floor);
        floorInstance.transform.setToTranslation(0f, -0.25f, 0f);
        models.add(floor);
        instances.add(floorInstance);

        // Algunos objetos para atrapar la luz
        Material objMaterial = new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY));
        
        Model box = modelBuilder.createBox(2f, 2f, 2f, objMaterial, Usage.Position | Usage.Normal);
        ModelInstance boxInstance = new ModelInstance(box);
        boxInstance.transform.setToTranslation(-4f, 1f, 0f);
        models.add(box);
        instances.add(boxInstance);

        Model sphere = modelBuilder.createSphere(2.5f, 2.5f, 2.5f, 20, 20, objMaterial, Usage.Position | Usage.Normal);
        ModelInstance sphereInstance = new ModelInstance(sphere);
        sphereInstance.transform.setToTranslation(0f, 1.25f, 0f);
        models.add(sphere);
        instances.add(sphereInstance);

        Model cylinder = modelBuilder.createCylinder(2f, 4f, 2f, 20, objMaterial, Usage.Position | Usage.Normal);
        ModelInstance cylinderInstance = new ModelInstance(cylinder);
        cylinderInstance.transform.setToTranslation(4f, 2f, 0f);
        models.add(cylinder);
        instances.add(cylinderInstance);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Model model : models) {
            model.dispose();
        }
        models.clear();
    }
}
