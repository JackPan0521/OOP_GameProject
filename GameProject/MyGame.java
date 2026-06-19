import game.framework.*;
import java.util.*;
import java.awt.*;

public class MyGame {
    public static void main(String[] args) {
        GameContext ctx = new GameContext("Minesweeper Adventure", 1080, 720, Color.white);
        Game gameEngine = new Game(ctx);
        ArrayList<Role> myroles = new ArrayList<>();

        ImageSequence[][] is = {
            { new ImageSequence("src/stop/",       "png", 1) },
            { new ImageSequence("src/walk_right/", "png", 9),
              new ImageSequence("src/walk_left/",  "png", 9),
              new ImageSequence("src/walk_up/",    "png", 9),
              new ImageSequence("src/walk_down/",  "png", 9) },
            { new ImageSequence("src/fly_right/",  "png", 1),
              new ImageSequence("src/fly_left/",   "png", 1) },
            { new ImageSequence("src/special_move/", "png", 6) }
        };

        // 1. 建立初始地圖
        MineBoard board = new MineBoard.Builder()
                .setZoneDimensions(1, 1, 64)
                .setBoardOffset(0, 72)
                .setMinesPerZone(5)
                .build();

        // 2. BoardRole（地圖繪製）
        BoardRole boardRole = new BoardRole(board);
        myroles.add(boardRole);

        // 3. 玩家角色
        MyRole player = new MyRole(200, 350, 64, 64, 0, -100, 400, is, board);
        player.setBoardRole(boardRole);
        boardRole.setPlayer(player);
        myroles.add(player);

        // 4. 商店（加在最後，確保畫在最上層）
        ShopRole shop = new ShopRole(player.getPlayerData(), player);
        player.setShopRole(shop);
        myroles.add(shop);

        // 5. 註冊鍵盤事件（玩家 + 商店都需要）
        gameEngine.registerKeyEventHandler(player);
        gameEngine.registerKeyEventHandler(shop);

        gameEngine.go(myroles);
    }
}