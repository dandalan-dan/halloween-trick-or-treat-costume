
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ClaimSummary extends JDialog {

    private final DefaultListModel<String> sharedQueueModel;
    private final List<HalloweenQueueApp.QueueEntry> queueEntries;
    private final Map<String, Integer> availableTotals;
    private final Map<String, String> candyPluralNames;

    public ClaimSummary(JFrame owner,
                        DefaultListModel<String> sharedQueueModel,
                        List<HalloweenQueueApp.QueueEntry> queueEntries,
                        Map<String, Integer> availableTotals,
                        Map<String, String> candyPluralNames) {
        super(owner, "Claiming of Prizes — Summary", true);
        this.sharedQueueModel = sharedQueueModel;
        this.queueEntries = queueEntries;
        this.availableTotals = new LinkedHashMap<>(availableTotals);
        this.candyPluralNames = new LinkedHashMap<>(candyPluralNames);
        initUI();
    }

    private void initUI() {
        setSize(720, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header
        JLabel title = new JLabel("Claiming of Prizes");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        main.add(title, BorderLayout.NORTH);

        // Compute aggregates
        LinkedHashMap<String, Integer> personTotals = computePersonTotals();
        LinkedHashMap<String, Integer> buildingClaimed = computeBuildingClaimed();

        int overallClaimed = 0;
        for (int v : personTotals.values()) overallClaimed += v;

        // Center panel with two sections
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Section: Total Prizes Claimed per person
        JPanel claimedPanel = makeClaimedPanel(personTotals);
        center.add(claimedPanel, gbc);

        // Section: Total Prizes Left
        gbc.gridy++;
        JPanel leftPanel = makeLeftoverPanel(buildingClaimed);
        center.add(leftPanel, gbc);

        main.add(center, BorderLayout.CENTER);

        // Footer: overall total + buttons
        JPanel footer = new JPanel(new BorderLayout());

        JLabel overallLbl = new JLabel("Total Claimed Overall - " + overallClaimed + " Prizes Claimed");
        overallLbl.setFont(overallLbl.getFont().deriveFont(Font.BOLD, 15f));
        footer.add(overallLbl, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton checkQueueBtn = new JButton("Check Queue");
        JButton exitBtn       = new JButton("Exit");

        checkQueueBtn.addActionListener(e -> {
            // Go back to the directory
            dispose();
            QueueDirectory dir = new QueueDirectory((JFrame) getOwner(), sharedQueueModel, queueEntries);
            dir.setVisible(true);
        });
        exitBtn.addActionListener(e -> dispose());

        buttons.add(checkQueueBtn);
        buttons.add(exitBtn);
        footer.add(buttons, BorderLayout.EAST);

        main.add(footer, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
    }

    private JPanel makeClaimedPanel(LinkedHashMap<String, Integer> personTotals) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel hdr = new JLabel("  Total Prizes Claimed");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(hdr, BorderLayout.NORTH);

        // Prepare two columns if many names
        java.util.List<String> lines = new ArrayList<>();
        int idx = 1;
        for (Map.Entry<String, Integer> e : personTotals.entrySet()) {
            lines.add(String.format("%2d. %s - %d Prizes", idx++, e.getKey(), e.getValue()));
        }
        if (lines.isEmpty()) lines.add("    — (no claims yet)");

        int mid = (int) Math.ceil(lines.size() / 2.0);
        String leftText  = String.join("\n", lines.subList(0, mid));
        String rightText = String.join("\n", lines.subList(mid, lines.size()));

        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        JTextArea leftArea = new JTextArea(leftText);
        JTextArea rightArea = new JTextArea(rightText);
        for (JTextArea ta : new JTextArea[]{leftArea, rightArea}) {
            ta.setEditable(false);
            ta.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        }
        cols.add(new JScrollPane(leftArea));
        cols.add(new JScrollPane(rightArea));

        panel.add(cols, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeLeftoverPanel(LinkedHashMap<String, Integer> buildingClaimed) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel hdr = new JLabel("  Total Prizes Left");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(hdr, BorderLayout.NORTH);

        // Build lines per building
        java.util.List<String> lines = new ArrayList<>();
        for (String building : availableTotals.keySet()) {
            int available = availableTotals.getOrDefault(building, 0);
            int claimed   = buildingClaimed.getOrDefault(building, 0);
            int left      = Math.max(0, available - claimed);
            String candy  = candyPluralNames.getOrDefault(building, "Prizes");
            lines.add(String.format("%s - %d %s", building, left, candy));
        }

        int mid = (int) Math.ceil(lines.size() / 2.0);
        String leftText  = String.join("\n", lines.subList(0, mid));
        String rightText = String.join("\n", lines.subList(mid, lines.size()));

        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        JTextArea leftArea = new JTextArea(leftText);
        JTextArea rightArea = new JTextArea(rightText);
        for (JTextArea ta : new JTextArea[]{leftArea, rightArea}) {
            ta.setEditable(false);
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        }
        cols.add(new JScrollPane(leftArea));
        cols.add(new JScrollPane(rightArea));

        panel.add(cols, BorderLayout.CENTER);
        return panel;
    }

    private LinkedHashMap<String, Integer> computePersonTotals() {
        LinkedHashMap<String, Integer> totals = new LinkedHashMap<>();
        for (HalloweenQueueApp.QueueEntry qe : queueEntries) {
            int sum = totals.getOrDefault(qe.name, 0);
            for (HalloweenQueueApp.Reward r : qe.rewards) sum += r.amount;
            totals.put(qe.name, sum);
        }
        return totals;
    }

    private LinkedHashMap<String, Integer> computeBuildingClaimed() {
        LinkedHashMap<String, Integer> claimed = new LinkedHashMap<>();
        for (String b : availableTotals.keySet()) claimed.put(b, 0);

        for (HalloweenQueueApp.QueueEntry qe : queueEntries) {
            for (HalloweenQueueApp.Reward r : qe.rewards) {
                String label = r.type + " No. " + r.number;
                claimed.put(label, claimed.getOrDefault(label, 0) + r.amount);
            }
        }
        return claimed;
    }
}