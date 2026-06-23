import game.framework.*;

/**
 * 統一管理所有音效與背景音樂
 * 各類別持有 SoundManager 參考，呼叫對應方法即可
 */
public class SoundManager {

    private GameMediaPlayer bgm;
    private GameMediaPlayer sfxGameOver;
    private GameMediaPlayer sfxGameWin;
    private GameMediaPlayer sfxMoney;
    private GameMediaPlayer sfxNo;
    private GameMediaPlayer sfxSkill;
    private GameMediaPlayer sfxBoom;

    public SoundManager() {
        bgm         = load("src/audio/BackgroundMusic.mp3");
        sfxGameOver = load("src/audio/GameOver.mp3");
        sfxGameWin  = load("src/audio/GameWin.mp3");
        sfxMoney    = load("src/audio/money.mp3");
        sfxNo       = load("src/audio/no.mp3");
        sfxSkill    = load("src/audio/SkillUse.mp3");
        sfxBoom     = load("src/audio/boom.mp3");
    }

    private GameMediaPlayer load(String path) {
        try {
            return new SpecialGameSoundManager().getPlayer(path);
        } catch (Exception e) {
            System.out.println("音效載入失敗：" + path);
            return null;
        }
    }

    private void play(GameMediaPlayer p) {
        if (p == null) return;
        p.rewind();
    }

    // -------------------------------------------------------
    // 背景音樂（循環播放）
    // -------------------------------------------------------
    public void playBGM() {
        if (bgm == null) return;
        bgm.rewind();
    }

    public void stopBGM() {
        if (bgm == null) return;
        bgm.stop();
    }

    // -------------------------------------------------------
    // 音效（單次播放）
    // -------------------------------------------------------
    public void playGameOver() { play(sfxGameOver); stopBGM(); }
    public void playGameWin()  { play(sfxGameWin);  stopBGM(); }
    public void playMoney()    { play(sfxMoney); }
    public void playNo()       { play(sfxNo); }
    public void playSkill()    { play(sfxSkill); }
    public void playBoom()     { play(sfxBoom); }

    // -------------------------------------------------------
    // 資源釋放
    // -------------------------------------------------------
    public void close() {
        closePlayer(bgm);
        closePlayer(sfxGameOver);
        closePlayer(sfxGameWin);
        closePlayer(sfxMoney);
        closePlayer(sfxNo);
        closePlayer(sfxSkill);
        closePlayer(sfxBoom);
    }

    private void closePlayer(GameMediaPlayer p) {
        if (p != null) p.close();
    }
}