
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class QueueDirectory extends JDialog {

    private final DefaultListModel<String> sharedQueueModel;
    private final List<HalloweenQueueApp.QueueEntry> queueEntries;

    // Building labels exactly as displayed
    private final List<String> leftColumn = Arrays.asList(
            "House No. 428",
            "House No. 144",
            "House No. 467",
            "House No. 8",
            "House No. 128"
    );
    private final List<String> rightColumn = Arrays.asList(
            "House No. 67",
            "House No. 15",
            "House No. 1",
            "Mansion No. 2",
            "Mansion No. 4"
    );

    // Available totals per building
    private final Map<String, Integer> availableTotals = new LinkedHashMap<>();

    // Candy display names (plural forms)
    private final Map<String, String> candyPluralNames = new LinkedHashMap<>();

    public QueueDirectory(JFrame owner,
                          DefaultListModel<String> sharedQueueModel,
                          List<HalloweenQueueApp.QueueEntry> queueEntries) {
        super(owner, "Queues Directory", true);
        this.sharedQueueModel = sharedQueueModel;
        this.queueEntries = queueEntries;
        initTotals();
        initCandyNames();
        initUI();
    }

    private void initTotals() {
        availableTotals.put("House No. 428", 100);
        availableTotals.put("House No. 144", 110);
        availableTotals.put("House No. 467", 240);
        availableTotals.put("House No. 8",   120);
        availableTotals.put("House No. 128", 105);
        availableTotals.put("House No. 67",  267);
        availableTotals.put("House No. 15",  67);
        availableTotals.put("House No. 1",   50);
        availableTotals.put("Mansion No. 2", 550);
        availableTotals.put("Mansion No. 4", 500);
    }

    private void initCandyNames() {
        candyPluralNames.put("House No. 428", "Rare Candies");
        candyPluralNames.put("House No. 144", "Reese's Cups");
        candyPluralNames.put("House No. 467", "M&Ms");
        candyPluralNames.put("House No. 8",   "Skittles");
        candyPluralNames.put("House No. 128", "Candy Canes");
        candyPluralNames.put("House No. 67",  "Twix");
        candyPluralNames.put("House No. 15",  "Kitkats");
        candyPluralNames.put("House No. 1",   "Candy Corn");
        candyPluralNames.put("Mansion No. 2", "Sour Candies");
        candyPluralNames.put("Mansion No. 4", "Mint Candies");
    }



    private void initUI() {
        setSize(760, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Queues for Houses & Mansions");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(new EmptyBorder(12, 12, 0, 12));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        for (String label : leftColumn) {
            leftPanel.add(buildRow(label));
            leftPanel.add(Box.createVerticalStrut(8));
        }

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        for (String label : rightColumn) {
            rightPanel.add(buildRow(label));
            rightPanel.add(Box.createVerticalStrut(8));
        }

        gbc.gridx = 0; gbc.gridy = 0;
        content.add(leftPanel, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        content.add(rightPanel, gbc);

        add(content, BorderLayout.CENTER);

        // Bottom-right buttons: Claim + Exit
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton claimBtn = new JButton("Claim");
        JButton exitBtn  = new JButton("Exit");

        claimBtn.addActionListener(e -> openClaimSummary());
        exitBtn.addActionListener(e -> dispose());

        bottom.add(claimBtn);
        bottom.add(exitBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildRow(String buildingLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));

        JLabel lbl = new JLabel(buildingLabel);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        row.add(lbl, BorderLayout.WEST);

        JButton btn = new JButton("Check Queue");
        btn.setPreferredSize(new Dimension(130, 34));
        btn.addActionListener(e -> openDetail(buildingLabel));
        row.add(btn, BorderLayout.EAST);

        return row;
    }


    private void openDetail(String buildingLabel) {
        String type;
        int number;
        if (buildingLabel.startsWith("House No. ")) {
            type = "House";
            number = Integer.parseInt(buildingLabel.replace("House No. ", "").trim());
        } else {
            type = "Mansion";
            number = Integer.parseInt(buildingLabel.replace("Mansion No. ", "").trim());
        }

        JDialog dlg = new JDialog(this, buildingLabel + " — Queue Detail", true);
        dlg.setSize(640, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel(buildingLabel);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        main.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));

        int available = availableTotals.getOrDefault(buildingLabel, 0);
        String candyNamePlural = candyPluralNames.getOrDefault(buildingLabel, "Prizes");
        JLabel availableLbl = new JLabel("Available Prizes: " + available + " " + candyNamePlural);
        availableLbl.setFont(availableLbl.getFont().deriveFont(Font.PLAIN, 14f));
        center.add(availableLbl, BorderLayout.NORTH);

        JTextArea listArea = new JTextArea();
        listArea.setEditable(false);
        listArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        StringBuilder sb = new StringBuilder();
        sb.append("  List of Names\n\n");

        int index = 1;
        int totalClaimed = 0;

        for (HalloweenQueueApp.QueueEntry qe : queueEntries) {
            for (HalloweenQueueApp.Reward r : qe.rewards) {
                if (r.type.equals(type) && r.number == number) {
                    totalClaimed += r.amount;
                    sb.append(String.format("%2d. %s - %d (%s)\n",
                            index++,
                            qe.name,
                            r.amount,
                            qe.costumeSummary()
                    ));
                }
            }
        }

        if (index == 1) sb.append("    — (no claims yet)\n");

        listArea.setText(sb.toString());
        center.add(new JScrollPane(listArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        JLabel claimedLbl = new JLabel("Total Prizes Claimed: " + totalClaimed);
        claimedLbl.setFont(claimedLbl.getFont().deriveFont(Font.BOLD, 14f));
        int left = Math.max(0, available - totalClaimed);
        JLabel leftLbl = new JLabel("Total Prizes Left: " + left);
        leftLbl.setFont(leftLbl.getFont().deriveFont(Font.BOLD, 14f));
        stats.add(claimedLbl);
        stats.add(Box.createVerticalStrut(4));
        stats.add(leftLbl);

        bottom.add(stats, BorderLayout.WEST);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("Go Back");
        back.addActionListener(ev -> dlg.dispose());
        backPanel.add(back);
        bottom.add(backPanel, BorderLayout.EAST);

        center.add(bottom, BorderLayout.SOUTH);

        main.add(center, BorderLayout.CENTER);
        dlg.add(main, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    // Open the Claim summary window
    private void openClaimSummary() {
        ClaimSummary summary = new ClaimSummary(
                (JFrame) getOwner(),
                sharedQueueModel,
                queueEntries,
                availableTotals,
                candyPluralNames
        );
        summary.setVisible(true);
    }
}