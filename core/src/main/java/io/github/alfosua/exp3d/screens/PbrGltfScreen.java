package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import io.github.alfosua.exp3d.Main;

public class PbrGltfScreen extends Base3DScreen {
    private SceneManager sceneManager;
    private Model metalModel;
    private Model plasticModel;

    public PbrGltfScreen(Main game) {
        super(game);

        sceneManager = new SceneManager();
        sceneManager.setCamera(cam);

        // Standard setup for PBR
        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -2, -1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        ModelBuilder modelBuilder = new ModelBuilder();

        // High metallic, low roughness (Shiny Gold)
        Material metalMat = new Material(
                PBRColorAttribute.createBaseColorFactor(Color.GOLD),
                PBRFloatAttribute.createMetallic(1.0f),
                PBRFloatAttribute.createRoughness(0.2f)
        );
        metalModel = modelBuilder.createSphere(5f, 5f, 5f, 32, 32, metalMat,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        Scene metalScene = new Scene(metalModel);
        metalScene.modelInstance.transform.setToTranslation(-4, 0, 0);
        sceneManager.addScene(metalScene);

        // Low metallic, high roughness (Matte Red Plastic)
        Material plasticMat = new Material(
                PBRColorAttribute.createBaseColorFactor(Color.RED),
                PBRFloatAttribute.createMetallic(0.0f),
                PBRFloatAttribute.createRoughness(0.8f)
        );
        plasticModel = modelBuilder.createSphere(5f, 5f, 5f, 32, 32, plasticMat,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        Scene plasticScene = new Scene(plasticModel);
        plasticScene.modelInstance.transform.setToTranslation(4, 0, 0);
        sceneManager.addScene(plasticScene);

        cam.position.set(0f, 0f, 15f);
        cam.lookAt(0, 0, 0);
        cam.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Rotate scenes around Y axis slowly
        for (com.badlogic.gdx.graphics.g3d.RenderableProvider provider : sceneManager.getRenderableProviders()) {
            if (provider instanceof Scene) {
                ((Scene) provider).modelInstance.transform.rotate(0, 1, 0, 15f * delta);
            }
        }

        sceneManager.update(delta);
        sceneManager.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        sceneManager.updateViewport(width, height);
    }

    @Override
    public void dispose() {
        if (sceneManager != null) {
            sceneManager.dispose();
            sceneManager = null;
        }
        if (metalModel != null) {
            metalModel.dispose();
            metalModel = null;
        }
        if (plasticModel != null) {
            plasticModel.dispose();
            plasticModel = null;
        }
        super.dispose();
    }
}
