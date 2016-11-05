package puyo;

import java.awt.*;
import javax.swing.*;

//The container class, sets everything running
public class PuyoMain {

	//FIELDS
	private static final long serialVersionUID = 1L; //avoids compiler warnings
	private static int spherewidth = 32; //specifies sphere width in px
	private static int sphereheight = 32; //specifies sphere height in px
	private static int gridwidth = 6; //specifies grid width
	private static int gridheight = 12; //specifies grid height
	
	//CONSTRUCTORS
	
	//METHODS
	
    public static void main(String[] args) {
    	//Create frame for game to run in
    	JFrame frame = new JFrame("PeterLPuyo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Init frame size
        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(spherewidth * gridwidth, sphereheight * gridheight));
        frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

        //More frame init
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        //Create game pannel, add to frame
        JPanel pannel = new PuyoGame(gridwidth, gridheight, spherewidth, sphereheight);
        frame.add(pannel);
    }
}
