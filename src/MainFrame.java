import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;

public class MainFrame {
    private JFrame frame;
    private JPanel panel;
    private JButton button1, startSyncButton, stopSyncButton;
    private JTextField textField1;
    private JRadioButton a1099RadioButton, a1100RadioButton, a1RadioButton, a2RadioButton;
    private ButtonGroup buttonGroup, idGroup;
    private JLabel titleLabel, chooseSourceLabel, specifyIPLabel, choosePortLabel, chooseIDLabel, statusLabel;
    private BidirectionalSync bidirectionalSync;

    public MainFrame() throws RemoteException {
        initComponents();
        layoutComponents();
    }

    private void initComponents() throws RemoteException {
        frame = new JFrame("SyncMaster Pro");
        panel = new JPanel();
        panel.setBackground(new Color(0, 48, 73));
        button1 = new JButton("Select Directory");
        startSyncButton = new JButton("Start Synchronization");
        stopSyncButton = new JButton("Stop Synchronization");
        textField1 = new JTextField(20);
        a1RadioButton = new JRadioButton("1");
        a2RadioButton = new JRadioButton("2");
        a1099RadioButton = new JRadioButton("1099");
        a1100RadioButton = new JRadioButton("1100");
        buttonGroup = new ButtonGroup();
        idGroup = new ButtonGroup();

        titleLabel = new JLabel("SyncMaster Pro");
        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        titleLabel.setForeground(new Color(32, 178, 170));

        chooseSourceLabel = new JLabel("Choose the source directory:");
        chooseSourceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        chooseSourceLabel.setForeground(new Color(255, 255, 255));

        specifyIPLabel = new JLabel("Specify the IP address of the destination directory:");
        specifyIPLabel.setFont(new Font("Arial", Font.BOLD, 13));
        specifyIPLabel.setForeground(new Color(255, 255, 255));

        choosePortLabel = new JLabel("Choose a port number :");
        choosePortLabel.setFont(new Font("Arial", Font.BOLD, 13));
        choosePortLabel.setForeground(new Color(255, 255, 255));

        chooseIDLabel = new JLabel("Choose an ID :");
        chooseIDLabel.setFont(new Font("Arial", Font.BOLD, 13));
        chooseIDLabel.setForeground(new Color(255, 255, 255));


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
                    System.out.println("Selected directory: " + selectedDirectory.getAbsolutePath());
                    bidirectionalSync.setSourcePath(selectedDirectory.getAbsolutePath());
                    button1.setText(selectedDirectory.getName()); // Update the button text
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

        stopSyncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    bidirectionalSync.stopSynchronization();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                                .addComponent(textField1)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(a1099RadioButton)
                                        .addComponent(a1100RadioButton))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(a1RadioButton)
                                        .addComponent(a2RadioButton))))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(startSyncButton)
                        .addComponent(stopSyncButton))
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
                        .addComponent(textField1))
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(choosePortLabel)
                        .addComponent(a1099RadioButton)
                        .addComponent(a1100RadioButton))
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(chooseIDLabel)
                        .addComponent(a1RadioButton)
                        .addComponent(a2RadioButton))
                .addGap(20)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(startSyncButton)
                        .addComponent(stopSyncButton))
        );

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
