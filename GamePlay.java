import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.sound.sampled.*;

public class GamePlay extends JPanel implements KeyListener, ActionListener {

    private int[] snakexlength = new int[750];
    private int[] snakeylength = new int[750];
    private boolean up = false;
    private boolean down = false;
    private boolean right = false;
    private boolean left = false;
    private boolean inGame = true;
    private boolean isMoving = false;
    private boolean isPaused = false;
    private boolean showGameOverText = true;
    private Timer gameOverFlashTimer;
    private boolean showStartText = true;
    private Timer startTextFlashTimer;
    private int hungerTime = 20; // in seconds
    private Timer hungerTimer;
    private int hungerCounter = 20;
    private boolean showHungerWarning = false;
    private int[][] obstacles = new int[5][2]; // Up to 5 obstacles at a time
    private Timer obstacleTimer;
    private int obstacleChangeInterval = 5000; // Obstacles change every 5 seconds
    private int foodEatenSinceLastObstacleChange = 0;
    private int nextObstacleChangeAfter = 3; // Can be set randomly at start
    private ImageIcon upmouth, downmouth, rightmouth, leftmouth, snakeimage, enemyimage, titleImage;
    private Clip backgroundClip;

    private boolean showStartScreen = true;
    private boolean showCountdown = false;
    private int countdownValue = 3;
    private Timer countdownTimer;

    private Timer timer;
    private int delay = 100;
    private int score = 0;
    private int lengthofsnake = 3;

    private int[] enemyxpos = { 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350,
            375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675,
            700, 725, 750, 775, 800, 825, 850 };
    private int[] enemyypos = { 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
            425, 450, 475, 500, 525, 550, 575, 600, 625 };

    private Random random = new Random();
    private int xpos = random.nextInt(34);
    private int ypos = random.nextInt(23);

    public GamePlay() {

        setBackground(Color.DARK_GRAY);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(true);
        timer = new Timer(delay, this);
        timer.start();
        playBackgroundMusic();
        initializeGame();

    }

    private void playBackgroundMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem
                    .getAudioInputStream(getClass().getResource("/resources/gam.wav"));
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioInputStream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            boolean safePosition;
            int ox, oy;
            do {
                ox = enemyxpos[random.nextInt(34)];
                oy = enemyypos[random.nextInt(23)];

                // Check it doesn't overlap with food
                boolean onFood = (ox == enemyxpos[xpos] && oy == enemyypos[ypos]);

                // Check it doesn't spawn in front of initial snake direction
                boolean inFront = right && (ox == snakexlength[0] + 25) && (oy == snakeylength[0]);

                // Check it doesn't overlap with snake body
                boolean onSnake = false;
                for (int j = 0; j < lengthofsnake; j++) {
                    if (snakexlength[j] == ox && snakeylength[j] == oy) {
                        onSnake = true;
                        break;
                    }
                }

                safePosition = !onFood && !inFront && !onSnake;
            } while (!safePosition);

            obstacles[i][0] = ox;
            obstacles[i][1] = oy;
        }
    }

    private void playSound(String soundFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem
                    .getAudioInputStream(getClass().getResource("/resources/" + soundFile));

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void triggerGameOver() {
        inGame = false;
        isMoving = false;

        if (hungerTimer != null) {
            hungerTimer.stop();
        }

        playSound("gameover.wav");
        startGameOverAnimation();
    }

    private void initializeGame() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        if (gameOverFlashTimer != null) {
            gameOverFlashTimer.stop();
        }

        if (hungerTimer != null) {
            hungerTimer.stop();
        }

        score = 0;
        lengthofsnake = 3;
        right = false;
        left = false;
        up = false;
        down = false;
        inGame = true;
        isMoving = false;
        showStartScreen = true;
        showStartText = true;
        showHungerWarning = false; // Reset hunger warning flag
        generateObstacles();
        foodEatenSinceLastObstacleChange = 0;
        nextObstacleChangeAfter = random.nextInt(3) + 2; // 2 to 4 food pickups
        generateObstacles();

        if (obstacleTimer != null) {
            obstacleTimer.stop();
        }
        obstacleTimer = new Timer(obstacleChangeInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateObstacles();
                repaint();
            }
        });
        obstacleTimer.start();

        if (startTextFlashTimer != null) {
            startTextFlashTimer.stop();
        }

        startTextFlashTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStartText = !showStartText;
                repaint();
            }
        });
        startTextFlashTimer.start();

        showCountdown = false;
        countdownValue = 3;
        delay = 100;
        timer.setDelay(delay); // Reset timer speed to default

        snakexlength[0] = 100;
        snakexlength[1] = 75;
        snakexlength[2] = 50;

        snakeylength[0] = 100;
        snakeylength[1] = 100;
        snakeylength[2] = 100;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Title area border
        g.setColor(Color.CYAN); // Neon Blue
        g.drawRect(24, 10, 851, 55);

        // Gameplay area border
        g.setColor(Color.MAGENTA); // Neon Pink
        g.drawRect(24, 74, 851, 577);

        // Fill gameplay area
        g.setColor(Color.BLACK);
        g.fillRect(25, 75, 850, 575);

        titleImage = new ImageIcon("snaketitle.png");

        titleImage.paintIcon(this, g, 25, 11);

        // Draw Snake
        for (int a = 0; a < lengthofsnake; a++) {
            if (a == 0) {
                if (right) {
                    rightmouth = new ImageIcon("rightmouth.png");
                    rightmouth.paintIcon(this, g, snakexlength[a], snakeylength[a]);
                } else if (left) {
                    leftmouth = new ImageIcon("leftmouth.png");
                    leftmouth.paintIcon(this, g, snakexlength[a], snakeylength[a]);
                } else if (up) {
                    upmouth = new ImageIcon("upmouth.png");
                    upmouth.paintIcon(this, g, snakexlength[a], snakeylength[a]);
                } else if (down) {
                    downmouth = new ImageIcon("downmouth.png");
                    downmouth.paintIcon(this, g, snakexlength[a], snakeylength[a]);
                } else {
                    rightmouth = new ImageIcon("rightmouth.png"); // Default head
                    rightmouth.paintIcon(this, g, snakexlength[a], snakeylength[a]);
                }
            } else {
                snakeimage = new ImageIcon("snakeimage.png");
                snakeimage.paintIcon(this, g, snakexlength[a], snakeylength[a]);
            }
        }

        // Draw Enemy
        enemyimage = new ImageIcon("enemy.png");
        enemyimage.paintIcon(this, g, enemyxpos[xpos], enemyypos[ypos]);
        g.setColor(Color.PINK); // Neon Pink for obstacles
        for (int i = 0; i < obstacles.length; i++) {
            g.fillRect(obstacles[i][0], obstacles[i][1], 25, 25);
        }

        // Scoreboard (Smaller font neatly inside header)
        g.setColor(Color.YELLOW);
        g.setFont(new Font("arial", Font.PLAIN, 12));

        int textX = 720;
        int textY = 30;

        g.drawString("Scores : " + score, textX, textY);
        g.drawString("Length : " + lengthofsnake, textX, textY + 15);
        g.drawString("Speed : " + (100 - delay + 5), textX, textY + 30);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("arial", Font.PLAIN, 12));
        g.drawString("Hunger Timer: " + hungerCounter + "s", 35, textY + 15);

        if (showHungerWarning && inGame) {
            g.setColor(Color.RED);
            g.setFont(new Font("arial", Font.BOLD, 20));
            g.drawString("FANGS FAIL without FOOD!", 350, 110);
        }

        // Game Over Message
        if (!inGame) {

            // GAME OVER text
            if (showGameOverText) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("arial", Font.BOLD, 50));

                String gameOverText = "GAME OVER";
                FontMetrics metrics = g.getFontMetrics();
                int gameOverX = (getWidth() - metrics.stringWidth(gameOverText)) / 2;

                g.drawString(gameOverText, gameOverX, 300);
            }

            // PRESS Space to RESTART text
            g.setFont(new Font("arial", Font.BOLD, 20));
            String restartText = "PRESS (SpaceBar) to RESTART";
            FontMetrics restartMetrics = g.getFontMetrics();
            int restartX = (getWidth() - restartMetrics.stringWidth(restartText)) / 2;

            g.drawString(restartText, restartX, 340);
        }

        // Welcome Screen
        if (showStartScreen) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("arial", Font.BOLD, 40));
            String welcomeText = "WELCOME TO SNAKE GAME";
            FontMetrics welcomeMetrics = g.getFontMetrics();
            int welcomeX = (getWidth() - welcomeMetrics.stringWidth(welcomeText)) / 2;
            g.drawString(welcomeText, welcomeX, 200);

            g.setFont(new Font("arial", Font.PLAIN, 18));
            String rulesTitle = "RULES:";
            FontMetrics rulesTitleMetrics = g.getFontMetrics();
            int rulesTitleX = (getWidth() - rulesTitleMetrics.stringWidth(rulesTitle)) / 2;
            g.drawString(rulesTitle, rulesTitleX, 250);

            g.setFont(new Font("arial", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            g.drawString("- Use (Arrow Keys) to control the snake", 300, 280);
            g.drawString("- Eat the food to grow and score points", 300, 305);
            g.drawString("- The snake wraps around screen edges", 300, 330);
            g.drawString("- Avoid colliding with yourself", 300, 355);
            g.drawString("- Speed increases as you score", 300, 380);

            g.setColor(Color.RED);
            g.drawString("- Feed the snake before the Hunger Timer runs out", 300, 405);

            g.setColor(Color.WHITE);
            g.drawString("- Dodge the glowing obstacles or the snake dies", 300, 430);

            g.setColor(Color.RED);
            g.drawString("- Obstacles randomly change positions after random intervals", 300, 455);

            g.setColor(Color.WHITE);
            g.drawString("- Press (P) to Pause/Resume the game", 300, 480);

            if (showStartText) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("arial", Font.PLAIN, 20));
                String startText = "Press (ANY KEY) to Start";
                FontMetrics startMetrics = g.getFontMetrics();
                int startX = (getWidth() - startMetrics.stringWidth(startText)) / 2;
                g.drawString(startText, startX, 550);
            }
        }

        // Countdown Screen
        if (showCountdown) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("arial", Font.BOLD, 60));
            g.drawString("" + countdownValue, 420, 300);
        }
        if (isPaused) {
            g.setColor(Color.YELLOW);

            // Large "PAUSED" text
            g.setFont(new Font("arial", Font.BOLD, 40));
            String pausedText = "PAUSED";
            FontMetrics pausedMetrics = g.getFontMetrics();
            int pausedX = (getWidth() - pausedMetrics.stringWidth(pausedText)) / 2;
            g.drawString(pausedText, pausedX, 300);

            // Smaller instruction text below
            g.setFont(new Font("arial", Font.PLAIN, 20));
            String resumeText = "Press (P) again to RESUME";
            FontMetrics resumeMetrics = g.getFontMetrics();
            int resumeX = (getWidth() - resumeMetrics.stringWidth(resumeText)) / 2;
            g.drawString(resumeText, resumeX, 340);
        }

    }

    private void startCountdown() {
        showCountdown = true;
        countdownValue = 3;

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdownValue--;
                if (countdownValue == 0) {
                    showCountdown = false;
                    isMoving = true;
                    // Start moving right by default
                    right = true;
                    left = up = down = false;
                    startHungerTimer(); // Start hunger timer only after countdown finishes
                    countdownTimer.stop();
                }
                repaint();
            }
        });
        countdownTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame && isMoving && !showCountdown && !isPaused) {
            moveSnake();
            checkEnemy();
            checkCollision();
        }
        repaint();
    }

    private void moveSnake() {
        for (int i = lengthofsnake - 1; i > 0; i--) {
            snakexlength[i] = snakexlength[i - 1];
            snakeylength[i] = snakeylength[i - 1];
        }

        if (right) {
            snakexlength[0] += 25;
            if (snakexlength[0] > 850)
                snakexlength[0] = 25;
        } else if (left) {
            snakexlength[0] -= 25;
            if (snakexlength[0] < 25)
                snakexlength[0] = 850;
        } else if (up) {
            snakeylength[0] -= 25;
            if (snakeylength[0] < 75)
                snakeylength[0] = 625;
        } else if (down) {
            snakeylength[0] += 25;
            if (snakeylength[0] > 625)
                snakeylength[0] = 75;
        }
    }

    private void startHungerTimer() {
        if (hungerTimer != null) {
            hungerTimer.stop();
        }

        hungerTime = Math.max(3, 20 - (100 - delay) / 5); // 20 seconds reduced as speed increases, min 3 sec
        hungerCounter = hungerTime;
        showHungerWarning = false;

        hungerTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hungerCounter--;
                if (hungerCounter <= 3) {
                    showHungerWarning = true;
                }
                if (hungerCounter <= 0) {
                    triggerGameOver();
                }

                repaint();
            }
        });
        hungerTimer.start();
    }

    private void resetHungerTimer() {
        hungerTimer.stop();
        startHungerTimer();
    }

    private void checkEnemy() {
        if (snakexlength[0] == enemyxpos[xpos] && snakeylength[0] == enemyypos[ypos]) {
            lengthofsnake++;
            score++;
            playSound("eat.wav");
            resetHungerTimer();

            // Initialize new segment position to avoid flickering
            snakexlength[lengthofsnake - 1] = snakexlength[lengthofsnake - 2];
            snakeylength[lengthofsnake - 1] = snakeylength[lengthofsnake - 2];

            // New food position
            xpos = random.nextInt(34);
            ypos = random.nextInt(23);

            // Speed increase logic
            if (delay > 30) {
                delay -= 5;
                timer.setDelay(delay);
            }

            // Count food eaten & handle obstacle relocation
            foodEatenSinceLastObstacleChange++;
            if (foodEatenSinceLastObstacleChange >= nextObstacleChangeAfter) {
                generateObstacles(); // You should define this method to reposition obstacles
                foodEatenSinceLastObstacleChange = 0;
                nextObstacleChangeAfter = random.nextInt(3) + 2;
            }
        }
    }

    private void checkCollision() {
        // Self-collision
        for (int i = 1; i < lengthofsnake; i++) {
            if (snakexlength[i] == snakexlength[0] && snakeylength[i] == snakeylength[0]) {
                triggerGameOver();
                return;
            }
        }

        // Collision with obstacles
        for (int i = 0; i < obstacles.length; i++) {
            if (snakexlength[0] == obstacles[i][0] && snakeylength[0] == obstacles[i][1]) {
                triggerGameOver();
                return;
            }
        }
    }

    private void startGameOverAnimation() {
        if (gameOverFlashTimer != null) {
            gameOverFlashTimer.stop();
        }

        showGameOverText = true;
        gameOverFlashTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGameOverText = !showGameOverText;
                repaint();
            }
        });
        gameOverFlashTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (showStartScreen) {
            showStartScreen = false;
            startCountdown();
            if (startTextFlashTimer != null) {
                startTextFlashTimer.stop();
            }

            repaint();
            return;
        }
        if (inGame && !showCountdown && e.getKeyCode() == KeyEvent.VK_P) {
            isPaused = !isPaused;

            if (hungerTimer != null) {
                if (isPaused) {
                    hungerTimer.stop();
                } else {
                    hungerTimer.start();
                }
            }
        }

        if (inGame) {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT && !left) {
                right = true;
                left = up = down = false;
                isMoving = true;
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT && !right) {
                left = true;
                right = up = down = false;
                isMoving = true;
            } else if (e.getKeyCode() == KeyEvent.VK_UP && !down) {
                up = true;
                down = left = right = false;
                isMoving = true;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN && !up) {
                down = true;
                up = left = right = false;
                isMoving = true;
            }
        } else {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                initializeGame();
                repaint();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}