import game.framework.*;
import java.util.*;
import java.awt.*;

public class MyGame {
    public static void main(String[] args) {
        GameContext ctx = new GameContext("Minesweeper Adventure", 1080, 720, Color.white);
        Game gameEngine = new Game(ctx);
        ArrayList<Role> myroles = new ArrayList<>();

        ImageSequence[][] is = {
            { new ImageSequence("src/stop/", "png", 1) },
            { new ImageSequence("src/walk_right/", "png", 9),
              new ImageSequence("src/walk_left/", "png", 9),
              new ImageSequence("src/walk_up/", "png", 9),
              new ImageSequence("src/walk_down/", "png", 9) },
            { new ImageSequence("src/fly_right/", "png", 1),
              new ImageSequence("src/fly_left/", "png", 1) },
            { new ImageSequence("src/special_move/", "png", 6) }
        };

        // 1. 建立初始地圖（設定為大世界 4x4 的區塊，每格 64 像素）
        MineBoard board = new MineBoard.Builder()
                        .setZoneDimensions(4, 4, 64)    
                        .setBoardOffset(0, 72)          
                        .setMinesPerZone(20)                   
                        .build();                                
        
        BoardRole boardRole = new BoardRole(board);
        myroles.add(boardRole);

        // 2. 建立玩家角色，寬高設定為符合格子的 64x64 像素大小
        MyRole player = new MyRole(200, 350, 64, 64, 0, -100, 400, is, board);
        
        // ⭐ 3. 雙向綁定設定：讓兩者可以互相通知重製與追蹤攝影機
        player.setBoardRole(boardRole);
        boardRole.setPlayer(player);
        
        myroles.add(player);
        gameEngine.registerKeyEventHandler(player);

        gameEngine.go(myroles);
    }
}