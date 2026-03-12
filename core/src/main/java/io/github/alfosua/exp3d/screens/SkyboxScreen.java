package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

import io.github.alfosua.exp3d.Main;

public class SkyboxScreen extends Base3DScreen {

    private SceneManager sceneManager;
    private SceneSkybox skybox;
    private Cubemap cubemap;
    private Model box;
    private Scene boxScene;
    private CameraInputController camController;

    public SkyboxScreen(Main game) {
        super(game);

        cam.position.set(0f, 0f, 5f);
        cam.lookAt(0, 0, 0);
        cam.update();

        sceneManager = new SceneManager();
        sceneManager.setCamera(cam);

        // Procedural gradient skybox using Pixmaps
        int resolution = 512;
        
        // Define colors
        Color colorZenith = new Color(0.1f, 0.4f, 0.8f, 1f); // Deep blue top
        Color colorHorizon = new Color(0.8f, 0.9f, 1.0f, 1f); // Hazy white/blue horizon
        Color colorGround = new Color(0.15f, 0.2f, 0.15f, 1f); // Dark green ground
        
        Pixmap pSky = new Pixmap(resolution, resolution, Pixmap.Format.RGBA8888);
        pSky.setColor(colorZenith);
        pSky.fill();
        
        Pixmap pGround = new Pixmap(resolution, resolution, Pixmap.Format.RGBA8888);
        pGround.setColor(colorGround);
        pGround.fill();
        
        // Create 4 side faces with a smooth vertical gradient
        Pixmap sideFace = new Pixmap(resolution, resolution, Pixmap.Format.RGBA8888);
        for (int y = 0; y < resolution; y++) {
            float t = (float) y / (resolution - 1);
            // In libgdx Pixmap, y=0 is top, y=height is bottom.
            // When building a Cubemap, the side faces point +Y up.
            // So y=0 (top) is Zenith, y=resolution (bottom) is Horizon... wait,
            // actually we want the top half to be sky gradient, bottom half to be ground.
            Color c = new Color();
            if (y < resolution / 2) {
                // Top half: Zenith down to Horizon
                float blend = (float) y / (resolution / 2 - 1); // 0 to 1
                c.set(colorZenith).lerp(colorHorizon, blend);
            } else {
                // Bottom half: Horizon down to Ground
                float blend = (float) (y - resolution / 2) / (resolution / 2 - 1); // 0 to 1
                c.set(colorHorizon).lerp(colorGround, blend);
            }
            sideFace.setColor(c);
            sideFace.drawLine(0, y, resolution, y);
        }

        // Right, Left, Top, Bottom, Front, Back
        cubemap = new Cubemap(sideFace, sideFace, pSky, pGround, sideFace, sideFace);

        // create Skybox
        skybox = new SceneSkybox(cubemap);
        sceneManager.setSkyBox(skybox);

        ModelBuilder modelBuilder = new ModelBuilder();
        Material mat = new Material(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createDiffuse(Color.WHITE));
        box = modelBuilder.createBox(1f, 1f, 1f, mat, Usage.Position | Usage.Normal);
        boxScene = new Scene(new ModelInstance(box));
        sceneManager.addScene(boxScene);
        
        camController = new CameraInputController(cam);
    }

    @Override
    public void show() {
        super.show(); // sets up multiplexer for ESC
        multiplexer.addProcessor(camController);
    }

    @Override
    public void render(float delta) {
        camController.update();
        
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (boxScene != null) {
            boxScene.modelInstance.transform.rotate(1, 1, 0, 15f * delta);
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
        if (sceneManager != null) sceneManager.dispose();
        if (skybox != null) skybox.dispose();
        if (cubemap != null) cubemap.dispose();
        if (box != null) box.dispose();
        super.dispose();
    }
}
