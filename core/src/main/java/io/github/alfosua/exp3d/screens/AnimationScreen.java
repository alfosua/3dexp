package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import io.github.alfosua.exp3d.Main;

public class AnimationScreen extends Base3DScreen {
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;

    public AnimationScreen(Main game) {
        super(game);
        sceneManager = new SceneManager();
        sceneManager.setCamera(cam);

        // Usamos GLTFLoader para archivos .gltf (texto/json)
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("BoxAnimated.gltf"));
        scene = new Scene(sceneAsset.scene);
        sceneManager.addScene(scene);

        // Reproducir la primera animación en bucle
        if (sceneAsset.animations.size > 0) {
            scene.animationController.setAnimation(sceneAsset.animations.first().id, -1);
        }

        cam.position.set(4f, 4f, 4f);
        cam.lookAt(0, 0, 0);
        cam.update();

        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

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
