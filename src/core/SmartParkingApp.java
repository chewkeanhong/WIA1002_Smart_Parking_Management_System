package core;

import javax.swing.SwingUtilities;
import ui.MainFrame;

public class SmartParkingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
