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

public class PhysicsScreen extends Base3DScreen {
    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld dynamicsWorld;
    private btCollisionShape floorShape;
    private btCollisionShape boxShape;

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

        public MyMotionState(Matrix4 transform) {
            this.transform = transform;
        }

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.set(worldTrans);
        }
    }

    public PhysicsScreen(Main game) {
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
                new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)))
                .box(100f, 1f, 100f);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(2f, 2f, 2f);
        Model model = mb.end();
        models.add(model);

        // Físicas del suelo
        floorShape = new btBoxShape(new Vector3(50f, 0.5f, 50f));
        btRigidBody.btRigidBodyConstructionInfo floorInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, floorShape, Vector3.Zero);
        GameObject floor = new GameObject(model, "floor", floorInfo);
        dynamicsWorld.addRigidBody(floor.body);
        instances.add(floor);

        // Físicas de las cajas cayendo
        boxShape = new btBoxShape(new Vector3(1f, 1f, 1f));
        Vector3 localInertia = new Vector3();
        boxShape.calculateLocalInertia(1f, localInertia);
        btRigidBody.btRigidBodyConstructionInfo boxInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, null, boxShape, localInertia);

        for (int i = 0; i < 30; i++) {
            GameObject box = new GameObject(model, "box", boxInfo);
            box.transform.setToTranslation((float) Math.random() * 10 - 5, 20f + i * 4f, (float) Math.random() * 10 - 5);
            
            // Aleatorizar rotación
            box.transform.rotate(Vector3.X, (float)Math.random() * 360f);
            box.transform.rotate(Vector3.Y, (float)Math.random() * 360f);
            box.transform.rotate(Vector3.Z, (float)Math.random() * 360f);

            box.body.setWorldTransform(box.transform);
            dynamicsWorld.addRigidBody(box.body);
            instances.add(box);
        }

        floorInfo.dispose();
        boxInfo.dispose();

        cam.position.set(0f, 10f, 30f);
        cam.lookAt(0, 0, 0);
        cam.update();
    }

    @Override
    public void render(float delta) {
        super.render(delta); // limpia la pantalla
        dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

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
        if (boxShape != null) boxShape.dispose();

        dynamicsWorld.dispose();
        solver.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfig.dispose();

        for (Model m : models) {
            m.dispose();
        }
        models.clear();
        super.dispose();
    }
}
