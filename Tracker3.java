
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Tracker3 extends Canvas implements KeyListener, MouseListener, MouseMotionListener, WindowListener
{
	public static final String VERSION = "Tracker3 (c)2020 mumart@gmail.com";
	
	private static final long[] TOPAZ_8 = new long[]
	{
		0x0000000000000000L,
		0x1818181818001800L,
		0x6C6C000000000000L,
		0x6C6CFE6CFE6C6C00L,
		0x183E603C067C1800L,
		0x0066ACD8366ACC00L,
		0x386C6876DCCE7B00L,
		0x1818300000000000L,
		0x0C18303030180C00L,
		0x30180C0C0C183000L,
		0x00663CFF3C660000L,
		0x0018187E18180000L,
		0x0000000000181830L,
		0x0000007E00000000L,
		0x0000000000181800L,
		0x03060C183060C000L,
		0x3C666E7E76663C00L,
		0x1838781818181800L,
		0x3C66060C18307E00L,
		0x3C66061C06663C00L,
		0x1C3C6CCCFE0C0C00L,
		0x7E607C0606663C00L,
		0x1C30607C66663C00L,
		0x7E06060C18181800L,
		0x3C66663C66663C00L,
		0x3C66663E060C3800L,
		0x0018180000181800L,
		0x0018180000181830L,
		0x0006186018060000L,
		0x00007E007E000000L,
		0x0060180618600000L,
		0x3C66060C18001800L,
		0x7CC6DED6DEC07800L,
		0x3C66667E66666600L,
		0x7C66667C66667C00L,
		0x1E30606060301E00L,
		0x786C6666666C7800L,
		0x7E60607860607E00L,
		0x7E60607860606000L,
		0x3C66606E66663E00L,
		0x6666667E66666600L,
		0x3C18181818183C00L,
		0x0606060606663C00L,
		0xC6CCD8F0D8CCC600L,
		0x6060606060607E00L,
		0xC6EEFED6C6C6C600L,
		0xC6E6F6DECEC6C600L,
		0x3C66666666663C00L,
		0x7C66667C60606000L,
		0x78CCCCCCCCDC7E00L,
		0x7C66667C6C666600L,
		0x3C66703C0E663C00L,
		0x7E18181818181800L,
		0x6666666666663C00L,
		0x666666663C3C1800L,
		0xC6C6C6D6FEEEC600L,
		0xC3663C183C66C300L,
		0xC3663C1818181800L,
		0xFE0C183060C0FE00L,
		0x3C30303030303C00L,
		0xC06030180C060300L,
		0x3C0C0C0C0C0C3C00L,
		0x10386CC600000000L,
		0x00000000000000FEL,
		0x18180C0000000000L,
		0x00003C063E663E00L,
		0x60607C6666667C00L,
		0x00003C6060603C00L,
		0x06063E6666663E00L,
		0x00003C667E603C00L,
		0x1C307C3030303000L,
		0x00003E66663E063CL,
		0x60607C6666666600L,
		0x1800181818180C00L,
		0x0C000C0C0C0C0C78L,
		0x6060666C786C6600L,
		0x1818181818180C00L,
		0x0000ECFED6C6C600L,
		0x00007C6666666600L,
		0x00003C6666663C00L,
		0x00007C66667C6060L,
		0x00003E66663E0606L,
		0x00007C6660606000L,
		0x00003C603C067C00L,
		0x30307C3030301C00L,
		0x0000666666663E00L,
		0x00006666663C1800L,
		0x0000C6C6D6FE6C00L,
		0x0000C66C386CC600L,
		0x00006666663C1830L,
		0x00007E0C18307E00L,
		0x0E18187018180E00L,
		0x1818181818181800L,
		0x7018180E18187000L,
		0x729C000000000000L
	};

	private static final Color SHADOW = toColor( 0x000 );
	private static final Color HIGHLIGHT = toColor( 0xFFF );
	private static final Color BACKGROUND = toColor( 0xAAA );
	private static final Color SELECTED = toColor( 0x68B );

	private static final int TEXT_SHADOW_BACKGROUND = 0;
	private static final int TEXT_HIGHLIGHT_BACKGROUND = 1;
	private static final int TEXT_SHADOW_SELECTED = 2;
	private static final int TEXT_HIGHLIGHT_SELECTED = 3;
	private static final int TEXT_BLUE = 4;
	private static final int TEXT_GREEN = 5;
	private static final int TEXT_CYAN = 6;
	private static final int TEXT_RED = 7;
	private static final int TEXT_MAGENTA = 8;
	private static final int TEXT_YELLOW = 9;
	private static final int TEXT_WHITE = 10;
	private static final int TEXT_LIME = 11;
	
	private static final int[] FX_COLOURS = new int[]
	{
		/* 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ A B C D E F */
		TEXT_GREEN, TEXT_GREEN, TEXT_GREEN, TEXT_GREEN,
		TEXT_GREEN, TEXT_LIME, TEXT_LIME, TEXT_YELLOW, TEXT_YELLOW,
		TEXT_MAGENTA, 0, 0, 0, 0, 0, 0, 0, TEXT_YELLOW, TEXT_WHITE,
		TEXT_YELLOW, TEXT_WHITE, 0, TEXT_WHITE
	};
	
	private static final int[] EX_COLOURS = new int[]
	{
		/* 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ A B C D E F */
		TEXT_BLUE, TEXT_GREEN, TEXT_GREEN, TEXT_GREEN, TEXT_GREEN,
		TEXT_GREEN, TEXT_WHITE, TEXT_YELLOW, TEXT_BLUE, TEXT_MAGENTA,
		0, 0, 0, 0, 0, 0, 0, TEXT_YELLOW, TEXT_YELLOW, TEXT_MAGENTA,
		TEXT_MAGENTA, TEXT_MAGENTA, TEXT_MAGENTA
	};
	
	private static final int[] KEY_MAP = new int[]
	{
		KeyEvent.VK_SPACE,
		KeyEvent.VK_Z, KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_D,
		KeyEvent.VK_C, KeyEvent.VK_V, KeyEvent.VK_G, KeyEvent.VK_B,
		KeyEvent.VK_H, KeyEvent.VK_N, KeyEvent.VK_J, KeyEvent.VK_M,
		KeyEvent.VK_Q, KeyEvent.VK_2, KeyEvent.VK_W, KeyEvent.VK_3,
		KeyEvent.VK_E, KeyEvent.VK_R, KeyEvent.VK_5, KeyEvent.VK_T,
		KeyEvent.VK_6, KeyEvent.VK_Y, KeyEvent.VK_7, KeyEvent.VK_U,
		KeyEvent.VK_I, KeyEvent.VK_9, KeyEvent.VK_O, KeyEvent.VK_0,
		KeyEvent.VK_P
	};
	
	private static final int[] HEX_MAP = new int[]
	{
		KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
		KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7,
		KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_A, KeyEvent.VK_B,
		KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_F
	};
	
	private static final String KEY_TO_STR = "A-A#B-C-C#D-D#E-F-F#G-G#";
	private static final String HEX_TO_STR = "0123456789ABCDEF";
	
	private static final int GAD_COUNT = 40;
	private static final int GAD_TYPE_LABEL = 1;
	private static final int GAD_TYPE_BUTTON = 2;
	private static final int GAD_TYPE_TEXTBOX = 3;
	private static final int GAD_TYPE_SLIDER = 4;
	private static final int GAD_TYPE_LISTBOX = 5;
	private static final int GAD_TYPE_PATTERN = 6;
	
	private static final int KEY_ESCAPE = KeyEvent.VK_ESCAPE;
	private static final int KEY_BACKSPACE = KeyEvent.VK_BACK_SPACE;
	private static final int KEY_DELETE = KeyEvent.VK_DELETE;
	private static final int KEY_HOME = KeyEvent.VK_HOME;
	private static final int KEY_END = KeyEvent.VK_END;
	private static final int KEY_PAGE_UP = KeyEvent.VK_PAGE_UP;
	private static final int KEY_PAGE_DOWN = KeyEvent.VK_PAGE_DOWN;
	private static final int KEY_UP = KeyEvent.VK_UP;
	private static final int KEY_DOWN = KeyEvent.VK_DOWN;
	private static final int KEY_LEFT = KeyEvent.VK_LEFT;
	private static final int KEY_RIGHT = KeyEvent.VK_RIGHT;
	
	private static final int GADNUM_PATTERN = 1;
	private static final int GADNUM_PATTERN_SLIDER = 2;
	private static final int GADNUM_DIR_TEXTBOX = 3;
	private static final int GADNUM_DIR_BUTTON = 4;
	private static final int GADNUM_DIR_LISTBOX = 5;
	private static final int GADNUM_DIR_SLIDER = 6;
	private static final int GADNUM_TITLE_LABEL = 7;
	private static final int GADNUM_TITLE_TEXTBOX = 8;
	private static final int GADNUM_INST_LABEL = 9;
	private static final int GADNUM_INST_TEXTBOX = 10;
	private static final int GADNUM_INST_DEC_BUTTON = 11;
	private static final int GADNUM_INST_INC_BUTTON = 12;
	private static final int GADNUM_INST_NAME_LABEL = 14;
	private static final int GADNUM_INST_NAME_TEXTBOX = 15;
	private static final int GADNUM_INST_REP_LABEL = 16;
	private static final int GADNUM_INST_REP_TEXTBOX = 17;
	private static final int GADNUM_INST_VOL_LABEL = 18;
	private static final int GADNUM_INST_VOL_TEXTBOX = 19;
	private static final int GADNUM_INST_LEN_LABEL = 20;
	private static final int GADNUM_INST_LEN_TEXTBOX = 21;
	private static final int GADNUM_INST_FINE_LABEL = 22;
	private static final int GADNUM_INST_FINE_TEXTBOX = 23;
	private static final int GADNUM_SEQ_TEXTBOX = 24;
	private static final int GADNUM_SEQ_INS_BUTTON = 25;
	private static final int GADNUM_SEQ_DEL_BUTTON = 26;
	private static final int GADNUM_SEQ_LISTBOX = 27;
	private static final int GADNUM_SEQ_SLIDER = 28;
	private static final int GADNUM_LOAD_BUTTON = 29;
	private static final int GADNUM_SAVE_BUTTON = 30;
	private static final int GADNUM_VER_LABEL = 31;
	private static final int GADNUM_PLAY_BUTTON = 32;
	
	private static final int MAX_CHANNELS = 8;
	private static final int SAMPLING_RATE = 48000;
	
	private int width, height, clickX, clickY, focus;
	
	private int[] gadType = new int[ GAD_COUNT ];
	private int[] gadX = new int[ GAD_COUNT ];
	private int[] gadY = new int[ GAD_COUNT ];
	private int[] gadWidth = new int[ GAD_COUNT ];
	private int[] gadHeight = new int[ GAD_COUNT ];
	private boolean[] gadRedraw = new boolean[ GAD_COUNT ];
	private String[][] gadText = new String[ GAD_COUNT ][];
	private int[][] gadValues = new int[ GAD_COUNT ][];
	private boolean[] gadSelected = new boolean[ GAD_COUNT ];
	private int[] gadValue = new int[ GAD_COUNT ];
	private int[] gadRange = new int[ GAD_COUNT ];
	private int[] gadMax = new int[ GAD_COUNT ];
	private int[] gadItem = new int[ GAD_COUNT ];
	private int[] gadLink = new int[ GAD_COUNT ];
	
	private Image charset, image;
	
	private ModPlay3 modPlay3 = new ModPlay3( MAX_CHANNELS );
	private int instrument, octave = 2, selectedFile, triggerChannel;
	private byte[] copyBuf = new byte[ 0 ];
	private boolean reverb;
	private String error;
	
	private static Color toColor( int rgb12 )
	{
		return new Color( ( ( rgb12 >> 8 ) & 0xF ) * 17, ( ( rgb12 >> 4 ) & 0xF ) * 17, ( rgb12 & 0xF ) * 17 );
	}
	
	private static int toRgb12( Color clr )
	{
		return ( clr.getRed() / 17 << 8 ) | ( clr.getGreen() / 17 << 4 ) | ( clr.getBlue() / 17 );
	}
	
	private static int toRgb24( int rgb12 )
	{
		int r = ( ( rgb12 & 0xF00 ) >> 8 ) * 17;
		int g = ( ( rgb12 & 0xF0 ) >> 4 ) * 17;
		int b = ( rgb12 & 0xF ) * 17;
		return ( r << 16 ) | ( g << 8 ) | b;
	}
	
	private void createDiskGadgets( int x, int y )
	{
		int rows = 7, cols = 32;
		createTextbox( GADNUM_DIR_TEXTBOX, x, y, ( cols - 1 ) * 8, 28, "" );
		createButton( GADNUM_DIR_BUTTON, x + ( cols - 1 ) * 8 + 4, y + 2, 44, 24, "Dir" );
		createListbox( GADNUM_DIR_LISTBOX, x, y + 32, ( cols + 2 ) * 8, rows * 16 + 12, GADNUM_DIR_SLIDER );
		createSlider( GADNUM_DIR_SLIDER, x + ( cols + 2 ) * 8 + 4, y + 32, 20, rows * 16 + 12, 1, 1 );
		createButton( GADNUM_LOAD_BUTTON, x, y + rows * 16 + 48, 64, 24, "Load" );
		createButton( GADNUM_SAVE_BUTTON, x + 64 + 4, y + rows * 16 + 48, 64, 24, "Save" );
	}
	
	private void createInstGadgets( int x, int y )
	{
		createLabel( GADNUM_INST_LABEL, x, y + 6, "Instrument", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_INST_TEXTBOX, x + 10 * 8 + 4, y, 4 * 8, 28, "00" );
		createButton( GADNUM_INST_DEC_BUTTON, x + 15 * 8, y + 2, 24, 24, "<" );
		createButton( GADNUM_INST_INC_BUTTON, x + 15 * 8 + 28, y + 2, 24, 24, ">" );
		createLabel( GADNUM_INST_NAME_LABEL, x, y + 32 + 6, "Name", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_INST_NAME_TEXTBOX, x + 4 * 8 + 4, y + 32, 24 * 8, 28, "[001 C-2 C#D#EF#G#A#B]" );
		createLabel( GADNUM_INST_REP_LABEL, x, y + 32 * 2 + 6, "Repeat", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_INST_REP_TEXTBOX, x + 6 * 8 + 4, y + 32 * 2, 8 * 8, 28, "999999" );
		createLabel( GADNUM_INST_VOL_LABEL, x + 16 * 8, y + 32 * 2 + 6, "Volume", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_INST_VOL_TEXTBOX, x + 24 * 8 + 4, y + 32 * 2, 4 * 8, 28, "64" );
		createLabel( GADNUM_INST_LEN_LABEL, x, y + 32 * 3 + 6, "Length", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_INST_LEN_TEXTBOX, x + 6 * 8 + 4, y + 32 * 3, 8 * 8, 28, "999999" );
		createLabel( GADNUM_INST_FINE_LABEL, x + 16 * 8, y + 32 * 3 + 6, "Finetune", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_INST_FINE_TEXTBOX, x + 24 * 8 + 4, y + 32 * 3, 4 * 8, 28, "-8" );
	}
	
	private void createSequenceGadgets( int x, int y )
	{
		int rows = 7;
		createTextbox( GADNUM_SEQ_TEXTBOX, x, y, 5 * 8, 28, "0" );
		createButton( GADNUM_SEQ_INS_BUTTON, x + 5 * 8 + 4, y + 2, 3 * 8, 24, "+" );
		createButton( GADNUM_SEQ_DEL_BUTTON, x + 9 * 8, y + 2, 3 * 8, 24, "-" );
		createListbox( GADNUM_SEQ_LISTBOX, x, y + 32, 9 * 8, rows * 16 + 12, GADNUM_SEQ_SLIDER );
		gadText[ GADNUM_SEQ_LISTBOX ] = new String[] { "000   0" };
		createSlider( GADNUM_SEQ_SLIDER, x + 9 * 8 + 4, y + 32, 20, rows * 16 + 12, 1, 1 );
	}
	
	public Tracker3( int width, int height )
	{
		this.width = width;
		this.height = height;
		addKeyListener( this );
		addMouseListener( this );
		addMouseMotionListener( this );
		createPattern( GADNUM_PATTERN, 4, 192, GADNUM_PATTERN_SLIDER );
		createSlider( GADNUM_PATTERN_SLIDER, 616, 192, 20, 256, 15, 78 );
		createDiskGadgets( 4, 4 );
		createLabel( GADNUM_TITLE_LABEL, 306, 10, "Title", TEXT_SHADOW_SELECTED );
		createTextbox( GADNUM_TITLE_TEXTBOX, 306 + 5 * 8 + 4, 4, 23 * 8, 28, "" );
		createInstGadgets( 306, 36 );
		createSequenceGadgets( 540, 4 );
		createLabel( GADNUM_VER_LABEL, 200, 6 + 7 * 16 + 50, VERSION, TEXT_HIGHLIGHT_SELECTED );
		createButton( GADNUM_PLAY_BUTTON, 540, 6 + 7 * 16 + 46, 96, 24, "Play" );
		modPlay3.setPatternData( new byte[ MAX_CHANNELS * 4 * 64 * 128 ], MAX_CHANNELS );
		modPlay3.setSequencer( false );
		setInstrument( 1 );
		listDir( getDir() );
		gadRedraw[ 0 ] = true;
	}
	
	public synchronized void keyPressed( KeyEvent e )
	{
		try
		{
			switch( e.getKeyCode() )
			{
				case KeyEvent.VK_F1:
					octave = 1;
					break;
				case KeyEvent.VK_F2:
					octave = 2;
					break;
				case KeyEvent.VK_F3:
					octave = 3;
					break;
				case KeyEvent.VK_F4:
					octave = 4;
					break;
				case KeyEvent.VK_F5:
					copy();
					break;
				case KeyEvent.VK_F6:
					paste( 0 );
					break;
				case KeyEvent.VK_F7:
					reverb = !reverb;
					break;
				case KeyEvent.VK_F8:
					setNumChannels( modPlay3.getNumChannels() < MAX_CHANNELS ? MAX_CHANNELS : 4 );
					break;
				case KeyEvent.VK_F9:
					if( e.isShiftDown() )
					{
						cropInstrument();
					}
					else
					{
						saveInstrument();
					}
					break;
				case KeyEvent.VK_F10:
					if( e.isShiftDown() )
					{
						optimize();
					}
					break;
				default:
					switch( gadType[ focus ] )
					{
						case GAD_TYPE_TEXTBOX:
							keyTextbox( focus, e.getKeyChar(), e.getKeyCode(), e.isShiftDown() );
							break;
						case GAD_TYPE_LISTBOX:
							keyListbox( focus, e.getKeyChar(), e.getKeyCode(), e.isShiftDown()  );
							trigger( -1, mapEventKey( KEY_MAP, e.getKeyCode() ) );
							break;
						case GAD_TYPE_PATTERN:
							keyPattern( focus, e.getKeyChar(), e.getKeyCode(), e.isShiftDown() );
							break;
						default:
							trigger( -1, mapEventKey( KEY_MAP, e.getKeyCode() ) );
							break;
					}
					break;
			}
		}
		catch( Exception x )
		{
			x.printStackTrace();
			setError( x.getMessage() );
		}
		repaint();
	}
	
	public synchronized void keyReleased( KeyEvent e )
	{
	}
	
	public synchronized void keyTyped( KeyEvent e )
	{
	}
	
	public synchronized void mouseClicked( MouseEvent e )
	{
	}
	
	public synchronized void mouseEntered( MouseEvent e )
	{
	}
	
	public synchronized void mouseExited( MouseEvent e )
	{
	}
	
	public synchronized void mousePressed( MouseEvent e )
	{
		clickX = e.getX();
		clickY = e.getY();
		int clicked = findGadget( clickX, clickY );
		if( focus > 0 && focus != clicked )
		{
			escape( focus );
			gadRedraw[ focus ] = true;
		}
		switch( gadType[ clicked ] )
		{
			case GAD_TYPE_BUTTON:
				gadSelected[ clicked ] = true;
				gadRedraw[ clicked ] = true;
				break;
			case GAD_TYPE_TEXTBOX:
				clickTextbox( clicked );
				break;
			case GAD_TYPE_SLIDER:
				clickSlider( clicked, e.isShiftDown() );
				break;
			case GAD_TYPE_LISTBOX:
				clickListbox( clicked, e.isShiftDown() );
				break;
			case GAD_TYPE_PATTERN:
				clickPattern( clicked, e.isShiftDown() );
				break;
			default:
				if( clicked > 0 )
				{
					action( clicked, e.isShiftDown() );
				}
		}
		focus = clicked;
		repaint();
	}
	
	public synchronized void mouseReleased( MouseEvent e )
	{
		if( focus > 0 )
		{
			switch( gadType[ focus ] )
			{
				case GAD_TYPE_BUTTON:
					if( findGadget( e.getX(), e.getY() ) == focus )
					{
						gadSelected[ focus ] = false;
						gadRedraw[ focus ] = true;
						action( focus, e.isShiftDown() );
						focus = 0;
						repaint();
					}
					break;
				case GAD_TYPE_SLIDER:
					action( focus, e.isShiftDown() );
					focus = 0;
					repaint();
					break;
			}
		}
	}
	
	public synchronized void mouseDragged( MouseEvent e )
	{
		if( focus > 0 )
		{
			switch( gadType[ focus ] )
			{
				case GAD_TYPE_BUTTON:
					boolean selected = findGadget( e.getX(), e.getY() ) == focus;
					if( gadSelected[ focus ] != selected )
					{
						gadSelected[ focus ] = selected;
						gadRedraw[ focus ] = true;
						repaint();
					}
					break;
				case GAD_TYPE_SLIDER:
					dragSlider( focus, e.getY() );
					break;
			}
		}
	}
	
	public synchronized void mouseMoved( MouseEvent e )
	{
	}
	
	public synchronized void windowActivated( WindowEvent e )
	{
	}
	
	public synchronized void windowClosed( WindowEvent e )
	{
	}
	
	public synchronized void windowClosing( WindowEvent e )
	{
		e.getWindow().dispose();
	}
	
	public synchronized void windowDeactivated( WindowEvent e )
	{
	}
	
	public synchronized void windowDeiconified( WindowEvent e )
	{
	}
	
	public synchronized void windowIconified( WindowEvent e )
	{
	}
	
	public synchronized void windowOpened( WindowEvent e )
	{
	}
	
	public synchronized void paint( Graphics g ) {
		if( charset == null ) {
			charset = createImage( charsetImage( TOPAZ_8 ).getSource() );
		}
		if( image == null ) {
			image = createImage( width, height );
		}
		boolean redraw = gadRedraw[ 0 ];
		for( int idx = 1; idx < GAD_COUNT && !redraw; idx++ )
		{
			redraw = gadRedraw[ idx ];
		}
		if( redraw )
		{
			Graphics imageGraphics = image.getGraphics();
			try
			{
				if( gadRedraw[ 0 ] )
				{
					imageGraphics.setColor( SELECTED );
					imageGraphics.fillRect( 0, 0, width, height );
				}
				for( int idx = 1; idx < GAD_COUNT; idx++ )
				{
					if( gadRedraw[ idx ] || gadRedraw[ 0 ] )
					{
						switch( gadType[ idx ] )
						{
							case GAD_TYPE_LABEL:
								drawLabel( imageGraphics, idx );
								break;
							case GAD_TYPE_BUTTON:
								drawButton( imageGraphics, idx );
								break;
							case GAD_TYPE_TEXTBOX:
								drawTextbox( imageGraphics, idx );
								break;
							case GAD_TYPE_SLIDER:
								if( gadLink[ idx ] <= 0 )
								{
									drawSlider( imageGraphics, idx );
								}
								break;
							case GAD_TYPE_LISTBOX:
								drawListbox( imageGraphics, idx );
								break;
							case GAD_TYPE_PATTERN:
								drawPattern( imageGraphics, idx );
								break;
						}
						gadRedraw[ idx ] = false;
					}
				}
				gadRedraw[ 0 ] = false;
			}
			finally
			{
				imageGraphics.dispose();
			}
		}
		g.drawImage( image, 0, 0, null );
	}
	
	public synchronized void update( Graphics g ) {
		paint( g );
	}
	
	public synchronized Dimension getPreferredSize()
	{
		return new Dimension( width, height );
	}
	
	private static int readIntBe( java.io.InputStream inputStream, int length ) throws IOException
	{
		int value = 0;
		for( int idx = 0; idx < length; idx++ )
		{
			value = ( value << 8 ) | inputStream.read();
		}
		return value;
	}
	
	private static int readIntLe( java.io.InputStream inputStream, int length ) throws IOException
	{
		int value = 0;
		for( int idx = 0; idx < length; idx++ )
		{
			value = value | ( inputStream.read() << idx * 8 );
		}
		return value;
	}
	
	private static void drawChar( long[] source, int chr, int x, int y, int bg, int fg, int[] dest, int stride )
	{
		int destIdx = y * stride + x;
		for( int cy = 0; cy < 8; cy++ ) {
			for( int cx = 0; cx < 8; cx++ ) {
				int pixel = ( ( source[ chr ] >> 63 - cy * 8 - cx ) & 1 ) == 0 ? bg : fg;
				dest[ destIdx + cx ] = dest[ destIdx + cx + stride ] = pixel;
			}
			destIdx += stride * 2;
		}
	}
	
	private static Image iconImage()
	{
		BufferedImage image = new BufferedImage( 24, 24, BufferedImage.TYPE_INT_RGB );
		int[] pixels = new int[ 24 * 24 ];
		drawChar( TOPAZ_8, 'T' - 32, 4, 5, 0, toRgb24( 0x07F ), pixels, 24 );
		drawChar( TOPAZ_8, '3' - 32, 12, 5, 0, toRgb24( 0xF70 ), pixels, 24 );
		image.setRGB( 0, 0, 24, 24, pixels, 0, 24 );
		return image;
	}
	
	private static Image charsetImage( long[] source )
	{
		int[] pal = new int[]
		{
			( toRgb12( BACKGROUND ) << 12 ) | toRgb12( SHADOW ),
			( toRgb12( BACKGROUND ) << 12 ) | toRgb12( HIGHLIGHT ),
			( toRgb12( SELECTED ) << 12 ) | toRgb12( SHADOW ),
			( toRgb12( SELECTED ) << 12 ) | toRgb12( HIGHLIGHT ),
		/*  Blue   Green  Cyan   Red   Magenta Yellow White  Lime */
			0x00C, 0x080, 0x088, 0x800, 0x808, 0x860, 0x888, 0x680,
			0x06F, 0x0F0, 0x0FF, 0xF00, 0xF0F, 0xFC0, 0xFFF, 0xCF0
		};
		int w = 8 * source.length;
		int h = 16 * pal.length;
		int[] pixels = new int[ w * h ];
		for( int clr = 0; clr < pal.length; clr++ ) {
			int bg = toRgb24( pal[ clr ] >> 12 );
			int fg = toRgb24( pal[ clr ] & 0xFFF );
			for( int chr = 0; chr < source.length; chr++ ) {
				drawChar( source, chr, chr * 8, clr * 16, bg, fg, pixels, w );
			}
		}
		BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
		image.setRGB( 0, 0, w, h, pixels, 0, w );
		return image;
	}
	
	private void setError( String message )
	{
		error = message;
		gadRedraw[ GADNUM_PATTERN ] = true;
	}
	
	private void drawText( Graphics g, int x, int y, String text, int colour )
	{
		for( int idx = 0, len = text.length(); idx < len; idx++ )
		{
			int chr = text.charAt( idx );
			if( chr < 32 || ( chr > 126 && chr < 192 ) || chr > 255 )
			{
				chr = 32;
			}
			else if( chr >= 192 )
			{
				chr = "AAAAAAECEEEEIIIIDNOOOOO*0UUUUYPSaaaaaaeceeeeiiiidnooooo/0uuuuypy".charAt( chr - 192 );
			}
			g.setClip( x, y, 8, 16 );
			g.drawImage( charset, x - ( chr - 32 ) * 8, y - colour * 16, null );
			x += 8;
		}
		g.setClip( null );
	}
	
	private void drawInt( Graphics g, int x, int y, int value, int len, int colour )
	{
		char[] chars = new char[ len ];
		while( len > 0 ) {
			len = len - 1;
			chars[ len ] = ( char ) ( '0' + value % 10 );
			value = value / 10;
		}
		drawText( g, x, y, new String( chars ), colour );
	}
	
	private void raiseBox( Graphics g, int x, int y, int w, int h )
	{
		g.setColor( SHADOW );
		g.fillRect( x + w - 2, y, 2, h );
		g.setColor( HIGHLIGHT );
		g.fillRect( x, y, 2, h );
		g.setColor( SHADOW );
		g.fillRect( x + 1, y + h - 2, w - 1, 2 );
		g.setColor( HIGHLIGHT );
		g.fillRect( x, y, w - 1, 2 );
	}

	private void lowerBox( Graphics g, int x, int y, int w, int h )
	{
		g.setColor( HIGHLIGHT );
		g.fillRect( x + w - 2, y, 2, h );
		g.setColor( SHADOW );
		g.fillRect( x, y, 2, h );
		g.setColor( HIGHLIGHT );
		g.fillRect( x + 1, y + h - 2, w - 1, 2 );
		g.setColor( SHADOW );
		g.fillRect( x, y, w - 1, 2 );
	}

	private void bevelBox( Graphics g, int x, int y, int w, int h )
	{
		raiseBox( g, x, y, w, h );
		lowerBox( g, x + 2, y + 2, w - 4, h - 4 );
	}
	
	private void drawLabel( Graphics g, int gadnum )
	{
		drawText( g, gadX[ gadnum ], gadY[ gadnum ], gadText[ gadnum ][ 0 ], gadValue[ gadnum ] );
	}
	
	private void drawButton( Graphics g, int gadnum )
	{
		int x = gadX[ gadnum ];
		int y = gadY[ gadnum ];
		int w = gadWidth[ gadnum ];
		int h = gadHeight[ gadnum ];
		int textColour;
		if( gadSelected[ gadnum ] )
		{
			g.setColor( SELECTED );
			g.fillRect( x, y, w, h );
			lowerBox( g, x, y, w, h );
			textColour = TEXT_HIGHLIGHT_SELECTED;
		}
		else
		{
			g.setColor( BACKGROUND );
			g.fillRect( x, y, w, h );
			raiseBox( g, x, y, w, h );
			textColour = TEXT_SHADOW_BACKGROUND;
		}
		String text = gadText[ gadnum ][ 0 ];
		drawText( g, x + ( w - text.length() * 8 ) / 2,
			y + ( h - 14 ) / 2, text, textColour );
	}
	
	private void drawTextbox( Graphics g, int gadnum )
	{
		int x = gadX[ gadnum ];
		int y = gadY[ gadnum ];
		int w = gadWidth[ gadnum ];
		int h = gadHeight[ gadnum ];
		String text = gadText[ gadnum ][ 0 ];
		int cursor = gadItem[ gadnum ];
		int columns = ( w - 16 ) / 8;
		int offset = focus == gadnum ? gadValue[ gadnum ] : text.length() - columns;
		if( offset < 0 || offset > text.length() )
		{
			offset = 0;
		}
		if( offset + columns > text.length() )
		{
			columns = text.length() - offset;
		}
		g.setColor( BACKGROUND );
		g.fillRect( x, y, w, h );
		drawText( g, x + 8, y + 6, text.substring( offset, offset + columns ), TEXT_SHADOW_BACKGROUND );
		if( focus == gadnum && cursor >= offset && cursor <= offset + columns )
		{
			String chr = cursor < offset + columns ? String.valueOf( text.charAt( cursor ) ) : " ";
			drawText( g, x + ( cursor - offset + 1 ) * 8, y + 6, chr, TEXT_SHADOW_SELECTED );
		}
		bevelBox( g, x, y, w, h );
	}
	
	private void clickTextbox( int gadnum )
	{
		int columns = ( gadWidth[ gadnum ] - 16 ) / 8;
		String text = gadText[ gadnum ][ 0 ];
		int offset = focus == gadnum ? gadValue[ gadnum ] : text.length() - columns;
		if( offset < 0 || offset > text.length() )
		{
			offset = 0;
		}
		if( offset + columns > text.length() )
		{
			columns = text.length() - offset;
		}
		int cursor = offset + ( clickX - gadX[ gadnum ] ) / 8 - 1;
		if( cursor > text.length() )
		{
			cursor = text.length();
		}
		if( cursor < 0 )
		{
			cursor = 0;
		}
		gadValue[ gadnum ] = offset;
		gadItem[ gadnum ] = cursor;
		gadRedraw[ gadnum ] = true;
	}

	private void keyTextbox( int gadnum, char chr, int key, boolean shift )
	{
		int columns = ( gadWidth[ gadnum ] - 16 ) / 8;
		String text = gadText[ gadnum ][ 0 ];
		int offset = gadValue[ gadnum ];
		if( offset < 0 || offset > text.length() )
		{
			offset = 0;
		}
		int cursor = gadItem[ gadnum ];
		if( cursor > text.length() )
		{
			cursor = text.length();
		}
		switch( key ) 
		{
			case KEY_BACKSPACE:
				if( cursor > 0 )
				{
					text = text.substring( 0, cursor - 1 ) + text.substring( cursor );
					cursor--;
					if( cursor < offset )
					{
						offset = cursor;
					}
				}
				break;
			case KEY_DELETE:
				if( cursor < text.length() )
				{
					text = text.substring( 0, cursor ) + text.substring( cursor + 1 );
				}
				break;
			case KEY_END:
				cursor = text.length();
				if( cursor - offset >= columns )
				{
					offset = cursor - columns;
				}
				break;
			case KEY_ESCAPE:
				escape( gadnum );
				text = gadText[ gadnum ][ 0 ];
				focus = 0;
				break;
			case KEY_HOME:
				offset = cursor = 0;
				break;
			case KEY_LEFT:
				if( cursor > 0 )
				{
					cursor--;
					if( cursor < offset )
					{
						offset = cursor;
					}
				}
				break;
			case KEY_RIGHT:
				if( cursor < text.length() )
				{
					cursor++;
					if( cursor - offset >= columns )
					{
						offset = cursor - columns + 1;
					}
				}
				break;
			default:
				if( chr == 10 )
				{
					action( gadnum, shift );
					text = gadText[ gadnum ][ 0 ];
					focus = 0;
				}
				else if( chr >= 32 && chr < 127 )
				{
					text = text.substring( 0, cursor )
						+ String.valueOf( chr ) + text.substring( cursor );
					cursor++;
					if( cursor - offset > columns )
					{
						offset = cursor - columns;
					}
				}
				break;
		}
		gadText[ gadnum ][ 0 ] = text;
		gadValue[ gadnum ] = offset;
		gadItem[ gadnum ] = cursor;
		gadRedraw[ gadnum ] = true;
	}
	
	private void drawSlider( Graphics g, int gadnum )
	{
		int x = gadX[ gadnum ];
		int y = gadY[ gadnum ];
		int w = gadWidth[ gadnum ];
		int h = gadHeight[ gadnum ];
		int s = ( h - 12 ) * gadRange[ gadnum ] / gadMax[ gadnum ] + 8;
		int d = ( h - 12 ) * gadValue[ gadnum ] / gadMax[ gadnum ];
		g.setColor( SELECTED );
		g.fillRect( x, y, w, h );
		lowerBox( g, x, y, w, h );
		g.setColor( BACKGROUND );
		g.fillRect( x + 2, y + d + 2, w - 4, s );
		raiseBox( g, x + 2, y + d + 2, w - 4, s );
	}
	
	private void clickSlider( int gadnum, boolean shift )
	{
		int ss = ( gadHeight[ gadnum ] - 12 ) * gadRange[ gadnum ] / gadMax[ gadnum ] + 8;
		int so = ( gadHeight[ gadnum ] - 12 ) * gadValue[ gadnum ] / gadMax[ gadnum ];
		int sy = gadY[ gadnum ] + so + 2;
		if( clickY < sy )
		{
			int sp = gadValue[ gadnum ] - gadRange[ gadnum ];
			if( sp < 0 )
			{
				sp = 0;
			}
			gadValue[ gadnum ] = sp;
			gadRedraw[ gadLink[ gadnum ] > 0 ? gadLink[ gadnum ] : gadnum ] = true;
			action( gadnum, shift );
		}
		else if( clickY > ( sy + ss ) )
		{
			int sp = gadValue[ gadnum ] + gadRange[ gadnum ];
			if( sp > ( gadMax[ gadnum ] - gadRange[ gadnum ] ) )
			{
				sp = gadMax[ gadnum ] - gadRange[ gadnum ];
			}
			gadValue[ gadnum ] = sp;
			gadRedraw[ gadLink[ gadnum ] > 0 ? gadLink[ gadnum ] : gadnum ] = true;
			action( gadnum, shift );
		}
	}
	
	private void dragSlider( int gadnum, int y )
	{
		int ss = ( gadHeight[ gadnum ] - 12 ) * gadRange[ gadnum ] / gadMax[ gadnum ] + 8;
		int so = ( gadHeight[ gadnum ] - 12 ) * gadValue[ gadnum ] / gadMax[ gadnum ];
		int sg = gadHeight[ gadnum ] - 4 - ss;
		int sy = gadY[ gadnum ] + so + 2;
		if( clickY > sy && clickY < ( sy + ss ) )
		{
			int sp = so + y - clickY;
			if( sp < 0 )
			{
				sp = 0;
			}
			if( sp > sg )
			{
				sp = sg;
			}
			gadValue[ gadnum ] = sp > 0 ? sp * ( gadMax[ gadnum ] - gadRange[ gadnum ] ) / sg : 0;
			clickY += ( gadHeight[ gadnum ] - 12 ) * gadValue[ gadnum ] / gadMax[ gadnum ] - so;
			gadRedraw[ gadLink[ gadnum ] > 0 ? gadLink[ gadnum ] : gadnum ] = true;
			repaint();
		}
	}
	
	private void drawListbox( Graphics g, int gadnum ) {
		int x = gadX[ gadnum ];
		int y = gadY[ gadnum ];
		int w = gadWidth[ gadnum ];
		int h = gadHeight[ gadnum ];
		int tw = ( w - 16 ) / 8;
		int th = ( h - 12 ) / 16;
		if( gadLink[ gadnum ] > 0 )
		{
			scrollListbox( gadnum, gadLink[ gadnum ] );
			drawSlider( g, gadLink[ gadnum ] );
		}
		g.setColor( BACKGROUND );
		g.fillRect( x, y, w, h );
		lowerBox( g, x, y, w, h );
		int end = gadText[ gadnum ].length;
		if( gadValue[ gadnum ] + th > end )
		{
			th = end - gadValue[ gadnum ];
		}
		int ty = y + 6;
		for( int idx = gadValue[ gadnum ], len = idx + th; idx < len; idx++ ) {
			if( gadText[ gadnum ] != null && idx < gadText[ gadnum ].length )
			{
				String text = gadText[ gadnum ][ idx ];
				if( text.length() > tw )
				{
					text = text.substring( 0, tw );
				}
				else if( text.length() < tw )
				{
					char[] chars = new char[ tw ];
					text.getChars( 0, text.length(), chars, 0 );
					for( int c = text.length(); c < chars.length; c++ )
					{
						chars[ c ] = 32;
					}
					text = new String( chars );
				}
				
				int clr = TEXT_SHADOW_BACKGROUND;
				if( gadItem[ gadnum ] == idx )
				{
					clr = TEXT_HIGHLIGHT_SELECTED;
				}
				else if( gadValues[ gadnum ] != null && idx < gadValues[ gadnum ].length )
				{
					clr = gadValues[ gadnum ][ idx ];
				}
				drawText( g, x + 8, ty, text, clr );
			}
			ty += 16;
		}
	}
	
	private void clickListbox( int gadnum, boolean shift )
	{
		int time = ( int ) System.currentTimeMillis();
		int dt = time - gadRange[ gadnum ];
		int item = gadValue[ gadnum ] + ( clickY - gadY[ gadnum ] - 6 ) / 16;
		if( item == gadItem[ gadnum ] && dt > 0 && dt < 500 )
		{
			action( gadnum, shift );
			gadRange[ gadnum ] = 0;
		}
		else
		{
			if( item < gadText[ gadnum ].length )
			{
				gadItem[ gadnum ] = item;
			}
			gadRange[ gadnum ] = time;
			gadRedraw[ gadnum ] = true;
		}
	}
	
	private void keyListbox( int gadnum, char chr, int key, boolean shift )
	{
		int item = gadItem[ gadnum ];
		switch( key )
		{
			case KEY_UP:
				if( item > 0 )
				{
					gadItem[ gadnum ] = --item;
					int link = gadLink[ gadnum ] > 0 ? gadLink[ gadnum ] : gadnum;
					if( gadValue[ link ] > item )
					{
						gadValue[ link ] = item;
					}
					gadRedraw[ gadnum ] = true;
				}
				break;
			case KEY_DOWN:
				if( item < gadText[ gadnum ].length - 1 )
				{
					gadItem[ gadnum ] = ++item;
					int rows = ( gadHeight[ gadnum ] - 12 ) / 16;
					int link = gadLink[ gadnum ] > 0 ? gadLink[ gadnum ] : gadnum;
					if( gadValue[ link ] + rows <= item )
					{
						gadValue[ link ] = item - rows + 1;
					}
					gadRedraw[ gadnum ] = true;
				}
				break;
			default:
				if( chr == 10 )
				{
					action( gadnum, shift );
				}
				break;
		}
	}
	
	private void scrollListbox( int listbox, int slider )
	{
		gadRange[ slider ] = ( gadHeight[ listbox ] - 12 ) / 16;
		if( gadText[ listbox ] != null )
		{
			gadMax[ slider ] = gadText[ listbox ].length;
		}
		if( gadRange[ slider ] > gadMax[ slider ] )
		{
			gadRange[ slider ] = gadMax[ slider ];
		}
		if( gadValue[ slider ] + gadRange[ slider ] > gadMax[ slider ] )
		{
			gadValue[ slider ] = gadMax[ slider ] - gadRange[ slider ];
		}
		if( gadValue[ slider ] < 0 ) 
		{
			gadValue[ slider ] = 0;
		}
		gadValue[ listbox ] = gadValue[ slider ];
	}
	
	private int getNoteOffset( int pat, int row, int chn )
	{
		return ( ( pat * 64 + row ) * MAX_CHANNELS + chn ) * 4;
	}
	
	private int getNoteKey( int pat, int row, int chn )
	{
		return modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) ] & 0xFF;
	}
	
	private void setNoteKey( int pat, int row, int chn, int key )
	{
		modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) ] = ( byte ) key;
	}
	
	private int getNoteInstrument( int pat, int row, int chn )
	{
		return modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) + 1 ] & 0x1F;
	}
	
	private void setNoteInstrument( int pat, int row, int chn, int instrument )
	{
		modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) + 1 ] = ( byte ) ( instrument & 0x1F );
	}
	
	private int getNoteEffect( int pat, int row, int chn )
	{
		return modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) + 2 ] & 0xF;
	}
	
	private void setNoteEffect( int pat, int row, int chn, int effect )
	{
		modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) + 2 ] = ( byte ) ( effect & 0xF );
	}
	
	private int getNoteParam( int pat, int row, int chn )
	{
		return modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) + 3 ] & 0xFF;
	}
	
	private void setNoteParam( int pat, int row, int chn, int param )
	{
		modPlay3.getPatternData()[ getNoteOffset( pat, row, chn ) + 3 ] = ( byte ) param;
	}
	
	private String getNoteString( int pat, int row, int chn )
	{
		byte[] patternData = modPlay3.getPatternData();
		int offset = getNoteOffset( pat, row, chn );
		int key = patternData[ offset ] & 0xFF;
		int instrument = patternData[ offset + 1 ] & 0xFF;
		int effect = patternData[ offset + 2 ] & 0xFF;
		int param = patternData[ offset + 3 ] & 0xFF;
		char[] chars = new char[ 8 ];
		chars[ 0 ] = ( key > 0 && key < 118 ) ? KEY_TO_STR.charAt( ( ( key + 2 ) % 12 ) * 2 ) : '-';
		chars[ 1 ] = ( key > 0 && key < 118 ) ? KEY_TO_STR.charAt( ( ( key + 2 ) % 12 ) * 2 + 1 ) : '-';
		chars[ 2 ] = ( key > 0 && key < 118 ) ? ( char ) ( '0' + ( key + 2 ) / 12 ) : '-';
		chars[ 3 ] = ( instrument > 9 && instrument < 100 ) ? ( char ) ( '0' + instrument / 10 ) : '-';
		chars[ 4 ] = ( instrument > 0 && instrument < 100 ) ? ( char ) ( '0' + instrument % 10 ) : '-';
		chars[ 5 ] = ( effect > 0 || param > 0 ) && effect < 16 ? HEX_TO_STR.charAt( effect ) : '-';
		chars[ 6 ] = ( effect > 0 || param > 0 ) ? HEX_TO_STR.charAt( ( param >> 4 ) & 0xF ) : '-';
		chars[ 7 ] = ( effect > 0 || param > 0 ) ? HEX_TO_STR.charAt( param & 0xF ) : '-';
		return new String( chars );
	}
	
	private int getSelectedPattern()
	{
		return ( gadItem[ GADNUM_PATTERN ] >> 24 ) & 0xFF;
	}
	
	private int getSelectedChannel()
	{
		return ( ( gadItem[ GADNUM_PATTERN ] >> 16 ) & 0xFF ) - 1;
	}
	
	private int getSelectedRow1()
	{
		return ( gadItem[ GADNUM_PATTERN ] >> 10 ) & 0x3F;
	}
	
	private int getSelectedRow2()
	{
		return ( gadItem[ GADNUM_PATTERN ] >> 4 ) & 0x3F;
	}
	
	private int getSelectedColumn()
	{
		return gadItem[ GADNUM_PATTERN ] & 0xF;
	}
	
	private void setSelection( int pat, int chn, int row1, int row2, int col )
	{
		gadItem[ GADNUM_PATTERN ] = ( pat << 24 ) | ( ( chn + 1 ) << 16 ) | ( row1 << 10 ) | ( row2 << 4 ) | col;
	}
	
	private int getCurrentPattern()
	{
		return modPlay3.getPattern( modPlay3.getSequencePos() );
	}
	
	private void drawPattern( Graphics g, int gadnum )
	{
		int numChannels = modPlay3.getNumChannels();
		byte[] patternData = modPlay3.getPatternData();
		int pat = getCurrentPattern();
		int mute = modPlay3.getMute();
		int x = gadX[ gadnum ];
		int y = gadY[ gadnum ];
		int selPat = getSelectedPattern();
		int selChan = getSelectedChannel();
		int selRow1 = getSelectedRow1();
		int selRow2 = getSelectedRow2();
		int selCol = getSelectedColumn();
		if( gadLink[ gadnum ] > 0 )
		{
			gadValue[ gadnum ] = gadValue[ gadLink[ gadnum ] ];
			if( gadValue[ gadnum ] > 63 )
			{
				gadValue[ gadnum ] = 63;
			}
			drawSlider( g, gadLink[ gadnum ] );
		}
		drawInt( g, x, y, pat, 3, 7 );
		drawText( g, x + 3 * 8, y, " ", 7 );
		for( int c = 0; c < MAX_CHANNELS; c++ )
		{
			int clr = ( ( mute >> c ) & 1 ) > 0 ? TEXT_RED : TEXT_BLUE;
			drawText( g, x + ( c * 9 + 4 ) * 8, y, clr == TEXT_RED ? " Muted   " : "Channel  ", clr );
			drawInt( g, x + ( c * 9 + 11 ) * 8, y, c + 1, 1, clr );
		}
		for( int r = 1; r < 16; r++ )
		{
			int dr = gadValue[ gadnum ] - 8 + r;
			if( r == 15 && error != null )
			{
				String msg = error.length() > 9 * MAX_CHANNELS ? error.substring( 0, 9 * MAX_CHANNELS ) : error;
				drawText( g, x, y + r * 16, "*** " + ModPlay3.pad( error, ' ', 9 * MAX_CHANNELS, false ), TEXT_RED );
			}
			else if( dr < 0 || dr > 63 )
			{
				g.setColor( Color.BLACK );
				g.fillRect( x, y + r * 16, ( 4 + 9 * 8 ) * 8, 16 );
			}
			else
			{
				int hl = r == 8 ? 8 : 0;
				drawText( g, x, y + r * 16, "    ", TEXT_BLUE + hl );
				drawInt( g, x + 8, y + r * 16, dr, 2, TEXT_BLUE + hl );
				for( int c = 0; c < numChannels; c++ )
				{
					String note = getNoteString( pat, dr, c );
					if( ( ( mute >> c ) & 1 ) > 0 )
					{
						drawText( g, x + ( c * 9 + 4 ) * 8, y + r * 16, note, TEXT_BLUE );
						drawText( g, x + ( c * 9 + 12 ) * 8, y + r * 16, " ", TEXT_BLUE );
					}
					else
					{
						int clr = note.charAt( 0 ) == '-' ? TEXT_BLUE : TEXT_CYAN;
						drawText( g, x + ( c * 9 + 4 ) * 8, y + r * 16, note.substring( 0, 3 ), clr + hl );
						clr = note.charAt( 3 ) == '-' ? TEXT_BLUE : TEXT_RED;
						drawText( g, x + ( c * 9 + 7 ) * 8, y + r * 16, note.substring( 3, 4 ), clr + hl );
						clr = note.charAt( 4 ) == '-' ? TEXT_BLUE : TEXT_RED;
						drawText( g, x + ( c * 9 + 8 ) * 8, y + r * 16, note.substring( 4, 5 ), clr + hl );
						if( note.charAt( 5 ) == 'E' && note.charAt( 6 ) >= '0' && note.charAt( 6 ) <= 'F')
						{
							clr = EX_COLOURS[ note.charAt( 6 ) - '0' ];
						}
						else if( note.charAt( 5 ) >= '0' && note.charAt( 5 ) <= '~' )
						{
							clr = FX_COLOURS[ note.charAt( 5 ) - '0' ];
						}
						else
						{
							clr = TEXT_BLUE;
						}
						drawText( g, x + ( c * 9 + 9 ) * 8, y + r * 16, note.substring( 5, 8 ), clr + hl );
						drawText( g, x + ( c * 9 + 12 ) * 8, y + r * 16, " ", clr + hl );
					}
					if( selPat == pat && selChan == c && ( ( dr >= selRow1 && dr <= selRow2 ) || ( dr >= selRow2 && dr <= selRow1 ) ) )
					{
						int bx = c * 9 + 4;
						int bw = 8;
						if( selCol > 2 && selCol < 8 )
						{
							bx += selCol;
							bw = 1;
						}
						g.setColor( Color.YELLOW );
						g.fillRect( x + bx * 8 - 1, y + r * 16 - 1, 1, 16 );
						if( ( selRow2 >= selRow1 && dr == selRow1 ) || ( selRow2 < selRow1 && dr == selRow2 ) ) 
						{
							g.fillRect( x + bx * 8, y + r * 16 - 2, bw * 8, 1 );
						}
						if( ( selRow2 >= selRow1 && dr == selRow2 ) || ( selRow2 < selRow1 && dr == selRow1 ) ) 
						{
							g.fillRect( x + bx * 8, y + ( r + 1 ) * 16 - 1, bw * 8, 1 );
						}
						g.fillRect( x + ( bx + bw ) * 8, y + r * 16 - 1, 1, 16 );
					}
				}
				if( numChannels < MAX_CHANNELS )
				{
					g.setColor( Color.BLACK );
					g.fillRect( x + ( numChannels * 9 + 4 ) * 8, y + r * 16, ( MAX_CHANNELS - numChannels ) * 9 * 8, 16 );
				}
			}
		}
	}
	
	private void clickPattern( int gadnum, boolean shift )
	{
		int dspRow = ( clickY - gadY[ gadnum ] ) / 16;
		int dspCol = ( clickX - gadX[ gadnum ] ) / 8;
		int chn = ( dspCol - 4 ) / 9;
		int row = gadValue[ gadnum ] + dspRow - 8;
		if( dspRow > 0 && dspCol > 3 && row >= 0 && row < 64 && chn < modPlay3.getNumChannels() && !modPlay3.getSequencer() )
		{
			int pat = getCurrentPattern();
			if( shift && getSelectedPattern() == pat )
			{
				setSelection( pat, chn, getSelectedRow1(), row, 0 );
			}
			else
			{
				setSelection( pat, chn, row, row, dspCol - chn * 9 - 4 );
			}
		}
		else
		{
			setSelection( 0, -1, 0, 0, 0 );
			int mask = 1 << chn;
			int mute = modPlay3.getMute();
			if( mute == ~mask || dspCol < 4 || chn >= modPlay3.getNumChannels() )
			{
				/* Solo channel, unmute all. */
				mute = 0;
			}
			else if( ( mute & mask ) > 0 )
			{
				/* Muted channel, unmute. */
				mute ^= mask;
			}
			else
			{
				/* Unmuted channel, set as solo. */
				mute = -1 ^ mask;
			}
			modPlay3.setMute( mute );
		}
		gadRedraw[ GADNUM_PATTERN ] = true;
	}
	
	private static int mapEventKey( int[] keyMap, int eventKey )
	{
		for( int idx = 0; idx < keyMap.length; idx++ )
		{
			if( keyMap[ idx ] == eventKey )
			{
				return idx;
			}
		}
		return -1;
	}
	
	private void trigger( int channel, int noteKey )
	{
		if( !modPlay3.getSequencer() )
		{
			if( noteKey > 0 )
			{
				int key = noteKey + octave * 12;
				int vol = modPlay3.getSampleVolume( instrument );
				if( channel < 0 )
				{
					for( int chn = 0; chn < modPlay3.getNumChannels(); chn++ )
					{
						triggerChannel = ( triggerChannel + 1 ) % modPlay3.getNumChannels();
						if( ( ( modPlay3.getMute() >> triggerChannel ) & 1 ) == 0 )
						{
							break;
						}
					}
					channel = triggerChannel;
				}
				modPlay3.trigger( channel, instrument, key, vol );
			}
			else
			{
				stop();
			}
		}
	}
	
	private void keyPattern( int gadnum, char chr, int key, boolean shift )
	{
		int pat = getSelectedPattern();
		int chn = getSelectedChannel();
		int row1 = getSelectedRow1();
		int row2 = getSelectedRow2();
		int col = getSelectedColumn();
		if( pat == getCurrentPattern() && chn >= 0 && chn < modPlay3.getNumChannels() )
		{
			switch( key )
			{
				case KEY_ESCAPE:
					chn = -1;
					break;
				case KEY_HOME:
					row2 = 0;
					gadValue[ GADNUM_PATTERN_SLIDER ] = 0;
					break;
				case KEY_END:
					row2 = 63;
					gadValue[ GADNUM_PATTERN_SLIDER ] = 63;
					break;
				case KEY_PAGE_UP:
					row2 = row2 - 6;
				case KEY_UP:
					row2 = row2 > 0 ? row2 - 1 : 0;
					if( gadValue[ GADNUM_PATTERN_SLIDER ] - 7 > row2 )
					{
						gadValue[ GADNUM_PATTERN_SLIDER ] = row2 + 7;
					}
					break;
				case KEY_PAGE_DOWN:
					row2 = row2 + 6;
				case KEY_DOWN:
					row2 = row2 < 63 ? row2 + 1 : 63;
					if( gadValue[ GADNUM_PATTERN_SLIDER ] + 7 < row2 )
					{
						gadValue[ GADNUM_PATTERN_SLIDER ] = row2 - 7;
					}
					break;
				case KEY_LEFT:
					if( col == 0 || shift )
					{
						chn = chn > 0 ? chn - 1 : 0;
						col = 7;
					}
					else if( col > 3 )
					{
						col = col - 1;
					}
					else
					{
						col = 0;
					}
					break;
				case KEY_RIGHT:
					if( col > 6 || shift )
					{
						chn = chn < MAX_CHANNELS - 1 ? chn + 1 : MAX_CHANNELS - 1;
						col = 0;
					}
					else if( col > 2 )
					{
						col = col + 1;
					}
					else
					{
						col = 3;
					}
					break;
				case KEY_DELETE:
					int row = row2 > row1 ? row1 : row2;
					while( row <= row1 || row <= row2 )
					{
						if( col < 3 )
						{
							setNoteKey( pat, row, chn, 0 );
						}
						if( col < 5 )
						{
							setNoteInstrument( pat, row, chn, 0 );
						}
						setNoteEffect( pat, row, chn, 0 );
						setNoteParam( pat, row, chn, 0 );
						row++;
					}
					break;
				default:
					if( row1 == row2 )
					{
						if( col > 2 )
						{
							int hex = mapEventKey( HEX_MAP, key );
							if( col == 3 && hex >= 0 && hex < 4 )
							{
								setNoteInstrument( pat, row2, chn, hex * 10 );
								col++;
							}
							else if( col == 4 && hex >= 0 && hex < 10 )
							{
								setNoteInstrument( pat, row2, chn, getNoteInstrument( pat, row2, chn ) / 10 * 10 + hex );
								col++;
							}
							else if( col == 5 && hex >= 0 )
							{
								setNoteEffect( pat, row2, chn, hex );
								col++;
							}
							else if( col == 6 && hex >= 0 )
							{
								setNoteParam( pat, row2, chn, hex << 4 );
								col++;
							}
							else if( col == 7 && hex >= 0 )
							{
								setNoteParam( pat, row2, chn, ( getNoteParam( pat, row2, chn ) & 0xF0 ) | hex );
								col--;
							}
						}
						else
						{
							int noteKey = mapEventKey( KEY_MAP, key );
							if( noteKey > 0 )
							{
								if( shift )
								{
									paste( noteKey + octave * 12 - 25 );
								}
								else
								{
									setNoteKey( pat, row2, chn, noteKey + octave * 12 );
									setNoteInstrument( pat, row2, chn, instrument );
								}
							}
							trigger( chn, noteKey );
						}
					}
					break;
			}
			if( shift )
			{
				col = 0;
			}
			else
			{
				row1 = row2;
			}
			setSelection( pat, chn, row1, row2, col );
			gadRedraw[ gadnum ] = true;
		}
		else
		{
			trigger( -1, mapEventKey( KEY_MAP, key ) );
		}
	}
	
	private int findGadget( int x, int y )
	{
		for( int idx = 0; idx < GAD_COUNT; idx++ )
		{
			if( gadType[ idx ] > 0 )
			{
				int x0 = gadX[ idx ];
				int y0 = gadY[ idx ];
				int x1 = x0 + gadWidth[ idx ];
				int y1 = y0 + gadHeight[ idx ];
				if( x >= x0 && y >= y0 && x < x1 && y < y1 )
				{
					return idx;
				}
			}
		}
		return 0;
	}

	private void createGadget( int gadnum, int type, int x, int y, int w, int h )
	{
		gadType[ gadnum ] = type;
		gadX[ gadnum ] = x;
		gadY[ gadnum ] = y;
		gadWidth[ gadnum ] = w;
		gadHeight[ gadnum ] = h;
	}
	
	private void createLabel( int gadnum, int x, int y, String text, int colour )
	{
		createGadget( gadnum, GAD_TYPE_LABEL, x, y, text.length() * 8, 16 );
		gadText[ gadnum ] = new String[] { text };
		gadValue[ gadnum ] = colour;
	}
	
	private void createButton( int gadnum, int x, int y, int w, int h, String text )
	{
		createGadget( gadnum, GAD_TYPE_BUTTON, x, y, w, h );
		gadText[ gadnum ] = new String[] { text };
	}
	
	private void createTextbox( int gadnum, int x, int y, int w, int h, String text )
	{
		createGadget( gadnum, GAD_TYPE_TEXTBOX, x, y, w, h );
		gadText[ gadnum ] = new String[] { text };
	}
	
	private void createSlider( int gadnum, int x, int y, int w, int h, int range, int max )
	{
		createGadget( gadnum, GAD_TYPE_SLIDER, x, y, w, h );
		gadValue[ gadnum ] = 0;
		gadRange[ gadnum ] = range;
		gadMax[ gadnum ] = max;
	}
	
	private void createListbox( int gadnum, int x, int y, int w, int h, int slider )
	{
		createGadget( gadnum, GAD_TYPE_LISTBOX, x, y, w, h );
		gadLink[ gadnum ] = slider;
		gadLink[ slider ] = gadnum;
	}
	
	private void createPattern( int gadnum, int x, int y, int slider )
	{
		createGadget( gadnum, GAD_TYPE_PATTERN, x, y, ( 4 + 9 * 8 ) * 8, 16 * 16 );
		gadLink[ gadnum ] = slider;
		gadLink[ slider ] = gadnum;
	}
	
	private void escape( int gadnum )
	{
		switch( gadnum )
		{
			case GADNUM_TITLE_TEXTBOX:
				gadText[ gadnum ][ 0 ] = modPlay3.getSongName();
				break;
			case GADNUM_INST_NAME_TEXTBOX:
				gadText[ gadnum ][ 0 ] = modPlay3.getInstrumentName( instrument );
				break;
			case GADNUM_INST_REP_TEXTBOX:
				gadText[ gadnum ][ 0 ] = String.valueOf( modPlay3.getSampleLoopStart( instrument ) );
				break;
			case GADNUM_INST_VOL_TEXTBOX:
				gadText[ gadnum ][ 0 ] = String.valueOf( modPlay3.getSampleVolume( instrument ) );
				break;
			case GADNUM_INST_LEN_TEXTBOX:
				gadText[ gadnum ][ 0 ] = String.valueOf( modPlay3.getSampleLoopLength( instrument ) );
				break;
			case GADNUM_INST_FINE_TEXTBOX:
				gadText[ gadnum ][ 0 ] = String.valueOf( modPlay3.getSampleFinetune( instrument ) );
				break;
		}
	}
	
	private void action( int gadnum, boolean shift )
	{
		try
		{
			switch( gadnum ) 
			{
				case GADNUM_DIR_TEXTBOX:
					selectedFile = 0;
				case GADNUM_DIR_BUTTON:
					listDir( getDir() );
					gadItem[ GADNUM_DIR_LISTBOX ] = selectedFile;
					gadValue[ GADNUM_DIR_SLIDER ] = selectedFile - 3;
					break;
				case GADNUM_LOAD_BUTTON:
				case GADNUM_DIR_LISTBOX:
					if( gadValues[ GADNUM_DIR_LISTBOX ][ 0 ] == 0 )
					{
						setInstrument( gadItem[ GADNUM_DIR_LISTBOX ] + 1 );
					}
					else
					{
						File file = new File( gadText[ GADNUM_DIR_TEXTBOX ][ 0 ] );
						if( gadItem[ GADNUM_DIR_LISTBOX ] > 0 )
						{
							file = new File( file, gadText[ GADNUM_DIR_LISTBOX ][ gadItem[ GADNUM_DIR_LISTBOX ] ].substring( 6 ) );
							if( file.isDirectory() )
							{
								selectedFile = 0;
								listDir( file );
							}
							else
							{
								selectedFile = gadItem[ GADNUM_DIR_LISTBOX ];
								load( file, shift );
							}
						}
						else
						{
							file = file.getParentFile();
							if( file != null )
							{
								selectedFile = 0;
								listDir( file );
							}
						}
					}
					break;
				case GADNUM_SAVE_BUTTON:
					saveModule();
					break;
				case GADNUM_TITLE_TEXTBOX:
					modPlay3.setSongName( gadText[ gadnum ][ 0 ] );
					gadText[ gadnum ][ 0 ] = modPlay3.getSongName();
					break;
				case GADNUM_INST_INC_BUTTON:
					setInstrument( instrument + 1 );
					listInstruments();
					break;
				case GADNUM_INST_DEC_BUTTON:
					setInstrument( instrument - 1 );
					listInstruments();
					break;
				case GADNUM_INST_NAME_TEXTBOX:
					modPlay3.setInstrumentName( instrument, gadText[ gadnum ][ 0 ] );
					setInstrument( instrument );
					listInstruments();
					break;
				case GADNUM_INST_REP_TEXTBOX:
					int rep = parsePositiveInt( gadText[ gadnum ][ 0 ], 0x1FFFE );
					modPlay3.setSampleLoop( instrument, rep, modPlay3.getSampleLoopLength( instrument ) );
					setInstrument( instrument );
					break;
				case GADNUM_INST_VOL_TEXTBOX:
					modPlay3.setSampleVolume( instrument, parsePositiveInt( gadText[ gadnum ][ 0 ], 64 ) );
					setInstrument( instrument );
					break;
				case GADNUM_INST_LEN_TEXTBOX:
					int len = parsePositiveInt( gadText[ gadnum ][ 0 ], 0x1FFFE );
					modPlay3.setSampleLoop( instrument, modPlay3.getSampleLoopStart( instrument ), len );
					setInstrument( instrument );
					break;
				case GADNUM_INST_FINE_TEXTBOX:
					int fine = parsePositiveInt( gadText[ gadnum ][ 0 ], 8 );
					if( fine > 0 && gadText[ gadnum ][ 0 ].charAt( 0 ) == '-' )
					{
						fine = -fine;
					}
					modPlay3.setSampleFinetune( instrument, fine );
					setInstrument( instrument );
					break;
				case GADNUM_SEQ_LISTBOX:
					setSeqPos( gadItem[ gadnum ] );
					setRow( 0 );
					if( modPlay3.getSequencer() )
					{
						modPlay3.seek( getSeqPos(), 0, SAMPLING_RATE );
					}
					else
					{
						modPlay3.setSequencePos( getSeqPos(), 0 );
						stop();
					}
					break;
				case GADNUM_SEQ_INS_BUTTON:
					insertSeq();
					break;
				case GADNUM_SEQ_DEL_BUTTON:
					deleteSeq();
					break;
				case GADNUM_PLAY_BUTTON:
					if( modPlay3.getSequencer() )
					{
						stop();
					}
					else
					{
						play();
					}
					break;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			setError( e.getMessage() );
		}
	}
	
	private static String[] getFileNames( File[] files, String[] names )
	{
		if( names != null )
		{
			names[ 0 ] = "[Parent Dir]";
		}
		int len = 1;
		for( int idx = 0; idx < files.length; idx++ )
		{
			File file = files[ idx ];
			if( !file.isHidden() && file.isDirectory() )
			{
				if( names != null )
				{
					names[ len ] = "[Dir] " + file.getName();
				}
				len++;
			}
		}
		for( int idx = 0; idx < files.length; idx++ )
		{
			File file = files[ idx ];
			if( !file.isHidden() && file.isFile() )
			{
				if( names != null )
				{
					String prefix;
					long size = file.length();
					if( size > 1048576 * 9216 ) 
					{
						prefix = "(>9g) ";
					}
					else if( size > 1024 * 9999 )
					{
						prefix = ModPlay3.pad( Long.toString( size / 1048576 ), ' ', 4, true ) + "m ";
					}
					else if( size > 9999 )
					{
						prefix = ModPlay3.pad( Long.toString( size / 1024 ), ' ', 4, true ) + "k ";
					}
					else
					{
						prefix = ModPlay3.pad( Long.toString( size ), ' ', 5, true ) + " ";
					}
					names[ len ] = prefix + file.getName();
				}
				len++;
			}
		}
		return names != null ? names : getFileNames( files, new String[ len ] );
	}
	
	private File getDir()
	{
		File file = new File( gadText[ GADNUM_DIR_TEXTBOX ][ 0 ] );
		if( !file.isDirectory() )
		{
			file = new File( System.getProperty( "user.home" ) );
		}
		return file;
	}
	
	private void listDir( File file )
	{
		File[] files = file.listFiles();
		Arrays.sort( files );
		String[] names = getFileNames( files, null );
		int[] values = new int[ names.length ];
		for( int idx = 0; idx < names.length; idx++ )
		{
			values[ idx ] = names[ idx ].charAt( 0 ) == '[' ? 1 : 0;
		}
		gadText[ GADNUM_DIR_TEXTBOX ][ 0 ] = file.getAbsolutePath();
		gadText[ GADNUM_DIR_LISTBOX ] = names;
		gadItem[ GADNUM_DIR_LISTBOX ] = 0;
		gadValue[ GADNUM_DIR_SLIDER ] = 0;
		gadValues[ GADNUM_DIR_LISTBOX ] = values;
		gadRedraw[ GADNUM_DIR_TEXTBOX ] = true;
		gadRedraw[ GADNUM_DIR_LISTBOX ] = true;
	}
	
	private static int parsePositiveInt( String str, int max )
	{
		int value = 0;
		for( int idx = 0, len = str.length(); idx < len; idx++ )
		{
			char chr = str.charAt( idx );
			if( chr >= '0' && chr <= '9' )
			{
				value = value * 10 + chr - '0';
			}
		}
		return value > max ? max : value;
	}
	
	private void setSequence( byte[] sequence )
	{
		String[] items = new String[ sequence.length ];
		for( int idx = 0; idx < items.length; idx++ )
		{
			String pat = String.valueOf( sequence[ idx ] );
			items[ idx ] = ModPlay3.pad( String.valueOf( idx ), '0', 3, true )
				+ ' ' + ModPlay3.pad( pat, ' ', 3, true );
		}
		gadText[ GADNUM_SEQ_LISTBOX ] = items;
		modPlay3.setSequence( sequence );
		int seqPos = getSeqPos();
		if( seqPos >= sequence.length )
		{
			seqPos = sequence.length - 1;
		}
		setSeqPos( seqPos );
	}
	
	private void insertSeq()
	{
		int songLength = modPlay3.getSongLength();
		if( songLength < 127 )
		{
			byte[] sequence = new byte[ songLength + 1 ];
			int seqPos = gadItem[ GADNUM_SEQ_LISTBOX ];
			for( int idx = 0; idx <= seqPos; idx++ )
			{
				sequence[ idx ] = ( byte ) modPlay3.getPattern( idx );
			}
			sequence[ seqPos + 1 ] = ( byte ) parsePositiveInt( gadText[ GADNUM_SEQ_TEXTBOX ][ 0 ], 127 );
			for( int idx = seqPos + 2; idx < sequence.length; idx++ )
			{
				sequence[ idx ] = ( byte ) modPlay3.getPattern( idx - 1 );
			}
			setSequence( sequence );
		}
	}
	
	private void deleteSeq()
	{
		int songLength = modPlay3.getSongLength();
		if( songLength > 1 )
		{
			byte[] sequence = new byte[ songLength - 1 ];
			int seqPos = gadItem[ GADNUM_SEQ_LISTBOX ];
			for( int idx = 0; idx < seqPos; idx++ )
			{
				sequence[ idx ] = ( byte ) modPlay3.getPattern( idx );
			}
			for( int idx = seqPos; idx < sequence.length; idx++ )
			{
				sequence[ idx ] = ( byte ) modPlay3.getPattern( idx + 1 );
			}
			setSequence( sequence );
		}
	}
	
	private int getRow()
	{
		return gadValue[ GADNUM_PATTERN_SLIDER ];
	}
	
	private void setRow( int row )
	{
		gadValue[ GADNUM_PATTERN_SLIDER ] = row;
		gadRedraw[ GADNUM_PATTERN ] = true;
	}
	
	private int getSeqPos()
	{
		return gadItem[ GADNUM_SEQ_LISTBOX ];
	}
	
	private void setSeqPos( int seqPos )
	{
		if( seqPos >= modPlay3.getSongLength() )
		{
			seqPos = 0;
		}
		gadText[ GADNUM_SEQ_TEXTBOX ][ 0 ] = String.valueOf( modPlay3.getPattern( seqPos ) );
		gadItem[ GADNUM_SEQ_LISTBOX ] = seqPos;
		gadRedraw[ GADNUM_SEQ_TEXTBOX ] = true;
		gadRedraw[ GADNUM_SEQ_LISTBOX ] = true;
		gadRedraw[ GADNUM_PATTERN ] = true;
	}
	
	private void setInstrument( int idx )
	{
		if( idx < 1 )
		{
			idx = 1;
		}
		if( idx > 31 )
		{
			idx = 31;
		}
		instrument = idx;
		gadText[ GADNUM_INST_TEXTBOX ][ 0 ] = String.valueOf( instrument );
		gadRedraw[ GADNUM_INST_TEXTBOX ] = true;
		gadText[ GADNUM_INST_NAME_TEXTBOX ][ 0 ] = String.valueOf( modPlay3.getInstrumentName( idx ) );
		gadRedraw[ GADNUM_INST_NAME_TEXTBOX ] = true;
		gadText[ GADNUM_INST_REP_TEXTBOX ][ 0 ] = String.valueOf( modPlay3.getSampleLoopStart( idx ) );
		gadRedraw[ GADNUM_INST_REP_TEXTBOX ] = true;
		gadText[ GADNUM_INST_VOL_TEXTBOX ][ 0 ] = String.valueOf( modPlay3.getSampleVolume( idx ) );
		gadRedraw[ GADNUM_INST_VOL_TEXTBOX ] = true;
		gadText[ GADNUM_INST_LEN_TEXTBOX ][ 0 ] = String.valueOf( modPlay3.getSampleLoopLength( idx ) );
		gadRedraw[ GADNUM_INST_LEN_TEXTBOX ] = true;
		gadText[ GADNUM_INST_FINE_TEXTBOX ][ 0 ] = String.valueOf( modPlay3.getSampleFinetune( idx ) );
		gadRedraw[ GADNUM_INST_FINE_TEXTBOX ] = true;
	}
	
	private void listInstruments()
	{
		String[] names = new String[ 31 ];
		for( int ins = 1; ins <= names.length; ins++ )
		{
			String name = ModPlay3.pad( modPlay3.getInstrumentName( ins ), ' ', 22, false );
			String len = ModPlay3.pad( String.valueOf( modPlay3.getSampleLength( ins ) ), ' ', 6, true );
			names[ ins - 1 ] = ModPlay3.pad( String.valueOf( ins ), '0', 2, true ) + ' ' + name + ' ' + len;
		}
		gadValues[ GADNUM_DIR_LISTBOX ] = new int[ names.length ];
		gadItem[ GADNUM_DIR_LISTBOX ] = instrument - 1;
		gadText[ GADNUM_DIR_LISTBOX ] = names;
		gadValue[ GADNUM_DIR_SLIDER ] = instrument - 4;
		gadRedraw[ GADNUM_DIR_LISTBOX ] = true;
	}
	
	private void play()
	{
		error = null;
		modPlay3.setSequencer( true );
		modPlay3.seek( getSeqPos(), gadValue[ GADNUM_PATTERN ], SAMPLING_RATE );
		gadText[ GADNUM_PLAY_BUTTON ][ 0 ] = "Stop";
		gadRedraw[ GADNUM_PLAY_BUTTON ] = true;
	}
	
	private void stop()
	{
		modPlay3.setSequencer( false );
		for( int idx = 0; idx < MAX_CHANNELS; idx ++ )
		{
			modPlay3.trigger( idx, 0, 0, 0 );
		}
		gadText[ GADNUM_PLAY_BUTTON ][ 0 ] = "Play";
		gadRedraw[ GADNUM_PLAY_BUTTON ] = true;
	}
	
	private void copy()
	{
		int patt = getSelectedPattern();
		int chan = getSelectedChannel();
		int row1 = getSelectedRow1();
		int row2 = getSelectedRow2();
		if( patt == getCurrentPattern() && chan >= 0 && chan < modPlay3.getNumChannels() )
		{
			byte[] patternData = modPlay3.getPatternData();
			int offset = getNoteOffset( patt, row2 > row1 ? row1 : row2, chan );
			int count = ( row2 > row1 ? row2 - row1 : row1 - row2 ) + 1;
			copyBuf = new byte[ count * 4 ];
			for( int idx = 0; idx < count; idx++ )
			{
				System.arraycopy( patternData, offset + idx * MAX_CHANNELS * 4, copyBuf, idx * 4, 4 );
			}
		}
	}
	
	private void paste( int transpose )
	{
		int patt = getSelectedPattern();
		int chan = getSelectedChannel();
		int row1 = getSelectedRow1();
		int row2 = getSelectedRow2();
		if( patt == getCurrentPattern() && chan >= 0 && chan < modPlay3.getNumChannels() && row1 == row2 )
		{
			byte[] patternData = modPlay3.getPatternData();
			int offset = getNoteOffset( patt, row1, chan );
			int count = copyBuf.length / 4;
			if( count > 64 - row1 )
			{
				count = 64 - row1;
			}
			for( int idx = 0; idx < count; idx++ )
			{
				System.arraycopy( copyBuf, idx * 4, patternData, offset, 4 );
				int key = patternData[ offset ] & 0xFF;
				if( key > 0 )
				{
					key += transpose;
					while( key < 1 )
					{
						key += 12;
					}
					while( key > 72 )
					{
						key -= 12;
					}
					patternData[ offset ] = ( byte ) key;
				}
				offset += MAX_CHANNELS * 4;
			}
			gadRedraw[ GADNUM_PATTERN ] = true;
		}
	}
	
	private void setNumChannels( int numChannels )
	{
		modPlay3.setMute( 0 );
		modPlay3.setNumChannels( numChannels );
		for( int chn = numChannels; chn < MAX_CHANNELS; chn++ )
		{
			modPlay3.trigger( chn, 0, 0, 0 );
		}
		gadRedraw[ GADNUM_PATTERN ] = true;
	}
	
	private void cropInstrument()
	{
		int loopStart = modPlay3.getSampleLoopStart( instrument );
		int loopLength = modPlay3.getSampleLoopLength( instrument );
		byte[] sampleData = new byte[ loopLength ];
		System.arraycopy( modPlay3.getSampleData( instrument ), loopStart, sampleData, 0, loopLength );
		modPlay3.setSampleData( instrument, sampleData );
		modPlay3.setSampleLoop( instrument, 0, loopLength );
		setInstrument( instrument );
	}
	
	private void deletePattern( int pat )
	{
		int patLen = MAX_CHANNELS * 4 * 64;
		int offset = ( pat + 1 ) * patLen;
		byte[] patternData = modPlay3.getPatternData();
		System.arraycopy( patternData, offset, patternData, offset - patLen, patternData.length - offset );
		byte[] sequence = new byte[ modPlay3.getSongLength() ];
		for( int pos = 0; pos < sequence.length; pos++ )
		{
			int idx = modPlay3.getPattern( pos );
			if( idx == pat )
			{
				idx = 0;
			}
			sequence[ pos ] = ( byte ) ( idx < pat ? idx : idx - 1 );
		}
		setSequence( sequence );
	}
	
	private void optimize()
	{
		boolean[] patternUsed = new boolean[ 128 ];
		int numPatterns = 0;
		int songLength = modPlay3.getSongLength();
		for( int pos = 0; pos < songLength; pos++ )
		{
			int pat = modPlay3.getPattern( pos );
			if( numPatterns <= pat )
			{
				numPatterns = pat + 1;
			}
			patternUsed[ pat ] = true;
		}
		int deleted = 0;
		for( int pat = 0; pat < numPatterns; pat++ )
		{
			if( !patternUsed[ pat ] )
			{
				deletePattern( pat - deleted );
				deleted++;
			}
		}
		boolean[] instrumentUsed = new boolean[ 32 ];
		byte[] patternData = modPlay3.getPatternData();
		int numRows = ( numPatterns - deleted ) * 64;
		int numChannels = modPlay3.getNumChannels();
		for( int row = 0; row < numRows; row++ )
		{
			for( int chn = 0; chn < numChannels; chn++ )
			{
				int ins = patternData[ ( row * MAX_CHANNELS + chn ) * 4 + 1 ] & 0xFF;
				if( ins < 32 )
				{
					instrumentUsed[ ins ] = true;
				}
			}
		}
		for( int idx = 1; idx < instrumentUsed.length; idx++ )
		{
			if( !instrumentUsed[ idx ] )
			{
				modPlay3.setSampleData( idx, new byte[ 0 ] );
			}
		}
		setInstrument( 1 );
		listInstruments();
	}
	
	private void load( File file, boolean shift ) throws IOException
	{
		String extension = file.getName().toLowerCase();
		if( extension.length() > 3 )
		{
			extension = extension.substring( extension.length() - 4 );
		}
		if( extension.equals( ".wav" ) )
		{
			loadWav( file, 0 );
		}
		else if( extension.equals( ".iff" ) || extension.equals( ".sam" )
			|| extension.equals( ".smp" ) || extension.equals( ".raw" ) || shift )
		{
			try
			{
				loadIff( file );
			}
			catch( IllegalArgumentException e )
			{
				loadRaw( file );
			}
		}
		else
		{
			loadMod( file );
		}
	}
	
	private void loadWav( File file, int channel ) throws IOException
	{
		System.out.println( "LoadWav " + file.getAbsolutePath() );
		FileInputStream inputStream = new FileInputStream( file );
		try
		{
			String chunkId = ModPlay3.readString( inputStream, 4 );
			if( !"RIFF".equals( chunkId ) )
			{
				throw new IllegalArgumentException( "Riff header not found." );
			}
			int chunkSize = readIntLe( inputStream, 4 );
			chunkId = ModPlay3.readString( inputStream, 4 );
			if( !"WAVE".equals( chunkId ) )
			{
				throw new IllegalArgumentException( "Wave header not found." );
			}
			chunkId = ModPlay3.readString( inputStream, 3 );
			if( !"fmt".equals( chunkId ) )
			{
				throw new IllegalArgumentException( "Format header not found." );
			}
			inputStream.skip( 1 );
			chunkSize = readIntLe( inputStream, 4 );
			int format = readIntLe( inputStream, 2 );
			int numChannels = readIntLe( inputStream, 2 );
			if( channel < 0 || channel >= numChannels )
			{
				throw new IllegalArgumentException( "No such channel: " + channel );
			}
			int sampleRate = readIntLe( inputStream, 4 );
			int bytesPerSec = readIntLe( inputStream, 4 );
			int bytesPerSample = readIntLe( inputStream, 2 );
			int bytesPerChannel = bytesPerSample / numChannels;
			int bitsPerSample = readIntLe( inputStream, 2 );
			if( format == 0xFFFE )
			{
				int blockSize = readIntLe( inputStream, 2 );
				int validBits = readIntLe( inputStream, 2 );
				int channelMask = readIntLe( inputStream, 4 );
				format = 1;
				for( int idx = 0; idx < 16; idx++ )
				{
					if( inputStream.read() != "\1\0\0\0\0\0\20\0\200\0\0\252\0\70\233\161".charAt( idx ) )
					{
						format = 0;
					}
				}
				inputStream.skip( chunkSize - 40 );
			}
			else
			{
				inputStream.skip( chunkSize - 16 );
			}
			if( format != 1 || bitsPerSample > 24 )
			{
				throw new IllegalArgumentException( "Unsupported sample format." );
			}
			chunkId = ModPlay3.readString( inputStream, 4 );
			while( !"data".equals( chunkId ) )
			{
				//System.err.println( "Ignoring chunk: " + new String( chunkId ) );
				chunkSize = readIntLe( inputStream, 4 );
				inputStream.skip( chunkSize );
				chunkId = ModPlay3.readString( inputStream, 4 );
			}
			int numSamples = readIntLe( inputStream, 4 ) / bytesPerSample;
			byte[] sampleData = new byte[ numSamples ];
			byte[] inputBuf = ModPlay3.readBytes( inputStream, numSamples * bytesPerSample );
			int inputIdx = channel * bytesPerChannel;
			int outputIdx = 0;
			switch( bytesPerChannel )
			{
				case 1: // 8-bit unsigned.
					while( outputIdx < numSamples )
					{
						sampleData[ outputIdx++ ] = ( byte ) ( ( inputBuf[ inputIdx ] & 0xFF ) - 128 );
						inputIdx += bytesPerSample;
					}
					break;
				case 2: // 16-bit signed little-endian.
					while( outputIdx < numSamples )
					{
						sampleData[ outputIdx++ ] = ( byte ) inputBuf[ inputIdx + 1 ];
						inputIdx += bytesPerSample;
					}
					break;
				case 3: // 24-bit signed little-endian.
					while( outputIdx < numSamples )
					{
						sampleData[ outputIdx++ ] = ( byte ) inputBuf[ inputIdx + 2 ];
						inputIdx += bytesPerSample;
					}
					break;
			}
			setSampleData( file.getName(), sampleData );
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private void loadIff( File file ) throws IOException
	{
		System.out.println( "LoadIff " + file.getAbsolutePath() );
		FileInputStream inputStream = new FileInputStream( file );
		try
		{
			String chunkId = ModPlay3.readString( inputStream, 4 );
			if( !"FORM".equals( chunkId ) )
			{
				throw new IllegalArgumentException( "FORM chunk not found." );
			}
			int chunkSize = readIntBe( inputStream, 4 );
			chunkId = ModPlay3.readString( inputStream, 4 );
			if( !"8SVX".equals( chunkId ) )
			{
				throw new IllegalArgumentException( "8SVX chunk not found." );
			}
			chunkId = ModPlay3.readString( inputStream, 4 );
			if( !"VHDR".equals( chunkId ) )
			{
				throw new IllegalArgumentException( "VHDR chunk not found." );
			}
			chunkSize = readIntBe( inputStream, 4 );
			int attackLen = readIntBe( inputStream, 4 );
			int sustainLen = readIntBe( inputStream, 4 );
			int samplesHigh = readIntBe( inputStream, 4 );
			int sampleRate = readIntBe( inputStream, 2 );
			int numOctaves = inputStream.read();
			int compression = inputStream.read();
			if( compression != 0 )
			{
				throw new IllegalArgumentException( "Compressed IFF not supported." );
			}
			int volume = readIntBe( inputStream, 4 );
			chunkId = ModPlay3.readString( inputStream, 4 );
			while( !"BODY".equals( chunkId ) )
			{
				chunkSize = readIntBe( inputStream, 4 );
				inputStream.skip( chunkSize );
				chunkId = ModPlay3.readString( inputStream, 4 );
			}
			int numSamples = readIntBe( inputStream, 4 );
			setSampleData( file.getName(), ModPlay3.readBytes( inputStream, numSamples ) );
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private void loadRaw( File file ) throws IOException
	{
		System.out.println( "LoadRaw " + file.getAbsolutePath() );
		FileInputStream inputStream = new FileInputStream( file );
		try
		{
			setSampleData( file.getName(), ModPlay3.readBytes( inputStream, ( int ) file.length() ) );
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private void setSampleData( String instrumentName, byte[] sampleData )
	{
		modPlay3.setInstrumentName( instrument, instrumentName );
		modPlay3.setSampleData( instrument, sampleData );
		modPlay3.setSampleVolume( instrument, 64 );
		setInstrument( instrument );
		stop();
	}
	
	private void loadMod( File file ) throws IOException
	{
		System.out.println( "LoadMod " + file.getAbsolutePath() );
		FileInputStream inputStream = new FileInputStream( file );
		try
		{
			modPlay3 = new ModPlay3( inputStream, false );
		}
		finally
		{
			inputStream.close();
		}
		byte[] newPatternData = new byte[ MAX_CHANNELS * 4 * 64 * 128 ];
		byte[] patternData = modPlay3.getPatternData();
		int rowLength = modPlay3.getNumChannels() * 4;
		int numRows = patternData.length / rowLength;
		for( int row = 0; row < numRows; row++ )
		{
			System.arraycopy( patternData, row * rowLength, newPatternData, row * MAX_CHANNELS * 4, rowLength );
		}
		modPlay3.setPatternData( newPatternData, MAX_CHANNELS );
		gadItem[ GADNUM_PATTERN ] = 0;
		gadText[ GADNUM_TITLE_TEXTBOX ][ 0 ] = modPlay3.getSongName();
		gadRedraw[ GADNUM_TITLE_TEXTBOX ] = true;
		setSeqPos( 0 );
		gadValue[ GADNUM_SEQ_SLIDER ] = 0;
		setRow( 0 );
		byte[] sequence = new byte[ modPlay3.getSongLength() ];
		for( int seqPos = 0; seqPos < sequence.length; seqPos++ )
		{
			sequence[ seqPos ] = ( byte ) modPlay3.getPattern( seqPos );
		}
		setSequence( sequence );
		gadValue[ GADNUM_DIR_SLIDER ] = 0;
		setInstrument( 1 );
		listInstruments();
		stop();
	}
	
	private static String toFileName( String name )
	{
		int len = 0;
		char[] chars = name.toCharArray();
		for( int idx = 0; idx < chars.length; idx++ )
		{
			char chr = chars[ idx ];
			if( ( chr >= '0' && chr <= '9' ) || ( chr >= 'A' && chr <= 'Z' ) || ( chr >= 'a' && chr <= 'z' ) )
			{
				chars[ len++ ] = chr;
			}
			else if( len > 0 && chars[ len - 1 ] > 32 )
			{
				chars[ len++ ] = 32;
			}
		}
		return new String( chars, 0, len < 1 || chars[ len - 1 ] > 32 ? len : len - 1 );
	}
	
	private static String getTimestamp( int length )
	{
		char[] chars = new char[ length ];
		long time = System.currentTimeMillis() / 2000;
		for( int idx = length - 1; idx >= 0; idx-- )
		{
			chars[ idx ] = HEX_TO_STR.charAt( ( int ) time & 0xF );
			time = time >> 4;
		}
		return new String( chars );
	}
	
	private void saveModule() throws IOException
	{
		String name = toFileName( modPlay3.getSongName() );
		File file = new File( getDir(), name + ".mod" );
		if( file.exists() )
		{
			file = new File( getDir(), name + '_' + getTimestamp( 6 ) + ".mod" );
		}
		if( file.exists() )
		{
			throw new IOException( "File already exists!" );
		}
		else
		{
			FileOutputStream outputStream = new FileOutputStream( file );
			try
			{
				modPlay3.writeModule( outputStream );
			}
			finally
			{
				outputStream.close();
			}
			listDir( getDir() );
		}
	}
	
	private void saveInstrument() throws IOException
	{
		byte[] data = modPlay3.getSampleData( instrument );
		if( data.length > 0 )
		{
			String num = ( instrument < 10 ? "0" : "" ) + instrument;
			String name = toFileName( modPlay3.getInstrumentName( instrument ) );
			File file = new File( getDir(), num + ( name.length() > 0 ? "_" + name : "" ) + ".raw" );
			if( file.exists() )
			{
				throw new IOException( "File already exists!" );
			}
			else
			{
				FileOutputStream outputStream = new FileOutputStream( file );
				try
				{
					outputStream.write( data );
				}
				finally
				{
					outputStream.close();
				}
				listDir( getDir() );
			}
		}
	}
	
	private synchronized int getAudio( int sampleRate, int[] output )
	{
		int count = modPlay3.getAudio( sampleRate, output );
		if( modPlay3.getSequencer() && focus != GADNUM_PATTERN_SLIDER )
		{
			int seqPos = modPlay3.getSequencePos();
			int row = modPlay3.getRow();
			if( seqPos != getSeqPos() || row != getRow() )
			{
				int dt = ( ( int ) System.currentTimeMillis() ) - gadRange[ GADNUM_SEQ_LISTBOX ];
				if( seqPos != getSeqPos() && ( dt < 0 || dt > 500 ) )
				{
					setSeqPos( seqPos );
					gadValue[ GADNUM_SEQ_SLIDER ] = seqPos - 3;
				}
				setRow( row );
				repaint();
			}
		}
		return count;
	}
	
	public static void main( String[] args ) throws Exception
	{
		Tracker3 tracker3 = new Tracker3( 640, 452 );
		Frame frame = new Frame( VERSION );
		frame.setIconImage( tracker3.iconImage() );
		frame.addWindowListener( tracker3 );
		frame.add( tracker3, BorderLayout.CENTER );
		frame.pack();
		frame.setResizable( false );
		frame.setVisible( true );
		final int DOWNSAMPLE_BUF_SAMPLES = 2048;
		final int[] FILTER_COEFFS = { -512, 0, 4096, 8192, 4096, 0, -512 };
		javax.sound.sampled.AudioFormat audioFormat = new javax.sound.sampled.AudioFormat( SAMPLING_RATE, 16, 2, true, false );
		javax.sound.sampled.SourceDataLine sourceDataLine = ( javax.sound.sampled.SourceDataLine )
			javax.sound.sampled.AudioSystem.getLine( new javax.sound.sampled.DataLine.Info(
				javax.sound.sampled.SourceDataLine.class, audioFormat ) );
		sourceDataLine.open( audioFormat, 16384 );
		try
		{
			sourceDataLine.start();
			byte[] outBuf = new byte[ DOWNSAMPLE_BUF_SAMPLES * 2 ];
			int[] reverbBuf = new int[ ( SAMPLING_RATE / 20 ) * 2 ];
			int[] downsampleBuf = new int[ ( DOWNSAMPLE_BUF_SAMPLES + FILTER_COEFFS.length ) * 2 ];
			int[] mixBuf = new int[ SAMPLING_RATE * 2 / 5 ];
			int mixIdx = 0, mixLen = 0, reverbIdx = 0;
			while( frame.isDisplayable() )
			{
				System.arraycopy( downsampleBuf, DOWNSAMPLE_BUF_SAMPLES * 2, downsampleBuf, 0, FILTER_COEFFS.length * 2 );
				int offset = FILTER_COEFFS.length;
				int length = offset + DOWNSAMPLE_BUF_SAMPLES;
				while( offset < length )
				{
					if( mixIdx >= mixLen )
					{
						mixLen = tracker3.getAudio( SAMPLING_RATE * 2, mixBuf );
						mixIdx = 0;
					}
					int count = length - offset;
					if( count > mixLen - mixIdx )
					{
						count = mixLen - mixIdx;
					}
					System.arraycopy( mixBuf, mixIdx * 2, downsampleBuf, offset * 2, count * 2 );
					mixIdx += count;
					offset += count;
				}
				ModPlay3.downsample( downsampleBuf, DOWNSAMPLE_BUF_SAMPLES / 2, FILTER_COEFFS );
				if( tracker3.reverb )
				{
					reverbIdx = ModPlay3.reverb( downsampleBuf, reverbBuf, reverbIdx, DOWNSAMPLE_BUF_SAMPLES / 2 );
				}
				ModPlay3.clip( downsampleBuf, outBuf, DOWNSAMPLE_BUF_SAMPLES );
				sourceDataLine.write( outBuf, 0, DOWNSAMPLE_BUF_SAMPLES * 2 );
			}
		}
		finally
		{
			sourceDataLine.close();
		}
	}
}
