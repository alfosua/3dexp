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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
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

        // We will just use the default skin's logic by creating a simple one,
        // or actually, libgdx doesn't have a built-in default skin file ready without loading assets.
        // Let's create a minimal programmatic skin or just rely on a simple font for now.
        // For simplicity, we can use Scene2D but without a loaded skin file we need to generate it, 
        // which might be tedious. Let's create a basic TextButton style programmatically.
        
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        String[] examples = {
            "1. Simple 3D model rendering",
            "2. Simple FPS camera",
            "3. Simple 3D animation",
            "4. Custom Shader",
            "5. Custom model generation",
            "6. Terrain rendering with heightmaps",
            "7. Realistic rendering with GLTF",
            "8. Simple Physics sandbox with Bullet"
        };
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = game.font;

        table.add(new com.badlogic.gdx.scenes.scene2d.ui.Label("3D Examples in LibGDX", labelStyle)).padBottom(20).row();

        for (int i = 0; i < examples.length; i++) {
            final int index = i;
            TextButton button = new TextButton(examples[i], textButtonStyle);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    launchExample(index);
                }
            });
            table.add(button).pad(5).row();
        }
        
        TextButton exitButton = new TextButton("Exit", textButtonStyle);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        table.add(exitButton).padTop(20).row();
    }

    private void launchExample(int index) {
        // We will implement these one by one.
        com.badlogic.gdx.Screen nextScreen = null;
        switch (index) {
            case 0:
                nextScreen = new SimpleModelScreen(game);
                break;
            case 1:
                nextScreen = new FpsCameraScreen(game);
                break;
            case 2:
                nextScreen = new AnimationScreen(game);
                break;
            case 3:
                nextScreen = new CustomShaderScreen(game);
                break;
            case 4:
                nextScreen = new CustomModelScreen(game);
                break;
            case 5:
                nextScreen = new TerrainScreen(game);
                break;
            case 6:
                nextScreen = new PbrGltfScreen(game);
                break;
            case 7:
                nextScreen = new PhysicsScreen(game);
                break;
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
