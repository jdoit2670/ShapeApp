import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RectangleApp extends JFrame {
    private JTextField lengthField;
    private JTextField widthField;
    private JButton saveButton;
    private JTextArea rectangleList;

    private Connection connection;
    private Statement statement;

    public RectangleApp() {
        super("Rectangle App");

        // Initialize components
        lengthField = new JTextField(10);
        widthField = new JTextField(10);
        saveButton = new JButton("Save");
        rectangleList = new JTextArea(10, 20);
        rectangleList.setEditable(false);

        // Set up the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 2));
        mainPanel.add(new JLabel("Length:"));
        mainPanel.add(lengthField);
        mainPanel.add(new JLabel("Width:"));
        mainPanel.add(widthField);
        mainPanel.add(saveButton);

        // Set up the frame
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
        add(new JScrollPane(rectangleList), BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // Set up database connection
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:rectangles.db");
            statement = connection.createStatement();

            // Create the table if it doesn't exist
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS rectangles (id INTEGER PRIMARY KEY AUTOINCREMENT, length REAL, width REAL)");

            // Load existing rectangles from the database
            loadRectangles();

            // Save button action listener
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveRectangle();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRectangles() {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM rectangles");

            StringBuilder sb = new StringBuilder();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                double length = resultSet.getDouble("length");
                double width = resultSet.getDouble("width");

                sb.append("Rectangle ").append(id).append(": Length=").append(length).append(", Width=").append(width).append("\n");
            }

            rectangleList.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveRectangle() {
        String lengthStr = lengthField.getText();
        String widthStr = widthField.getText();

        if (!lengthStr.isEmpty() && !widthStr.isEmpty()) {
            try {
                double length = Double.parseDouble(lengthStr);
                double width = Double.parseDouble(widthStr);

                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO rectangles (length, width) VALUES (?, ?)");
                preparedStatement.setDouble(1, length);
                preparedStatement.setDouble(2, width);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                // Clear the input fields
                lengthField.setText("");
                widthField.setText("");

                // Reload the rectangles from the database
                loadRectangles();
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new RectangleApp().setVisible(true);
            }
        });
    }
}
