import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import java.nio.FloatBuffer;
import java.util.*;


public class Draw implements GLEventListener {

  private final double ROTATE_SPEED = 6;
  private final double LIGHT_ROTATE_SPEED = 0;

  private final float SIZE = 1; //relative size
  private final double SCENE_SCALE = -3;
  private final long MORPHING_DELAY;

  private double rotateAngle = 0;
  private double lightRotateAngle = 0;

  private GL2 gl;
  private GLU glu;
  private GLUT glut;

  FloatBuffer lightPos = FloatBuffer.wrap(new float[]{-SIZE * 10, SIZE * 5, SIZE * 5, 1});
  FloatBuffer diffuseLight = FloatBuffer.wrap(new float[]{1, 1, 1, 1});

  List<float[]> torusVertexes;
  List<float[]> cubeVertexes;
  int MORPHING_STEPS;
  List<float[]> figureVertexes;

  boolean startMorphingDisplayFlag;

  public Draw(int steps, long morphingStageDelay) {
    MORPHING_STEPS = steps;
    MORPHING_DELAY = morphingStageDelay;
  }

  @Override
  public void init(GLAutoDrawable glAutoDrawable) {
    gl = glAutoDrawable.getGL().getGL2();
    glu = new GLU();
    glut = new GLUT();
    gl.glEnable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_LIGHTING);
    gl.glEnable(GL2.GL_COLOR_MATERIAL);
    gl.glEnable(GL2.GL_NORMALIZE);

    gl.glEnable(GL2.GL_LIGHT0);


    double tubeRadius = SIZE / 6;
    double pathRadius = SIZE / 2 - tubeRadius;
    torusVertexes = doughnut(tubeRadius, pathRadius, 8, 32);
    cubeVertexes = cube(SIZE);

    figureVertexes = new ArrayList<>(torusVertexes.size());
    for (int i = 0; i < torusVertexes.size(); i++) {
      figureVertexes.add(new float[]{torusVertexes.get(i)[0], torusVertexes.get(i)[1], torusVertexes.get(i)[2]});
    }

    /*Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        for (int steps = 0; steps < MORPHING_STEPS; steps++) {
          try {
            Thread.sleep(MORPHING_DELAY);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          startMorphingDisplayFlag = true;

          for (int i = 0, figureInd = 0; i < figureVertexes.size() / cubeVertexes.size() + 1; i++) {
            for (int cubeInd = 0; cubeInd < cubeVertexes.size() && figureInd < figureVertexes.size(); ++cubeInd, ++figureInd) {
              for (int xyz = 0; xyz < 3; xyz++) {
                figureVertexes.get(figureInd)[xyz] += (cubeVertexes.get(cubeInd)[xyz] - torusVertexes.get(figureInd)[xyz]) / MORPHING_STEPS;
              }
            }
          }
        }
        timer.cancel();
      }
    }, 0);*/
  }

  @Override
  public void dispose(GLAutoDrawable glAutoDrawable) {
  }

  @Override
  public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
    gl.glViewport(0, 0, width, height);
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();

    GLU glu = new GLU();
    glu.gluPerspective(45.0f, (float) width / height, 1.0, 20.0);

    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  @Override
  public void display(GLAutoDrawable glAutoDrawable) {
    gl.glClearColor(0, 0, 0, 0);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

    displayLight();
    displayFigure();

    rotateAngle += 0.1 * ROTATE_SPEED;
    lightRotateAngle += 0.1 * LIGHT_ROTATE_SPEED;
  }

  private void displayLight() {
    gl.glLoadIdentity();
    gl.glPushMatrix();

    gl.glTranslated(0, 0, SCENE_SCALE);
    gl.glRotated(rotateAngle, 0, 1, 0);

    gl.glRotated(lightRotateAngle, 0, 1, 0);
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight);
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos);

    gl.glPopMatrix();
  }

  private void displayFigure() {
    gl.glLoadIdentity();
    gl.glPushMatrix();

    gl.glTranslated(0, 0, SCENE_SCALE);
    gl.glRotated(rotateAngle, 0, 1, 0);
    gl.glColor3d(0.4961, 0, 0.3125);

    gl.glBegin(GL2.GL_TRIANGLE_STRIP);

    List<float[]> p = new ArrayList<>(3);
    for (int i = 0; i < 3; i++) {
      p.add(null);
    }

    if (startMorphingDisplayFlag) {
      for (int i = 0; i < figureVertexes.size(); i++) {
        for (int j = 0; j < figureVertexes.size(); j++) {
          gl.glVertex3fv(figureVertexes.get(i), 0);
          gl.glVertex3fv(figureVertexes.get(j), 0);
        }
      }
    }
    else {
      gl.glVertex3fv(figureVertexes.get(0), 0);
      gl.glVertex3fv(figureVertexes.get(1), 0);

      for (int i = 2; i < figureVertexes.size(); i++) {
        updateTriangleVertexes(figureVertexes, p, i);
        gl.glNormal3fv(triangleNorm(p.get(0), p.get(1), p.get(2)), 0);
        gl.glVertex3fv(figureVertexes.get(i), 0);
      }
    }
    gl.glEnd();

    gl.glPopMatrix();
  }

  private static void updateTriangleVertexes(List<float[]> vertexes, List<float[]> triangleVertexes, int curIndex) {
    triangleVertexes.set(0, vertexes.get(curIndex - 2));
    triangleVertexes.set(1, vertexes.get(curIndex - 1));
    triangleVertexes.set(2, vertexes.get(curIndex));
  }

  private static float[] triangleNorm(float[] p1, float[] p2, float[] p3) {
    /*
    Пусть a = P2 - P1, b = P3 - P1
    Тогда нормаль N = a x b = [(a_y*b_z - a_z*b_y), (a_z*b_x - a_x*b_z), (a_x*b_y - a_y*b_x)]
    */

    float[] a = new float[]{p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
    float[] b = new float[]{p3[0] - p1[0], p3[1] - p1[1], p3[2] - p1[2]};

    float[] norm = new float[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    float length = (float) Math.sqrt(Math.pow(norm[0], 2) + Math.pow(norm[1], 2) + Math.pow(norm[2], 2));

    for (int i = 0; i < 3; i++) {
      norm[i] /= length;
    }
    return norm;
  }

  private List<float[]> cube(float size) {
    List<float[]> vertexes = new ArrayList<>();

    float[][] boxVertices = null;
    float[][] boxNormals = new float[][]{{-1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {1.0F, 0.0F, 0.0F}, {0.0F, -1.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, -1.0F}};
    int[][] boxFaces = new int[][]{{0, 1, 2, 3}, {3, 2, 6, 7}, {7, 6, 5, 4}, {4, 5, 1, 0}, {5, 6, 2, 1}, {7, 4, 0, 3}};

    float[][] var4;
    if (boxVertices == null) {
      var4 = new float[8][];

      for (int var5 = 0; var5 < 8; ++var5) {
        var4[var5] = new float[3];
      }

      var4[0][0] = var4[1][0] = var4[2][0] = var4[3][0] = -0.5F;
      var4[4][0] = var4[5][0] = var4[6][0] = var4[7][0] = 0.5F;
      var4[0][1] = var4[1][1] = var4[4][1] = var4[5][1] = -0.5F;
      var4[2][1] = var4[3][1] = var4[6][1] = var4[7][1] = 0.5F;
      var4[0][2] = var4[3][2] = var4[4][2] = var4[7][2] = -0.5F;
      var4[1][2] = var4[2][2] = var4[5][2] = var4[6][2] = 0.5F;
      boxVertices = var4;
    }

    var4 = boxVertices;
    float[][] var9 = boxNormals;
    int[][] var6 = boxFaces;

    for (int var7 = 5; var7 >= 0; --var7) {
      float[] var8 = var4[var6[var7][0]];
      float[] ver1 = new float[]{var8[0] * size, var8[1] * size, var8[2] * size};
      var8 = var4[var6[var7][1]];
      float[] ver2 = new float[]{var8[0] * size, var8[1] * size, var8[2] * size};
      var8 = var4[var6[var7][2]];
      float[] ver3 = new float[]{var8[0] * size, var8[1] * size, var8[2] * size};
      var8 = var4[var6[var7][3]];
      float[] ver4 = new float[]{var8[0] * size, var8[1] * size, var8[2] * size};
      vertexes.add(ver1);
      vertexes.add(ver2);
      vertexes.add(ver3);
      vertexes.add(ver4);
    }

    return vertexes;
  }

  private List<float[]> doughnut(double var1, double var3, int var5, int var6) {
    List<float[]> vertexes = new ArrayList<>();

    float var16 = (float) (6.283185307179586D / (double) var6);
    float var17 = (float) (6.283185307179586D / (double) var5);
    float var9 = 0.0F;
    float var12 = 1.0F;
    float var13 = 0.0F;

    for (int var7 = var6 - 1; var7 >= 0; --var7) {
      float var11 = var9 + var16;
      float var14 = (float) Math.cos((double) var11);
      float var15 = (float) Math.sin((double) var11);
      //gl.glBegin(GL2.GL_QUAD_STRIP);
      float var10 = 0.0F;

      for (int var8 = var5; var8 >= 0; --var8) {
        var10 += var17;
        float var18 = (float) Math.cos((double) var10);
        float var19 = (float) Math.sin((double) var10);
        float var20 = (float) (var3 + var1 * (double) var18);

        float[] ver1 = new float[]{var14 * var20, -var15 * var20, (float) var1 * var19};
        float[] ver2 = new float[]{var12 * var20, -var13 * var20, (float) var1 * var19};
        vertexes.add(ver1);
        vertexes.add(ver2);

        //gl.glNormal3f(var14 * var18, -var15 * var18, var19);
        //gl.glVertex3fv(ver1, 0);
        //gl.glNormal3f(var12 * var18, -var13 * var18, var19);
        //gl.glVertex3fv(ver2, 0);
      }

      //gl.glEnd();
      var9 = var11;
      var12 = var14;
      var13 = var15;
    }

    return vertexes;
  }

  /*private void displayCube() {
    float[][] boxNormals = new float[][]{{-1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {1.0F, 0.0F, 0.0F}, {0.0F, -1.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, -1.0F}};

    gl.glBegin(GL2.GL_QUADS);
    for (int cubeInd = 0, var7 = 5; var7 >= 0; --var7) {
      gl.glNormal3fv(boxNormals[var7], 0);

      gl.glVertex3fv(cubeVertexes.get(cubeInd++), 0);
      gl.glVertex3fv(cubeVertexes.get(cubeInd++), 0);
      gl.glVertex3fv(cubeVertexes.get(cubeInd++), 0);
      gl.glVertex3fv(cubeVertexes.get(cubeInd++), 0);
    }
    gl.glEnd();
  }*/
}
