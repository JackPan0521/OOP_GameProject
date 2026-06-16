import game.framework.*; 
import java.awt.*;
import java.awt.event.* ;

public class SampleRole5 implements Role, KeyListener,  MouseListener, MouseMotionListener
{
    protected String path= null ; //"pics/animal0/cat";
    protected String type = null ; //"png";
    protected ImageSequence[][] is = null; // new ImageSequence("pics/animal0/cat", "png", 3); //default

    protected int x=500, y=500, w=50, h=50; //default values
    protected int dx = 0;
    protected int dy = 0;
    protected int dir = 0;
    protected int level = 0;
    protected int dim1 = 0;
    protected int dim2 = 0;
    protected int jvx = 0;
    protected int jvy = -100;
    protected int bottom = 500;
    protected int delay = 1;
    protected boolean jumpAbility = false;
    protected boolean visibleTest = false;
    protected Model model = null;
    protected Movement jump=null; 
    protected Game gameCtl = null;
    private Voice voice=null;
    private String vfn = null;
    
    private class Voice {
            private GameMediaPlayer player = null;
            private boolean speaking = false;

            Voice(String fn){              
                  player =new SpecialGameSoundManager().getPlayer(fn);speaking = false;
            }
            void speak() {if (!speaking) {
                player.rewind(); speaking=true;
               }
            }
            void stop() {player.stop();speaking=false;}
            void close() {player.close();speaking = false;}
    }
    public void setVoice(String fn) {
        //String fn = "SoundEffects/SpecialSound/broken0.wav";
        if (!fn.equals(vfn)) {
            vfn=fn; 
            if (voice!=null) voice.close();
            voice = new Voice(fn); 
        } 
    }
    public void speak() {
       if (voice!=null) voice.speak();   
    }
    public void silent() {
       if (voice!=null) voice.stop();   
    }
    public SampleRole5() {
        
    }
    public SampleRole5(int x, int y, int w, int h , String path, String type, int img_num) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.path= path;
        this.type = type;
        is =  new ImageSequence[1][1];
        is[0][0]= new ImageSequence(path, type, img_num);
    }
    public SampleRole5(int x, int y, int w, int h ,int jvx, int jvy, int bottom, ImageSequence[][] is) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.path= path;
        this.type = type;
        this.is= is;
    }
    public SampleRole5(int x, int y, int w, int h ,int jvx, int jvy, int bottom, int delay, ImageSequence[][] is) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.path= path;
        this.type = type;
        this.is= is;
        this.delay = delay;
    }
    public SampleRole5(int x, int y, int w, int h ,int jvx, int jvy, int bottom, int delay) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.path= path;
        this.type = type;
        this.delay = delay;
    }
    public SampleRole5(int x, int y, int w, int h ,int jvx, int jvy, int bottom, int delay, Game ctl) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.path= path;
        this.type = type;
        this.delay = delay;
        this.gameCtl = ctl;
    }
     public SampleRole5(int x, int y, int w, int h ,int jvx, int jvy, int bottom, int delay, boolean setVisibleTest, Game ctl) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.path= path;
        this.type = type;
        this.delay = delay;
        this.gameCtl = ctl;
        this.visibleTest = setVisibleTest;
    }   
    

    protected void setIS(ImageSequence[][] is) {
        this.is= is;
    }
    
    @Override
    public void getReady() {        
        model = new Model(x, y, w, h);
         if (jvy!=0) jump = new Jump(jvx,jvy);
    }
    @Override
    public void run() {
        x += dx; y+= dy;
        model.setState(x, y);
        if ( jump != null && jumpAbility ) 
            jump.updatePos(model);
        else if (!jumpAbility)    
            if (jump!=null) jump.reset();
        x= model.getX(); y=model.getY();
        
        if (y>bottom && jump!=null) jump.reset();// = new Jump(0,-100,0.5, 0.5);
        if (visibleTest) {
         int ww = gameCtl.getWidth();
         int hh =gameCtl.getHeight();
         if ( (x < 0) || (x > ww) || (y < 0) || (y >hh))
            gameCtl.removeRole(this);
         
            
        }
        
    }
    @Override
    public void end() {
    }
    @Override
    public Model getModel() {
        model.setState(x, y);
        return model;
    }

    public void setModel(Model model) {
        this.model= model ;
       
    }   
    @Override
    public void display(Graphics g) {
        if (is == null) return;
        if (dim1 < 0 || dim1 >= is.length) return;
        if (is[dim1] == null) return;
        if (dim2 < 0 || dim2 >= is[dim1].length) return;
        if (is[dim1][dim2] == null) return;
    
        is[dim1][dim2].delay(delay);
        g.drawImage(is[dim1][dim2].next(true), x, y, w, h, null);
    }
    @Override
    public Effect conflict(Role role, Rectangle rec) {
        return null;
    }
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public  void keyPressed(KeyEvent e) {   }
    @Override
    public void keyReleased(KeyEvent e) { }
    @Override
    public void mouseClicked(MouseEvent e) { }
    @Override
    public void mouseEntered(MouseEvent e) { }
    @Override
    public void mouseExited(MouseEvent e) { }
    @Override
    public void mousePressed(MouseEvent e) { }
    @Override
    public void mouseReleased(MouseEvent e) { }
    @Override
    public void mouseDragged(MouseEvent e) { }
    @Override
    public void mouseMoved(MouseEvent e) { }  
    
    public int getX() {return x;}
    public int getY() {return y;}
    public int getDX() {return dx;}
    public int getDY() {return dy;}
    public int getJVX() {return jvx;}
    public int getJVY() {return jvy;}
    public int getW() {return w;}
    public int getH() {return h;}    
    public int getDir() {return dir;}
    public int getLevel() {return level;}
    public int getDim1() {return dim1;}
    public int getDim2() {return dim2;}
    public int getBottom() {return bottom;}
    public boolean getJumpAbility() {return jumpAbility;}
    public boolean getVisibleTest() {return visibleTest;}
    public Movement getJump( ) {return jump;}
    
    
    public void setX(int x) {this.x = x;}
    public void setY(int y) {this.y = y;}
    public void setDX(int x) { this.dx = x; }
    public void setDY(int y) { this.dy = y; }
    public void setJVX(int x) { this.jvx = x; }
    public void setJVY(int y) { this.jvy = y; }
    public void setW(int w) {this.w = w;}
    public void setH(int h) {this.h = h;}
    public void setDir(int dir) {this.dir = dir;}
    public void setLevel(int level) {this.level = level;}
    public void setDim1(int dim1) {this.dim1 = dim1;}
    public void setDim2(int dim2) {this.dim2 = dim2;}
    public void setBottom(int bottom) {this.bottom = bottom;}
    
    public void setJumpAbility(boolean jumpAbility) {this.jumpAbility = jumpAbility;}
    public void setVisibleTest(boolean visibleTest) {this.visibleTest = visibleTest;}
    public void setJump(Movement jump) {this.jump = jump;}
    
}
