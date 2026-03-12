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
import com.badlogic.gdx.graphics.g3d.Material;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;
import io.github.alfosua.exp3d.Main;

public class EnvironmentScreen extends Base3DScreen {
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;

    // Físicas
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

    // Entorno
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

        // --- ENTORNO, LUCES E IBL ---
        sceneManager.setAmbientLight(0.05f); // Luz ambiental muy oscura para la noche

        // Convertir luces puntuales a SpotLights
        SpotLightEx spot1 = new SpotLightEx();
        // Amarillo pálido realista (incandescente cálido), menor intensidad (de 100f a 40f)
        spot1.set(1.0f, 0.9f, 0.7f, 8f, 5f, 0f, 0f, -1f, 0f, 40f, 60f, 1f);
        sceneManager.environment.add(spot1);

        SpotLightEx spot2 = new SpotLightEx();
        spot2.set(1.0f, 0.9f, 0.7f, -8f, 5f, 0f, 0f, -1f, 0f, 40f, 60f, 1f);
        sceneManager.environment.add(spot2);

        // Amplio mapa de sombras para la luz de la luna en el patio de Sponza
        shadowLight = new DirectionalShadowLight(2048, 2048, 120f, 120f, 1f, 300f);
        shadowLight.set(0.4f, 0.5f, 0.7f, 0.5f, -1f, 0.2f); // Luz de luna azulada, brillando en diagonal hacia abajo
        sceneManager.environment.add(shadowLight);
        sceneManager.environment.shadowMap = shadowLight;

        // Skybox nocturno procedural oscuro
        Pixmap pSky = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pSky.setColor(0.02f, 0.02f, 0.06f, 1f); // Azul medianoche
        pSky.fill();
        
        // Añadir algunas estrellas blancas simples al cielo nocturno
        pSky.setColor(Color.WHITE);
        for(int i = 0; i < 50; i++) {
            pSky.drawPixel((int)(Math.random() * 256), (int)(Math.random() * 256));
        }

        Pixmap pGround = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pGround.setColor(0.01f, 0.01f, 0.01f, 1f); // Suelo negro intenso
        pGround.fill();

        cubemap = new Cubemap(pSky, pSky, pSky, pGround, pSky, pSky);
        skybox = new SceneSkybox(cubemap);
        sceneManager.setSkyBox(skybox);

        // --- CARGAR ESCENA ---
        sceneAsset = new GLBLoader().load(Gdx.files.internal("Sponza.glb"));
        scene = new Scene(sceneAsset.scene);

        // Evitar que Sponza brille, para que parezca piedra
        for (Material m : scene.modelInstance.materials) {
            // Establecer rugosidad y metallic en los materiales
            m.set(PBRFloatAttribute.createRoughness(1.0f));
            m.set(PBRFloatAttribute.createMetallic(0.0f));
        }

        sceneManager.addScene(scene);

        // --- CONFIGURACIÓN DE FÍSICAS BULLET ---
        Bullet.init();
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -20f, 0)); // Gravedad más fuerte para mejor sensación

        // 1. Colisionador de malla estática para Sponza
        sponzaShape = Bullet.obtainStaticNodeShape(scene.modelInstance.nodes);
        sponzaInfo = new btRigidBody.btRigidBodyConstructionInfo(0, null, sponzaShape, Vector3.Zero);
        sponzaBody = new btRigidBody(sponzaInfo);
        dynamicsWorld.addRigidBody(sponzaBody);

        // 1.5 Plano de suelo sólido (Prueba de fallos) - MASIVAMENTE GRUESO para evitar recortes a alta velocidad
        groundShape = new btBoxShape(new Vector3(200f, 10f, 200f)); // 20 unidades de grosor
        groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, groundShape, Vector3.Zero);
        groundBody = new btRigidBody(groundInfo);
        groundBody.setWorldTransform(new Matrix4().setToTranslation(0f, -10.05f, 0f)); // Borde superior descansa exactamente en Y=-0.05
        dynamicsWorld.addRigidBody(groundBody);

        // 2. Colisionador dinámico del jugador (Cápsula)
        playerShape = new btCapsuleShape(0.5f, 1.0f); // Cápsula de 2m de altura
        Vector3 localInertia = new Vector3();
        playerShape.calculateLocalInertia(60f, localInertia); // Persona de 60kg
        playerInfo = new btRigidBody.btRigidBodyConstructionInfo(60f, null, playerShape, localInertia);
        playerBody = new btRigidBody(playerInfo);

        // Evitar que el jugador se vuelque
        playerBody.setAngularFactor(Vector3.Zero);
        playerBody.setActivationState(Collision.DISABLE_DEACTIVATION);

        // Habilitar la detección continua de colisiones (CCD) para evitar caer por el suelo de Sponza
        playerBody.setCcdMotionThreshold(1e-7f);
        playerBody.setCcdSweptSphereRadius(0.5f);

        // Posición de inicio bajada y desplazada
        Matrix4 startTransform = new Matrix4().setToTranslation(0f, 5f, 5f);
        playerBody.setWorldTransform(startTransform);
        dynamicsWorld.addRigidBody(playerBody);

        // Inicializar cámara
        cam.position.set(0f, 5f, 5f);
        cam.lookAt(0f, 5f, 0f);
        cam.up.set(0, 1, 0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();
    }

    @Override
    public void show() {
        super.show(); // Configura el multiplexor con la tecla escape
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

        // --- VISTA DE CÁMARA FPS (Ratón) ---
        float lookSpeed = 0.2f;
        float deltaX = -Gdx.input.getDeltaX() * lookSpeed;
        float deltaY = -Gdx.input.getDeltaY() * lookSpeed;

        cam.direction.rotate(cam.up, deltaX);
        Vector3 right = new Vector3(cam.direction).crs(cam.up).nor();
        cam.direction.rotate(right, deltaY);
        // Evitar el bloqueo de cardán / mirar arriba/abajo de forma extrema
        if (cam.direction.y > 0.9f) cam.direction.y = 0.9f;
        if (cam.direction.y < -0.9f) cam.direction.y = -0.9f;
        cam.direction.nor();
        cam.update();

        // --- MOVIMIENTO FPS DEL JUGADOR (Teclado a Físicas) ---
        Vector3 vel = playerBody.getLinearVelocity();
        Vector3 forward = new Vector3(cam.direction.x, 0, cam.direction.z).nor();
        Vector3 rightMove = new Vector3(forward).crs(Vector3.Y).nor();

        Vector3 targetVel = new Vector3();
        if(Gdx.input.isKeyPressed(Input.Keys.W)) targetVel.add(forward);
        if(Gdx.input.isKeyPressed(Input.Keys.S)) targetVel.sub(forward);
        if(Gdx.input.isKeyPressed(Input.Keys.D)) targetVel.add(rightMove);
        if(Gdx.input.isKeyPressed(Input.Keys.A)) targetVel.sub(rightMove);

        // Velocidad de Movimiento
        targetVel.nor().scl(12f);

        // Conservar velocidad Y de salto/caída
        playerBody.setLinearVelocity(new Vector3(targetVel.x, vel.y, targetVel.z));

        // Salto
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Solo saltar si nos movemos algo lento en el eje Y (verificación primitiva de suelo)
            if (Math.abs(vel.y) < 0.1f) {
                playerBody.applyCentralImpulse(new Vector3(0, 400f, 0));
            }
        }

        // --- PASO DE FÍSICAS ---
        dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

        // --- SINCRONIZAR CÁMARA AL CUERPO DEL JUGADOR ---
        Matrix4 transform = new Matrix4();
        playerBody.getWorldTransform(transform);
        Vector3 pos = new Vector3();
        transform.getTranslation(pos);
        pos.y += 0.8f; // Mover cámara a la parte superior de la cápsula (Altura de los ojos)
        cam.position.set(pos);
        cam.update();

        // --- RENDERIZADO ---
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
