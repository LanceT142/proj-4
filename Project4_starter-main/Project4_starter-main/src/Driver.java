import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import org.openstreetmap.gui.jmapviewer.*;

public class Driver {
	
	// Declare class data
	private static final int TIMER_DELAY_MS = 20;
	private static final String COMBO_PROMPT = "Animation Time";
	
	private static JMapViewer mapViewer;
	private static JComboBox<String> timeBox;
	private static JCheckBox stopsBox;
	private static JButton playButton;
	
	private static Timer animationTimer;
	private static IconMarker raccoonMarker;
	private static Image raccoonImage;

    public static void main(String[] args) throws FileNotFoundException, IOException {

    	System.setProperty("http.agent", "Project4-LanceTruong/1.0 (student assignment)");
    	
    	// Read file and call stop detection
    	TripPoint.readFile("triplog.csv");
    	TripPoint.h2StopDetection();
    	
    	// Set up frame, include your name in the title
    	JFrame frame = new JFrame("Project 4 - Lance Truong");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLayout(new BorderLayout());
        
        // Set up Panel for input selections
        JPanel topPanel = new JPanel();
    	
        // Play Button
        playButton = new JButton("Play");
    	
        // CheckBox to enable/disable stops
        stopsBox = new JCheckBox("Include Stops");
        stopsBox.setSelected(false);
    	
        // ComboBox to pick animation time
        timeBox = new JComboBox<>(new String[] {
        	COMBO_PROMPT, "15", "30", "60", "90" });
    	
        // Add all to top panel
        topPanel.add(timeBox);
        topPanel.add(stopsBox);
        topPanel.add(playButton);
        frame.add(topPanel, BorderLayout.NORTH);
        
        // Set up mapViewer
        mapViewer = new JMapViewer();
        frame.add(mapViewer, BorderLayout.CENTER);
        raccoonImage = new ImageIcon("raccoon.png").getImage();
        
        // Add listeners for GUI components
        playButton.addActionListener(e -> animate());

        // Set the map center and zoom level
        ArrayList<TripPoint> all = TripPoint.getTrip();
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        for (TripPoint p : all) {
        	minLat = Math.min(minLat, p.getLat());
        	maxLat = Math.max(maxLat, p.getLat());
        	minLon = Math.min(minLon, p.getLon());
        	maxLon = Math.max(maxLon, p.getLon());
        }
        mapViewer.setDisplayPosition(new Coordinate((minLat + maxLat) / 2,
        		(minLon + maxLon) / 2), 6);
    
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    // Animate the trip based on selections from the GUI components
    private static void animate() {
    	if (animationTimer != null) {
    		animationTimer.stop();
    	}
    	mapViewer.removeAllMapMarkers();
    	mapViewer.removeAllMapPolygons();
    	raccoonMarker = null;
    	
    	String selected = (String) timeBox.getSelectedItem();
    	int seconds = selected.equals(COMBO_PROMPT) ? 15 :
    		Integer.parseInt(selected);
    	final long durationMs = seconds * 1000L;
    	
    	final ArrayList<TripPoint> points = stopsBox.isSelected()
    			? TripPoint.getTrip()
    					: TripPoint.getMovingTrip();
    	
    	final long startTime = System.currentTimeMillis();
    	
    	final int[] drawn = { 0 };
    	placeRaccoon(points.get(0));
    	
    	animationTimer = new Timer(TIMER_DELAY_MS, null);
    	animationTimer.addActionListener(e -> {
    		double fraction = Math.min(1.0,  (System.currentTimeMillis() -
    				startTime) / (double) durationMs);
    		int target = (int) Math.round(fraction * (points.size() - 1));
    		
    		while (drawn[0] < target) {
    			TripPoint from = points.get(drawn[0]);
    			TripPoint to = points.get(drawn[0] + 1);
    			addSegment(from, to);
    			drawn[0]++;
    		}
    		
    		placeRaccoon(points.get(drawn[0]));
    		
    		if (fraction >= 1.0) {
    			animationTimer.stop();
    		}
    	});
    	animationTimer.start();
    }
    
    private static void addSegment(TripPoint a, TripPoint b) {
    	Coordinate c1 = new Coordinate(a.getLat(), a.getLon());
    	Coordinate c2 = new Coordinate(b.getLat(), b.getLon());
    	
    	MapPolygonImpl line = new MapPolygonImpl(c1, c2, c1);
    	line.setColor(Color.RED);
    	line.setBackColor(null);
    	line.setStroke(new BasicStroke(3));
    	mapViewer.addMapPolygon(line);
    }
    		
    private static void placeRaccoon(TripPoint p) {
    	if (raccoonMarker != null) {
    		mapViewer.removeMapMarker(raccoonMarker);
    	}
    	raccoonMarker = new IconMarker(new Coordinate(p.getLat(), p.getLon()), raccoonImage);
    	mapViewer.addMapMarker(raccoonMarker);
    }		
}