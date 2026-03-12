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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

import io.github.alfosua.exp3d.Main;

public class ReflectionScreen extends Base3DScreen {

    private SceneManager sceneManager;
    private SceneSkybox skybox;
    private Cubemap cubemap;
    private Model sphere;

    public ReflectionScreen(Main game) {
        super(game);

        cam.position.set(0f, 0f, 5f);
        cam.lookAt(0, 0, 0);
        cam.update();

        sceneManager = new SceneManager();
        sceneManager.setCamera(cam);

        // We make a checkerboard-like cubemap to actually see reflection distortions
        Pixmap p = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        for(int x=0; x<256; x+=64) {
            for(int y=0; y<256; y+=64) {
                if(( (x/64) + (y/64) ) % 2 == 0) {
                    p.setColor(Color.WHITE);
                } else {
                    p.setColor(Color.ROYAL);
                }
                p.fillRectangle(x, y, 64, 64);
            }
        }

        Pixmap pGround = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pGround.setColor(Color.DARK_GRAY);
        pGround.fill();

        cubemap = new Cubemap(p, p, p, pGround, p, p);
        skybox = new SceneSkybox(cubemap);
        sceneManager.setSkyBox(skybox);

        // Setup PBR image-based lighting environment reflections
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(cubemap));
        // Provide diffuse ambient matching the cubemap
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(cubemap));

        sceneManager.setAmbientLight(0.02f); // reduce flat ambient light because IBL will provide lighting

        ModelBuilder modelBuilder = new ModelBuilder();
        Material mat = new Material();
        mat.set(PBRColorAttribute.createBaseColorFactor(Color.GOLD));
        mat.set(PBRFloatAttribute.createMetallic(1.0f)); // 100% metallic like a mirror
        mat.set(PBRFloatAttribute.createRoughness(0.1f)); // Slight roughness to avoid pure black artifacts

        sphere = modelBuilder.createSphere(2.5f, 2.5f, 2.5f, 40, 40, mat, Usage.Position | Usage.Normal);
        sceneManager.addScene(new Scene(new ModelInstance(sphere)));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Rotate camera around to see the reflections warp
        cam.rotateAround(Vector3.Zero, Vector3.Y, 20f * delta);
        cam.update();

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
        if (sphere != null) sphere.dispose();
        super.dispose();
    }
}
