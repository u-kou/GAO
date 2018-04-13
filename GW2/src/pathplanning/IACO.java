package pathplanning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import node.Location;

/**
 * IACO
 * @author taku
 *
 */
public class IACO extends Thread {

	private boolean debug = false;
	private int displayTime = 500;
	
	private VRP vrp;
	private boolean isSolving;
	
	// 定数
	private double alpha; 	// フェロモンの影響度
	private double beta;	// 可視性の影響度
	private double pmin;	// 突然変異率（小）
	private double pmax;	// 突然変異率（大）
	private int T;			// 最大の世代
	private double rho;		// フェロモンの蒸発速度
	private double Q;		// 定数
	
	// FR(k)の Path のリスト
	private List<List<Integer>> paths;
	
	// FR(k)の 訪問不可カスタマーリスト
	// 他の FR に訪問されたかどうか
	private List<List<Integer>> taboos;
	
	// FR(k)の 現在位置（カスタマーの位置）
	private int[] currentPosition;
	
	// 変数
	private double[][] p;	// Path(i,j)のフェロモン
	private double[][] v;	// Path(i,j)の可視性
	private double[][] d;	// Path(i,j)の距離
	private double[] D;		// 各 Path の距離
	private int[] m;		// 各 Path の顧客数
	private int[] q;		// 各 FR の容量
	private double L;		// 全ての Path の総距離
	private double K;		// Path の数
	
	public IACO(VRP vrp) {
		this.vrp = vrp;
		this.alpha = 0.5f;
		this.beta = 1.0f;
		this.pmin = 1.0/vrp.getCustomers().size() - 1;
		this.pmax = 1.0/vrp.getFlyingRouters().size();
		this.T = vrp.getCustomers().size() * 100;
		this.rho = 0.8;
		this.Q = 100;
		this.isSolving = false;
	}
	
	public void init() {
		initDistance();
		initPheromone();
		initVisibility();
	}
	
	@Override
	public void run() {
	}
	
	public void caluculate()  {
		try {
			init();
			for (int t = 0; t <= T; t++) {
				//System.out.println((int)(t/(float)T * 100) + " %");
				initCapacity();
				initCurrentPosition();
				initPaths();
				initTaboos();
				routeConstruction();
				if (isMutation(t)) {
					mutate();
				}
				for (int k=0; k < paths.size(); k++) {
					localSearch(k);
				}
				
				// 各変数を計算
				L = 0;
				K = paths.size();
				D = new double[paths.size()];
				m = new int[paths.size()];
				for (int k=0; k< D.length; k++) {
					List<Integer> path = paths.get(k);
					D[k] = calculatePathLength(k);
					L += D[k];
					if ((m[k] = path.size()) != 0)
						m[k] -= 2; // Depot の分引く
				}
				
				updateAllPheromone();
				
			}
			vrp.setSolution(paths);
			vrp.setDistances(D);
			System.out.println("ネットワーク構築完了");
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	private double deltaPheromone(int k, int i, int j) {
		if (!isLinkExists(k,i,j)) 
			return 0;
		double global = Q / (K * L);
		double local = (D[k] - d[i][j])/(m[k] * D[k]);
		return global * local;
	}
	
	private double updatePheromone(int i, int j) {
		double sum = 0;
		for (int k=0; k< paths.size(); k++) {
			sum += deltaPheromone(k, i, j);
		}
		double ret = rho * p[i][j] + sum;
		//System.out.println("Link(" + i + "," + j + ")のフェロモン：" + ret);
		return ret;
	}
	
	private void updateAllPheromone() {
		//System.out.println("updateAllPheromone");
		for (int i=0; i<vrp.getCustomers().size(); i++) {
			for (int j=0; j<vrp.getCustomers().size(); j++) {
				if (i == j)
					continue;
				p[i][j] = updatePheromone(i, j);
			}
		}
	}
	
	private void localSearch(int k) throws InterruptedException {
		//System.out.println("localSearch");
		List<Integer> path = paths.get(k);
		// 2-opt
		double da, db, preD, postD;
		for (int i=0; i<path.size()-1; i++) {
			for (int j=i+2; j<path.size()-1; j++) {
				// 交換前の距離
				da = d[path.get(i)][path.get(i+1)];
				db = d[path.get(j)][path.get(j+1)];
				preD = da + db;
				// 交換後の距離
				da = d[path.get(i)][path.get(j)];
				db = d[path.get(i+1)][path.get(j+1)];
				postD = da + db;
				if (postD < preD) {
					int m = i+1;
					int n = j;
					while(m < n) {
						// m と n 間の Path を逆順に変更
						swapPoints(k, m, k, n);
						m++; n--;
					}
					// 交換したら最初からやり直し
					if (debug)
						sleep(displayTime);
					i = 0;
					break;
				}
			}
		}
	}
	
	private void mutate() throws InterruptedException {
		//System.out.println("mutate");
		// Select 2 tours
		int k, k1, k2;
		List<Integer> path1;
		List<Integer> path2;
		Random r = new Random();
		k = paths.size();
		if (k < 2) {
			return;
		}
		while (true) {
			k1 = r.nextInt(k);
			k2 = r.nextInt(k-1);
			//System.out.println("k1:" + k1 + " k2:" + k2);
			path1 = paths.get(k1);
			path2 = paths.get(k2);
			if (k1 != k2 && path1.size() != 2 && path2.size() != 2)
				break;
		}
		
		// Select 2 points (except for depot)
		int i1 = r.nextInt(path1.size() - 2) + 1;
		int i2 = r.nextInt(path2.size() - 2) + 1;
		
		// Exchange 2 points
		swapPoints(k1, i1, k2, i2);
		if (debug)
			sleep(displayTime);

	}
	
	private boolean isMutation(int t) {
		double pm = pmin;
		pm += Math.pow(pmax - pmin, 1-(double)t/T);
		Random r = new Random();
		if (pm > r.nextDouble()) {
			return true;
		}
		return false;
	}
	
	private void routeConstruction() throws InterruptedException {
		//System.out.println("routeConstruction");
		for (int k=0; k < paths.size(); k++) {
			while (q[k] > 0) {
				int ret = selectCustomer(k, currentPosition[k]);
				if (ret != 0) {
					currentPosition[k] = ret;
					insertAllTaboos(ret);
					addPath(k, ret);
					q[k]--;
					if (debug)
						sleep(displayTime);
				} 
			}
			currentPosition[k] = 0;
			addPath(k, 0);
			if (debug)
				sleep(displayTime);
		}
	}
	
	/**
	 * FR(k)が c(i) にいる時の，次の行き先を返す
	 * @param k
	 * @return
	 */
	private int selectCustomer(int k, int i) {
		for (int j=1; j<vrp.getCustomers().size(); j++) {
			double p = probability(k, i, j);
			Random r = new Random();
			if (p > r.nextDouble()) {
				return j;
			}
		}
		return 0;
	}
	
	/**
	 * FR(k) が c(i) にいる時，c(j) を次の行き先として選択する確率
	 * @param k
	 * @param i
	 * @param j
	 * @return
	 */
	private double probability(int k, int i, int j) {
		if (isTaboo(k, j) || i == j) {
			// c(j)がTaboo(k)に入ってたら訪問する確率は 0
			// i から j に行く確率も 0
			return 0;
		}
		// c(j)がTaboo(k)に入ってない場合は確率を計算
		double sum = 0;
		for (int h=1; h<vrp.getCustomers().size(); h++) {
			if (isTaboo(k, h) || i == h)
				continue;
			//System.out.println("i = " + i + " h = " + h);
			//System.out.println("p[i][h] = " + p[i][h] + " v[i][h] = " + v[i][h]);
			sum += Math.pow(p[i][h], alpha) * Math.pow(v[i][h], beta);
		}
		
		return Math.pow(p[i][j], alpha) * Math.pow(v[i][j], beta) / sum;
	}
	
	private void initCurrentPosition() {
		this.currentPosition = new int[vrp.getFlyingRouters().size()];
		for (int k=0; k<currentPosition.length; k++) {
			this.currentPosition[k] = 0;
		}
	}
	
	private void initPaths() {
		vrp.initPaths();
		this.paths = new ArrayList<List<Integer>>();
		for (int k=0; k<vrp.getFlyingRouters().size(); k++) {
			this.paths.add(new ArrayList<Integer>());
			this.addPath(k, 0);
		}
	}
	
	private void initTaboos() {
		this.taboos = new ArrayList<List<Integer>>();
		for (int k=0; k<vrp.getFlyingRouters().size(); k++) {
			this.taboos.add(new ArrayList<Integer>());
		}
	}
	
	private void initDistance() {
		int customersSize = vrp.getCustomers().size();
		this.d = new double[customersSize][customersSize];
		for (int i=0; i < customersSize; i++) {
			for (int j=0; j < customersSize; j++) {
				d[i][j] = vrp.getDistance(i, j);
			}
		}
	}
	
	private void initVisibility() {
		// d を初期化してから呼び出す
		int customersSize = vrp.getCustomers().size();
		this.v = new double[customersSize][customersSize];
		for (int i=0; i < customersSize; i++) {
			for (int j=0; j < customersSize; j++) {
				v[i][j] = 1/d[i][j];
			}
		}
	}
	
	private void initPheromone() {
		int customersSize = vrp.getCustomers().size();
		this.p = new double[customersSize][customersSize];
		for (int i=0; i < customersSize; i++) {
			for (int j=0; j < customersSize; j++) {
				p[i][j] = 1;
			}
		}
	}
	
	private void initCapacity() {
		int k, about, cNum, frNum;
		frNum = vrp.getFlyingRouters().size();
		cNum = vrp.getCustomers().size() - 1;
		about = cNum / frNum;
		this.q = new int[frNum];
		for (k=0; k<frNum; k++) {
			q[k] = about;
		}
		cNum -= about * frNum;
		for(k=0; cNum>0; k++) {
			q[k]++;
			cNum--;
		}
	}
	
	/**
	 * Taboo(k)にc(i)が入ってるかチェック
	 * @param k
	 * @param i
	 * @return
	 */
	private boolean isTaboo(int k, int j) {
		List<Integer> tabooK = taboos.get(k);
		for (Integer i: tabooK) {
			if (i.intValue() == j)
				return true;
		}
		return false;
	}
	
	/**
	 * Taboo(k)にc(i)を入れる
	 * @param k
	 * @param i
	 */
	private void insertTaboo(int k, int i) {
		List<Integer> tabooK = taboos.get(k);
		tabooK.add(i);
	}
	
	/**
	 * 全てのTabooリストにc(i)を入れる
	 * @param i
	 */
	private void insertAllTaboos(int i) {
		for (int k=0; k < taboos.size(); k++) {
			insertTaboo(k, i);
		}
	}

	public boolean isSolving() {
		return isSolving;
	}

	public void setSolving(boolean isSolving) {
		this.isSolving = isSolving;
	}
	
	/**
	 * 全てのカスタマーが訪問されたかどうか
	 * @return
	 */
	public boolean isAllCustomersVisited() {
		int sum = 0;
		for (int k=0; k<paths.size(); k++) {
			List<Integer> path = paths.get(k);
			for (int m=0; m<path.size(); m++) {
				if (path.get(m).intValue() != 0) {
					// Depot じゃないなら足す
					sum += 1;
				}
			}
		}
		return sum == vrp.getCustomers().size() - 1;
	}
	
	/**
	 * Path(k) にC(i)を追加
	 * @param k
	 * @param i
	 */
	private void addPath(int k, int i) {
		List<Integer> pathK = this.paths.get(k);
		pathK.add(i);
		if (pathK.size() >= 2) {
			int c1 = pathK.get(pathK.size() - 2);
			int c2 = pathK.get(pathK.size() - 1);
			if (c1 == c2) {
				// c1 と c2 が同じ（depot->depot等）場合、Path を作らない
				return;
			}
			Location loc1 = this.vrp.getCustomers().get(c1).getLocation();
			Location loc2 = this.vrp.getCustomers().get(c2).getLocation();
			Path path = new Path(loc1, loc2);
			vrp.addPath(path);
		}
	}
	
	/**
	 * Path(k1) の c(i1) と Path(k2) の c(i2) を交換
	 * @param k1
	 * @param i1
	 * @param k2
	 * @param i2
	 */
	private void swapPoints(int k1, int i1, int k2, int i2) {
		// Exchange 2 points
		List<Integer> path1 = paths.get(k1);
		List<Integer> path2 = paths.get(k2);
		int c1 = path1.get(i1);
		int c2 = path2.get(i2);
		path1.remove(i1);
		path1.add(i1, c2);
		path2.remove(i2);
		path2.add(i2, c1);
		vrp.setPaths(getAllPaths());
	}
	
	/**
	 * Path の リストを生成
	 * @return
	 */
	private List<Path> getAllPaths() {
		List<Path> ret = new ArrayList<Path>();
		for (int k=0; k<paths.size(); k++) {
			List<Integer> pathK = paths.get(k);
			for (int m=0; m<pathK.size() - 1; m++) {
				int c1 = pathK.get(m);
				int c2 = pathK.get(m+1);
				if (c1 == c2) {
					// c1 と c2 が同じ（depot->depot等）場合、Path を作らない
					continue;
				}
				Location loc1 = this.vrp.getCustomers().get(c1).getLocation();
				Location loc2 = this.vrp.getCustomers().get(c2).getLocation();
				Path path = new Path(loc1, loc2);
				ret.add(path);
			}
		}
		return ret;
	}
	
	/**
	 * path(k) の距離を計算
	 * @param k
	 * @return
	 */
	private double calculatePathLength(int k) {
		double sum = 0;
		List<Integer> path = paths.get(k);
		for (int m=0; m<path.size() - 1; m++) {
			sum += d[path.get(m)][path.get(m+1)];
		}
		return sum;
	}
	
	/**
	 * Path(k)にc(i)->c(j)のリンクがあるかどうかチェック
	 * @param k
	 * @param i
	 * @param j
	 * @return
	 */
	private boolean isLinkExists(int k, int i, int j) {
		List<Integer> path = paths.get(k);
		for (int m=0; m<path.size()-1; m++) {
			if (path.get(m) == i && path.get(m+1) == j)
				return true;
		}
		return false;
	}
	
	public void outputPheromone(String fileName) {
		double avg = 0;
		for (int i=0; i<p.length; i++) {
			for (int j=0; j<p.length; j++) {
				if (i!=j)
					avg += p[i][j];
			}
		}
		avg = avg/(p.length * p.length - p.length);
		File file = new File(fileName);
		try {
			FileWriter filewriter = new FileWriter(file);
			
			for (int i=0; i<p.length; i++) {
				for (int j=0; j<p.length; j++) {
					String s;
					if (p[i][j] - avg > 0) 
						s = "more";
					else
						s = "less";
					filewriter.write(s);
					if (j != p.length -1)
						filewriter.write(",");
				}
				filewriter.write(System.lineSeparator());
			}
			
			filewriter.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
}
