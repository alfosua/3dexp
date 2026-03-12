package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import io.github.alfosua.exp3d.Main;

public class CellShadingScreen extends Base3DScreen {
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;

    public CellShadingScreen(Main game) {
        super(game);

        // Instead of writing a full custom GLSL toon shader that understands GLTF bones,
        // We can override PBRShaderProvider and inject custom GLSL, or we can just 
        // use a custom fragment shader snippet to posterize the output of the PBR shader.
        // For a true "cell shading" example, we modify the fragment shader
        // to posterize the final color. We inject a snippet at the end of the shader.
        PBRShaderConfig config = new PBRShaderConfig();
        config.numBones = 60; // Miku model requires 52 bones
        
        // We safely intercept the lighting color calculation without breaking GLSL `#if` block structure
        String defaultFrag = net.mgsx.gltf.scene3d.shaders.PBRShaderProvider.getDefaultFragmentShader();
        
        defaultFrag = defaultFrag.replace(
            "vec3 color = ambientColor + f_diffuse + f_specular;",
            "vec3 rawLight = f_diffuse / max(baseColor.rgb, 0.001);\n" +
            "    float lightInt = max(max(rawLight.r, rawLight.g), rawLight.b);\n" +
            "    float toonLight = lightInt > 0.4 ? 1.1 : (lightInt > 0.1 ? 0.7 : 0.4);\n" +
            "    vec3 color = ambientColor + (baseColor.rgb * toonLight);\n" +
            "    float specInt = max(max(f_specular.r, f_specular.g), f_specular.b);\n" +
            "    if (specInt > 0.1) color += mix(baseColor.rgb, vec3(1.0), 0.3);"
        );
        
        config.fragmentShader = defaultFrag;

        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 60;
        
        // Provide the custom config to the SceneManager
        sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
        sceneManager.setCamera(cam);

        sceneManager.setAmbientLight(0.4f);
        
        net.mgsx.gltf.scene3d.lights.DirectionalLightEx light = new net.mgsx.gltf.scene3d.lights.DirectionalLightEx();
        light.direction.set(1, -1, -0.5f).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        // Note: VRM files are actually GLB files. We can use GLBLoader.
        // However, gdx-gltf throws JSON parse errors on some advanced VRM extensions. 
        // We will use a standard anime model miku.glb instead.
        sceneAsset = new GLBLoader().load(Gdx.files.internal("miku.glb"));
        scene = new Scene(sceneAsset.scene);
        
        // Model is too big, scale it down
        scene.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
        
        sceneManager.addScene(scene);

        cam.position.set(0f, 1f, 2f);
        cam.lookAt(0f, 1f, 0f);
        cam.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Slowly rotate scene
        scene.modelInstance.transform.rotate(0, 1, 0, 15f * delta);

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
