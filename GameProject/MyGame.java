import game.framework.*;
import java.util.*;
import java.awt.*; //for Color.white
import java.awt.event.*;
public class MyGame {
    public static void main(String[] args) {
//Step 1. 產生遊戲背景物件: 標題, 寬度, 高度, 背景顏色
    GameContext ctx = new GameContext ("MY Warrior Game", 1080, 720, Color.white){
        
        /*@Override
        public String getBackgroundImgPath(){
            return "background.png";
        }
        
        @Override
        public String getBackgroundMusicPath(){
            return "Gameboy.mp3";
        }*/
    } ;
//Step 2. 產生遊戲物件 
    Game gameEngine = new Game(ctx); //Game就是遊戲引擎
//Step 3. 產生各種角色 (目前是空的)
    ArrayList<Role> myroles = new ArrayList<> (); //建立角色清單
    //[act][dir]: act:0 stop, act 1: walk, act 2: fly
    ImageSequence[][] is = {  { new ImageSequence("src/stop/" , "png", 1)},
                              { new ImageSequence("src/walk_right/" , "png", 9),
                                new ImageSequence("src/walk_left/" , "png", 9),
                                new ImageSequence("src/walk_up/" , "png", 9),
                                new ImageSequence("src/walk_down/" , "png", 9)}, 
                              { new ImageSequence("src/fly_right/" , "png", 1), 
                                new ImageSequence("src/fly_left/" , "png", 1) },
                              { new ImageSequence("src/special_move/" , "png", 6) }
                         };  //建立角色分鏡圖
        
    MyRole player = new MyRole(200, 350, 100, 100, 0, -100, 400, is);
    myroles.add(player ); 
    gameEngine.registerKeyEventHandler(player); //註冊接受鍵盤事件
    
//Step 4: 開始執行
    gameEngine.go(myroles);
}
}