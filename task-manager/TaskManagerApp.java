import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

class Task {
    String description;
    String dueDate;
    boolean isComplete;

    public Task(String description, String dueDate) {
        this.description = description;
        this.dueDate = dueDate;
        this.isComplete = false;
    }

    public void markComplete() {
        this.isComplete = true;
    }

    @Override
    public String toString() {
        return (isComplete ? "[X] " : "[  ] ") + description + " (Due: " + dueDate + ")";
    }
}

public class TaskManagerApp {
    private JFrame frame;
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JTextField taskField;
    private JTextField dueDateField;

    public TaskManagerApp() {
        frame = new JFrame("Task Manager");
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskField = new JTextField(20);
        dueDateField = new JTextField(10);

        ((AbstractDocument) dueDateField.getDocument()).setDocumentFilter(new DateDocumentFilter());

        setupUI();
        loadTasks();
    }

    private void setupUI() {
        // Set a modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main panel for layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side (Input and buttons)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel taskLabel = new JLabel("Task:");
        taskLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(taskLabel, gbc);

        taskField.setFont(new Font("Arial", Font.PLAIN, 14));
        taskField.setToolTipText("Enter the task description");
        gbc.gridx = 1; gbc.gridy = 0;
        leftPanel.add(taskField, gbc);

        JLabel dueDateLabel = new JLabel("Due Date:");
        dueDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        leftPanel.add(dueDateLabel, gbc);

        // Setup dueDateField with a placeholder
        dueDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        dueDateField.setToolTipText("Enter the due date (e.g., MM/DD/YY)");
        gbc.gridx = 1; gbc.gridy = 1;
        leftPanel.add(dueDateField, gbc);

        // Add phantom text (placeholder) for dueDateField
        dueDateField.setForeground(Color.GRAY);
        dueDateField.setText("MM/DD/YY");
        dueDateField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dueDateField.getText().equals("MM/DD/YY")) {
                    dueDateField.setText("");
                    dueDateField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (dueDateField.getText().isEmpty()) {
                    dueDateField.setForeground(Color.GRAY);
                    dueDateField.setText("MM/DD/YY");
                }
            }
        });

        // Buttons
        JButton addButton = new JButton("Add Task");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBackground(new Color(76, 175, 80));
        addButton.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        leftPanel.add(addButton, gbc);

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setBackground(new Color(244, 67, 54));
        deleteButton.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        leftPanel.add(deleteButton, gbc);

        JButton completeButton = new JButton("Mark Complete");
        completeButton.setFont(new Font("Arial", Font.BOLD, 14));
        completeButton.setBackground(new Color(33, 150, 243));
        completeButton.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        leftPanel.add(completeButton, gbc);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Right side (Task list)
        taskList.setFont(new Font("Arial", Font.PLAIN, 14));
        taskList.setCellRenderer(new TaskCellRenderer());
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Task List"));
        scrollPane.setPreferredSize(new Dimension(300, 300));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        // Add action listeners
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        completeButton.addActionListener(e -> markComplete());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    private void addTask() {
        String description = taskField.getText();
        String dueDate = dueDateField.getText();
        if (!description.isEmpty() && !dueDate.isEmpty()) {
            taskListModel.addElement(new Task(description, dueDate));
            saveTasks();
            taskField.setText("");
            dueDateField.setText("");
        }
    }

    private void deleteTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            taskListModel.removeElement(selectedTask);
            saveTasks();
        }
    }

    private void markComplete() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            selectedTask.markComplete();
            taskList.repaint();
            saveTasks();
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            ArrayList<Task> tasks = new ArrayList<>();
            for (int i = 0; i < taskListModel.size(); i++) {
                tasks.add(taskListModel.getElementAt(i));
            }
            out.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            ArrayList<Task> tasks = (ArrayList<Task>) in.readObject();
            for (Task task : tasks) {
                taskListModel.addElement(task);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TaskManagerApp();
    }

    // Custom cell renderer to highlight completed tasks
    private class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Task task = (Task) value;
            if (task.isComplete) {
                setForeground(new Color(76, 175, 80)); // Green for completed tasks
            } else {
                setForeground(Color.BLACK); // Black for pending tasks
            }
            return c;
        }
    }

    private class DateDocumentFilter extends DocumentFilter {
        private final StringBuilder currentInput = new StringBuilder();

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            super.insertString(fb, offset, string, attr);
            updateCurrentInput();
            formatAndSetDate(fb);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            super.replace(fb, offset, length, text, attrs);
            updateCurrentInput();
            formatAndSetDate(fb);
        }

        private void updateCurrentInput() {
            currentInput.setLength(0); // Clear previous input
            try {
                currentInput.append(dueDateField.getText().replace("/", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void formatAndSetDate(FilterBypass fb) throws BadLocationException {
            String formattedDate = formatDate(currentInput.toString());
            fb.replace(0, dueDateField.getText().length(), formattedDate, null);
        }

        private String formatDate(String input) {
            StringBuilder formattedDate = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                if (i == 2 || i == 4) {
                    formattedDate.append('/');
                }
                formattedDate.append(input.charAt(i));
            }
            return formattedDate.toString();
        }
    }
}
