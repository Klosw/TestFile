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
 * ����:̹����Ϸ��1.0
 * 1.����̹��
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
		this.setResizable(false);// ��С���ɸ���
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// ���õ��رմ���ʱ��֤JVMҲ�˳�
		Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();// ��Ļ����
		Dimension frameSize = this.getSize();// ��Ĵ�С
		this.setLocation((displaySize.width - frameSize.width) / 2, (displaySize.height - frameSize.height) / 2);
	}
}

// һ������ װ̹�� ��ȫ������
class TankPanel extends JPanel implements KeyListener {
	// �����ӵ�
	private final HashSet<Bullet> mFoeBullet = new HashSet<>();
	// ����
	private final HashSet<Tank> mFoeTank = new HashSet<>();
	// ��ֵ�洢
	private final LinkedHashSet<Integer> mKeytmp = new LinkedHashSet<>();
	// �ҵ�tank�������ӵ�����
	private final HashSet<Bullet> mHeroBullet = new HashSet<>();
	// Tank ˢ��ʱ��
	private final int mTimeFoeTank = 10 * 1000;
	// ����ˢ��ʱ��
	private final int mTimekey = 32;
	// FPSˢ��ʱ��
	private int mTimefps = 16;
	private static final long serialVersionUID = -5336208631429308273L;
	// ����һ���ҵ�̹��
	private Hero hero = null;
	private Thread mThread = null;

	private Random mRandom;
	// ��ǰ��Ļ��ʾ���ٸ� �л�
	private final int mMaxFoe = 5;
	// ��ǰ�����ʾ���ٸ�
	private final int mSizeFoe = 20;

	// ֻ�����ƶ��¼�
	private void onKeyDown(int keycode) {
		switch (keycode) {
		// ����
		case 38:
			hero.u();
			break;
		// ��
		case 40:
			hero.d();
			break;
		// ��
		case 37:
			hero.l();
			break;
		// ��
		case 39:
			hero.r();
			break;
		}
	}

	private void onKeyDownBullet(int keycode) {
		switch (keycode) {
		case 32:// �ո�����ӵ�
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

	// ���캯��
	public TankPanel(int width, int height) {
		mRandom = new Random();
		this.setSize(width, height);
		this.width = width;
		this.height = height;
		hero = new Hero(24, 10, 10, width, height);
		startThread();
	}

	private void startThread() {
		// ֹͣ��ǰ�����߳�
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

	// ˢ���߳�
	private Runnable mRunable = new Runnable() {
		@Override
		public void run() {
			try {
				int i = 0;
				for (;;) {// ���߳�
					// ˢ������ 60 FPS
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
							while (m.hasNext()) {// �������������ƺ�����
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

	// ��дpaint
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.black);
		g.fillRect(0, 0, 400, 300);
		hero.onDraw(g);
		// �����ҵ��ӵ�
		Iterator<Bullet> m = mHeroBullet.iterator();
		while (m.hasNext()) {
			Bullet b = m.next();
			if (b.mDirection != Direction.t) {
				b.onDraw(g);
			} else {
				m.remove();
			}
		}
		// ���� ����̹��
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
	// ��������ȥ��
	public void keyPressed(KeyEvent e) {
		if (getMoveKey(e.getKeyCode()))
			mKeytmp.add(e.getKeyCode());
		onKeyDownBullet(e.getKeyCode());
	}

	@Override
	// �����ͷ���
	public void keyReleased(KeyEvent e) {
		if (getMoveKey(e.getKeyCode()))
			mKeytmp.remove(e.getKeyCode());
	}

	@Override
	// ����һ��ֵ�����
	public void keyTyped(KeyEvent e) {
	}
}

// ������
abstract class Unity {
	// �Ƿ��Զ�����
	protected boolean autorun = true;
	// �ٶ�
	protected int speed = 5;

	// ��ʾ̹�˵Ŀ��
	protected int w = 0;
	// ̹�˵��ݸ߶�
	protected int h = 0;

	// ��ʾ̹�˵ĺ�����
	protected int x = 0;
	// ̹�˵�������
	protected int y = 0;
	// ���λ��
	protected int maxx = 0;
	// ���λ��
	protected int maxy = 0;
	// ����
	protected Direction mDirection = Direction.o;

	// ����
	static enum Direction {
		u(90), d(270), l(180), r(0),
		/** ��û�и������� */
		o(-1),
		/** ��ʾ���߽��� */
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
	 *            ��ǰtankλ��
	 * @param y
	 *            ��ǰtankλ��
	 * @param mx
	 *            ���λ��
	 * @param my
	 *            ���λ��
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
	 *            ��ǰtankλ��
	 * @param y
	 *            ��ǰtankλ��
	 * @param mx
	 *            ���λ��
	 * @param my
	 *            ���λ��
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
	 * ��ͼ
	 * 
	 * @param g
	 */
	public abstract void onDraw(Graphics g);

	// ���ƶ�
	public final void u() {
		if (mDirection == Direction.u) {
			this.y -= speed;
			if (y < 0) {
				y = 0;
			}
		}
		this.mDirection = Direction.u;
	}

	// ���ƶ�
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

	// ���ƶ�
	public final void l() {
		if (mDirection == Direction.l) {
			this.x -= speed;
			if (x < 0) {
				x = 0;
			}
		}
		this.mDirection = Direction.l;
	}

	// ���ƶ�
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

// ̹����
class Tank extends Unity {
	/**
	 * TanK ����
	 */
	public int type = TYPE_SELF;

	public static final int TYPE_SELF = 0;
	public static final int TYPE_FOE = 1;

	public Tank(int w, int h, int x, int y, int mx, int my) {
		super(getWW(w), h, x, y, mx - h, my - h * 2 + getWW(w) / 4 + 1);// ����tankʵ��λ��
	}

	// Tank �Ŀ��Ҫ�� 4�ı���������������
	protected static int getWW(int w) {
		w = ((int) (w / 4f + 0.49f)) * 4;// 4��5��fa
		return w;
	}

	// Tank �Ŀ�� ����߶�
	protected static int getHH(int w) {
		return getWW(w) * 25 / 20;
	}

	@Override
	public void onDraw(Graphics g) {
		this.DrawTank(g);

	}

	// �ӵ���ʼλ��
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

	// ����̹�˵ĺ���
	// direct����Ϊ��������--0123
	protected final void DrawTank(Graphics g) {
		// �ж���ʲô���͵�̹��
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
		// ����
		case u:
			int mx = x;
			int my = y;
			int mw = w / 4;
			int mh = h;
			// �����ҵ�̹��(��ʱ�ڷ�װ��һ������)
			// ��������ľ���
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w * 3 / 4;
			my = y;
			mw = w / 4;
			mh = h;
			// �����ұߵľ���
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4;
			my = y + h / 6;
			mw = w / 2;
			mh = h * 2 / 3;
			// �����м����
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4 - 1;
			my = (y + h / 6) + ((h * 2 / 3) - w / 2) / 2;
			mh = w / 2;
			mw = w / 2;
			// ����Բ��
			g.drawOval(mx, my, mw, mh);
			mx = x + w / 2 - 1;
			my = y + h / 2;
			mh = y - h / 10;
			// ������
			g.drawLine(mx, my, mx, mh);
			break;
		// ����
		case d:
			mx = x;
			my = y;
			mw = w / 4;
			mh = h;
			// ��������ľ���
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w * 3 / 4;
			my = y;
			mw = w / 4;
			mh = h;
			// �����ұߵľ���
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4;
			my = y + h / 6;
			mw = w / 2;
			mh = h * 2 / 3;
			// �����м����
			g.fill3DRect(mx, my, mw, mh, false);
			mx = x + w / 4 - 1;
			my = (y + h / 6) + ((h * 2 / 3) - w / 2) / 2;
			mh = w / 2;
			mw = w / 2;
			// ����Բ��
			g.drawOval(mx, my, mw, mh);

			mx = x + w / 2 - 1;
			my = y + h / 2;
			mh = y + h + h / 10;
			// ������
			g.drawLine(mx, my, mx, mh);
			break;
		// ����
		case r:// ��Ϊ���ҵ�ʱ�� ��� ���Ǹ߶�
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
			// ������
			g.drawLine(mx, my, mw, my);
			break;
		// ����
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
			// ������
			g.drawLine(mx, my, mw, my);
			break;

		default:
			break;
		}
	}
}

// �ӵ���
class Bullet extends Unity {

	/**
	 * 
	 * @param w
	 *            �ӵ����
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
	 *            �ӵ����
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
		speed = 6;// Ĭ�Ͽ��
	}

	@Override
	public void onDraw(Graphics g) {
		if (autorun) {
			if (y > maxy || x > maxx || y < 0 || x < 0) {// ���߽���
				mDirection = Direction.t; // ��־λ��
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

// �ҵ�̹��
class Hero extends Tank {
	private Hero(int w, int h, int x, int y, int mx, int my) {
		super(w, h, x, y, mx, my);
		mDirection = Direction.u;// �ҵ�̹�� �ǳ��ϵ�
		autorun = false; // ���Զ�����
	}

	/**
	 * @param w
	 *            Tank �����δ�С
	 * @param x
	 *            xλ��
	 * @param y
	 *            yλ��
	 * @param mx
	 *            ����λ��x
	 * @param my
	 *            ����λ��y <br>
	 *            ���ֱ��� �߶��ÿ�ȼ������
	 */
	public Hero(int w, int x, int y, int mx, int my) {
		this(getWW(w), getHH(w), x, y, mx, my);
	}
}

// ���˵�Tank
class FoeTank extends Tank {
	private FoeTank(int w, int h, int x, int y, int mx, int my) {
		super(w, h, x, y, mx, my);
		mDirection = Direction.d;// �ҵ�̹�� �ǳ��ϵ�
		autorun = true; // �Զ�����
		type = TYPE_FOE;
	}

	public FoeTank(int w, int x, int y, int mx, int my) {
		this(getWW(w), getHH(w), x, y, mx, my);
	}

	@Override
	public void onDraw(Graphics g) {
		// �����Զ�����
		super.onDraw(g);
	}

}
