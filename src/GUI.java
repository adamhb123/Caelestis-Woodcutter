import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.imageio.ImageIO;
import org.dreambot.api.methods.MethodProvider;
class GuiMouseListener implements MouseListener {
	public boolean dragging=false;
	public Point dragPoint;
	public void mousePressed(MouseEvent mouseEv) {
		MethodProvider.log("Mouse press");
		dragPoint = mouseEv.getPoint();
		dragging=true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		MethodProvider.log("Mouse depress");
		dragging=false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
}
public class GUI extends JFrame {
	private JLabel contentPane;
	public boolean started, worldHop, bank, birdsNest;
	public String tree;
	public double maxDistance;
	private JTextField textMaxDistance,treeType;
	public GuiMouseListener gml;
	public void close(){
		dispatchEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));
	}
	public GUI() {
		gml = new GuiMouseListener();
		addMouseListener(gml);
		setUndecorated(true);
		
		setBackground(new Color(1,1,1,0));
		MethodProvider.log("Testing output in GUI");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Caelestis' Woodcutter");
		setResizable(false);
		setBounds(0,0,480,505);
		try {
			contentPane = new JLabel(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/gui.png"))));
		} 
		catch(IOException e) {
			MethodProvider.log("Failed to load GUI image!");
			MethodProvider.log(e.toString());
		}
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(null);
		panel.setOpaque(false);
		
		JCheckBox chckbxBank = new JCheckBox("");
		chckbxBank.setBackground(new Color(1,1,1,0));
		chckbxBank.setBounds(177, 310, 21, 23);
		panel.add(chckbxBank);
		
		JCheckBox chckbxBirdsNests = new JCheckBox("");
		chckbxBirdsNests.setBounds(177, 360, 21, 23);
		chckbxBirdsNests.setBackground(new Color(1,1,1,0));
		panel.add(chckbxBirdsNests);
		
		JCheckBox chckbxWorldHop = new JCheckBox("");
		chckbxWorldHop.setBackground(new Color(1,1,1,0));
		chckbxWorldHop.setBounds(177, 410, 21, 23);
		panel.add(chckbxWorldHop);
		textMaxDistance = new JTextField();
		textMaxDistance.setBounds(177, 258, 131, 20);
		//textMaxDistance.setBackground(new Color(1,1,1,0));
		panel.add(textMaxDistance);
		textMaxDistance.setColumns(10);
		
		treeType = new JTextField();
		treeType.setBounds(177, 205, 131, 20);
		//treeType.setBackground(new Color(1,1,1,0));
		panel.add(treeType);
		treeType.setColumns(10);
		
		JButton startButton = new JButton("Start Script");
		startButton.setBounds(301, 410, 110, 23);
		panel.add(startButton);
		
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				worldHop = chckbxWorldHop.isSelected();
				bank = chckbxBank.isSelected();
				tree = treeType.getText();
				birdsNest = chckbxBirdsNests.isSelected();
				maxDistance = Double.parseDouble(textMaxDistance.getText());
				started = true;
			}
		});
		
		this.setVisible(true);
	
	}
}
