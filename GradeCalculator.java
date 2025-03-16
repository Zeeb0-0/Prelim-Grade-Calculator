import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.Timer;

/**
 * FadePanel extends JPanel to support fade‐in effects using an adjustable alpha transparency.
 */
class FadePanel extends JPanel {
    private float alpha = 1f; // Fully opaque by default

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Apply the alpha composite to enable fade-in effect
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintComponent(g2d);
        g2d.dispose();
    }
}

/**
 * GradeCalculator is a JFrame-based application that calculates Lecture or Laboratory prelim grades.
 * It supports manual entry and CSV file parsing with animated transitions and includes a clear button.
 */
public class GradeCalculator extends JFrame {

    // Layout management: CardLayout is used for smooth mode transitions.
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Mode selection radio buttons.
    private JRadioButton lectureButton;
    private JRadioButton laboratoryButton;

    // Panels for each mode (using our animated FadePanel).
    private FadePanel lecturePanel;
    private FadePanel laboratoryPanel;

    // Components for Lecture Mode manual entry.
    private JTextField lecExamField, essayField, pvmField, javaBasicsField, introJSField, lecAbsencesField;
    private JTextArea lecResultArea;

    // Components for Laboratory Mode manual entry.
    private JTextField labJava1Field, labJava2Field, labJS1Field, labJS2Field, labWorkField, labAbsencesField;
    private JTextArea labResultArea;

    /**
     * Constructor: Sets up the UI with a gradient background, animated transitions, and event handling.
     */
    public GradeCalculator() {
        setTitle("Prelim Grade Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        // Set a gradient background for a modern, artistic look.
        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(45, 62, 80);
                Color color2 = new Color(236, 240, 241);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        });
        getContentPane().setLayout(new BorderLayout());

        // Mode selection panel at the top with refined styling.
        JPanel modePanel = new JPanel();
        modePanel.setOpaque(false);
        modePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel modeLabel = new JLabel("Select Mode: ");
        modeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        lectureButton = new JRadioButton("Lecture", true);
        laboratoryButton = new JRadioButton("Laboratory");
        lectureButton.setOpaque(false);
        laboratoryButton.setOpaque(false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(lectureButton);
        modeGroup.add(laboratoryButton);
        modePanel.add(modeLabel);
        modePanel.add(lectureButton);
        modePanel.add(laboratoryButton);

        // Main panel with CardLayout for switching modes.
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        lecturePanel = createLecturePanel();
        laboratoryPanel = createLaboratoryPanel();
        mainPanel.add(lecturePanel, "Lecture");
        mainPanel.add(laboratoryPanel, "Laboratory");

        // Listeners to trigger animated transitions when mode changes.
        lectureButton.addActionListener(e -> animateTransition("Lecture"));
        laboratoryButton.addActionListener(e -> animateTransition("Laboratory"));

        add(modePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the Lecture mode panel with manual input fields, CSV load button, calculate button, and clear button.
     */
    private FadePanel createLecturePanel() {
        FadePanel panel = new FadePanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel for manual input fields.
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("Prelim Exam Score:"));
        lecExamField = new JTextField();
        inputPanel.add(lecExamField);
        
        inputPanel.add(new JLabel("Essay Score:"));
        essayField = new JTextField();
        inputPanel.add(essayField);
        
        inputPanel.add(new JLabel("PVM Score:"));
        pvmField = new JTextField();
        inputPanel.add(pvmField);
        
        inputPanel.add(new JLabel("Java Basics Score:"));
        javaBasicsField = new JTextField();
        inputPanel.add(javaBasicsField);
        
        inputPanel.add(new JLabel("Intro to JS Score:"));
        introJSField = new JTextField();
        inputPanel.add(introJSField);
        
        inputPanel.add(new JLabel("Number of Absences:"));
        lecAbsencesField = new JTextField();
        inputPanel.add(lecAbsencesField);
        
        // Panel for buttons (Calculate, Load CSV, and Clear).
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton calcButton = new JButton("Calculate Lecture Grade");
        calcButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        calcButton.setBackground(new Color(52, 152, 219));
        calcButton.setForeground(Color.WHITE);
        calcButton.setFocusPainted(false);
        calcButton.addActionListener(e -> calculateLectureGrade());
        buttonPanel.add(calcButton);
        
        JButton loadCsvButton = new JButton("Load CSV for Lecture");
        loadCsvButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadCsvButton.setBackground(new Color(41, 128, 185));
        loadCsvButton.setForeground(Color.WHITE);
        loadCsvButton.setFocusPainted(false);
        loadCsvButton.addActionListener(e -> loadLectureCSV());
        buttonPanel.add(loadCsvButton);
        
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearLectureFields());
        buttonPanel.add(clearButton);
        
        // Text area for displaying results.
        lecResultArea = new JTextArea(10, 40);
        lecResultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        lecResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(lecResultArea);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Creates the Laboratory mode panel with manual input fields, CSV load button, calculate button, and clear button.
     */
    private FadePanel createLaboratoryPanel() {
        FadePanel panel = new FadePanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel for manual input fields.
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("Java 1 Score:"));
        labJava1Field = new JTextField();
        inputPanel.add(labJava1Field);
        
        inputPanel.add(new JLabel("Java 2 Score:"));
        labJava2Field = new JTextField();
        inputPanel.add(labJava2Field);
        
        inputPanel.add(new JLabel("JS 1 Score:"));
        labJS1Field = new JTextField();
        inputPanel.add(labJS1Field);
        
        inputPanel.add(new JLabel("JS 2 Score:"));
        labJS2Field = new JTextField();
        inputPanel.add(labJS2Field);
        
        inputPanel.add(new JLabel("Lab Work Score:"));
        labWorkField = new JTextField();
        inputPanel.add(labWorkField);
        
        inputPanel.add(new JLabel("Number of Absences:"));
        labAbsencesField = new JTextField();
        inputPanel.add(labAbsencesField);
        
        // Panel for buttons (Calculate, Load CSV, and Clear).
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton calcButton = new JButton("Calculate Laboratory Grade");
        calcButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        calcButton.setBackground(new Color(46, 204, 113));
        calcButton.setForeground(Color.WHITE);
        calcButton.setFocusPainted(false);
        calcButton.addActionListener(e -> calculateLaboratoryGrade());
        buttonPanel.add(calcButton);
        
        JButton loadCsvButton = new JButton("Load CSV for Laboratory");
        loadCsvButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadCsvButton.setBackground(new Color(39, 174, 96));
        loadCsvButton.setForeground(Color.WHITE);
        loadCsvButton.setFocusPainted(false);
        loadCsvButton.addActionListener(e -> loadLaboratoryCSV());
        buttonPanel.add(loadCsvButton);
        
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearLaboratoryFields());
        buttonPanel.add(clearButton);
        
        // Text area for displaying results.
        labResultArea = new JTextArea(10, 40);
        labResultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        labResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(labResultArea);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Calculates Lecture prelim grade from manual inputs and displays a detailed breakdown.
     */
    private void calculateLectureGrade() {
        try {
            double exam = Double.parseDouble(lecExamField.getText());
            double essay = Double.parseDouble(essayField.getText());
            double pvm = Double.parseDouble(pvmField.getText());
            double javaBasics = Double.parseDouble(javaBasicsField.getText());
            double introJS = Double.parseDouble(introJSField.getText());
            int absences = Integer.parseInt(lecAbsencesField.getText());

            double prelimQuizzes = (essay + pvm + javaBasics + introJS) / 4.0;
            double attendance = Math.max(0, 100 - (10 * absences));
            double classStanding = 0.6 * prelimQuizzes + 0.4 * attendance;
            double prelimGrade = 0.6 * exam + 0.4 * classStanding;

            StringBuilder result = new StringBuilder();
            result.append("=== Lecture Prelim Grade Calculation ===\n");
            result.append(String.format("Exam: %.2f\n", exam));
            result.append(String.format("Essay: %.2f, PVM: %.2f, Java Basics: %.2f, IntroJS: %.2f\n", essay, pvm, javaBasics, introJS));
            result.append(String.format("Quizzes (avg): %.2f\n", prelimQuizzes));
            result.append(String.format("Attendance: %.2f (after %d absence(s))\n", attendance, absences));
            result.append(String.format("Class Standing: %.2f\n", classStanding));
            result.append(String.format("Final Grade: %.2f\n", prelimGrade));
            result.append("\nSummary: " + generateSummary(prelimGrade));

            lecResultArea.setText(result.toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers for all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Calculates Laboratory prelim grade from manual inputs and displays a detailed breakdown.
     */
    private void calculateLaboratoryGrade() {
        try {
            double java1 = Double.parseDouble(labJava1Field.getText());
            double java2 = Double.parseDouble(labJava2Field.getText());
            double js1 = Double.parseDouble(labJS1Field.getText());
            double js2 = Double.parseDouble(labJS2Field.getText());
            double labWork = Double.parseDouble(labWorkField.getText());
            int absences = Integer.parseInt(labAbsencesField.getText());

            double attendance = Math.max(0, 100 - (10 * absences));
            double prelimExam = 0.2 * java1 + 0.3 * java2 + 0.2 * js1 + 0.3 * js2;
            double classStanding = 0.6 * labWork + 0.4 * attendance;
            double prelimGrade = 0.6 * prelimExam + 0.4 * classStanding;

            StringBuilder result = new StringBuilder();
            result.append("=== Laboratory Prelim Grade Calculation ===\n");
            result.append(String.format("Java1: %.2f, Java2: %.2f, JS1: %.2f, JS2: %.2f\n", java1, java2, js1, js2));
            result.append(String.format("Prelim Exam (weighted): %.2f\n", prelimExam));
            result.append(String.format("Lab Work: %.2f\n", labWork));
            result.append(String.format("Attendance: %.2f (after %d absence(s))\n", attendance, absences));
            result.append(String.format("Class Standing: %.2f\n", classStanding));
            result.append(String.format("Final Grade: %.2f\n", prelimGrade));
            result.append("\nSummary: " + generateSummary(prelimGrade));

            labResultArea.setText(result.toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers for all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generates a summary message based on the computed grade.
     */
    private String generateSummary(double grade) {
        if (grade >= 75) {
            return "Excellent work! You are passing comfortably.";
        } else if (grade >= 60) {
            return "Borderline performance. Improve exam scores and attendance.";
        } else {
            return "Below passing threshold. Significant improvement needed.";
        }
    }

    /**
     * Animates a fade-in transition when switching between panels.
     */
    private void animateTransition(String mode) {
        FadePanel targetPanel = mode.equals("Lecture") ? lecturePanel : laboratoryPanel;
        targetPanel.setAlpha(0f);
        cardLayout.show(mainPanel, mode);
        Timer timer = new Timer(20, null);
        timer.addActionListener(new ActionListener() {
            float alpha = 0f;
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1f) {
                    alpha = 1f;
                    timer.stop();
                }
                targetPanel.setAlpha(alpha);
            }
        });
        timer.start();
    }

    /**
     * Opens a file chooser and processes the CSV file for Lecture mode.
     */
    private void loadLectureCSV() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File csvFile = chooser.getSelectedFile();
            ArrayList<String> results = processLectureCSV(csvFile);
            if (!results.isEmpty()) {
                StringBuilder output = new StringBuilder("=== Lecture CSV Results ===\n");
                for (String line : results) {
                    output.append(line).append("\n");
                }
                lecResultArea.setText(output.toString());
            }
        }
    }

    /**
     * Parses a Lecture CSV file and computes grades for each row.
     * CSV expected format (with header): Exam,Essay,PVM,JavaBasics,IntroJS,Absences
     *
     * @param file The CSV file.
     * @return List of result strings for each row.
     */
    private ArrayList<String> processLectureCSV(File file) {
        ArrayList<String> results = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine(); // skip header
            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 6) continue;
                double exam = Double.parseDouble(tokens[0]);
                double essay = Double.parseDouble(tokens[1]);
                double pvm = Double.parseDouble(tokens[2]);
                double javaBasics = Double.parseDouble(tokens[3]);
                double introJS = Double.parseDouble(tokens[4]);
                int absences = Integer.parseInt(tokens[5]);
                double prelimQuizzes = (essay + pvm + javaBasics + introJS) / 4.0;
                double attendance = Math.max(0, 100 - (10 * absences));
                double classStanding = 0.6 * prelimQuizzes + 0.4 * attendance;
                double prelimGrade = 0.6 * exam + 0.4 * classStanding;
                String summary = generateSummary(prelimGrade);
                results.add(String.format("Row %d: Grade=%.2f, %s", row++, prelimGrade, summary));
            }
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error reading CSV file.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
        return results;
    }

    /**
     * Opens a file chooser and processes the CSV file for Laboratory mode.
     */
    private void loadLaboratoryCSV() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File csvFile = chooser.getSelectedFile();
            ArrayList<String> results = processLaboratoryCSV(csvFile);
            if (!results.isEmpty()) {
                StringBuilder output = new StringBuilder("=== Laboratory CSV Results ===\n");
                for (String line : results) {
                    output.append(line).append("\n");
                }
                labResultArea.setText(output.toString());
            }
        }
    }

    /**
     * Parses a Laboratory CSV file and computes grades for each row.
     * CSV expected format (with header): Java1,Java2,JS1,JS2,LabWork,Absences
     *
     * @param file The CSV file.
     * @return List of result strings for each row.
     */
    private ArrayList<String> processLaboratoryCSV(File file) {
        ArrayList<String> results = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine(); // skip header
            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 6) continue;
                double java1 = Double.parseDouble(tokens[0]);
                double java2 = Double.parseDouble(tokens[1]);
                double js1 = Double.parseDouble(tokens[2]);
                double js2 = Double.parseDouble(tokens[3]);
                double labWork = Double.parseDouble(tokens[4]);
                int absences = Integer.parseInt(tokens[5]);
                double attendance = Math.max(0, 100 - (10 * absences));
                double prelimExam = 0.2 * java1 + 0.3 * java2 + 0.2 * js1 + 0.3 * js2;
                double classStanding = 0.6 * labWork + 0.4 * attendance;
                double prelimGrade = 0.6 * prelimExam + 0.4 * classStanding;
                String summary = generateSummary(prelimGrade);
                results.add(String.format("Row %d: Grade=%.2f, %s", row++, prelimGrade, summary));
            }
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error reading CSV file.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
        return results;
    }

    /**
     * Clears all input fields and result area in Lecture mode.
     */
    private void clearLectureFields() {
        lecExamField.setText("");
        essayField.setText("");
        pvmField.setText("");
        javaBasicsField.setText("");
        introJSField.setText("");
        lecAbsencesField.setText("");
        lecResultArea.setText("");
    }

    /**
     * Clears all input fields and result area in Laboratory mode.
     */
    private void clearLaboratoryFields() {
        labJava1Field.setText("");
        labJava2Field.setText("");
        labJS1Field.setText("");
        labJS2Field.setText("");
        labWorkField.setText("");
        labAbsencesField.setText("");
        labResultArea.setText("");
    }

    /**
     * Main method to launch the GradeCalculator.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GradeCalculator frame = new GradeCalculator();
            frame.setVisible(true);
        });
    }
}
