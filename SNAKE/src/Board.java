import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

public class Board extends JPanel implements ActionListener {

    private final int B_WIDTH = 900;
    private final int B_HEIGHT = 900;
    private final int DOT_SIZE = 30;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 20;
    private final int DELAY = 100;
    private int score;
    public static int bestScore;
    private final int[] x = new int[ALL_DOTS];
    private final int[] y = new int[ALL_DOTS];
    private int dots;
    private int apple_x;
    private int apple_y;
    private int blueApple_x;
    private int blueApple_y;
    private int orange_x;
    private int orange_y;
    private int poison_x;
    private int posion_y;
    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true;
    private Timer timer;
    private Image ball;
    private Image apple;
    private Image blueApple;
    private Image orange;
    private Image head;
    private Image poison;
    private int bytesRead;
    private GameStatus status;
    private JButton easyButton, mediumButton, hardButton;
    private Timer easyTimer, mediumTimer, hardTimer;

    private static Font FONT_S = new Font("MV Boli", 2, 18);
    private static Font FONT_M = new Font("MV Boli", 0, 24);
    private static Font FONT_M_ITALIC = new Font("MV Boli", 2, 24);
    private static Font FONT_L = new Font("MV Boli", 0, 84);
    private static Font FONT_XL = new Font("MV Boli", 0, 150);
    private static Font FONT_SCORE = new Font("MV Boli", 1, 20);

    enum GameStatus {
        NOT_STARTED,
        RUNNING,
        GAME_OVER;
    }

    public Board() {
        this.initBoard();
        this.initTimers();
    }
    private void initBoard() {
        this.addKeyListener(new TAdapter());
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(B_WIDTH, B_WIDTH));
        this.loadImages();
        this.initGame();
        this.status = Board.GameStatus.NOT_STARTED;
        this.addStartScreenButtons();
    }
    private void initTimers() {
        easyTimer = new Timer(DELAY * 2, this);
        mediumTimer = new Timer(DELAY, this);
        hardTimer = new Timer(DELAY / 2, this);

        timer.start();
    }

    public void startScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.setFont(FONT_M);
        if (this.status == Board.GameStatus.NOT_STARTED) {
            this.drawCenteredString(g2d, "SNAKE", FONT_XL, 200);
            this.drawCenteredString(g2d, "GAME", FONT_XL, 330);
            this.drawCenteredString(g2d, "Select difficulty mode and press any key to start", FONT_M_ITALIC, 380);

            g2d.setFont(FONT_S);
            g2d.setColor(Color.BLACK);
            this.drawCenteredString(g2d, "When you eat a red apple, the tail shortens by 1 and you lose 10 points.", FONT_S, 430);
            this.drawCenteredString(g2d, "When you eat an orange, the tail grows by 1 and you get 10 points.", FONT_S, 450);
            this.drawCenteredString(g2d, "When you eat a blue apple, the tail grows by 3 and you get 30 points.", FONT_S, 470);
            this.drawCenteredString(g2d, "Drink poison or hit its tail you die!", FONT_S, 490);
        }
    }

    private void drawCenteredString(Graphics g, String text, Font font, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (900 - metrics.stringWidth(text)) / 2;
        g.setFont(font);
        g.drawString(text, x, y);
    }
    private void addStartScreenButtons() {

        if (this.status == Board.GameStatus.NOT_STARTED) {

            easyButton = new JButton("Easy");
            mediumButton = new JButton("Medium");
            hardButton = new JButton("Hard");

            easyButton.addActionListener(e -> setDifficulty(easyTimer)); 
            mediumButton.addActionListener(e -> setDifficulty(mediumTimer));
            hardButton.addActionListener(e -> setDifficulty(hardTimer));

            easyButton.setBounds(B_WIDTH / 2 - 250, B_HEIGHT / 2 + 200, 150, 50);
            mediumButton.setBounds(B_WIDTH / 2 - 75, B_HEIGHT / 2 + 200, 150, 50);
            hardButton.setBounds(B_WIDTH / 2 + 100, B_HEIGHT / 2 + 200, 150, 50);

            setButtonProperties(easyButton);
            setButtonProperties(mediumButton);
            setButtonProperties(hardButton);

            add(easyButton);
            add(mediumButton);
            add(hardButton);
        }
    }

    private void setButtonProperties(JButton button) {
        Color buttonTextColor = new Color(130, 205, 71);
        Font buttonFont = new Font("MV Boli", Font.PLAIN, 18);

        button.setBackground(Color.BLACK);
        button.setForeground(buttonTextColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFont(buttonFont);
        button.setFocusable(false);
    }
    private void removeStartScreenButtons() {
        remove(easyButton);
        remove(mediumButton);
        remove(hardButton);
        repaint();
    }
    private void setDifficulty(Timer selectedTimer) {
        timer.stop();
        timer = selectedTimer;
        timer.start();
        removeStartScreenButtons();
    }

    private void initGame() {
        this.score = 0;
        this.dots = 4;

        for (int z = 0; z < this.dots; ++z) {
            this.x[z] = 50 - z * 10;
            this.y[z] = 50;
        }

        this.locateApple();
        this.locateBlueApple();
        this.locateOrange();
        this.locatePoison();

        Timer blueAppleTimer = new Timer(4000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Board.this.inGame) {
                    Board.this.locateBlueApple();
                }

            }
        });
        blueAppleTimer.start();

        Timer appleTimer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Board.this.inGame) {
                    Board.this.locateApple();
                }

            }
        });
        appleTimer.start();

        Timer poisonTimer = new Timer(4500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Board.this.inGame) {
                    Board.this.locatePoison();
                }

            }
        });
        poisonTimer.start();

        this.timer = new Timer(DELAY, this);
        this.timer.stop();
    }

    public void actionPerformed(ActionEvent e) {
        if (this.status == Board.GameStatus.RUNNING) {
            this.checkApple();
            this.checkBlueApple();
            this.checkOrange();
            this.checkPoison();
            this.checkCollision();
            this.move();
        }
        this.repaint();
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(130, 205, 71));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (this.status == Board.GameStatus.RUNNING) {
            this.doDrawing(g);
        } else if (this.status == Board.GameStatus.GAME_OVER) {
            this.gameOverScreen(g);
        } else {
            this.startScreen(g);
        }

    }
    private void doDrawing(Graphics g) {
        if (this.inGame) {
            g.drawImage(this.apple, this.apple_x, this.apple_y, this);
            g.drawImage(this.blueApple, this.blueApple_x, this.blueApple_y, this);
            g.drawImage(this.orange, this.orange_x, this.orange_y, this);
            g.drawImage(this.poison, this.poison_x, this.posion_y, this);

            String scr = "Score: " + this.score;
            FontMetrics metr = this.getFontMetrics(FONT_SCORE);;
            g.setColor(Color.black);
            g.setFont(FONT_SCORE);
            g.drawString(scr, B_WIDTH - metr.stringWidth(scr) - 5, 20);


            String bestScr = "Best Score: " + this.bestScore;
            FontMetrics metr1 = this.getFontMetrics(FONT_SCORE);
            g.setColor(Color.black);
            g.setFont(FONT_SCORE);
            g.drawString(bestScr, 0, 20);

            if (score>bestScore){
                bestScore=score;
            }

            for (int z = 0; z < this.dots; ++z) {
                if (z == 0) {
                    g.drawImage(this.head, this.x[z], this.y[z], this);
                } else {
                    g.drawImage(this.ball, this.x[z], this.y[z], this);
                }
            }

            Toolkit.getDefaultToolkit().sync();
        } else {
            this.gameOverScreen(g);
        }

    }

    private void checkCollision() {
        for (int z = this.dots; z > 0; --z) {
            if (z > 4 && this.x[0] == this.x[z] && this.y[0] == this.y[z]) {
                this.inGame = false;
                this.playSound("/Users/seyma/Desktop/SNAKE/resources/poison.wav");
            }
        }

        if (this.y[0] >= B_HEIGHT) {
            this.y[0] = 0;
        }

        if (this.y[0] < 0) {
            this.y[0] = B_HEIGHT;
        }

        if (this.x[0] >= B_WIDTH) {
            this.x[0] = 0;
        }

        if (this.x[0] < 0) {
            this.x[0] = B_WIDTH;
        }

        if (!this.inGame) {
            this.timer.stop();
            this.status = Board.GameStatus.GAME_OVER;
        }

    }
    private void move() {
        for (int z = this.dots; z > 0; --z) {
            this.x[z] = this.x[z - 1];
            this.y[z] = this.y[z - 1];
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            x[0] += DOT_SIZE;
        }

        if (upDirection) {
            y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            y[0] += DOT_SIZE;
        }

    }
    private void playSound(String s) {
        try {
            File soundFile = new File(s);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioIn.getFormat();
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            byte[] buffer = new byte[1024];
            (new Thread(() -> {
                while (true) {
                    try {
                        if ((this.bytesRead = audioIn.read(buffer, 0, buffer.length)) != -1) {
                            line.write(buffer, 0, this.bytesRead);
                            continue;
                        }

                        line.drain();
                        line.close();
                        audioIn.close();
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }

                    return;
                }
            })).start();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    private void loadImages() {
        ImageIcon iid = new ImageIcon("/Users/seyma/Desktop/SNAKE/resources/dot.png");
        this.ball = iid.getImage();
        ImageIcon iia = new ImageIcon("/Users/seyma/Desktop/SNAKE/resources/apple.png");
        this.apple = iia.getImage();
        ImageIcon iib = new ImageIcon("/Users/seyma/Desktop/SNAKE/resources/blueApple.png");
        this.blueApple = iib.getImage();
        ImageIcon iio = new ImageIcon("/Users/seyma/Desktop/SNAKE/resources/orange.png");
        this.orange = iio.getImage();
        ImageIcon iih = new ImageIcon("/Users/seyma/Desktop/SNAKE/resources/head.png");
        this.head = iih.getImage();
        ImageIcon iip = new ImageIcon("/Users/seyma/Desktop/SNAKE/resources/poison.png");
        this.poison = iip.getImage();
    }

    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        this.apple_x = r * DOT_SIZE;
        r = (int) (Math.random() * RAND_POS);
        this.apple_y = r * DOT_SIZE;
    }

    private void locateBlueApple() {
        int n = (int) (Math.random() * RAND_POS);
        this.blueApple_x = n * DOT_SIZE;
        n = (int) (Math.random() * RAND_POS);
        this.blueApple_y = n * DOT_SIZE;
    }

    private void locateOrange() {
        int n = (int) (Math.random() * RAND_POS);
        this.orange_x = n * DOT_SIZE;
        n = (int) (Math.random() * RAND_POS);
        this.orange_y = n * DOT_SIZE;
    }

    private void locatePoison() {
        int n = (int) (Math.random() * RAND_POS);
        this.poison_x = n * DOT_SIZE;
        n = (int) (Math.random() * RAND_POS);
        this.posion_y = n * DOT_SIZE;
    }
    private void checkApple() {
        if (Math.abs(this.x[0] - this.apple_x) < DOT_SIZE && Math.abs(this.y[0] - this.apple_y) < DOT_SIZE) {
            --this.dots;
            this.score -= 10;
            this.locateApple();
            this.playSound("/Users/seyma/Desktop/SNAKE/resources/eat.wav");
            if (dots == 0){
                status = GameStatus.GAME_OVER;
                inGame = false;
                this.playSound("/Users/seyma/Desktop/SNAKE/resources/poison.wav");
                timer.stop();}
        }

    }

    private void checkBlueApple() {
        if (Math.abs(this.x[0] - this.blueApple_x) < DOT_SIZE && Math.abs(this.y[0] - this.blueApple_y) < DOT_SIZE) {
            this.dots += 3;
            this.score += 30;
            this.locateBlueApple();
            this.playSound("/Users/seyma/Desktop/SNAKE/resources/eat.wav");
        }

    }

    private void checkOrange() {
        if (Math.abs(this.x[0] - this.orange_x) < DOT_SIZE && Math.abs(this.y[0] - this.orange_y) < DOT_SIZE) {
            ++this.dots;
            this.score += 10;
            this.locateOrange();
            this.playSound("/Users/seyma/Desktop/SNAKE/resources/eat.wav");
        }

    }

    private void checkPoison() {
        if (Math.abs(this.x[0] - this.poison_x) < DOT_SIZE && Math.abs(this.y[0] - this.posion_y) < DOT_SIZE) {
            this.inGame = false;
            this.playSound("/Users/seyma/Desktop/SNAKE/resources/poison.wav");
        }

    }
    public void gameOverScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setFont(FONT_M);
        if (this.status == Board.GameStatus.GAME_OVER) {
            this.drawCenteredString(g2d, "GAME OVER", FONT_XL, 200);
            this.drawCenteredString(g2d, "Score: " + this.score, FONT_L, 300);
            this.drawCenteredString(g2d, "Press R to play again ", FONT_M_ITALIC, 360);
            this.drawCenteredString(g2d, "Press Q to exit ", FONT_M_ITALIC, 400);

        }

    }

      private class TAdapter extends KeyAdapter {
        private Board board;

        private TAdapter() {
        }

        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (Board.this.status == Board.GameStatus.NOT_STARTED) {
                Board.this.status = Board.GameStatus.RUNNING;
                Board.this.timer.start();
                removeStartScreenButtons();
            }
            else if (Board.this.status == Board.GameStatus.RUNNING) {
                if (key == 37 && !Board.this.rightDirection) {
                    Board.this.leftDirection = true;
                    Board.this.upDirection = false;
                    Board.this.downDirection = false;
                } else if (key == 39 && !Board.this.leftDirection) {
                    Board.this.rightDirection = true;
                    Board.this.upDirection = false;
                    Board.this.downDirection = false;
                } else if (key == 38 && !Board.this.downDirection) {
                    Board.this.upDirection = true;
                    Board.this.rightDirection = false;
                    Board.this.leftDirection = false;
                } else if (key == 40 && !Board.this.upDirection) {
                    Board.this.downDirection = true;
                    Board.this.rightDirection = false;
                    Board.this.leftDirection = false;
                }
            }
            else if (Board.this.status == Board.GameStatus.GAME_OVER && key == 81) {
                System.exit(0);
            }
            else if (Board.this.status == Board.GameStatus.GAME_OVER && key == 82) {
                Snake snakeGame = new Snake();
                snakeGame.setVisible(true);


                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(Board.this);
                currentFrame.dispose();

            }
        }
    }
}
