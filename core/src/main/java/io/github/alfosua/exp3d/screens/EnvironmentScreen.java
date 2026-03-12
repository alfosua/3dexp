package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import io.github.alfosua.exp3d.Main;

public class EnvironmentScreen extends Base3DScreen {
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;

    // Physics
    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld dynamicsWorld;

    private btCollisionShape sponzaShape;
    private btRigidBody sponzaBody;
    private btRigidBody.btRigidBodyConstructionInfo sponzaInfo;

    private btCollisionShape groundShape;
    private btRigidBody groundBody;
    private btRigidBody.btRigidBodyConstructionInfo groundInfo;

    private btCollisionShape playerShape;
    private btRigidBody playerBody;
    private btRigidBody.btRigidBodyConstructionInfo playerInfo;

    // Environment
    private Cubemap cubemap;
    private SceneSkybox skybox;
    private DirectionalShadowLight shadowLight;

    public EnvironmentScreen(Main game) {
        super(game);

        PBRShaderConfig config = new PBRShaderConfig();
        config.numDirectionalLights = 1;
        config.numSpotLights = 2;

        DepthShader.Config depthConfig = new DepthShader.Config();

        sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
        sceneManager.setCamera(cam);

        // --- ENVIRONMENT, LIGHTS, AND IBL ---
        sceneManager.setAmbientLight(0.05f); // Very dark ambient for night time

        // Convert point lights to SpotLights because standard PointLights do not cast shadows in gdx-gltf
        net.mgsx.gltf.scene3d.lights.SpotLightEx spot1 = new net.mgsx.gltf.scene3d.lights.SpotLightEx();
        // Realistic pale yellow (warm incandescent), lower intensity (from 100f to 40f)
        spot1.set(1.0f, 0.9f, 0.7f, 8f, 5f, 0f, 0f, -1f, 0f, 40f, 60f, 1f);
        sceneManager.environment.add(spot1);

        net.mgsx.gltf.scene3d.lights.SpotLightEx spot2 = new net.mgsx.gltf.scene3d.lights.SpotLightEx();
        spot2.set(1.0f, 0.9f, 0.7f, -8f, 5f, 0f, 0f, -1f, 0f, 40f, 60f, 1f);
        sceneManager.environment.add(spot2);

        // Wide shadow map for Sponza courtyard moonlight
        shadowLight = new DirectionalShadowLight(2048, 2048, 120f, 120f, 1f, 300f);
        shadowLight.set(0.4f, 0.5f, 0.7f, 0.5f, -1f, 0.2f); // Blueish moonlight, shining down diagonally
        sceneManager.environment.add(shadowLight);
        sceneManager.environment.shadowMap = shadowLight;

        // Dark procedural night skybox
        Pixmap pSky = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pSky.setColor(0.02f, 0.02f, 0.06f, 1f); // Midnight blue
        pSky.fill();
        
        // Add a few simple white stars to the night sky
        pSky.setColor(Color.WHITE);
        for(int i = 0; i < 50; i++) {
            pSky.drawPixel((int)(Math.random() * 256), (int)(Math.random() * 256));
        }

        Pixmap pGround = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pGround.setColor(0.01f, 0.01f, 0.01f, 1f); // Pitch black floor
        pGround.fill();

        cubemap = new Cubemap(pSky, pSky, pSky, pGround, pSky, pSky);
        skybox = new SceneSkybox(cubemap);
        sceneManager.setSkyBox(skybox);

        // --- LOAD SCENE ---
        sceneAsset = new GLBLoader().load(Gdx.files.internal("Sponza.glb"));
        scene = new Scene(sceneAsset.scene);

        // Prevent Sponza itself from looking shiny so it looks like stone,
        // while allowing our gold sphere to use the global IBL reflections
        // Prevent Sponza itself from looking shiny so it looks like stone,
        // while allowing our gold sphere to use the global IBL reflections
        for (com.badlogic.gdx.graphics.g3d.Material m : scene.modelInstance.materials) {
            // Give Sponza a fake custom attribute so we know NOT to apply global env
            m.set(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.createRoughness(1.0f));
            m.set(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.createMetallic(0.0f));
        }

        sceneManager.addScene(scene);

        // --- BULLET PHYSICS SETUP ---
        Bullet.init();
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -20f, 0)); // Stronger gravity feels better

        // 1. Static Mesh Collider for Sponza
        sponzaShape = Bullet.obtainStaticNodeShape(scene.modelInstance.nodes);
        sponzaInfo = new btRigidBody.btRigidBodyConstructionInfo(0, null, sponzaShape, Vector3.Zero);
        sponzaBody = new btRigidBody(sponzaInfo);
        dynamicsWorld.addRigidBody(sponzaBody);

        // 1.5 Solid Floor Plane (Fail-safe) - MASSIVELY THICK to prevent any high velocity clipping
        groundShape = new btBoxShape(new Vector3(200f, 10f, 200f)); // 20 units thick
        groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, groundShape, Vector3.Zero);
        groundBody = new btRigidBody(groundInfo);
        groundBody.setWorldTransform(new Matrix4().setToTranslation(0f, -10.05f, 0f)); // Top edge rests exactly at Y=-0.05
        dynamicsWorld.addRigidBody(groundBody);

        // 2. Dynamic Player Collider (Capsule)
        playerShape = new btCapsuleShape(0.5f, 1.0f); // 2m tall capsule
        Vector3 localInertia = new Vector3();
        playerShape.calculateLocalInertia(60f, localInertia); // 60kg person
        playerInfo = new btRigidBody.btRigidBodyConstructionInfo(60f, null, playerShape, localInertia);
        playerBody = new btRigidBody(playerInfo);

        // Prevent player from tipping over
        playerBody.setAngularFactor(Vector3.Zero);
        playerBody.setActivationState(Collision.DISABLE_DEACTIVATION);

        // Enable Continuous Collision Detection (CCD) to prevent falling through Sponza floor
        playerBody.setCcdMotionThreshold(1e-7f);
        playerBody.setCcdSweptSphereRadius(0.5f);

        // Start position lowered and offset to look at sphere
        Matrix4 startTransform = new Matrix4().setToTranslation(0f, 5f, 5f);
        playerBody.setWorldTransform(startTransform);
        dynamicsWorld.addRigidBody(playerBody);

        // Initialize camera
        cam.position.set(0f, 5f, 5f);
        cam.lookAt(0f, 5f, 0f);
        cam.up.set(0, 1, 0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();
    }

    @Override
    public void show() {
        super.show(); // Sets up multiplexer with escape key
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.getScreen().dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        // --- FPS CAMERA LOOK (Mouse) ---
        float lookSpeed = 0.2f;
        float deltaX = -Gdx.input.getDeltaX() * lookSpeed;
        float deltaY = -Gdx.input.getDeltaY() * lookSpeed;

        cam.direction.rotate(cam.up, deltaX);
        Vector3 right = new Vector3(cam.direction).crs(cam.up).nor();
        cam.direction.rotate(right, deltaY);
        // Prevent gimbal lock / extreme looking up/down
        if (cam.direction.y > 0.9f) cam.direction.y = 0.9f;
        if (cam.direction.y < -0.9f) cam.direction.y = -0.9f;
        cam.direction.nor();
        cam.update();

        // --- FPS PLAYER MOVEMENT (Keyboard to Physics) ---
        Vector3 vel = playerBody.getLinearVelocity();
        Vector3 forward = new Vector3(cam.direction.x, 0, cam.direction.z).nor();
        Vector3 rightMove = new Vector3(forward).crs(Vector3.Y).nor();

        Vector3 targetVel = new Vector3();
        if(Gdx.input.isKeyPressed(Input.Keys.W)) targetVel.add(forward);
        if(Gdx.input.isKeyPressed(Input.Keys.S)) targetVel.sub(forward);
        if(Gdx.input.isKeyPressed(Input.Keys.D)) targetVel.add(rightMove);
        if(Gdx.input.isKeyPressed(Input.Keys.A)) targetVel.sub(rightMove);

        // Movement Speed
        targetVel.nor().scl(12f);

        // Preserve jumping/falling Y velocity
        playerBody.setLinearVelocity(new Vector3(targetVel.x, vel.y, targetVel.z));

        // Jump
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Only jump if we are moving somewhat slowly on Y axis (primitive grounded check)
            if (Math.abs(vel.y) < 0.1f) {
                playerBody.applyCentralImpulse(new Vector3(0, 400f, 0));
            }
        }

        // --- STEP PHYSICS ---
        dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

        // --- SYNC CAMERA TO PLAYER BODY ---
        Matrix4 transform = new Matrix4();
        playerBody.getWorldTransform(transform);
        Vector3 pos = new Vector3();
        transform.getTranslation(pos);
        pos.y += 0.8f; // Move camera to the top of the capsule (Eye height)
        cam.position.set(pos);
        cam.update();

        // --- RENDERING ---
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
        if (sceneManager != null) sceneManager.dispose();
        if (sceneAsset != null) sceneAsset.dispose();
        if (skybox != null) skybox.dispose();
        if (cubemap != null) cubemap.dispose();
        if (shadowLight != null) shadowLight.dispose();

        dynamicsWorld.removeRigidBody(playerBody);
        dynamicsWorld.removeRigidBody(sponzaBody);
        dynamicsWorld.removeRigidBody(groundBody);

        groundBody.dispose();
        groundInfo.dispose();
        groundShape.dispose();

        sponzaBody.dispose();
        sponzaInfo.dispose();
        sponzaShape.dispose();

        playerBody.dispose();
        playerInfo.dispose();
        playerShape.dispose();

        dynamicsWorld.dispose();
        solver.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfig.dispose();

        super.dispose();
    }
}
