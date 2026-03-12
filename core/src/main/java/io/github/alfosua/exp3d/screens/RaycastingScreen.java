package io.github.alfosua.exp3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import io.github.alfosua.exp3d.Main;

public class RaycastingScreen extends Base3DScreen {
    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld dynamicsWorld;
    private btCollisionShape sphereShape;

    private Array<Model> models = new Array<>();
    private Array<GameObject> instances = new Array<>();

    class GameObject extends ModelInstance {
        public final btRigidBody body;
        public final MyMotionState motionState;

        public GameObject(Model model, String node, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
            super(model, node);
            motionState = new MyMotionState(this.transform);
            body = new btRigidBody(constructionInfo);
            body.setMotionState(motionState);
        }

        public void dispose() {
            body.dispose();
            motionState.dispose();
        }
    }

    class MyMotionState extends btMotionState {
        Matrix4 transform;
        public MyMotionState(Matrix4 transform) { this.transform = transform; }
        @Override public void getWorldTransform(Matrix4 worldTrans) { worldTrans.set(transform); }
        @Override public void setWorldTransform(Matrix4 worldTrans) { transform.set(worldTrans); }
    }

    public RaycastingScreen(Main game) {
        super(game);

        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, 0, 0));

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(ColorAttribute.createDiffuse(Color.RED))).sphere(2f, 2f, 2f, 20, 20);
        Model model = mb.end();
        models.add(model);

        sphereShape = new btSphereShape(1f);
        btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(0f, null, sphereShape, Vector3.Zero);

        for (int i = 0; i < 5; i++) {
            GameObject sphere = new GameObject(model, "sphere", info);
            sphere.transform.setToTranslation(-8f + (i * 4f), 0f, 0f);
            sphere.body.setWorldTransform(sphere.transform);
            sphere.body.setUserValue(i);
            dynamicsWorld.addRigidBody(sphere.body);
            instances.add(sphere);
        }
        info.dispose();

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Ray ray = cam.getPickRay(screenX, screenY);
                Vector3 rayFrom = new Vector3(ray.origin);
                Vector3 rayTo = new Vector3(ray.direction).scl(100f).add(rayFrom);

                ClosestRayResultCallback callback = new ClosestRayResultCallback(rayFrom, rayTo);
                dynamicsWorld.rayTest(rayFrom, rayTo, callback);

                if (callback.hasHit()) {
                    btCollisionObject hitObject = callback.getCollisionObject();
                    int index = hitObject.getUserValue();
                    if (index >= 0 && index < instances.size) {
                        instances.get(index).materials.get(0).set(ColorAttribute.createDiffuse(Color.GREEN));
                    }
                }
                callback.dispose();
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        dynamicsWorld.stepSimulation(delta, 5, 1f/60f);
        modelBatch.begin(cam);
        for (GameObject obj : instances) {
            modelBatch.render(obj, environment);
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        for (GameObject obj : instances) {
            dynamicsWorld.removeRigidBody(obj.body);
            obj.dispose();
        }
        instances.clear();
        if (sphereShape != null) sphereShape.dispose();
        dynamicsWorld.dispose();
        solver.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfig.dispose();
        for (Model m : models) m.dispose();
        models.clear();
        super.dispose();
    }
}
