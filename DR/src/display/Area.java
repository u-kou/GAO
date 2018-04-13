package display;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



//import environment.ParamConfiguration;
import pathplanning.Path;
//import node.Gateway;
//import node.Location;
import node.RMICNNode;

/*
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
*/

/**
 * 対象エリアの様子を表示するクラス
 * @author taku
 *
 */
public class Area {
	
	//private static final long serialVersionUID = 1L;
	
	/*
	private int nodeSize;
	private float width;
	private float height;
	*/
	
	/*
	private Dimension windowSize;
	
	// Node Color Settings
	private Color drColor;
	private Color frColor;
	private Color gwColor;
	
	private Color pathColor;
	
	private JFrame frame;
	*/

	// Nodes
	private Map<String, RMICNNode> nodes;
	
	// Paths
	private List<Path> paths;
	
	public Area() {
		/*
		this.frame = frame;
		
		// Settings
		this.nodeSize = 10;
		this.width = ParamConfiguration.scale;
		this.height = ParamConfiguration.scale;
		this.drColor  = Color.RED;
		this.frColor = Color.BLUE;
		this.gwColor = Color.BLACK;
		this.pathColor = Color.BLACK;
		
		this.windowSize = this.frame.getContentPane().getSize();
		this.setPreferredSize(windowSize);
		this.setBackground(Color.WHITE);
		*/
		this.nodes = new ConcurrentHashMap<String, RMICNNode>();
		this.paths = new ArrayList<Path>();
	}
	
	public void addNode(RMICNNode node) {
		nodes.put(node.getName(), node);
		
		//this.repaint();
		
	}
	
	public void setNodes(Map<String, RMICNNode> nodes) {
		this.nodes = nodes;
		//this.repaint();
	}
	
	public void initNodes() {
		nodes.clear();
		//this.repaint();
	}
	
	public void addPath(Path path) {
		paths.add(path);
		//this.repaint();
	}
	
	public void setPaths(List<Path> paths) {
		this.paths = paths;
		//this.repaint();
	}
	
	public void initPaths() {
		this.paths.clear();
		//this.repaint();
	}
	
	public void deleteNode(String nodeName) {
		nodes.remove(nodeName);
		//this.repaint();
	}

	/*
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		int allContentNum = 0;
		int allAvgRetrievalTime = 0;
		
		// Paint nodes
		for (RMICNNode node: nodes.values()) {
			Location l = node.getNowLocation();
			if(!isOutBound(l)) {
				Point p = getRelativePosition(l);
				if (node.getNodeType() == RMICNNode.TYPE_GW) {
					g.setColor(gwColor);
					Gateway gw = (Gateway) node;
					int interestNum = gw.getInterestNum();
					int contentNum = gw.getContentNum();
					int avgRetrievalTime = gw.getContentRetrivalTimeAvg();
					g2.drawString(node.getName() 
							+ "(" + interestNum + "," + contentNum + ")" , p.x, p.y);
					allAvgRetrievalTime = (int)
							((allContentNum * allAvgRetrievalTime 
							+ contentNum * avgRetrievalTime)
							/ (double)(allContentNum + contentNum));
					allContentNum += contentNum;
				} else if (node.getNodeType() == RMICNNode.TYPE_FR) {
					g.setColor(frColor);
					g2.drawString(node.getName(), p.x, p.y);
				} else {
					g.setColor(drColor);
					g2.drawString(node.getName(), p.x, p.y);
				}
				g2.fillOval(p.x + 8, p.y, nodeSize, nodeSize);
			}
		}
		
		// Paint paths
		float[] dash = {2.0f};
		BasicStroke dashStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		g2.setStroke(dashStroke);
		g2.setColor(pathColor);
		Location locA = new Location();
		Location locB = new Location();
		float tweak = nodeSize/2;
		try {

			g2.drawString("Content Num: " + allContentNum, 5, 15);
			g2.drawString("Content Retrieval Time (AVG.): " + allAvgRetrievalTime + " [ms]", 5, 30);
			
			for (int i=0; i<paths.size(); i++) {
				Path path = paths.get(i);
				if (path == null)
					continue;
				Location nodeALoc = path.getLocA();
				Location nodeBLoc = path.getLocB();
				locA.setLocation(nodeALoc.x + tweak, nodeALoc.y + tweak);
				locB.setLocation(nodeBLoc.x + tweak, nodeBLoc.y + tweak);
				Point pA = getRelativePosition(locA);
				Point pB = getRelativePosition(locB);
				g2.draw(new Line2D.Double(pA.x, pA.y, pB.x, pB.y));
			}
		} catch(IndexOutOfBoundsException e) {
			// do nothing
		}
		
	}
	
	*/
	
	/*
	private Point getRelativePosition(Location l) {
		int x = (int) (windowSize.width * 0.95 * (float) l.x / width);
		int y = (int) (windowSize.height * 0.95 * (float) l.y / height);
		return new Point(x, y);
	}
	
	private boolean isOutBound(Location l) {
		if (l.x > width || l.y > height || l.x < 0 || l.y < 0)
			return true;
		return false;
	}
	
	*/


}
