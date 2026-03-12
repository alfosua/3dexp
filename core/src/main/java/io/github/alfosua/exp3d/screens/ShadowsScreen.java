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

import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;

import io.github.alfosua.exp3d.Main;

public class ShadowsScreen extends Base3DScreen {

    private SceneManager sceneManager;
    private DirectionalShadowLight shadowLight;
    
    // Usaremos formas básicas pero las envolveremos en objetos Scene para que sean manejados por SceneManager que maneja las sombras
    private Array<Model> models = new Array<Model>();
    private Scene boxScene;
    private Scene sphereScene;

    public ShadowsScreen(Main game) {
        super(game);

        cam.position.set(0f, 10f, 15f);
        cam.lookAt(0, 0, 0);
        cam.update();

        PBRShaderConfig config = new PBRShaderConfig();
        DepthShader.Config depthConfig = new DepthShader.Config();
        
        // SceneManager con shaders predeterminados maneja las sombras automáticamente si está configurado
        sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
        sceneManager.setCamera(cam);
        sceneManager.setAmbientLight(0.3f);

        // Configurar luz de sombra
        shadowLight = new DirectionalShadowLight(2048, 2048, 60f, 60f, 1f, 300f);
        shadowLight.set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f);
        sceneManager.environment.add(shadowLight);
        sceneManager.environment.shadowMap = shadowLight;

        ModelBuilder modelBuilder = new ModelBuilder();

        // Suelo (Recibe sombra)
        Material floorMaterial = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        Model floor = modelBuilder.createBox(30f, 0.5f, 30f, floorMaterial, Usage.Position | Usage.Normal);
        models.add(floor);
        Scene floorScene = new Scene(new ModelInstance(floor));
        floorScene.modelInstance.transform.setToTranslation(0f, -0.25f, 0f);
        sceneManager.addScene(floorScene);

        // Objetos (Emiten sombras)
        Material objMaterial = new Material(ColorAttribute.createDiffuse(Color.RED));
        Model box = modelBuilder.createBox(2f, 2f, 2f, objMaterial, Usage.Position | Usage.Normal);
        models.add(box);
        boxScene = new Scene(new ModelInstance(box));
        boxScene.modelInstance.transform.setToTranslation(-4f, 2f, 0f);
        sceneManager.addScene(boxScene);

        Material obj2Material = new Material(ColorAttribute.createDiffuse(Color.CYAN));
        Model sphere = modelBuilder.createSphere(3f, 3f, 3f, 20, 20, obj2Material, Usage.Position | Usage.Normal);
        models.add(sphere);
        sphereScene = new Scene(new ModelInstance(sphere));
        sphereScene.modelInstance.transform.setToTranslation(4f, 2f, -2f);
        sceneManager.addScene(sphereScene);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Rotar objetos para que puedas ver cómo se mueven las sombras
        boxScene.modelInstance.transform.rotate(Vector3.Y, 30f * delta);
        sphereScene.modelInstance.transform.rotate(Vector3.X, 40f * delta);

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
        super.dispose();
        if (sceneManager != null) sceneManager.dispose();
        if (shadowLight != null) shadowLight.dispose();
        for(Model m : models) {
            m.dispose();
        }
        models.clear();
    }
}
