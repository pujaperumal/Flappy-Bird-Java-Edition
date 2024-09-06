import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 660;
    int boardHeight = 600;

    // image
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // bird
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  //scaled by 1/6
    int pipeHeight = 512;

    class pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        pipe(Image img) {
            this.img = img;
        }
    }

    // game logic
    Bird bird;
    int velocityX = -4;  //move pipes to the left speed (simulates bird moving right)
    int velocityY = 0;   //move bird up/down speed.
    int gravity = 1;

    ArrayList<pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placepipesTimer;
    boolean gameOver = false;
    double score = 0;
    double highestScore = 0;

    JButton restartButton;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();  // Use a single background image
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<pipe>();

        // placepipe timer
        placepipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placepipes();
            }
        });
        placepipesTimer.start();

        // game timer
        //how long it takes to start timer, milliseconds gone between frames 
        gameLoop = new Timer(1000 / 60, this); // 1000/60 = 16.6ms for 60 FPS
        gameLoop.start();

        // restart button
        restartButton = new JButton("Restart");
        restartButton.setFocusable(false);
        restartButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 + 50, 100, 40);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        restartButton.setVisible(false);
        this.setLayout(null); // Absolute positioning
        this.add(restartButton);
    }

    public void placepipes() {
         //(0-1) * pipeHeight/2.
        // 0 -> -128 (pipeHeight/4)
        // 1 -> -128 - 256 (pipeHeight/4 - pipeHeight/2) = -3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        pipe toppipe = new pipe(topPipeImg);
        toppipe.y = randomPipeY;
        pipes.add(toppipe);

        pipe bottompipe = new pipe(bottomPipeImg);
        bottompipe.y = toppipe.y + pipeHeight + openingSpace;
        pipes.add(bottompipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Draw bird
        g.drawImage(bird.img, bird.x, bird.y, birdWidth, birdHeight, null);

        // Draw pipes
        for (int i = 0; i < pipes.size(); i++) {
            pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw score and high score
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        if (gameOver) {
            g.drawString("Game Over", 10, 35);
            g.drawString("Score: " + (int) score, 10, 80);
            g.drawString("High Score: " + (int) highestScore, 10, 125);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        if (!gameOver) {
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y, 0);

            // Move pipes
            for (int i = 0; i < pipes.size(); i++) {
                pipe pipe = pipes.get(i);
                pipe.x += velocityX;

                if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                    pipe.passed = true;
                    score += 0.5;
                }

                if (collision(bird, pipe)) {
                    gameOver = true;
                }
            }

            if (bird.y > boardHeight) {
                gameOver = true;
            }
        }
    }

    public boolean collision(Bird a, pipe b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
            }
    

    @Override
    public void actionPerformed(ActionEvent e) {  //called every x milliseconds by gameLoop timer
        if (!gameOver) {
            move();
            repaint();
        } else {
            if (score > highestScore) {
                highestScore = score;
            }
            placepipesTimer.stop();
            gameLoop.stop();
            restartButton.setVisible(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
            velocityY = -9; // Bird jumps on space bar press
        }
    }
 
    
    public void restartGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        gameLoop.start();
        placepipesTimer.start();
        restartButton.setVisible(false);
    }

    // not needed
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
