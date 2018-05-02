package com.zhou.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.zhou.game.Unity.Direction;

/*
 * 功能:坦克游戏的1.0
 * 1.画出坦克
 * */
public class TankGame extends JFrame {
	private static final long serialVersionUID = -5239748238921132750L;

	public static void main(String[] args) {
		TankGame.start();
	}

	public static void start() {
		new TankGame();
	}

	private TankPanel mp = null;
	private int width = 400, height = 300;

	public TankGame() {
		mp = new TankPanel(width, height);
		this.addKeyListener(mp);
		this.add(mp);
		this.setSize(width, height);
		this.setResizable(false);// 大小不可更改
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 设置当关闭窗口时保证JVM也退出
		Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();// 屏幕属性
		Dimension frameSize = this.getSize();// 框的大小
		this.setLocation((displaySize.width - frameSize.width) / 2, (displaySize.height - frameSize.height) / 2);
	}
}

// 一个容器 装坦克 和全部背景
class TankPanel extends JPanel implements KeyListener {
	// 敌人子弹
	private final HashSet<Bullet> mFoeBullet = new HashSet<>();
	// 敌人
	private final HashSet<Tank> mFoeTank = new HashSet<>();
	// 键值存储
	private final LinkedHashSet<Integer> mKeytmp = new LinkedHashSet<>();
	// 我的tank发出的子弹集合
	private final HashSet<Bullet> mHeroBullet = new HashSet<>();
	// Tank 刷新时间
	private final int mTimeFoeTank = 10 * 1000;
	// 按键刷新时间
	private final int mTimekey = 32;
	// FPS刷新时间
	private int mTimefps = 16;
	private static final long serialVersionUID = -5336208631429308273L;
	// 定义一个我的坦克
	private Hero hero = null;
	private Thread mThread = null;

	private Random mRandom;
	// 当前屏幕显示多少个 敌机
	private final int mMaxFoe = 5;
	// 当前最多显示多少个
	private final int mSizeFoe = 20;

	// 只处理移动事件
	private void onKeyDown(int keycode) {
		switch (keycode) {
		// 向上
		case 38:
			hero.u();
			break;
		// 下
		case 40:
			hero.d();
			break;
		// 左
		case 37:
			hero.l();
			break;
		// 右
		case 39:
			hero.r();
			break;
		}
	}

	private void onKeyDownBullet(int keycode) {
		switch (keycode) {
		case 32:// 空格键发子弹
			int[] is = hero.getBulletStartCoordinate();
			Bullet bu = new Bullet(is[0], is[1], width, height);
			bu.mDirection = hero.mDirection;
			mHeroBullet.add(bu);
			break;
		}
	}

	private boolean getMoveKey(int keycode) {
		switch (keycode) {
		case 38:
		case 40:
		case 37:
		case 39:
			return true;
		}
		return false;
	}

	final int width, height;

	// 构造函数
	public TankPanel(int width, int height) {
		mRandom = new Random();
		this.setSize(width, height);
		this.width = width;
		this.height = height;
		hero = new Hero(24, 10, 10, width, height);
		startThread();
	}

	private void startThread() {
		// 停止当前运行线程
		if (mThread != null) {
			try {
				if (mThread.isAlive())
					mThread.interrupt();
				mThread = null;
			} catch (Exception e) {
			}
		}
		mThread = new Thread(mRunable);
		mThread.start();
	}

	private void stopThread() {
		if (mThread != null) {
			try {
				if (mThread.isAlive())
					mThread.interrupt();
				mThread = null;
			} catch (Exception e) {
			}
		}
	}

	// 刷新线程
	private Runnable mRunable = new Runnable() {
		@Override
		public void run() {
			try {
				int i = 0;
				for (;;) {// 死线程
					// 刷新率是 60 FPS
					TankPanel.this.repaint();
					Thread.sleep(mTimefps);
					i++;
					if (i == Integer.MAX_VALUE)
						i = 0;
					if (mKeytmp.size() != 0) {
						if ((i % ((int) (((mTimekey / mTimefps * 1.0) + 0.6)))) == 0) {
							// mKeytmp.
							int cc = 0;
							Iterator<Integer> m = mKeytmp.iterator();
							while (m.hasNext()) {// 按键处理这样似乎不好
								cc = m.next();
							}
							onKeyDown(cc);
						}
					}
					if (mFoeTank.size() < mMaxFoe) {
						if (i * mTimefps > mTimeFoeTank && (i * mTimefps) % mTimeFoeTank == 0) {
							FoeTank foe = new FoeTank(24, Math.abs(mRandom.nextInt(width)), 0, width, height);
							mFoeTank.add(foe);
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	// 重写paint
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.black);
		g.fillRect(0, 0, 400, 300);
		hero.onDraw(g);
		// 绘制我的子弹
		Iterator<Bullet> m = mHeroBullet.iterator();
		while (m.hasNext()) {
			Bullet b = m.next();
			if (b.mDirection != Direction.t) {
				b.onDraw(g);
			} else {
				m.remove();
			}
		}
		// 绘制 敌人坦克
		Iterator<Tank> m2 = mFoeTank.iterator();
		while (m2.hasNext()) {
			Tank b = m2.next();
			if (b.mDirection != Direction.t) {
				b.onDraw(g);
			} else {
				m.remove();
			}
		}
	}

	@Override
	// 键被按下去了
	public void keyPressed(KeyEvent e) {
		if (getMoveKey(e.getKeyCode()))
			mKeytmp.add(e.getKeyCode());
		onKeyDownBullet(e.getKeyCode());
	}

	@Override
	// 键被释放了
	public void keyReleased(KeyEvent e) {
		if (getMoveKey(e.getKeyCode()))
			mKeytmp.remove(e.getKeyCode());
	}

	@Override
	// 键的一个值被输出
	public void keyTyped(KeyEvent e) {
	}
}

// 个体类
abstract class Unity {
	// 是否自动运行
	protected boolean autorun = true;
	// 速度
	protected int speed = 5;

	// 表示坦克的宽度
	protected int w = 0;
	// 坦克的纵高度
	protected int h = 0;

	// 表示坦克的横坐标
	protected int x = 0;
	// 坦克的纵坐标
	protected int y = 0;
	// 最大位置
	protected int maxx = 0;
	// 最大位置
	protected int maxy = 0;
	// 方向
	protected Direction mDirection = Direction.o;

	// 方向
	static enum Direction {
		u(90), d(270), l(180), r(0),
		/** 还没有给定方向 */
		o(-1),
		/** 表示出边界了 */
		t(-2);
		int i;

		Direction(int i) {
			this.i = i;
		}
	}

	public Unity(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 
	 * @param x
	 *            当前tank位置
	 * @param y
	 *            当前tank位置
	 * @param mx
	 *            最大位置
	 * @param my
	 *            最大位置
	 */
	public Unity(int x, int y, int mx, int my) {
		this.x = x;
		this.y = y;
		maxx = mx;
		maxy = my;
	}

	/**
	 * 
	 * @param x
	 *            当前tank位置
	 * @param y
	 *            当前tank位置
	 * @param mx
	 *            最大位置
	 * @param my
	 *            最大位置
	 */
	public Unity(int w, int h, int x, int y, int mx, int my) {
		this.x = x;
		this.y = y;
		maxx = mx;
		maxy = my;
		this.w = w;
		this.h = h;
	}

	/**
	 * 画图
	 * 
	 * @param g
	 */
	public abstract void onDraw(Graphics g);

	// 上移动
	public final void u() {
		if (mDirection == Direction.u) {
			this.y -= speed;
			if (y < 0) {
				y = 0;
			}
		}
		this.mDirection = Direction.u;
	}

	// 下移动
	public final void d() {
		if (mDirection == Direction.d) {
			this.y += speed;
			if (maxy != 0) {
				if (y > maxy) {
					y = maxy;
				}
			}
		}
		this.mDirection = Direction.d;
	}

	// 左移动
	public final void l() {
		if (mDirection == Direction.l) {
			this.x -= speed;
			if (x < 0) {
				x = 0;
			}
		}
		this.mDirection = Direction.l;
	}

	// 右移动
	public final void r() {
		if (mDirection == Direction.r) {
			this.x += speed;
			if (maxx != 0) {
				if (x > maxx) {
					x = maxx;
				}
			}
		}
		this.mDirection = Direction.r;
	}

}

// 坦克类
class Tank extends Unity {
	/**
	 * TanK 类型
	 */
	public int type = TYPE_SELF;

	public static final int TYPE_SELF = 0;
	public static final int TYPE_FOE = 1;

	public Tank(int w, int h, int x, int y, int mx, int my) {
		super(getWW(w), h, x, y, mx - h, my - h * 2 + getWW(w) / 4 + 1);// 更改tank实际位置
	}

	// Tank 的宽度要是 4的倍数才能完美呈现
	protected static int getWW(int w) {
		w = ((int) (w / 4f + 0.49f)) * 4;// 4舍5入fa
		return w;
	}

	// Tank 的宽度 计算高度
	protected static int getHH(int w) {
		return getWW(w) * 25 / 20;
	}

	@Override
	public void onDraw(Graphics g) {
		this.DrawTank(g);

	}

	// 子弹开始位置
	public int[] getBulletStartCoordinate() {
		int[] is = new int[2];
		int wx = 0;
		int hy = 0;
		switch (mDirection) {
		case u:
			wx = x + w / 2 - 1;
			hy = y - h / 10;
			break;
		case d:
			wx = x + w / 2 - 1;
			hy = y + h + h / 10;
			break;
		case l:
			hy = y + w / 2 - 1;
			wx = x - h / 10;
			break;
		case r:
			hy = y + w / 2 - 1;
			wx = x + h + h / 10;
			break;
		default:
			break;
		}
		is[0] = wx;
		is[1] = hy;
		return is;
	}

	// 画出坦克的函数
	// direct方向为上下左右--0123
	protected final void DrawTank(Graphics g) {
		// 判断是什么类型的坦克
		switch (type) {
		case TYPE_SELF:
			g.setColor(Color.cyan);
			break;
		case TYPE_FOE:
			g.setColor(Color.yellow);
			break;
		}
		// g.setColor(Color.yellow);
		// g.fill3DRect(x, y, (mDirection == Direction.r || mDirection ==
		// Direction.l) ? h : w,
		// (mDirection == Direction.r || mDirection == Direction.l) ? w : h,
		// false);
		// g.setColor(Color.cyan);
		switch (mDirection) {
		// 向上
		case u:
			int mx = x;
			int my = y;
			int mw = w / 4;
			int mh = h;
			// 画出我的坦克(到时在封装成一个函数)
			// 画出左面的矩形
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w * 3 / 4;
			my = y;
			mw = w / 4;
			mh = h;
			// 画出右边的矩形
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4;
			my = y + h / 6;
			mw = w / 2;
			mh = h * 2 / 3;
			// 画出中间矩形
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4 - 1;
			my = (y + h / 6) + ((h * 2 / 3) - w / 2) / 2;
			mh = w / 2;
			mw = w / 2;
			// 画出圆形
			g.drawOval(mx, my, mw, mh);
			mx = x + w / 2 - 1;
			my = y + h / 2;
			mh = y - h / 10;
			// 画出线
			g.drawLine(mx, my, mx, mh);
			break;
		// 向下
		case d:
			mx = x;
			my = y;
			mw = w / 4;
			mh = h;
			// 画出左面的矩形
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w * 3 / 4;
			my = y;
			mw = w / 4;
			mh = h;
			// 画出右边的矩形
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4;
			my = y + h / 6;
			mw = w / 2;
			mh = h * 2 / 3;
			// 画出中间矩形
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4 - 1;
			my = (y + h / 6) + ((h * 2 / 3) - w / 2) / 2;
			mh = w / 2;
			mw = w / 2;
			// 画出圆形
			g.drawOval(mx, my, mw, mh);

			mx = x + w / 2 - 1;
			my = y + h / 2;
			mh = y + h + h / 10;
			// 画出线
			g.drawLine(mx, my, mx, mh);
			break;
		// 向右
		case r:// 当为左右的时候 宽度 就是高度
			mx = x;
			my = y;
			mw = h;
			mh = w / 4;
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x;
			my = y + w * 3 / 4;
			mw = h;
			mh = w / 4;
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + h / 6;
			my = y + w / 4;
			mw = h * 2 / 3;
			mh = w / 2;
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + h / 6 + (h * 2 / 3 - w / 2) / 2;
			my = y + (w / 4) - 1;
			mh = w / 2;
			mw = w / 2;
			g.drawOval(mx, my, mw, mh);
			mx = x + h / 2;
			my = y + w / 2 - 1;
			mw = x + h + h / 10;
			// 画出线
			g.drawLine(mx, my, mw, my);
			break;
		// 向左
		case l:
			mx = x;
			my = y;
			mw = h;
			mh = w / 4;
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x;
			my = y + w * 3 / 4;
			mw = h;
			mh = w / 4;
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + h / 6;
			my = y + w / 4;
			mw = h * 2 / 3;
			mh = w / 2;
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + h / 6 + (h * 2 / 3 - w / 2) / 2;
			my = y + (w / 4) - 1;
			mh = w / 2;
			mw = w / 2;
			g.drawOval(mx, my, mw, mh);
			mx = x + h / 2;
			my = y + w / 2 - 1;
			mw = x - h / 10;
			// 画出线
			g.drawLine(mx, my, mw, my);
			break;

		default:
			break;
		}
	}
}

// 子弹类
class Bullet extends Unity {

	/**
	 * 
	 * @param w
	 *            子弹体积
	 * @param x
	 * @param y
	 * @param mx
	 * @param my
	 */
	public Bullet(int x, int y, int mx, int my) {
		this(2, 2, x, y, mx, my);
	}

	/**
	 * 
	 * @param w
	 *            子弹体积
	 * @param x
	 * @param y
	 * @param mx
	 * @param my
	 */
	public Bullet(int w, int x, int y, int mx, int my) {
		this(w, w, x, y, mx, my);
	}

	public Bullet(int w, int h, int x, int y, int mx, int my) {
		super(w, h, x, y, mx, my);
		speed = 6;// 默认快点
	}

	@Override
	public void onDraw(Graphics g) {
		if (autorun) {
			if (y > maxy || x > maxx || y < 0 || x < 0) {// 出边界了
				mDirection = Direction.t; // 标志位置
				return;
			}
			switch (mDirection) {
			case d:
				y += speed;
				break;
			case u:
				y -= speed;
				break;
			case l:
				x -= speed;
				break;
			case r:
				x += speed;
				break;
			default:
				break;
			}
		}
		int mx = x;
		int my = y;
		int mw = w;
		int mh = h;
		g.setColor(Color.WHITE);
		if (x == y) {
			g.fill3DRect(mx, my, mw, mh, false);
		} else {
			switch (mDirection) {
			case d:
			case u:
				g.fill3DRect(mx, my, mw, mh, false);
				break;
			case l:
			case r:
				g.fill3DRect(mx, my, mh, mw, false);
				break;
			default:
				break;
			}

		}

	}
}

// 我的坦克
class Hero extends Tank {
	private Hero(int w, int h, int x, int y, int mx, int my) {
		super(w, h, x, y, mx, my);
		mDirection = Direction.u;// 我的坦克 是朝上的
		autorun = false; // 不自动运行
	}

	/**
	 * @param w
	 *            Tank 的体形大小
	 * @param x
	 *            x位置
	 * @param y
	 *            y位置
	 * @param mx
	 *            限制位置x
	 * @param my
	 *            限制位置y <br>
	 *            保持比例 高度用宽度计算出来
	 */
	public Hero(int w, int x, int y, int mx, int my) {
		this(getWW(w), getHH(w), x, y, mx, my);
	}
}

// 敌人的Tank
class FoeTank extends Tank {
	private FoeTank(int w, int h, int x, int y, int mx, int my) {
		super(w, h, x, y, mx, my);
		mDirection = Direction.d;// 我的坦克 是朝上的
		autorun = true; // 自动运行
		type = TYPE_FOE;
	}

	public FoeTank(int w, int x, int y, int mx, int my) {
		this(getWW(w), getHH(w), x, y, mx, my);
	}

	@Override
	public void onDraw(Graphics g) {
		// 处理自动运行
		super.onDraw(g);
	}

}
