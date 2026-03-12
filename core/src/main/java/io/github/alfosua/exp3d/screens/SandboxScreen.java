package io.github.alfosua.exp3d.screens;

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
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import io.github.alfosua.exp3d.Main;

public class SandboxScreen extends Base3DScreen {
    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld dynamicsWorld;
    private btCollisionShape floorShape, rampShape, sphereShape;

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

    public SandboxScreen(Main game) {
        super(game);

        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -9.81f, 0));

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.node().id = "floor";
        mb.part("floor", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY))).box(40f, 1f, 40f);
        mb.node().id = "ramp";
        mb.part("ramp", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(ColorAttribute.createDiffuse(Color.BLUE))).box(10f, 1f, 20f);
        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(ColorAttribute.createDiffuse(Color.YELLOW))).sphere(2f, 2f, 2f, 20, 20);
        Model model = mb.end();
        models.add(model);

        // Suelo
        floorShape = new btBoxShape(new Vector3(20f, 0.5f, 20f));
        btRigidBody.btRigidBodyConstructionInfo floorInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, floorShape, Vector3.Zero);
        GameObject floor = new GameObject(model, "floor", floorInfo);
        dynamicsWorld.addRigidBody(floor.body);
        instances.add(floor);

        // Rampa
        rampShape = new btBoxShape(new Vector3(5f, 0.5f, 10f));
        btRigidBody.btRigidBodyConstructionInfo rampInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, rampShape, Vector3.Zero);
        GameObject ramp = new GameObject(model, "ramp", rampInfo);
        ramp.transform.setToTranslation(0, 5f, -5f);
        ramp.transform.rotate(Vector3.X, 30f);
        ramp.body.setWorldTransform(ramp.transform);
        dynamicsWorld.addRigidBody(ramp.body);
        instances.add(ramp);

        // Esferas
        sphereShape = new btSphereShape(1f);
        Vector3 localInertia = new Vector3();
        sphereShape.calculateLocalInertia(2f, localInertia);
        btRigidBody.btRigidBodyConstructionInfo sphereInfo = new btRigidBody.btRigidBodyConstructionInfo(2f, null, sphereShape, localInertia);

        for (int i = 0; i < 5; i++) {
            GameObject sphere = new GameObject(model, "sphere", sphereInfo);
            sphere.transform.setToTranslation((float)Math.random() * 4f - 2f, 15f + i * 4f, -10f);
            sphere.body.setWorldTransform(sphere.transform);
            sphere.body.setFriction(0.8f);
            sphere.body.setRollingFriction(0.1f);
            dynamicsWorld.addRigidBody(sphere.body);
            instances.add(sphere);
        }

        floorInfo.dispose();
        rampInfo.dispose();
        sphereInfo.dispose();
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
        if (floorShape != null) floorShape.dispose();
        if (rampShape != null) rampShape.dispose();
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
