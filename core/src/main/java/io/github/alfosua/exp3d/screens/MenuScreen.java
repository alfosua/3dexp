package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.Screen;
import io.github.alfosua.exp3d.Main;

public class MenuScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Solo usaremos la lógica del skin predeterminado creando uno simple,
        // o en realidad, libgdx no tiene un archivo skin predeterminado integrado sin cargar recursos.
        // Vamos a crear un skin programático mínimo o simplemente dependeremos de una fuente simple por ahora.
        // Para simplificar, podemos usar Scene2D pero sin un archivo skin cargado necesitamos generarlo,
        // lo que podría ser tedioso. Creemos un estilo TextButton básico de forma programática.

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        Table table = new Table();

        String[] examples = {
            "-- Basics --",
            "1. Simple 3D model rendering",
            "2. Simple GLTF scene",
            "3. Simple 3D animation",
            "4. Simple FPS camera",
            "-- Procedural --",
            "5. Model Builders (Procedural)",
            "6. Custom model generation",
            "7. Terrain rendering with heightmaps",
            "-- Lighting --",
            "8. Lighting Types",
            "9. Shadows",
            "10. Environment Skybox",
            "11. PBR Reflections (IBL)",
            "-- Shaders & Rendering --",
            "12. Realistic rendering with GLTF",
            "13. Custom Shader",
            "14. Cell Shaded Anime Character",
            "-- Advanced Environments --",
            "15. Simple Physics sandbox with Bullet",
            "16. Advanced Physics, shapes and forces",
            "17. Raycasting",
            "18. Large Environment Exploration"
        };

        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = game.font;

        table.add(new Label("3D Examples in LibGDX", labelStyle)).padBottom(20).row();

        for (int i = 0; i < examples.length; i++) {
            final int index = i;
            if (examples[i].startsWith("--")) {
                Label label = new Label(examples[i], labelStyle);
                label.setColor(Color.SCARLET);
                table.add(label).padTop(10).padBottom(5).row();
                continue;
            }
            TextButton button = new TextButton(examples[i], textButtonStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    launchExample(index);
                }
            });
            table.add(button).pad(2).row();
        }

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Desactivar el desplazamiento horizontal

        rootTable.add(scrollPane).fill().expand().padBottom(10).row();

        TextButton exitButton = new TextButton("Exit", textButtonStyle);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        rootTable.add(exitButton).padBottom(20).row();
    }

    private void launchExample(int index) {
        // Implementaremos estos uno por uno.
        Screen nextScreen = null;
        switch (index) {
            case 1: nextScreen = new SimpleModelScreen(game); break;
            case 2: nextScreen = new SimpleGltfScreen(game); break;
            case 3: nextScreen = new AnimationScreen(game); break;
            case 4: nextScreen = new FpsCameraScreen(game); break;
            case 6: nextScreen = new ModelBuildersScreen(game); break;
            case 7: nextScreen = new CustomModelScreen(game); break;
            case 8: nextScreen = new TerrainScreen(game); break;
            case 10: nextScreen = new LightingScreen(game); break;
            case 11: nextScreen = new ShadowsScreen(game); break;
            case 12: nextScreen = new SkyboxScreen(game); break;
            case 13: nextScreen = new ReflectionScreen(game); break;
            case 15: nextScreen = new PbrGltfScreen(game); break;
            case 16: nextScreen = new CustomShaderScreen(game); break;
            case 17: nextScreen = new CellShadingScreen(game); break;
            case 19: nextScreen = new PhysicsScreen(game); break;
            case 20: nextScreen = new SandboxScreen(game); break;
            case 21: nextScreen = new RaycastingScreen(game); break;
            case 22: nextScreen = new EnvironmentScreen(game); break;
        }
        if (nextScreen != null) {
            game.getScreen().dispose();
            game.setScreen(nextScreen);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
