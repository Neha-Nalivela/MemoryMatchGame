import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;  // This is the correct List
import java.io.*;
import java.util.*;

public class MemoryMatchGame extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final int SIZE = 4;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private String[] values;
    private JButton firstButton = null, secondButton = null;
    private Timer timer;
    private int matchedPairs = 0;
    private int moves = 0;
    private JLabel statusLabel;

    public MemoryMatchGame() {
        setTitle("Memory Match Game");
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        values = generateShuffledPairs(SIZE * SIZE);

        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton btn = new JButton("");
                btn.setFont(new Font("Arial", Font.BOLD, 24));
                btn.addActionListener(this);
                btn.putClientProperty("value", values[index++]);
                buttons[i][j] = btn;
                gridPanel.add(btn);
            }
        }

        statusLabel = new JLabel("Moves: 0", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        add(gridPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private String[] generateShuffledPairs(int totalCards) {
        String[] items = new String[totalCards];
        for (int i = 0; i < totalCards / 2; i++) {
            String val = String.valueOf(i + 1);
            items[i * 2] = val;
            items[i * 2 + 1] = val;
        }
        List<String> list = Arrays.asList(items);
        Collections.shuffle(list);
        return list.toArray(new String[0]);
    }

    public void actionPerformed(ActionEvent e) {
        JButton clicked = (JButton) e.getSource();
        if (clicked.getText().equals("") && secondButton == null) {
            clicked.setText((String) clicked.getClientProperty("value"));
            if (firstButton == null) {
                firstButton = clicked;
            } else {
                secondButton = clicked;
                moves++;
                statusLabel.setText("Moves: " + moves);
                checkForMatch();
            }
        }
    }

    private void checkForMatch() {
        String val1 = (String) firstButton.getClientProperty("value");
        String val2 = (String) secondButton.getClientProperty("value");

        if (val1.equals(val2)) {
            firstButton.setEnabled(false);
            secondButton.setEnabled(false);
            matchedPairs++;
            firstButton = null;
            secondButton = null;

            if (matchedPairs == (SIZE * SIZE) / 2) {
                handleWin();
            }
        } else {
            timer = new Timer(800, e -> {
                firstButton.setText("");
                secondButton.setText("");
                firstButton = null;
                secondButton = null;
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void handleWin() {
        String name = JOptionPane.showInputDialog(this, "🎉 You Win in " + moves + " moves!\nEnter your name for the leaderboard:");
        if (name != null && !name.trim().isEmpty()) {
            saveToLeaderboard(name.trim(), moves);
        }

        List<LeaderboardEntry> topPlayers = loadLeaderboard();
        StringBuilder sb = new StringBuilder("🏆 Leaderboard:\n");
        for (int i = 0; i < Math.min(5, topPlayers.size()); i++) {
            sb.append((i + 1)).append(". ").append(topPlayers.get(i)).append("\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString());
        System.exit(0); // Exit after game ends
    }

    private void saveToLeaderboard(String name, int moves) {
        try (FileWriter fw = new FileWriter("leaderboard.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(name + "," + moves);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<LeaderboardEntry> loadLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("leaderboard.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    entries.add(new LeaderboardEntry(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(entries);
        return entries;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MemoryMatchGame::new);
    }
}

class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    String name;
    int moves;

    LeaderboardEntry(String name, int moves) {
        this.name = name;
        this.moves = moves;
    }

    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(this.moves, other.moves);
    }

    public String toString() {
        return name + " - " + moves + " moves";
    }
}
