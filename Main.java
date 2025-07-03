import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        
        JFrame f = new JFrame("Snake Game"); // Set title directly in constructor
        f.setBounds(10, 10, 905, 700);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GamePlay gameplay = new GamePlay();
        f.add(gameplay);
        
        f.setVisible(true);
        gameplay.requestFocusInWindow();
    }
}
