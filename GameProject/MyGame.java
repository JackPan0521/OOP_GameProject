import game.framework.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

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

        MineBoard board = new MineBoard();
        myroles.add(new BoardRole(board));

        MyRole player = new MyRole(200, 350, 100, 100, 0, -100, 400, is, board);
        myroles.add(player);
        gameEngine.registerKeyEventHandler(player);

        gameEngine.go(myroles);
    }
}