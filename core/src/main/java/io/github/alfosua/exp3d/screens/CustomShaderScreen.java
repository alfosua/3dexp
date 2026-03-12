package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import io.github.alfosua.exp3d.Main;

public class CustomShaderScreen extends Base3DScreen {
    private Model model;
    private ModelInstance instance;
    private ShaderProgram shaderProgram;
    private ModelBatch customBatch;

    private String vert = "attribute vec3 a_position;\n" +
        "uniform mat4 u_projViewTrans;\n" +
        "uniform mat4 u_worldTrans;\n" +
        "varying vec3 v_pos;\n" +
        "void main() {\n" +
        "    vec4 pos = u_worldTrans * vec4(a_position, 1.0);\n" +
        "    v_pos = pos.xyz;\n" +
        "    gl_Position = u_projViewTrans * pos;\n" +
        "}";

    private String frag = "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform float u_time;\n" +
        "varying vec3 v_pos;\n" +
        "void main() {\n" +
        "    float r = sin(v_pos.x * 2.0 + u_time * 2.0) * 0.5 + 0.5;\n" +
        "    float g = cos(v_pos.y * 3.0 + u_time) * 0.5 + 0.5;\n" +
        "    float b = sin(v_pos.z * 1.5 - u_time * 1.5) * 0.5 + 0.5;\n" +
        "    gl_FragColor = vec4(r, g, b, 1.0);\n" +
        "}";

    public CustomShaderScreen(Main game) {
        super(game);

        shaderProgram = new ShaderProgram(vert, frag);
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error("CustomShader", "Shader compilation failed:\n" + shaderProgram.getLog());
        }

        Shader customShader = new Shader() {
            int u_projViewTrans;
            int u_worldTrans;
            int u_time;
            float time;

            @Override
            public void init() {
                u_projViewTrans = shaderProgram.getUniformLocation("u_projViewTrans");
                u_worldTrans = shaderProgram.getUniformLocation("u_worldTrans");
                u_time = shaderProgram.getUniformLocation("u_time");
            }

            @Override
            public int compareTo(Shader other) { return 0; }

            @Override
            public boolean canRender(Renderable instance) { return true; }

            @Override
            public void begin(Camera camera, RenderContext context) {
                shaderProgram.bind();
                shaderProgram.setUniformMatrix(u_projViewTrans, camera.combined);
                time += Gdx.graphics.getDeltaTime();
                shaderProgram.setUniformf(u_time, time);
                context.setDepthTest(GL20.GL_LEQUAL);
                context.setCullFace(GL20.GL_BACK);
            }

            @Override
            public void render(Renderable renderable) {
                shaderProgram.setUniformMatrix(u_worldTrans, renderable.worldTransform);
                renderable.meshPart.render(shaderProgram);
            }

            @Override
            public void end() {}

            @Override
            public void dispose() { }
        };

        customShader.init();

        customBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                return customShader;
            }
        });

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(5f, 5f, 5f, 32, 32,
            new Material(),
            VertexAttributes.Usage.Position);

        instance = new ModelInstance(model);

        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        customBatch.begin(cam);
        customBatch.render(instance);
        customBatch.end();
    }

    @Override
    public void dispose() {
        if (model != null) {
            model.dispose();
            model = null;
        }
        if (customBatch != null) {
            customBatch.dispose();
            customBatch = null;
        }
        if (shaderProgram != null) {
            shaderProgram.dispose();
            shaderProgram = null;
        }
        super.dispose();
    }
}
