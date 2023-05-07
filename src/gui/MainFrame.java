package gui;

import synchronization.BidirectionalSync;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;

/**
 * The MainFrame class provides the graphical user interface for the application,
 * allowing users to synchronize files between two directories.
 */
public class MainFrame {
    private JFrame frame;
    private JPanel panel;
    private JButton button1, startSyncButton;
    private JTextField textField1;
    private JRadioButton a1099RadioButton, a1100RadioButton, a1RadioButton, a2RadioButton, localIPRadioButton;
    private ButtonGroup buttonGroup, idGroup;
    private JLabel titleLabel;
    private JLabel chooseSourceLabel;
    private JLabel specifyIPLabel;
    private JLabel choosePortLabel;
    private JLabel chooseIDLabel, warning;
    private static JLabel messageLabel;
    private BidirectionalSync bidirectionalSync;

    /**
     * Constructs a new MainFrame object and initializes its components.
     *
     * @throws RemoteException If there is an issue with remote object communication.
     */
    public MainFrame() throws RemoteException {
        initComponents();
        layoutComponents();
    }

    /**
     * Updates the message displayed on the user interface.
     *
     * @param message The message to be displayed.
     */
    public static void message (String message) {
        messageLabel.setText(message);
        if (message == "It's your turn !")
            messageLabel.setForeground(new Color(76, 187, 23));
        else if (message == "It's almost over !")
            messageLabel.setForeground(new Color(255, 87, 51));
        else if (message == "Waiting for your turn..." | message == "Synchronisation is already in progress !" | message == "PC unreachable waiting time exceeded" | message == "Error on initial wait" | message == "Error while waiting for the other PC to connect")
            messageLabel.setForeground(new Color(220, 20, 60));
        else
            messageLabel.setForeground(new Color(214, 205, 84));
    }

    /**
     * Initializes the components of the MainFrame.
     *
     * @throws RemoteException If there is an issue with remote object communication.
     */
    private void initComponents() throws RemoteException {
        frame = new JFrame("SyncMaster Pro");
        frame.setMinimumSize(new Dimension(590,200));

        panel = new JPanel();
        panel.setBackground(new Color(0, 48, 73));
        button1 = new JButton("Select Directory");
        startSyncButton = new JButton("Start Synchronization");
        textField1 = new JTextField(20);
        a1RadioButton = new JRadioButton("1");
        a2RadioButton = new JRadioButton("2");
        localIPRadioButton = new JRadioButton("Local (127.0.0.1)");
        a1099RadioButton = new JRadioButton("1099");
        a1100RadioButton = new JRadioButton("1100");
        buttonGroup = new ButtonGroup();
        idGroup = new ButtonGroup();

        titleLabel = new JLabel("SyncMaster Pro");
        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        titleLabel.setForeground(new Color(32, 178, 170));

        chooseSourceLabel = new JLabel("Choose the source directory :");
        chooseSourceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        chooseSourceLabel.setForeground(new Color(255, 255, 255));

        specifyIPLabel = new JLabel("Specify the IP address of the destination directory :");
        specifyIPLabel.setFont(new Font("Arial", Font.BOLD, 13));
        specifyIPLabel.setForeground(new Color(255, 255, 255));

        choosePortLabel = new JLabel("Choose a port number :");
        choosePortLabel.setFont(new Font("Arial", Font.BOLD, 13));
        choosePortLabel.setForeground(new Color(255, 255, 255));

        chooseIDLabel = new JLabel("Choose an ID :");
        chooseIDLabel.setFont(new Font("Arial", Font.BOLD, 13));
        chooseIDLabel.setForeground(new Color(255, 255, 255));

        warning = new JLabel("Warning: The port number and the ID number must be different from the remote PC ! ");
        warning.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
        warning.setForeground(new Color(255, 255, 255));
        Border underline = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE);
        warning.setBorder(underline);

        messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.BOLD, 19));



        buttonGroup.add(a1099RadioButton);
        buttonGroup.add(a1100RadioButton);

        idGroup.add(a1RadioButton);
        idGroup.add(a2RadioButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        bidirectionalSync = new BidirectionalSync(40000, true);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnValue = fileChooser.showOpenDialog(panel);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    bidirectionalSync.setSourcePath(selectedDirectory.getAbsolutePath());
                    button1.setText(selectedDirectory.getName());
                }
            }
        });

        localIPRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (localIPRadioButton.isSelected()) {
                    textField1.setText("127.0.0.1");
                    textField1.setEnabled(false);
                } else {
                    textField1.setText("");
                    textField1.setEnabled(true);
                }
            }
        });


        startSyncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String remoteIP = textField1.getText();
                bidirectionalSync.setRemoteIP(remoteIP);
                bidirectionalSync.startSynchronization();
            }
        });

        a1099RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (a1099RadioButton.isSelected()) {
                    bidirectionalSync.setLocalPort(1099);
                }
            }
        });

        a1100RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (a1100RadioButton.isSelected()) {
                    bidirectionalSync.setLocalPort(1100);
                }
            }
        });

        a1RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (a1RadioButton.isSelected()) {
                    bidirectionalSync.setinstanceId(1);
                }
            }
        });

        a2RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (a2RadioButton.isSelected()) {
                    bidirectionalSync.setinstanceId(2);
                }
            }
        });

    }

    /**
     * Sets up the layout of the components within the MainFrame.
     */
    private void layoutComponents() {
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(titleLabel)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(chooseSourceLabel)
                                .addComponent(specifyIPLabel)
                                .addComponent(choosePortLabel)
                                .addComponent(chooseIDLabel))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(button1)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(localIPRadioButton)
                                        .addGap(10)
                                        .addComponent(textField1))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(a1099RadioButton)
                                        .addGap(10)
                                        .addComponent(a1100RadioButton))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(a1RadioButton)
                                        .addGap(10)
                                        .addComponent(a2RadioButton))))
                .addComponent(warning)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(startSyncButton))
                .addGap(10)
                .addComponent(messageLabel)
                .addGap(25)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(titleLabel)
                .addGap(25)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(chooseSourceLabel)
                        .addComponent(button1))
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(specifyIPLabel)
                        .addComponent(localIPRadioButton)
                        .addGap(10)
                        .addComponent(textField1))
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(choosePortLabel)
                        .addComponent(a1099RadioButton)
                        .addGap(10)
                        .addComponent(a1100RadioButton))
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(chooseIDLabel)
                        .addComponent(a1RadioButton)
                        .addGap(10)
                        .addComponent(a2RadioButton))
                .addGap(20)
                .addComponent(warning)
                .addGap(20)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(startSyncButton))
                .addComponent(messageLabel)
                .addGap(25)
        );

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
