
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class HalloweenQueueApp extends JFrame {

    private JTextField nameField;
    private JFormattedTextField rateField;
    private JTextArea rewardsArea;
    private JButton addButton;
    private JButton checkQueueButton;
    private JButton finishButton;

    private JComboBox<String> hatCombo;
    private JComboBox<String> outfitCombo;
    private JComboBox<String> bootsCombo;

    private JLabel hatImageLabel;
    private JLabel outfitImageLabel;
    private JLabel bootsImageLabel;

    private DefaultListModel<String> queueModel;
    private final List<QueueEntry> queueEntries = new ArrayList<>();

    private final Random rng = new Random();

    private final int[] houseNumbers = {428, 144, 467, 8, 128, 67, 15, 1};
    private final int[] mansionNumbers = {2, 4};

    private final Map<String, int[]> prizeRanges = new LinkedHashMap<>();
    private List<Reward> lastGeneratedRewards = null;

    private static final String ASSETS_DIR = "src";
    private static final String[] EXTENSIONS = {"png", "jpg", "jpeg", "gif"};

    private static final String[] HATS = {
            "hat_gnome", "hat_pirate", "hat_pumpkin", "hat_witch"
    };
    private static final String[] OUTFITS = {
            "clothes_witch", "clothes_death", "clothes_zombie",
            "clothes_ghost", "clothes_vampire", "clothes_pumpkin"
    };
    private static final String[] BOOTS = {
            "boots_witch", "boots_yellow", "boots_pumpkin", "boots_zombie"
    };

    // Per-item rate ranges (inclusive) used to compute the whole costume rate
    private static final Map<String, int[]> HAT_RATE_RANGES = new LinkedHashMap<>();
    private static final Map<String, int[]> OUTFIT_RATE_RANGES = new LinkedHashMap<>();
    private static final Map<String, int[]> BOOTS_RATE_RANGES = new LinkedHashMap<>();
    static {
        HAT_RATE_RANGES.put("hat_gnome",   new int[]{1, 4});
        HAT_RATE_RANGES.put("hat_pirate",  new int[]{3, 6});
        HAT_RATE_RANGES.put("hat_pumpkin", new int[]{2, 5});
        HAT_RATE_RANGES.put("hat_witch",   new int[]{4, 7});

        OUTFIT_RATE_RANGES.put("clothes_pumpkin", new int[]{3, 6});
        OUTFIT_RATE_RANGES.put("clothes_zombie",  new int[]{4, 7});
        OUTFIT_RATE_RANGES.put("clothes_ghost",   new int[]{5, 8});
        OUTFIT_RATE_RANGES.put("clothes_witch",   new int[]{6, 9});
        OUTFIT_RATE_RANGES.put("clothes_vampire", new int[]{7, 9});
        OUTFIT_RATE_RANGES.put("clothes_death",   new int[]{8, 10}); // allows 10

        BOOTS_RATE_RANGES.put("boots_yellow",  new int[]{1, 2});
        BOOTS_RATE_RANGES.put("boots_pumpkin", new int[]{1, 3});
        BOOTS_RATE_RANGES.put("boots_zombie",  new int[]{2, 4});
        BOOTS_RATE_RANGES.put("boots_witch",   new int[]{3, 5});
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new HalloweenQueueApp().setVisible(true);
        });
    }

    public HalloweenQueueApp() {
        super("Halloween Costume Trick or Treat");
        initPrizeRanges();
        initUI();
    }

    private static void setSystemLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private String key(String type, int number) {
        return type + ":" + number;
    }
    private void putRange(String type, int number, int min, int max) {
        prizeRanges.put(key(type, number), new int[]{min, max});
    }
    private void initPrizeRanges() {
        // Houses
        putRange("House", 428, 3, 7);
        putRange("House", 144, 9, 13);
        putRange("House", 467, 21, 27);
        putRange("House",   8, 16, 18);
        putRange("House", 128, 9, 10);
        putRange("House",  67, 21, 41);
        putRange("House",  15, 5, 8);
        putRange("House",   1, 5, 5); // fixed
        // Mansions
        putRange("Mansion", 2, 45, 65);
        putRange("Mansion", 4, 40, 50);
    }


    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));



        // Target your screenshot window size
        Dimension target = new Dimension(743, 484);
        setPreferredSize(target);
        setLayout(new BorderLayout(10, 10));

        // ==== Left: Title + Form + Rewards ====
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Halloween Costume Trick or Treat");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        leftPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        nameField = new JTextField(16);
        nameField.setFont(nameField.getFont().deriveFont(16f));
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Costume Rate:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        rateField = new JFormattedTextField();
        rateField.setColumns(4);
        rateField.setEditable(false);
        rateField.setHorizontalAlignment(SwingConstants.CENTER);
        rateField.setFont(rateField.getFont().deriveFont(Font.BOLD, 18f));
        rateField.setBorder(new LineBorder(Color.GRAY, 1, true));
        formPanel.add(rateField, gbc);

        leftPanel.add(formPanel, BorderLayout.CENTER);

        JPanel rewardsPanel = new JPanel(new BorderLayout(6, 6));
        JLabel rewardsLabel = new JLabel("Rewards");
        rewardsLabel.setFont(rewardsLabel.getFont().deriveFont(Font.BOLD, 18f));
        rewardsPanel.add(rewardsLabel, BorderLayout.NORTH);

        rewardsArea = new JTextArea(10, 24);  // compact columns
        rewardsArea.setEditable(false);
        rewardsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        rewardsArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        rewardsPanel.add(new JScrollPane(rewardsArea), BorderLayout.CENTER);

        leftPanel.add(rewardsPanel, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.WEST);

        // ==== Center: selectors, previews, queue, buttons ====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel selectors = new JPanel(new GridLayout(1, 3, 12, 0));
        selectors.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Choose Costume Items"));
        hatCombo = new JComboBox<>(prettify(HATS));
        outfitCombo = new JComboBox<>(prettify(OUTFITS));
        bootsCombo = new JComboBox<>(prettify(BOOTS));
        selectors.add(labeled("Hat", hatCombo));
        selectors.add(labeled("Outfit", outfitCombo));
        selectors.add(labeled("Boots", bootsCombo));
        centerPanel.add(selectors);
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel costumeStack = new JPanel();
        costumeStack.setLayout(new BoxLayout(costumeStack, BoxLayout.Y_AXIS));
        costumeStack.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Costume Preview"));

        hatImageLabel    = makeImageBox(costumeStack, "Hat",    200, 100);
        outfitImageLabel = makeImageBox(costumeStack, "Outfit", 200, 120);
        bootsImageLabel  = makeImageBox(costumeStack, "Boots",  200, 100);

        // Finish button
        finishButton = new JButton("Finish");
        JPanel finishRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        finishRow.add(finishButton);
        costumeStack.add(finishRow);

        centerPanel.add(costumeStack);
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel queuePanel = new JPanel(new BorderLayout(6, 6));
        queuePanel.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Queue"));
        queueModel = new DefaultListModel<>();
        JList<String> queueList = new JList<>(queueModel);
        queueList.setVisibleRowCount(8);
        queueList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);
        centerPanel.add(queuePanel);
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        checkQueueButton = new JButton("Check Queue");
        addButton = new JButton("Add");
        Dimension btnSize = new Dimension(110, 32);
        checkQueueButton.setPreferredSize(btnSize);
        addButton.setPreferredSize(btnSize);
        buttonsRow.add(checkQueueButton);
        buttonsRow.add(addButton);
        centerPanel.add(buttonsRow);


        JScrollPane centerScroll = new JScrollPane(
                centerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        centerScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(centerScroll, BorderLayout.CENTER);


        // Actions
        addButton.addActionListener(e -> doAddToQueue());
        checkQueueButton.addActionListener(e -> openQueueDirectory());
        finishButton.addActionListener(e -> doFinish());
        hatCombo.addActionListener(e -> updateHatPreview());
        outfitCombo.addActionListener(e -> updateOutfitPreview());
        bootsCombo.addActionListener(e -> updateBootsPreview());

        // Previews & startup
        hatCombo.setSelectedIndex(0);
        outfitCombo.setSelectedIndex(0);
        bootsCombo.setSelectedIndex(0);
        enableAutoRescalePreviews(); // auto-resize previews to fit
        updateHatPreview();
        updateOutfitPreview();
        updateBootsPreview();
        rateField.setText("");
        rewardsArea.setText("");

        // Finalize size after all preferred sizes set
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 560));
    }


    private JPanel labeled(String title, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(title);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(comp);
        return p;
    }

    private String[] prettify(String[] basenames) {
        String[] out = new String[basenames.length];
        for (int i = 0; i < basenames.length; i++) out[i] = prettyName(basenames[i]);
        return out;
    }
    private String prettyName(String base) {
        String s = base.replace('_', ' ');
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : parts) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) sb.append(w.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private JLabel makeImageBox(JPanel stack, String title, int width, int height) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1, true), new EmptyBorder(6, 6, 6, 6)));
        JLabel titleLbl = new JLabel(title, SwingConstants.LEFT);
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 13f));
        box.add(titleLbl, BorderLayout.NORTH);
        JLabel imgLabel = new JLabel("No image", SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(width, height));
        imgLabel.setForeground(Color.GRAY);
        box.add(imgLabel, BorderLayout.CENTER);
        stack.add(box);
        stack.add(Box.createVerticalStrut(8));
        return imgLabel;
    }

    private void enableAutoRescalePreviews() {
        java.awt.event.ComponentAdapter resizer = new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                Object src = e.getComponent();
                if (src == hatImageLabel)    updateHatPreview();
                else if (src == outfitImageLabel) updateOutfitPreview();
                else if (src == bootsImageLabel)  updateBootsPreview();
            }
        };
        hatImageLabel.addComponentListener(resizer);
        outfitImageLabel.addComponentListener(resizer);
        bootsImageLabel.addComponentListener(resizer);
    }


    private void updateHatPreview()    { setImage(hatImageLabel,    HATS[hatCombo.getSelectedIndex()]); }
    private void updateOutfitPreview() { setImage(outfitImageLabel, OUTFITS[outfitCombo.getSelectedIndex()]); }
    private void updateBootsPreview()  { setImage(bootsImageLabel,  BOOTS[bootsCombo.getSelectedIndex()]); }

    private void setImage(JLabel target, String base) {
        ImageIcon icon = loadScaledIcon(base, target.getWidth(), target.getHeight());
        if (icon != null) { target.setIcon(icon); target.setText(null); }
        else { target.setIcon(null); target.setText("Missing: " + base); }
    }

    private ImageIcon loadScaledIcon(String baseName, int targetW, int targetH) {
        if (targetW <= 0) targetW = 200;
        if (targetH <= 0) targetH = 120;

        // Hard caps (optional): never scale images larger than this
        targetW = Math.min(targetW, 220);
        targetH = Math.min(targetH, 140);

        BufferedImage img = tryLoadBuffered(baseName);
        if (img == null) return null;
        double sx = (double) targetW / img.getWidth();
        double sy = (double) targetH / img.getHeight();
        double scale = Math.min(sx, sy);
        int newW = Math.max(1, (int)Math.round(img.getWidth() * scale));
        int newH = Math.max(1, (int)Math.round(img.getHeight() * scale));
        Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private BufferedImage tryLoadBuffered(String baseName) {
        for (String ext : EXTENSIONS) {
            File f = new File(ASSETS_DIR + File.separator + baseName + "." + ext);
            if (f.exists()) { try { return ImageIO.read(f); } catch (Exception ignored) {} }
        }
        for (String ext : EXTENSIONS) {
            File f = new File(baseName + "." + ext);
            if (f.exists()) { try { return ImageIO.read(f); } catch (Exception ignored) {} }
        }
        for (String ext : EXTENSIONS) {
            try {
                java.net.URL url = getClass().getResource("/" + ASSETS_DIR + "/" + baseName + "." + ext);
                if (url != null) return ImageIO.read(url);
                url = getClass().getResource("/" + baseName + "." + ext);
                if (url != null) return ImageIO.read(url);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Rewards
    private void doFinish() {
        String hatBase    = HATS[hatCombo.getSelectedIndex()];
        String outfitBase = OUTFITS[outfitCombo.getSelectedIndex()];
        String bootsBase  = BOOTS[bootsCombo.getSelectedIndex()];

        int[] hatR    = HAT_RATE_RANGES.getOrDefault(hatBase,      new int[]{1, 3});
        int[] outfitR = OUTFIT_RATE_RANGES.getOrDefault(outfitBase,new int[]{1, 10});
        int[] bootsR  = BOOTS_RATE_RANGES.getOrDefault(bootsBase,  new int[]{1, 3});

        int minAvg = (int) Math.round((hatR[0] + outfitR[0] + bootsR[0]) / 3.0);
        int maxAvg = (int) Math.round((hatR[1] + outfitR[1] + bootsR[1]) / 3.0);
        int strongestMax = Math.max(hatR[1], Math.max(outfitR[1], bootsR[1]));
        int combinedMin = Math.max(1, minAvg);
        int combinedMax = Math.min(10, Math.max(maxAvg, strongestMax));
        if (combinedMin > combinedMax) combinedMin = combinedMax;

        int rate = rollInclusive(combinedMin, combinedMax);
        rateField.setValue(rate);

        lastGeneratedRewards = generateRewards(rate);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Rate: %d%n%n", rate));
        for (Reward r : lastGeneratedRewards) {
            sb.append(String.format("%-8s No. %3d  - %3d%n", r.type, r.number, r.amount));
        }
        rewardsArea.setText(sb.toString());
    }

    private void doAddToQueue() {
        String name = (nameField.getText() != null) ? nameField.getText().trim() : "";
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name before adding to the queue.",
                    "Missing Name", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        Object rv = rateField.getValue();
        int rate = (rv instanceof Number) ? ((Number) rv).intValue() : 0;
        if (rate <= 0 || lastGeneratedRewards == null || lastGeneratedRewards.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please click Finish to set the costume rate and rewards first.",
                    "Rate/Rewards Not Set", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hat = HATS[hatCombo.getSelectedIndex()];
        String outfit = OUTFITS[outfitCombo.getSelectedIndex()];
        String boots = BOOTS[bootsCombo.getSelectedIndex()];

        // Store structured entry (deep-copy rewards list)
        List<Reward> rewardsCopy = new ArrayList<>();
        for (Reward r : lastGeneratedRewards) rewardsCopy.add(new Reward(r.type, r.number, r.amount));
        QueueEntry entry = new QueueEntry(name, rate, hat, outfit, boots, rewardsCopy);
        queueEntries.add(entry);

        // Also add a readable line to the simple queue list
        String queueEntryStr = String.format("%s | Rate %d | Hat:%s | Outfit:%s | Boots:%s",
                name, rate, prettyName(hat), prettyName(outfit), prettyName(boots));
        queueModel.addElement(queueEntryStr);

        JOptionPane.showMessageDialog(this, "Added to queue:\n" + queueEntryStr,
                "Queued", JOptionPane.INFORMATION_MESSAGE);

        nameField.setText("");
        nameField.requestFocus();
    }

    private void openQueueDirectory() {
        QueueDirectory dir = new QueueDirectory(this, queueModel, queueEntries);
        dir.setVisible(true);
    }

    // Rewards logic
    private List<Reward> generateRewards(int rate) {
        List<Reward> results = new ArrayList<>(rate);

        if (rate == 10) {
            for (int hn : houseNumbers) results.add(createReward("House", hn));
            for (int mn : mansionNumbers) results.add(createReward("Mansion", mn));
            return results;
        }

        int mansionCount = 0;
        if (rate >= 5) {
            mansionCount = 1;
            if (rate >= 7 && rate <= 9 && rng.nextBoolean()) mansionCount = 2;
        }

        List<Integer> mansionPool = new ArrayList<>();
        for (int mn : mansionNumbers) mansionPool.add(mn);
        Collections.shuffle(mansionPool, rng);
        for (int i = 0; i < mansionCount && i < mansionPool.size(); i++) {
            results.add(createReward("Mansion", mansionPool.get(i)));
        }

        int remaining = rate - results.size();
        List<Integer> housePool = new ArrayList<>();
        for (int hn : houseNumbers) housePool.add(hn);
        Collections.shuffle(housePool, rng);
        for (int i = 0; i < remaining && i < housePool.size(); i++) {
            results.add(createReward("House", housePool.get(i)));
        }

        while (results.size() < rate) {
            int hn = houseNumbers[rng.nextInt(houseNumbers.length)];
            results.add(createReward("House", hn));
        }

        Collections.shuffle(results, rng);
        return results;
    }

    private Reward createReward(String type, int number) {
        int[] range = prizeRanges.get(key(type, number));
        if (range == null) {
            System.err.println("Missing range for " + type + " " + number + ". Using fallback 1-3.");
            range = new int[]{1, 3};
        }
        int amount = rollInclusive(range[0], range[1]);
        return new Reward(type, number, amount);
    }

    private int rollInclusive(int min, int max) {
        if (min >= max) return min;
        return rng.nextInt(max - min + 1) + min;
    }

    // Data classes
    public static class Reward {
        final String type;
        final int number;
        final int amount;
        Reward(String type, int number, int amount) {
            this.type = type; this.number = number; this.amount = amount;
        }
    }

    public static class QueueEntry {
        final String name;
        final int rate;
        final String hat;
        final String outfit;
        final String boots;
        final List<Reward> rewards;

        QueueEntry(String name, int rate, String hat, String outfit, String boots, List<Reward> rewards) {
            this.name = name; this.rate = rate;
            this.hat = hat; this.outfit = outfit; this.boots = boots;
            this.rewards = rewards;
        }

        String costumeSummary() {
            return String.format("%s / %s / %s",
                    pretty(hat), pretty(outfit), pretty(boots));
        }

        private String pretty(String base) {
            String s = base.replace('_', ' ');
            String[] parts = s.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String w : parts) {
                if (w.isEmpty()) continue;
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1));
                sb.append(' ');
            }
            return sb.toString().trim();
        }
    }
}