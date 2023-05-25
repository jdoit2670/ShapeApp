import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShapeApp extends JFrame {
    private JComboBox<String> shapeComboBox;
    private JLabel lengthLabel, widthLabel, sideLabel, radiusLabel;
    private JTextField lengthTextField, widthTextField, sideTextField, radiusTextField;
    private JButton colorButton, createButton, editButton, deleteButton;
    private JRadioButton twoDRadioButton, threeDRadioButton;
    private JList<String> shapeList;
    private DefaultListModel<String> shapeListModel;
    private JPanel shapePreviewPanel;

    private Connection connection;

    public ShapeApp() {
        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ShapeApp");
        setSize(800, 500);
        setLayout(new BorderLayout());

        // Set up the UI components
        setupComponents();

        // Set up the database
        setupDatabase();

        // Load the shape list from the database
        loadShapeList();

        // Set the initial shape preview to empty
        clearShapePreview();

        // Set the default shape to be selected in the combo box
        shapeComboBox.setSelectedIndex(0);
    }

    private void setupComponents() {
        // Set the Look and Feel to FlatLaf Light Theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the main panel
        JPanel mainPanel = new JPanel(new MigLayout("wrap", "[][][][][][][][][]", ""));

        // Set up the combo box
        shapeComboBox = new JComboBox<>(new String[]{"Rectangle", "Square", "Circle"});
        mainPanel.add(shapeComboBox);

        // Set up the labels and text fields
        lengthLabel = new JLabel("Length:");
        widthLabel = new JLabel("Width:");
        sideLabel = new JLabel("Side:");
        radiusLabel = new JLabel("Radius:");
        lengthTextField = new JTextField(10);
        widthTextField = new JTextField(10);
        sideTextField = new JTextField(10);
        radiusTextField = new JTextField(10);
        mainPanel.add(lengthLabel);
        mainPanel.add(lengthTextField);
        mainPanel.add(widthLabel);
        mainPanel.add(widthTextField);
        mainPanel.add(sideLabel);
        mainPanel.add(sideTextField);
        mainPanel.add(radiusLabel);
        mainPanel.add(radiusTextField);

        // Set up the color button
        colorButton = new JButton("Choose Color");
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(ShapeApp.this, "Choose Color", Color.BLACK);
                if (color != null) {
                    shapePreviewPanel.setBackground(color);
                }
            }
        });
        mainPanel.add(colorButton);

        // Set up the create button
        createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createShape();
            }
        });
        mainPanel.add(createButton);

        // Set up the edit button
        editButton = new JButton("Edit");
        mainPanel.add(editButton);

        // Set up the delete button
        deleteButton = new JButton("Delete");
        mainPanel.add(deleteButton);

        // Set up the radio buttons
        twoDRadioButton = new JRadioButton("2D", true);
        threeDRadioButton = new JRadioButton("3D");
        mainPanel.add(twoDRadioButton);
        mainPanel.add(threeDRadioButton);

        // Group the radio buttons together
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(twoDRadioButton);
        radioGroup.add(threeDRadioButton);

        // Add the main panel to the frame
        add(mainPanel, BorderLayout.NORTH);

        // Set up the divider
        JSeparator separator = new JSeparator();
        add(separator, BorderLayout.CENTER);

        // Set up the list
        shapeListModel = new DefaultListModel<>();
        shapeList = new JList<>(shapeListModel);
        shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shapeList.setBorder(new EmptyBorder(5, 5, 5, 5));
        shapeList.setPreferredSize(new Dimension(200, getHeight()));
        shapeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedShape = shapeList.getSelectedValue();
                if (selectedShape != null) {
                    loadShapePreview(selectedShape);
                }
            }
        });
        add(new JScrollPane(shapeList), BorderLayout.WEST);

        // Set up the shape preview panel
        shapePreviewPanel = new JPanel();
        shapePreviewPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        shapePreviewPanel.setPreferredSize(new Dimension(300, 300));
        add(shapePreviewPanel, BorderLayout.CENTER);
    }

    private void setupDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:shapes.db");
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS shapes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, type TEXT, dimensions TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadShapeList() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name, dimensions FROM shapes");
            shapeListModel.clear();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String dimensions = resultSet.getString("dimensions");
                shapeListModel.addElement(name + " - " + dimensions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createShape() {
        String shapeType = (String) shapeComboBox.getSelectedItem();
        String shapeName = shapeType + " " + System.currentTimeMillis();

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO shapes (name, type, dimensions) VALUES (?, ?, ?)");
            statement.setString(1, shapeName);
            statement.setString(2, shapeType);

            if (shapeType.equals("Rectangle")) {
                int length = Integer.parseInt(lengthTextField.getText());
                int width = Integer.parseInt(widthTextField.getText());
                statement.setString(3, "Length: " + length + ", Width: " + width);
            } else if (shapeType.equals("Square")) {
                int side = Integer.parseInt(sideTextField.getText());
                statement.setString(3, "Side: " + side);
            } else if (shapeType.equals("Circle")) {
                int radius = Integer.parseInt(radiusTextField.getText());
                statement.setString(3, "Radius: " + radius);
            }

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadShapeList();
        clearShapePreview();
    }

    private void loadShapePreview(String shapeName) {
        String[] shapeDetails = shapeName.split(" - ");
        String dimensions = shapeDetails[1];

        shapePreviewPanel.removeAll();
        shapePreviewPanel.revalidate();
        shapePreviewPanel.repaint();

        if (dimensions.startsWith("Length")) {
            String[] lengthWidth = dimensions.substring(dimensions.indexOf(":") + 1).trim().split(",");
            int length = Integer.parseInt(lengthWidth[0]);
            int width = Integer.parseInt(lengthWidth[1]);
            shapePreviewPanel.setLayout(new BorderLayout());
            shapePreviewPanel.add(new RectanglePanel(length, width), BorderLayout.CENTER);
        } else if (dimensions.startsWith("Side")) {
            int side = Integer.parseInt(dimensions.substring(dimensions.indexOf(":") + 1).trim());
            shapePreviewPanel.setLayout(new BorderLayout());
            shapePreviewPanel.add(new SquarePanel(side), BorderLayout.CENTER);
        } else if (dimensions.startsWith("Radius")) {
            int radius = Integer.parseInt(dimensions.substring(dimensions.indexOf(":") + 1).trim());
            shapePreviewPanel.setLayout(new BorderLayout());
            shapePreviewPanel.add(new CirclePanel(radius), BorderLayout.CENTER);
        }

        shapePreviewPanel.revalidate();
        shapePreviewPanel.repaint();
    }

    private void clearShapePreview() {
        shapePreviewPanel.removeAll();
        shapePreviewPanel.revalidate();
        shapePreviewPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ShapeApp().setVisible(true);
            }
        });
    }
}

class RectanglePanel extends JPanel {
    private int length, width;

    public RectanglePanel(int length, int width) {
        this.length = length;
        this.width = width;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        g.fillRect(0, 0, length, width);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(length, width);
    }
}

class SquarePanel extends JPanel {
    private int side;

    public SquarePanel(int side) {
        this.side = side;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, side, side);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(side, side);
    }
}

class CirclePanel extends JPanel {
    private int radius;

    public CirclePanel(int radius) {
        this.radius = radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GREEN);
        g.fillOval(0, 0, radius * 2, radius * 2);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(radius * 2, radius * 2);
    }
}
