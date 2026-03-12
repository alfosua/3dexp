package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import io.github.alfosua.exp3d.Main;

public class SimpleGltfScreen extends Base3DScreen {
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;

    public SimpleGltfScreen(Main game) {
        super(game);
        sceneManager = new SceneManager();
        sceneManager.setCamera(cam);

        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -2, -1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);
        sceneManager.setAmbientLight(1f);

        // Asegúrate de tener Duck.glb en tu carpeta assets
        sceneAsset = new GLBLoader().load(Gdx.files.internal("Duck.glb"));
        scene = new Scene(sceneAsset.scene);
        sceneManager.addScene(scene);

        cam.position.set(0f, 2f, 5f);
        cam.lookAt(0, 0, 0);
        cam.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        scene.modelInstance.transform.rotate(0, 1, 0, 30f * delta);

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
        if (sceneAsset != null) {
            sceneAsset.dispose();
            sceneAsset = null;
        }
        super.dispose();
    }
}
