import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class MineBoard{
    private int BOARD_W = 1080;
    private int BOARD_H = 648;
    private final int TILE = 108;
    private final int COLS = BOARD_W/TILE;
    private final int ROWS = BOARD_H/TILE;

    private final int boardLeft = 0;
    private final int boardTop = 72;

    private Image unopenTile;

    public MineBoard(){
        try{
            unopenTile = ImageIO.read(new File("src/minesweeper icon/unopen.png"));
        }catch(Exception e){
            e.printStackTrace();
            unopenTile = null;
        }
    }

    public void draw(Graphics g) {
        if (unopenTile == null) return;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = boardLeft + c * TILE;
                int y = boardTop + r * TILE;
                g.drawImage(unopenTile, x, y, TILE, TILE, null);
            }
        }
    }
}