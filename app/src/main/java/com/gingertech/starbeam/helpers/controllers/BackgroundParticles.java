package com.gingertech.starbeam.helpers.controllers;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class BackgroundParticles extends SurfaceView implements SurfaceHolder.Callback {

    class vector2D {
        float x;
        float y;

        vector2D(float x, float y) {
            this.x = x;
            this.y = y;
        }

        float getAngle() {
            float o = (float) Math.atan(this.y / this.x);

            if(x < 0) {
                o -= Math.PI;
            }

            return o;
        }

        void fromAngle(double angle) {
            this.x = (float) Math.cos(angle);
            this.y = (float) Math.sin(angle);

        }
    }

    class pixel {
        float x;
        float y;
        vector2D dir = new vector2D(0,0);
        final float speed = 0.7f;

        int boundX = 0;
        int boundY = 0;

        int xReflect = 1;
        int yReflect = 1;

        ArrayList<pixel> close = new ArrayList<>();
        ArrayList<pixel> connectedList = new ArrayList<>();

        String ID = "";

        final int distance = 175;

        pixel(int x, int y) {

            this.boundX = x;
            this.boundY = y;

            this.x = (float) new Random().nextInt(x);
            this.y = (float) new Random().nextInt(y);

            this.dir.fromAngle(2f * 3.1415f * (float) (new Random().nextInt(360) - 180) / 360f);

            this.ID = UUID.randomUUID().toString();

//            this.speed = (float) new Random().nextInt(200) / 100;
        }

        pixel(vector2D vector) {
            this.x = vector.x;
            this.y = vector.y;
        }
            void update() {

            this.x += speed * xReflect * cos(this.dir.getAngle());
            this.y += speed * yReflect * sin(this.dir.getAngle());

            if(this.x >= boundX || this.x <= 0) {
                xReflect = -xReflect;
                this.x = Math.round(this.x / boundX) == 1 ? (boundX - 3) : 3;
            }

            if(this.y >= boundY || this.y <= 0) {
                yReflect = -yReflect;
                this.y = Math.round(this.y / boundY) == 1 ? (boundY - 3) : 3;
            }

        }


        float distance(pixel pixel2) {
            return (float) Math.sqrt(Math.pow(this.x - pixel2.x, 2) + Math.pow(this.y - pixel2.y, 2));
        }

    }

    class pixel3D {

        float dof = 2;
        float x;
        float y;
        float z;

        pixel3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        pixel convertTo2D(pixel3D inpix) {
            pixel output = new pixel(1,1);

            output.x = inpix.x / (dof * inpix.z);
            output.y = inpix.y / (dof * inpix.z);

            return output;
        }
    }

    class Rectangle extends shape3D {
        float[] coordinates = {
                -1, -1, -1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, 1,
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1};

        int[] connections = {
                        0,1,
                        1,2,
                        2,3,
                        3,0,

                        4,5,
                        5,6,
                        6,7,
                        7,4,

                        0,4,
                        1,5,
                        2,6,
                        3,7,

        };

        Rectangle(int x, int y, float scale) {
            super(x, y, scale);
            super.coordinates = coordinates;
            super.connections = connections;
        }

        @Override
        void drawShape(Canvas canvas, Paint paint) {
            super.draw(canvas, paint);
            for (int i = 0; i < connections.length / 2; i++) {
                drawConnectPoints(canvas, paint,
                        super.points2D.get(connections[i * 2]),
                        super.points2D.get(connections[i * 2 + 1]));
            }
        }
    }

    class TrianglePyramid extends shape3D {
        float[] coordinates = {
                0, -1, 1,
                -0.866f, -1, -0.5f,
                0.866f, -1, -0.5f,
                0, 0.732f, 0};


        TrianglePyramid(int x, int y, float scale) {
            super(x, y, scale);
            super.coordinates = coordinates;
        }
        @Override
        void drawShape(Canvas canvas, Paint paint) {
            super.draw(canvas, paint);
            for(int u = 0; u < super.points2D.size(); u++) {
                for(int k = u; k < super.points2D.size(); k++) {

                    drawConnectPoints(canvas, paint, super.points2D.get(u), super.points2D.get(k));

                }
            }
        }

    }

    class SquarePyramid extends shape3D {
        float[] coordinates = {
                1, -1, 1,
                1, -1, -1,
                -1, -1, -1,
                -1, -1, 1,
                0, 1, 0};

        int[] connections = {
                0,1,
                1,2,
                2,3,
                3,0,
                4,0,
                4,1,
                4,2,
                4,3,
        };

        SquarePyramid(int x, int y, float scale) {
            super(x, y, scale);
            super.coordinates = coordinates;
            super.connections = connections;
        }

        @Override
        void drawShape(Canvas canvas, Paint paint) {
            super.draw(canvas, paint);
            for (int i = 0; i < connections.length / 2; i++) {
                drawConnectPoints(canvas, paint,
                        super.points2D.get(connections[i * 2]),
                        super.points2D.get(connections[i * 2 + 1]));
            }
        }
    }

    class Star extends shape3D {
        float[] coordinates = {
                -0.588f, 0.809f, 0,
                0.588f, 0.809f, 0,
                0.952f, -0.309f, 0,
                0, -1f, 0,
                -0.952f, -0.309f, 0,
                0, 0, 0.3f,
                0, 0, -0.3f};

        int[] connections = {
                6, 0,
                6, 1,
                6, 2,
                6, 3,
                6, 4,
                6, 5,

                5, 0,
                5, 1,
                5, 2,
                5, 3,
                5, 4,


                0, 2,
                0, 3,

                1, 3,
                1, 4,

                2, 4


        };

        Star(int x, int y, float scale) {
            super(x, y, scale);

            super.coordinates = coordinates;
            super.connections = connections;
        }

        @Override
        void drawShape(Canvas canvas, Paint paint) {
            super.draw(canvas, paint);
            for (int i = 0; i < connections.length / 2; i++) {
                drawConnectPoints(canvas, paint,
                        super.points2D.get(connections[i * 2]),
                        super.points2D.get(connections[i * 2 + 1]));
            }
        }
    }

    class shape3D {

        int boundX = 0;
        int boundY = 0;

        float speed = 1;
        float depth = 0;

        float scale = 1f;

        vector2D reflect = new vector2D(1, 1);

        vector2D location = new vector2D(1, 1);
        float drift_dir = 0;
        pixel3D rotationAxis = new pixel3D(0,0, 0);
        float rotation = 0;
        float rotationSpeed = 1;

        pixel3D size = new pixel3D(1,1,1);
        ArrayList<pixel3D> points = new ArrayList<>();
        ArrayList<vector2D> points2D = new ArrayList<>();


        float[] coordinates = {};
        int[] connections = {};

        ArrayList<pixel> exportPixels() {

            ArrayList<pixel> p = new ArrayList<>();

            for(vector2D o : points2D) {
                p.add(new pixel(o));
            }

            return p;
        }

        shape3D(int x, int y) {
            this.boundX = x;
            this.boundY = y;

            location.x = (float) new Random().nextInt(x);
            location.y = (float) new Random().nextInt(y);

            drift_dir = 2f * 3.1415f * (float) (new Random().nextInt(360) - 180) / 360f;

            rotationAxis.x = ((float) new Random().nextInt(200) / 100) - 1f;
            rotationAxis.y = ((float) new Random().nextInt(200) / 100) - 1f;
            rotationAxis.z = ((float) new Random().nextInt(200) / 100) - 1f;

            rotation = ((float) new Random().nextInt(360) / 360) * 3.14f * 2;
        }

        shape3D(int x, int y, float scale) {
            this.scale = scale;
            this.boundX = x;
            this.boundY = y;

            location.x = (float) new Random().nextInt(x);
            location.y = (float) new Random().nextInt(y);

            drift_dir = 2f * 3.1415f * (float) (new Random().nextInt(360) - 180) / 360f;

            rotationAxis.x = ((float) new Random().nextInt(200) / 100) - 1f;
            rotationAxis.y = ((float) new Random().nextInt(200) / 100) - 1f;
            rotationAxis.z = ((float) new Random().nextInt(200) / 100) - 1f;

            rotation = ((float) new Random().nextInt(360) / 360) * 3.14f * 2;
        }

        public void make() {

            points.clear();

            for(int u = 0; u < coordinates.length / 3; u++) {

                pixel3D n = new pixel3D(coordinates[3 * u] * 100,
                        coordinates[3 * u + 1] * 100,
                        coordinates[3 * u + 2] * 100
                );

                float[] results = RotateQuaternon(n.x, n.y, n.z, rotation, rotationAxis.x, rotationAxis.y, rotationAxis.z);
                n.x = results[1];
                n.y = results[2];
                n.z = results[3];

                points.add(n);

            }
        }

        public void move() {
            location.x += speed * reflect.x * cos(this.drift_dir);
            location.y += speed * reflect.y * sin(this.drift_dir);

            if(this.location.x >= boundX || this.location.x <= 0) {
                reflect.x = -reflect.x;
                this.location.x = Math.round(this.location.x / boundX) == 1 ? (boundX - 3) : 3;
            }

            if(this.location.y >= boundY || this.location.y <= 0) {
                reflect.y = -reflect.y;
                this.location.y = Math.round(this.location.y / boundY) == 1 ? (boundY - 3) : 3;
            }
        }

        void drawShape(Canvas canvas, Paint paint) {
            draw(canvas, paint);
        }

        float[] RotateQuaternon(float Px, float Py, float Pz, float w, float Vx, float Vy, float Vz) {

            float d = (float) Math.sqrt(Math.pow(Vx, 2) + Math.pow(Vy, 2) + Math.pow(Vz, 2));

            Vx = Vx / d;
            Vy = Vy / d;
            Vz = Vz / d;
            w = w / 2;

            //Serial.println(Vx * Vx + Vy * Vy + Vz * Vz);

            double[] FirstN = {cos(w), Vx * sin(w), Vy * sin(w), Vz * sin(w)};
            double[] SecondN = {Px, Py, Pz};
            double[] FirstNG = {cos(w), Vx * sin(w), Vy * sin(w), Vz * sin(w)};

            float[] ResultN = new float[16];
            float[] ResultNG = new float[16];

            int Length;
            int Config;

            //Multiply #1

            for (Length = 0; Length < 4; Length++) {
                for (Config = 0; Config < 3; Config++) {
                    ResultN[Length * 3 + Config] = (float) (FirstN[Length] * SecondN[Config]);
                }
            }

            ResultN[0] +=  ResultN[8] - ResultN[10];
            ResultN[1] +=  -ResultN[5] + ResultN[9];
            ResultN[2] +=  ResultN[4] - ResultN[6];
            ResultN[3] =  -ResultN[3] - ResultN[7] - ResultN[11];


            float temp = ResultN[3];
            ResultN[3] = ResultN[0];
            ResultN[0] = temp;

            temp = ResultN[3];
            ResultN[3] = ResultN[1];
            ResultN[1] = temp;

            temp = ResultN[3];
            ResultN[3] = ResultN[2];
            ResultN[2] = temp;

            for (Length = 0; Length < 4; Length++) {
                for (Config = 0; Config < 4; Config++) {
                    ResultNG[Length * 4 + Config] = (float) (ResultN[Length] * FirstNG[Config]);
                }
            }

            ResultNG[0] +=  ResultNG[5] + ResultNG[10] + ResultNG[15];
            ResultNG[1] =  -ResultNG[1] + ResultNG[4] - ResultNG[11] + ResultNG[14];
            ResultNG[2] =  -ResultNG[2] + ResultNG[7] + ResultNG[8] - ResultNG[13];
            ResultNG[3] =  -ResultNG[3] - ResultNG[6] + ResultNG[9] + ResultNG[12];

            return ResultNG;
        }



        private void draw(Canvas canvas, Paint paint) {

            rotation += rotationSpeed / 180;
            move();
            make();

            points2D.clear();
            for(pixel3D point : points) {
                vector2D p = new vector2D(
                        Math.round((point.x * scale + location.x - boundX / 2) / (1 + point.z * scale * depth)) + boundX / 2,
                        Math.round((point.y * scale + location.y - boundY / 2) / (1 + point.z * scale * depth)) + boundY / 2);

                points2D.add(p);
                canvas.drawCircle(p.x, p.y, 3, paint);
            }
        }

        void drawConnectPoints(Canvas canvas, Paint paint, vector2D p1, vector2D p2) {
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
        }
    }

    public BackgroundParticles(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public BackgroundParticles(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public BackgroundParticles(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    public BackgroundParticles(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getHolder().addCallback(this);
    }


    boolean runSimulation = true;
    boolean drawOk = false;

    SimulationThread simulationThread;

    @Override
    public void surfaceCreated(final @NonNull SurfaceHolder surfaceHolder) {

        Log.i("sss", "created");

        simulationThread = new SimulationThread(surfaceHolder);
        simulationThread.start();

    }
    class SimulationThread extends Thread {

        SurfaceHolder surfaceHolder;

        public SimulationThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public boolean isRunning = true;

        final ArrayList<pixel> pixels = new ArrayList<>();
            int numOfPix = 75;
            float shapeScale = 1;
            int pixColor;
            int shapeColor_normal;
            int shapeColor_premium;


        boolean lockedCanvas = false;
        private Object lock = new Object(); // Lock for synchronization


            SquarePyramid squarePyramid;
            SquarePyramid squarePyramid2;

            Rectangle rectangle;
            TrianglePyramid trianglePyramid;
            //            Star star;
            int alpha;

        @Override
        public synchronized void start() {
            isRunning = true;
            super.start();
        }

        @Override
            public void run() {

            Log.i("sss", "starting");

                int height = BackgroundParticles.this.getHeight();

                if (height != 0) {
                    numOfPix = (int) (numOfPix * height / 1440f);
                    shapeScale = height / 1440f;
                }

                Canvas canvas;

                pixColor = ContextCompat.getColor(getContext(), R.color.primary);
                shapeColor_normal = ContextCompat.getColor(getContext(), R.color.purple);
                shapeColor_premium = ContextCompat.getColor(getContext(), R.color.gold);


            Paint paint = new Paint();
                paint.setColor(pixColor);

                while (!isInterrupted()) {

                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {

                    }


                    if(surfaceHolder.getSurface() == null) { return;}
                    if(!surfaceHolder.getSurface().isValid()) { return;}

                    if (!lockedCanvas) {
                            canvas = surfaceHolder.lockCanvas();
                            synchronized (lock) {
                                lockedCanvas = true;
                                drawOk = true;

                                if (canvas == null) { return; }
                                if(!surfaceHolder.getSurface().isValid()) { return;}

                                if (pixels.size() == 0) {

                                    squarePyramid = new SquarePyramid(canvas.getWidth(), canvas.getHeight(), shapeScale);
                                    squarePyramid2 = new SquarePyramid(canvas.getWidth(), canvas.getHeight(), shapeScale);

                                    rectangle = new Rectangle(canvas.getWidth(), canvas.getHeight(), shapeScale);
                                    trianglePyramid = new TrianglePyramid(canvas.getWidth(), canvas.getHeight(), shapeScale);
//                                star = new Star(canvas.getWidth(), canvas.getHeight());

                                    for (int x = 0; x < numOfPix; x++) {
                                        pixel p = new pixel(canvas.getWidth(), canvas.getHeight());
                                        canvas.drawCircle(p.x, p.y, 3, paint);
                                        p.update();
                                        pixels.add(p);

                                    }
                                } else {

                                    canvas.drawPaint(paint);

                                    paint.setColor(ContextCompat.getColor(getContext(), R.color.black));
                                    paint.setAlpha(255);
                                    canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);

                                    if(UserData.isPremiumMode || UserData.CurrentFragment == UserData.PREMIUM) {
                                        paint.setColor(shapeColor_premium);
                                    } else {
                                        paint.setColor(shapeColor_normal);
                                    }

                                    paint.setAlpha(100);

                                    squarePyramid.drawShape(canvas, paint);
                                    squarePyramid2.drawShape(canvas, paint);
                                    rectangle.drawShape(canvas, paint);
                                    trianglePyramid.drawShape(canvas, paint);

                                    paint.setColor(pixColor);

                                    for (int u = 0; u < pixels.size(); u++) {

                                        pixel p1 = pixels.get(u);

                                        p1.update();

                                        paint.setAlpha(255);
                                        canvas.drawCircle(p1.x, p1.y, 1, paint);

                                        for (int f = 0; f < pixels.size(); f++) {

                                            pixel p2 = pixels.get(f);

                                            if (p1.distance(p2) > p1.distance) {
                                                continue;
                                            }

                                            alpha = (int) (100 * (1 - (p1.distance(p2) / p1.distance)));
                                            paint.setAlpha(alpha);
                                            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                                        }
                                    }
                                }
                            }

                            if (!isInterrupted() && surfaceHolder.getSurface().isValid() && drawOk && lockedCanvas) {

                                try {
                                    surfaceHolder.unlockCanvasAndPost(canvas);
                                    lockedCanvas = false;
                                    drawOk = false;

                                } catch (IllegalArgumentException e) {
                                    Log.i("errors", "argument illegal");
                                } catch (IllegalStateException e) {
                                    Log.i("errors", "state illegal");
                                }

                            }

                }
            }

            Log.i("sss", "ended");

        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i("sss", "changed");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.i("sss", "destroyed");
        if(simulationThread != null) {
            try {
                simulationThread.join();
            } catch (InterruptedException e) {

            }
        }
    }

}
