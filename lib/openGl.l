# 27jul10abu
# 27jul10jk
# (c) Software Lab. Alexander Burger

(load "@lib/math.l" "@lib/native.l")

### Constant Definitions ###
# Primitives
(def 'GL_POINTS         (hex "0000"))
(def 'GL_LINES          (hex "0001"))
(def 'GL_LINE_LOOP      (hex "0002"))
(def 'GL_LINE_STRIP     (hex "0003"))
(def 'GL_TRIANGLES      (hex "0004"))
(def 'GL_TRIANGLE_STRIP (hex "0005"))
(def 'GL_TRIANGLE_FAN   (hex "0006"))
(def 'GL_QUADS          (hex "0007"))
(def 'GL_QUAD_STRIP     (hex "0008"))
(def 'GL_POLYGON        (hex "0009"))

# Matrix Mode
(def 'GL_MATRIX_MODE (hex "0BA0"))
(def 'GL_MODELVIEW   (hex "1700"))
(def 'GL_PROJECTION  (hex "1701"))
(def 'GL_TEXTURE     (hex "1702"))

# glPush/PopAttrib bits
(def 'GL_CURRENT_BIT          (hex "00000001"))
(def 'GL_POINT_BIT            (hex "00000002"))
(def 'GL_LINE_BIT             (hex "00000004"))
(def 'GL_POLYGON_BIT          (hex "00000008"))
(def 'GL_POLYGON_STIPPLE_BIT  (hex "00000010"))
(def 'GL_PIXEL_MODE_BIT       (hex "00000020"))
(def 'GL_LIGHTING_BIT         (hex "00000040"))
(def 'GL_FOG_BIT              (hex "00000080"))
(def 'GL_DEPTH_BUFFER_BIT     (hex "00000100"))
(def 'GL_ACCUM_BUFFER_BIT     (hex "00000200"))
(def 'GL_STENCIL_BUFFER_BIT   (hex "00000400"))
(def 'GL_VIEWPORT_BIT         (hex "00000800"))
(def 'GL_TRANSFORM_BIT        (hex "00001000"))
(def 'GL_ENABLE_BIT           (hex "00002000"))
(def 'GL_COLOR_BUFFER_BIT     (hex "00004000"))
(def 'GL_HINT_BIT             (hex "00008000"))
(def 'GL_EVAL_BIT             (hex "00010000"))
(def 'GL_LIST_BIT             (hex "00020000"))
(def 'GL_TEXTURE_BIT          (hex "00040000"))
(def 'GL_SCISSOR_BIT          (hex "00080000"))
(def 'GL_ALL_ATTRIB_BITS      (hex "000FFFFF"))

# AlphaFunction
(def 'GL_LESS                 (hex "00000201"))

# BlendingFactorDest
(def 'GL_SRC_ALPHA            (hex "00000302"))
(def 'GL_ONE_MINUS_SRC_ALPHA  (hex "00000303"))

# DrawBufferMode
(def 'GL_FRONT_AND_BACK  		(hex "00000408"))

# GetTarget
(def 'GL_BLEND                (hex "00000BE2"))
(def 'GL_COLOR_MATERIAL       (hex "00000B57"))
(def 'GL_CULL_FACE            (hex "00000B44"))
(def 'GL_DEPTH_TEST           (hex "00000B71"))
(def 'GL_LIGHTING             (hex "00000B50"))
(def 'GL_LINE_SMOOTH          (hex "00000B20"))
(def 'GL_LINE_SMOOTH_HINT     (hex "00000C52"))

# HintMode
(def 'GL_NICEST					(hex "00001102"))

# LightName
(def 'GL_LIGHT0               (hex "00004000"))

# MaterialParameter
(def 'GL_AMBIENT_AND_DIFFUSE	(hex "00001602"))

# ShadingModel
(def 'GL_FLAT               	(hex "00001D00"))
(def 'GL_SMOOTH               (hex "00001D01"))

# GLUT API macro definitions -- the display mode definitions
(def 'GLUT_RGB          (hex "0000"))
(def 'GLUT_RGBA         (hex "0000"))
(def 'GLUT_INDEX        (hex "0001"))
(def 'GLUT_SINGLE       (hex "0000"))
(def 'GLUT_DOUBLE       (hex "0002"))
(def 'GLUT_ACCUM        (hex "0004"))
(def 'GLUT_ALPHA        (hex "0008"))
(def 'GLUT_DEPTH        (hex "0010"))
(def 'GLUT_STENCIL      (hex "0020"))
(def 'GLUT_MULTISAMPLE  (hex "0080"))
(def 'GLUT_STEREO       (hex "0100"))
(def 'GLUT_LUMINANCE    (hex "0200"))

# Function keys
(def 'GLUT_KEY_F1		1)
(def 'GLUT_KEY_F2		2)
(def 'GLUT_KEY_F3		3)
(def 'GLUT_KEY_F4		4)
(def 'GLUT_KEY_F5		5)
(def 'GLUT_KEY_F6		6)
(def 'GLUT_KEY_F7		7)
(def 'GLUT_KEY_F8		8)
(def 'GLUT_KEY_F9		9)
(def 'GLUT_KEY_F10	10)
(def 'GLUT_KEY_F11	11)
(def 'GLUT_KEY_F12	12)

# Directional keys
(def 'GLUT_KEY_LEFT			100)
(def 'GLUT_KEY_UP				101)
(def 'GLUT_KEY_RIGHT			102)
(def 'GLUT_KEY_DOWN			103)
(def 'GLUT_KEY_PAGE_UP		104)
(def 'GLUT_KEY_PAGE_DOWN	105)
(def 'GLUT_KEY_HOME			106)
(def 'GLUT_KEY_END			107)
(def 'GLUT_KEY_INSERT		108)

# Mouse state definitions
(def 'GLUT_LEFT_BUTTON		0)
(def 'GLUT_MIDDLE_BUTTON	1)
(def 'GLUT_RIGHT_BUTTON		2)


### OpenGL library interface ###
(default
   *GluLib "/usr/lib/libGLU.so"
   *GlutLib "/usr/lib/libglut.so" )


### Inline-C functions ###
(gcc "glut" (list *GlutLib)
   (glClearColor (Red Green Blue Alpha) "GlClearColor" NIL Red Green Blue Alpha 1.0)
   (glClearDepth (Depth) "GlClearDepth" NIL Depth 1.0)
   (glColor3f (Red Green Blue) "GlColor3f" NIL Red Green Blue 1.0)
   (glColor4f (Red Green Blue Alpha) "GlColor4f" NIL Red Green Blue Alpha 1.0)
   (glNormal3f (X Y Z) "GlNormal3f" NIL X Y Z 1.0)
   (glOrtho (Left Right Bottom Top Near Far) "GlOrtho" NIL Left Right Bottom Top Near Far 1.0)
   (glRotatef (X Y Z) "GlRotatef" NIL Angle X Y Z 1.0)
   (glTranslatef (X Y Z) "GlTranslatef" NIL X Y Z 1.0)
   (glVertex3f (X Y Z) "GlVertex3f" NIL X Y Z 1.0)
   (glutSolidCube () "GlutSolidCube")
   (glutWireCube () "GlutWireCube")
   (glutDisplayFunc () "GlutDisplayFunc")
   (glutCreateMenu () "GlutCreateMenu")
   (glutKeyboardFunc () "GlutKeyboardFunc")
   (glutMotionFunc () "GlutMotionFunc")
   (glutMouseFunc () "GlutMouseFunc")
   (glutReshapeFunc () "GlutReshapeFunc")
   (glutSpecialFunc () "GlutSpecialFunc")
   (glutTimerFunc () "GlutTimerFunc") )

#include <GL/glut.h>
#include <GL/glu.h>
#include <GL/gl.h>

extern long lisp(char*,long,long,long,long,long);

void GlClearColor(long red, long green, long blue, long alpha, int scl) {
   glClearColor(
      (GLclampf)red / (float)scl,
      (GLclampf)green / (float)scl,
      (GLclampf)blue / (float)scl,
      (GLclampf)alpha / (float)scl );
}

void GlClearDepth(long depth, int scl) {
   glClearDepth(
      (double)depth / (double)scl );
}

void GlColor3f(long red, long green, long blue, int scl) {
   glColor3f(
      (double)red / (double)scl,
      (double)green / (double)scl,
      (double)blue / (double)scl );
}

void GlColor4f(long red, long green, long blue, long alpha, int scl) {
   glColor4f(
      (double)red / (double)scl,
      (double)green / (double)scl,
      (double)blue / (double)scl,
      (double)alpha / (double)scl );
}

void GlNormal3f(long vx, long vy, long vz, int scl) {
   glNormal3f(
      (double)vx / (double)scl,
      (double)vy / (double)scl,
      (double)vz / (double)scl );
}

void GlOrtho(long left, long right, long bottom, long top, long near, long far, long scl) {
   glOrtho(
      (double)left / (double)scl,
      (double)right / (double)scl,
      (double)bottom / (double)scl,
      (double)top / (double)scl,
      (double)near / (double)scl,
      (double)far / (double)scl );
}

void GlRotatef(long angle, long vx, long vy, long vz, int scl) {
   glRotatef(
      (double)angle / (double)scl,
      (double)vx / (double)scl,
      (double)vy / (double)scl,
      (double)vz / (double)scl );
}

void GlTranslatef(long vx, long vy, long vz, int scl) {
   glTranslatef(
      (double)vx / (double)scl,
      (double)vy / (double)scl,
      (double)vz / (double)scl );
}

void GlVertex3f(long vx, long vy, long vz, int scl) {
   glVertex3f(
      (double)vx / (double)scl,
      (double)vy / (double)scl,
      (double)vz / (double)scl );
}

void GlutSolidCube(long size, int scl) {
   glutSolidCube(
      (double)size / (double)scl );
}

void GlutWireCube(long size, int scl) {
   glutWireCube(
      (double)size / (double)scl );
}

static void displayCallback(void) {
   lisp("displayCallback", 0, 0, 0, 0, 0);
}
void GlutDisplayFunc(void) {glutDisplayFunc(displayCallback);}

static void createMenuCallback(int val) {
   lisp("createMenuCallback", val, 0, 0, 0, 0);
}
void GlutCreateMenu(void) {glutCreateMenu(createMenuCallback);}

static void keyboardCallback(unsigned char key, int xv, int yv) {
   lisp("keyboardCallback", key, xv, yv, 0, 0);
}
void GlutKeyboardFunc(void) {glutKeyboardFunc(keyboardCallback);}

static void motionCallback(int xv, int yv) {
   lisp("motionCallback", xv, yv, 0, 0, 0);
}
void GlutMotionFunc(void) {glutMotionFunc(motionCallback);}

static void mouseCallback(int button, int state, int xv, int yv) {
   lisp("mouseCallback", button, state, xv, yv, 0);
}
void GlutMouseFunc(void) {glutMouseFunc(mouseCallback);}

static void reshapeCallback(int width, int height) {
   lisp("reshapeCallback", width, height, 0, 0, 0);
}
void GlutReshapeFunc(void) {glutReshapeFunc(reshapeCallback);}

static void specialCallback(int key, int xv, int yv) {
   lisp("specialCallback", key, xv, yv, 0, 0);
}
void GlutSpecialFunc(void) {glutSpecialFunc(specialCallback);}

static void timerCallback(int val) {
   lisp("timerCallback", val, 0, 0, 0, 0);
}
void GlutTimerFunc(int msec, int val) {glutTimerFunc(msec, timerCallback, val);}
/**/


### Native functions ###
(de glutInit ()
   (native `*GlutLib "glutInit" NIL '(NIL (8) . 0)) )

(de glutInitDisplayMode (N)
   (native `*GlutLib "glutInitDisplayMode" NIL N) )

(de glutInitWindowPosition (Width Height)
   (native `*GlutLib "glutInitWindowPosition" NIL Width Height) )

(de glutInitWindowSize (Width Height)
   (native `*GlutLib "glutInitWindowSize" NIL Width Height) )

(de glutCreateWindow (Name)
   (native `*GlutLib "glutCreateWindow" NIL Name) )

(de glutSwapBuffers ()
   (native `*GlutLib "glutSwapBuffers") )

(de glMatrixMode (Mode)
   (native `*GlutLib "glMatrixMode" NIL Mode) )

(de glLoadIdentity ()
   (native `*GlutLib "glLoadIdentity") )

(de glClear (Mask)
   (native `*GlutLib "glClear" NIL Mask) )

(de glBegin (Mode)
   (native `*GlutLib "glBegin" NIL Mode) )

(de glEnd ()
   (native `*GlutLib "glEnd") )

(de glColorMaterial (Face Mode)
   (native `*GlutLib "glColorMaterial" NIL Face Mode) )

(de glDepthFunc (Num)
   (native `*GlutLib "glDepthFunc" NIL Num) )

(de glEnable (Num)
   (native `*GlutLib "glEnable" NIL Num) )

(de glDisable (Num)
   (native `*GlutLib "glDisable" NIL Num) )

(de glBlendFunc (SFactor DFactor)
   (native `*GlutLib "glBlendFunc" NIL SFactor DFactor) )

(de glHint (Target Mode)
   (native `*GlutLib "glHint" NIL Target Mode) )

(de glLineWidth (Width)
   (native `*GlutLib "glLineWidth" NIL Width) )

(de glFlush ()
   (native `*GlutLib "glFlush") )

(de glPopMatrix ()
   (native `*GlutLib "glPopMatrix") )

(de glPushMatrix ()
   (native `*GlutLib "glPushMatrix") )

(de glShadeModel (Num)
   (native `*GlutLib "glShadeModel" NIL Num) )

(de glViewport (X Y W H)
   (native `*GlutLib "glViewport" NIL X Y W H) )

(de gluPerspective (Fovy Aspect ZNear ZFar)
   (native `*GluLib "gluPerspective" NIL Fovy Aspect ZNear ZFar) )

(de glutMainLoop ()
   (native `*GlutLib "glutMainLoop") )

(de glutAddMenuEntry (Name Val)
   (native `*GlutLib "glutAddMenuEntry" NIL Name Val) )

(de glutAttachMenu (Button)
   (native `*GlutLib "glutAttachMenu" NIL Button) )

(de glutPostRedisplay ()
   (native `*GlutLib "glutPostRedisplay") )


### Callbacks ###
# Keep references in global symbols, to protect from garbage collection

# Display Function
(de displayPrg Prg
   (setq *GlutDisplayPrg Prg)
   (glutDisplayFunc) )

(de displayCallback ()
   (run *GlutDisplayPrg) )

# CreateMenu Function
(de createMenu (Fun)
   (setq *CreateMenuFunc Fun)
   (glutCreateMenu) )

(de createMenuCallback (Val)
   (*CreateMenuFunc Val) )

# Keyboard Function
(de keyboardFunc (Fun)
   (setq *GlutKeyboardFunc Fun)
   (glutKeyboardFunc) )

(de keyboardCallback (Key Xv Yv)
   (*GlutKeyboardFunc Key Xv Yv) )

# Motion Function
(de motionFunc (Fun)
   (setq *GlutMotionFunc Fun)
   (glutMotionFunc) )

(de motionCallback (Xv Yv)
   (*GlutMotionFunc Xv Yv) )

# Mouse Function
(de mouseFunc (Fun)
   (setq *GlutMouseFunc Fun)
   (glutMouseFunc) )

(de mouseCallback (Button State Xv Yv)
   (*GlutMouseFunc Button State Xv Yv) )

# Reshape Function
(de reshapeFunc (Fun)
   (setq *GlutReshapeFunc Fun)
   (glutReshapeFunc) )

(de reshapeCallback (Width Height)
   (*GlutReshapeFunc Width Height) )

# Special Function
(de specialFunc (Fun)
   (setq *GlutSpecialFunc Fun)
   (glutSpecialFunc) )

(de specialCallback (Key Xv Yv)
   (*GlutSpecialFunc Key Xv Yv) )

# Timer Function
(de timerFunc (Msec Fun Val)
   (setq *GlutTimerFunc Fun)
   (glutTimerFunc Msec Val) )

(de timerCallback (Val)
   (*GlutTimerFunc Val) )

# vi:et:ts=3:sw=3
