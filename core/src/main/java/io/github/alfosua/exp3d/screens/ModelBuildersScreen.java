package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import io.github.alfosua.exp3d.Main;

public class ModelBuildersScreen extends Base3DScreen {

    private Array<Model> models = new Array<Model>();
    private Array<ModelInstance> instances = new Array<ModelInstance>();

    public ModelBuildersScreen(Main game) {
        super(game);

        cam.position.set(0f, 5f, 15f);
        cam.lookAt(0, 0, 0);
        cam.update();

        ModelBuilder modelBuilder = new ModelBuilder();

        // 1. Caja
        Material boxMaterial = new Material(ColorAttribute.createDiffuse(Color.RED));
        Model box = modelBuilder.createBox(2f, 2f, 2f, boxMaterial, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        ModelInstance boxInstance = new ModelInstance(box);
        boxInstance.transform.setToTranslation(-6f, 0f, 0f);
        models.add(box);
        instances.add(boxInstance);

        // 2. Esfera
        Material sphereMaterial = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Model sphere = modelBuilder.createSphere(2f, 2f, 2f, 20, 20, sphereMaterial, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        ModelInstance sphereInstance = new ModelInstance(sphere);
        sphereInstance.transform.setToTranslation(-2f, 0f, 0f);
        models.add(sphere);
        instances.add(sphereInstance);

        // 3. Cilindro
        Material cylinderMaterial = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Model cylinder = modelBuilder.createCylinder(2f, 4f, 2f, 20, cylinderMaterial, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        ModelInstance cylinderInstance = new ModelInstance(cylinder);
        cylinderInstance.transform.setToTranslation(2f, 0f, 0f);
        models.add(cylinder);
        instances.add(cylinderInstance);

        // 4. Cápsula
        Material capsuleMaterial = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Model capsule = modelBuilder.createCapsule(1f, 4f, 20, capsuleMaterial, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        ModelInstance capsuleInstance = new ModelInstance(capsule);
        capsuleInstance.transform.setToTranslation(6f, 0f, 0f);
        models.add(capsule);
        instances.add(capsuleInstance);

        // 5. Cono
        Material coneMaterial = new Material(ColorAttribute.createDiffuse(Color.MAGENTA));
        Model cone = modelBuilder.createCone(2f, 4f, 2f, 20, coneMaterial, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        ModelInstance coneInstance = new ModelInstance(cone);
        coneInstance.transform.setToTranslation(0f, 0f, 4f);
        models.add(cone);
        instances.add(coneInstance);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Rotar lentamente todas las instancias para una mejor visualización
        for (ModelInstance instance : instances) {
            instance.transform.rotate(Vector3.Y, 30f * delta);
            instance.transform.rotate(Vector3.X, 15f * delta);
        }

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
