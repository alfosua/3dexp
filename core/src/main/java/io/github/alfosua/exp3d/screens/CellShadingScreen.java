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
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
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

        // En lugar de escribir un shader toon GLSL personalizado completo que entienda los huesos GLTF,
        // Podemos anular PBRShaderProvider e inyectar GLSL personalizado, o simplemente podemos
        // usar un fragmento de shader de fragmentos personalizado para posterizar la salida del shader PBR.
        // Para un verdadero ejemplo de "cell shading", modificamos el shader de fragmentos
        // para posterizar el color final. Inyectamos un fragmento al final del shader.
        PBRShaderConfig config = new PBRShaderConfig();
        config.numBones = 60; // El modelo de Miku requiere 52 huesos

        // Interceptamos de forma segura el cálculo del color de iluminación sin romper la estructura del bloque `#if` de GLSL
        String defaultFrag = PBRShaderProvider.getDefaultFragmentShader();

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

        // Proporcionar la configuración personalizada al SceneManager
        sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
        sceneManager.setCamera(cam);

        sceneManager.setAmbientLight(0.4f);

        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -1, -0.5f).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        // Nota: Los archivos VRM son en realidad archivos GLB. Podemos usar GLBLoader.
        // Sin embargo, gdx-gltf lanza errores de análisis JSON en algunas extensiones VRM avanzadas.
        // En su lugar, usaremos un modelo de anime estándar miku.glb.
        sceneAsset = new GLBLoader().load(Gdx.files.internal("miku.glb"));
        scene = new Scene(sceneAsset.scene);

        // El modelo es demasiado grande, lo escalamos hacia abajo
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

        // Rotar lentamente la escena
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
