import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
  public static void main(String[] args) {

    GLProfile glprofile = GLProfile.getDefault();
    GLCapabilities glcapabilities = new GLCapabilities(glprofile);
    final GLCanvas glcanvas = new GLCanvas(glcapabilities);

    final Frame frame = new Frame("Lab 3");
    frame.add(glcanvas);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowevent) {
        frame.remove(glcanvas);
        frame.dispose();
        System.exit(0);
      }
    });

    frame.setSize(1500, 1000);
    frame.setVisible(true);

    glcanvas.addGLEventListener(new Draw(10, 2000));

    FPSAnimator animator = new FPSAnimator(glcanvas, 65, true);
    animator.start();
  }
}