//**************************************************************************
// 3D Mandelbrot
// Ryan Sweny
// rsweny@...
// 2009
// v1.0
// based on algorithm by Daniel White (http://www.skytopia.com/project/fractal/mandelbulb.html)
//**************************************************************************

import java.awt.*;
import java.awt.image.PixelGrabber;
import java.awt.image.MemoryImageSource;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.Vector;

public class Mandelbrot3D extends java.applet.Applet implements Runnable, KeyListener, ActionListener
{
	//detail level
	double occlusion_depth = 0.004;//0.003
	
	//main ray traced lights
	int RAY_STEPS = 20;
	double ray_step;
	double primary_light = 30.0;
	int shadow_darkness = 35;
	int frost = 1;
	
	//point search params - affect details discovered at edges
	int solidness = 10;
	int numpoints = 10000;
	int surface_search_limit = 2000;
	double close_dist;
	
	//fog based on path traces
	double fog_factor = 0.002;
	double fog_dist = 0.20;
	
	//formula variation
	int formula = 0;
	int inverse_azimuth = -1;
	
	String lastDir = "";
	private int reset = 0;
	
	Image img;
	MemoryImageSource screenMem;
	int[] pixels;
	Thread[] runner;
	
	//settings that need saving
	int depth = 20;
	int ximlen = 0;
	int yimlen = 0;
	int pal = 11;
	
	double power = 8;
	double gradient = 0.5;
	double brightness = 3.5;
	double zoom = 1.5;
	double xcen = 0.0;
	double ycen = 0.0;
	double cameraPersp = 0.1;
	double cameraZpos = 0.0;
	double cameraYaw = 0.0;
	double cameraPitch = 0.85;
	double cameraDOF = 0.0;
	double opacity = 2.0;
	double focus = 0.0; //higher values for further distance in focus.
	double glow = 0.01;

	int yRow;
	int half_ximlen, half_yimlen;
	double[][][] occlusionPositions = null;

	float[][] img_alpha = null;
	float[][] img_red = null;
	float[][] img_green = null;
	float[][] img_blue = null;
	
	
	int xcurr,ycurr,xanchor,yanchor;
	boolean m_down;
	double root_zoom = 1.2;
	
	//3D Camera
	double[][] CameraMatrix = new double[3][3];
	double[][] InverseCameraMatrix = new double[3][3];

	//Stats
	long visiblePixels, occludedPixels, randomPixels, rejectedPixels, notPixels, allPixels;
	int renderpass = 0;
	double max_alpha = 1;
	
	//UI
	TextField txtZoom = new TextField(8);
	TextField txtGradient = new TextField(5);
	TextField txtBrightness = new TextField(5);
	TextField txtPower = new TextField(5);
	TextField txtDepth = new TextField(5);
	Checkbox chkTrace = new Checkbox("Traces", null, false);
	
	Panel panelIndex = new Panel(new FlowLayout(FlowLayout.LEFT));
	Panel panelMain = new Panel(new BorderLayout());
	Panel panelButtons = new Panel(new FlowLayout(FlowLayout.LEFT));
	
	Panel panel3D = new Panel(new GridLayout(5,4));
	TextField txtPerspective = new TextField(5);
	TextField txtZPos = new TextField(5);
	TextField txtYaw = new TextField(5);
	TextField txtPitch = new TextField(5);
	TextField txtDOF = new TextField(5);
	TextField txtOpacity = new TextField(5);
	TextField txtFocus = new TextField(5);
	TextField txtZRes = new TextField(5);
	TextField txtFog = new TextField(5);
	TextField txtLight = new TextField(5);
	
	Button saveFractal = new Button("Save Fractal");
	Button openFractal = new Button("Open Fractal");
	Button exportPNG = new Button("Export PNG");
	
	boolean running = false;
	
	public void start()
	{
		running = true;
		for (int i = 0; i < runner.length; i++)
		{
			if (runner[i] == null);
			{
				runner[i] = new Thread(this);
				runner[i].start();
			}
		}
	}


	public void stop()
	{
		running = false;
		
		for (int i = 0; i < runner.length; i++)
			runner[i] = null;
			
		System.out.println("stop()");
	}
	
	public void destroy()
	{
		super.destroy();
		stop();
	}
	
	private void initVars(boolean fresh)
	{
		if (fresh)
		{
			String szheight = getParameter("height");
			yimlen = Integer.parseInt(szheight);
			
			String szwidth = getParameter("width");
			ximlen = Integer.parseInt(szwidth);
		}
		
		half_ximlen = ximlen / 2;
		half_yimlen = yimlen / 2;
		yRow = 0;


		pixels = new int[ ximlen * yimlen ];

		screenMem = new MemoryImageSource(ximlen, yimlen, pixels, 0, ximlen);
		clearScreenAndReset(fresh);
		setConstants();
		setCamera();
	}
	
	public void init()
	{
		Runtime runtime = Runtime.getRuntime();
        	int nrOfProcessors = runtime.availableProcessors();
		runner = new Thread[nrOfProcessors];
		System.out.println(nrOfProcessors + " processors detected.");
		
		initVars(true);
		
		setBackground(Color.black);
		panelIndex.add(new Label("Zoom"));
		panelIndex.add(txtZoom);
		panelIndex.add(new Label("Contrast"));
		panelIndex.add(txtGradient);
		panelIndex.add(new Label("Brightness"));
		panelIndex.add(txtBrightness);
		panelIndex.add(new Label("N"));
		panelIndex.add(txtPower);
		panelIndex.add(new Label("Iter"));
		panelIndex.add(txtDepth);

		txtZoom.addKeyListener(this);
		txtGradient.addKeyListener(this);
		txtBrightness.addKeyListener(this);
		txtPower.addKeyListener(this);
		txtDepth.addKeyListener(this);

		
		txtPerspective.addKeyListener(this);
		txtZPos.addKeyListener(this);
		txtPitch.addKeyListener(this);
		txtYaw.addKeyListener(this);
		txtDOF.addKeyListener(this);
		txtOpacity.addKeyListener(this);
		txtFocus.addKeyListener(this);
		txtZRes.addKeyListener(this);
		txtFog.addKeyListener(this);
		txtLight.addKeyListener(this);
		
		panel3D.add(new Label("Perspective"));
		panel3D.add(txtPerspective);
		panel3D.add(new Label("Z"));
		panel3D.add(txtZPos);
		panel3D.add(new Label("Yaw"));
		panel3D.add(txtYaw);
		panel3D.add(new Label("Pitch"));
		panel3D.add(txtPitch);
		panel3D.add(new Label("DOF"));
		panel3D.add(txtDOF);
		panel3D.add(new Label("Focus"));
		panel3D.add(txtFocus);
		panel3D.add(new Label("Depth"));
		panel3D.add(txtOpacity);
		panel3D.add(new Label("Glow"));
		panel3D.add(txtZRes);
		panel3D.add(new Label("Fog"));
		panel3D.add(txtFog);
		panel3D.add(new Label("Light Intensity"));
		panel3D.add(txtLight);
		
		
		
		saveFractal.addActionListener(this);
		openFractal.addActionListener(this);
		exportPNG.addActionListener(this);
		
		panelButtons.add(saveFractal);
		panelButtons.add(openFractal);
		panelButtons.add(exportPNG);
		
		panelMain.add(panelIndex, BorderLayout.NORTH);
		panelMain.add(panel3D, BorderLayout.CENTER);
		panelMain.add(panelButtons, BorderLayout.SOUTH);

		panelMain.setBackground(Color.WHITE);
		panelIndex.setBackground(new Color(128,128,128));
		
		add(panelMain);
	}

	public void run()
	{
		while (running)
		{
			try
			{
				if (reset == 1)
				{
					reset = 0;
					clearScreenAndReset(true);
				}
				else if (reset == 2)
				{
					reset = 0;
					updateHistogram();
					repaint();
				}
				
				if (!m_down) 
				{
					renderpass++;
					//local variable to keep consistent across threads
					int currentPass = renderpass;
					
					if (yRow < yimlen) gridpoints();
					nextpoints();
						
					Thread.sleep(10);
					
					int reset_cycle = ximlen*solidness;
					if ( (currentPass % reset_cycle) > ximlen )
					{
						if (currentPass%ximlen > ximlen/2)
							close_dist = 0.015;
						else
							close_dist = 0.024;
					}
					
					if (currentPass % reset_cycle == 0)
					{
						System.out.println("push back occulsions " + visiblePixels + " " + occludedPixels + " " + notPixels + " " + allPixels + " " + rejectedPixels + " " + occlusionPositions[half_ximlen][half_yimlen][3]);
						for (int i = 0; i < ximlen; i++)
						{
							for (int j = 0; j < yimlen; j++)
							{
								occlusionPositions[i][j][3] += 2.0; 
							}
						}
						close_dist = 0.04;
					}
					
					if (currentPass < 20 || currentPass % 50 == 0)
					{
						double completeness = Math.round(max_alpha*100)/100.0;
						String strStatus = (visiblePixels*100 / allPixels) + "% Pass: " + currentPass + " max value: " + completeness + " " + occlusionPositions[half_ximlen][half_yimlen][3];
						System.out.println(strStatus);
						showStatus(strStatus);
						
						updateHistogram();
						repaint();

						//autosave png and last 5 states.
						if (lastDir != null && lastDir.length() > 0 && (currentPass % 5000 == 0) )
						{
							if (currentPass%20000 == 10000) writePNG(lastDir + "Mandelbulb-" + currentPass + "-" + completeness + "-" + ximlen + "x" + yimlen + ".png");
							writeFractalData(lastDir + "Mandelbulb-" + (currentPass%30000) + "-" + ximlen + "x" + yimlen + ".fractal");
						}
						
						visiblePixels = 0;
						occludedPixels = 0;
						randomPixels = 0;
						notPixels = 0;
						rejectedPixels = 0;
						allPixels = 0;
					}

					

				}
				else
				{
					Thread.sleep(1000);
				}
			}
			catch (Exception e)
			{
				System.out.println("Thread error: " + e.toString());
			}
		}
		System.out.println("run() exited");
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void updateHistogram()
	{
		int red = 0;
		int green = 0;
		int blue = 0;
		
		max_alpha = Math.pow(FindPeak(img_alpha), gradient);
		
		for (int i=0; i<ximlen; i++)
		{
			for (int j=0; j<yimlen; j++)
			{
				double z = Math.pow(img_alpha[i][j], gradient)*brightness/max_alpha;

				red = (int)( (img_red[i][j]*z)/img_alpha[i][j] );
				green = (int)( (img_green[i][j]*z)/img_alpha[i][j] );
				blue = (int)( (img_blue[i][j]*z)/img_alpha[i][j] );
				
				if (red > 255) red = 255;
				if (green > 255) green = 255;
				if (blue > 255) blue = 255;
			
				red = red << 16;
				green = green << 8;
				pixels[j*ximlen + i] = 0xff000000 |(red & 0x00ff0000) | (green & 0x0000ff00) | (blue & 0xff);
			}
		}
		img = createImage(screenMem);
	}


	public void paint(Graphics g)
	{
		g.drawImage(img, 0, 0, this);
    	if (m_down) 
        {
			g.setColor(Color.white);
			g.drawRect(xanchor,yanchor,xcurr-xanchor,ycurr-yanchor);
        }
	}
	
	public long FindPeak(float[][] arr)
	{
		double max = 0;
		for (int i = 0; i < ximlen; i++)
		{
			for (int j = 0; j < yimlen; j++)
			{
				if (arr[i][j] > max)
				{
					max = arr[i][j];
				}
			}
		}
		return (long)(max+1);
	}

	public void clearScreenAndReset(boolean fresh)
	{
		yRow = 0;
		renderpass = 0;
		max_alpha = 1;
		
		if (fresh)
		{
			img_alpha = new float[ximlen][yimlen];
			img_red = new float[ximlen][yimlen];
			img_green = new float[ximlen][yimlen];
			img_blue = new float[ximlen][yimlen];
		}
		occlusionPositions = new double[ximlen][yimlen][4];


		clearOcclusions();
		
		m_down = false;
	}
	
	private void clearOcclusions()
	{
		System.out.println("clearOcclusions");
		close_dist = 0.08;
		
		for (int i = 0; i < ximlen; i++)
		{
			for (int j = 0; j < yimlen; j++)
			{
				occlusionPositions[i][j][3] = 10000;
				occlusionPositions[i][j][0] = 0;
			}
		}
	}
	
	
	private void gridpoints()
	{
		yRow++;
		double[] origPoint = new double[3];
		double worldY = (0.5 - (double)(yimlen - yRow)/(double)yimlen)*2.0*zoom - ycen;
		
		for (int i = 0; i < ximlen; i++)
		{
			double worldX = (0.5 - (double)i/(double)ximlen)*2.0*zoom - xcen + Math.random()*zoom*0.02;
			
			allPixels++;
			int hit = 0;
			double depth_z = 2.0;
			

			while (hit < 20)
			{
				origPoint[0] = worldX;
				origPoint[1] = worldY + Math.random()*zoom*0.02;
				origPoint[2] = depth_z + Math.random()*0.05;
				
				double[] worldPoint = reversePitchYaw(origPoint);
				
				//do the calculation
				insideFractal(worldPoint, null);
				int iter = (int)worldPoint[3];
				
				//if inside the set color the pixel
				if (iter == depth)
				{
					hit = 21;
					plotShadowPixel(worldPoint, worldPoint[4], worldPoint[5], null, 0);
				}
				else
				{
					hit++;
					depth_z -= 0.1;
				}
			}
			
			if (hit == 21)
			{
				//found surface, reverse course for a bit
				hit = 0;
				while (hit < 20)
				{
					origPoint[0] = worldX;
					origPoint[1] = worldY;
					origPoint[2] = depth_z;
					
					double[] worldPoint = reversePitchYaw(origPoint);
					
					//do the calculation
					insideFractal(worldPoint, null);
					int iter = (int)worldPoint[3];
					
					//if inside the set color the pixel
					if (iter == depth)
					{
						hit++;
						plotShadowPixel(worldPoint, worldPoint[4], worldPoint[5], null, 0);
					}
					else
					{
						hit+=4;
					}
					depth_z += 0.02;
					
				}
			}
		}
	}
	
	
	
	private void nextpoints()
	{
		double rand1 = Math.random();
		double rand2, rand3;
		
		double x = 0;
		double y = 0;
		double z = 0;

		double[] origPoint = new double[6];
		double[][] traceHistory = new double[depth+1][6];
		Vector goodPoints = new Vector();
		
		boolean wasPointVisible = false;
		int search_count = 0;
		
		for (int i = 0; i < numpoints; i++)
		{
			allPixels++;
			if (wasPointVisible && search_count < surface_search_limit)
			{
				//use a point close to the last one. try surface_search_limit times to find another point in the set that is near a point that was in the set and not occluded.
				x = origPoint[0] + (Math.random()- 0.5)*close_dist*zoom;
				y = origPoint[1] + (Math.random()- 0.5)*close_dist*zoom;
				z = origPoint[2] + (Math.random()- 0.5)*close_dist*zoom;
			}
			else
			{
				rand1 = Math.random();
				rand2 = Math.random();
				rand3 = Math.random();
				
				//pick random points near surface boundry
				int gridx = (int)(ximlen*rand1*0.99);
				int gridy = (int)(yimlen*rand2*0.99);
				boolean foundSurface = false;
				for (int j = 0; j < 9; j++)
				{
					if ( occlusionPositions[gridx][gridy][0] != 0)
					{
						x = occlusionPositions[gridx][gridy][0] + (rand1 - 0.5)*close_dist*root_zoom;
						y = occlusionPositions[gridx][gridy][1] + (rand2 - 0.5)*close_dist*root_zoom;
						z = occlusionPositions[gridx][gridy][2] + (rand3 - 0.5)*close_dist*root_zoom;
						foundSurface = true;
					}
					else
					{
						gridx = (int)(ximlen*Math.random()*0.99);
						gridy = (int)(yimlen*Math.random()*0.99);
					}
				}

				if (!foundSurface)
				{
					if (zoom > 0.5)
					{
						x = (8*(rand1 - 0.5));
						y = (8*(rand2 - 0.5));
						z = (8*(rand3 - 0.5));
					}
					else
					{	x = (2*(rand1 - 0.5));
						y = (2*(rand2 - 0.5));
						z = (2*(rand3 - 0.5));
					}
					randomPixels++;
				}
			}
			origPoint[0] = x;
			origPoint[1] = y;
			origPoint[2] = z;
			
			//do the calculation
			insideFractal(origPoint, traceHistory);
			int iter = (int)origPoint[3];

			//if inside the set color the pixel
			if (iter == depth)
			{
				wasPointVisible = plotShadowPixel(origPoint, origPoint[4], origPoint[5], goodPoints, 0);
				search_count = 0;
			}
			else
			{
				notPixels++;
				search_count += (depth - iter);
				if (fog_factor > 0)
				{
					//double whiteness = 200 - 10*(depth-iter);
					for (int c = 0; c < iter; c++)
					{
						double red = 255/(c+1);
						plotFogPixel(traceHistory[c], fog_factor, 255-red, Math.max(0, 175 - red), red);
					}
				}
			}
			
		}
		
		if (renderpass % 500 == 400) System.out.println( goodPoints.size() + " raypoints" );
		
		for (int i = 0; i < goodPoints.size(); i++)
		{
			allPixels++;
			double[] savedPoint = (double[])goodPoints.elementAt(i);
			plotShadowPixel(savedPoint, savedPoint[4], savedPoint[5], null, 0);
		}
		goodPoints = new Vector();	
	}
	
	private void insideFractal(double[] data, double[][] traceHistory)
	{
		double magnitude, r, theta_power, r_power, phi, phi_cos, phi_sin;
		int iter = 0;
		double x = data[0];
		double y = data[1];
		double z = data[2];
		
		double first_magnitude = x*x + y*y + z*z;
		
		do
		{	
			magnitude = x*x + y*y + z*z;
			r = Math.sqrt(magnitude);
			theta_power = Math.atan2(y,x)*power;
			r_power = Math.pow(r,power);
			
			if (formula == 0)
			{
				//2D compatible / sin
				phi = Math.asin(z / r);
				phi_cos = Math.cos(phi*power);
				x = r_power * Math.cos(theta_power) * phi_cos + data[0];
				y = r_power * Math.sin(theta_power) * phi_cos + data[1];
				z = r_power * Math.sin(phi*power)*inverse_azimuth + data[2];
			}
			else //if (formula == 1)
			{
				//wikipedia / original / cos
				phi = Math.atan2(Math.sqrt(x*x + y*y), z);
				phi_sin = Math.sin(phi*power);
				x = r_power * Math.cos(theta_power) * phi_sin + data[0];
				y = r_power * Math.sin(theta_power) * phi_sin + data[1];
				z = r_power * Math.cos(phi*power)*inverse_azimuth + data[2];
			}
			
			//tower
			/*
			phi = Math.acos(z / r);
			phi_cos = Math.cos(phi*power);
			x = r_power * Math.cos(theta_power) * phi_cos + data[0];
			y = r_power * Math.sin(theta_power) * phi_cos + data[1];
			z = r_power * Math.sin(phi*power)*inverse_azimuth + data[2];
			*/
			
			//used for nebula / fog
			if (traceHistory != null)
			{
				traceHistory[iter][0] = x;
				traceHistory[iter][1] = y;
				traceHistory[iter][2] = z;
				traceHistory[iter][3] = iter;
				traceHistory[iter][4] = phi;
				traceHistory[iter][5] = 1.0;
			}
			
			iter++;
		}
		while ( iter < depth && r < 8 );

		data[3] = iter;
		data[4] = phi;
		data[5] = first_magnitude;
	}
	
	private double calculateRays(double[] origPoint, Vector goodPoints, double rnd, double light_depth)
	{
		double light_factor = 1.0;
		double rndFuzzy = (0.95 + rnd*0.1)*light_depth;
		double n0 = ray_step / (20.0*rnd + 1);
		
		//ambient light
		light_factor += calculateRay(origPoint, RAY_STEPS, ray_step, n0, n0, 2.0, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, n0, ray_step, n0, 1.0, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, n0, n0, ray_step, 5.0, rndFuzzy, goodPoints);
		//light_factor += calculateRay(origPoint, RAY_STEPS,  n0, ray_step, ray_step, 2.0, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, -ray_step, n0, ray_step, 3.0, rndFuzzy, goodPoints);
		//light_factor += calculateRay(origPoint, RAY_STEPS, -ray_step, ray_step, n0, 2.0, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, -ray_step/2.0,  -ray_step, ray_step, 2.0, rndFuzzy, goodPoints);
		
		light_factor += calculateRay(origPoint, RAY_STEPS, -ray_step, n0, n0, 1.0, rndFuzzy, goodPoints);
		//light_factor += calculateRay(origPoint, RAY_STEPS, n0, -ray_step, n0, 1.0, rndFuzzy, goodPoints);
		//light_factor += calculateRay(origPoint, RAY_STEPS, n0, n0, -ray_step, 0.5, rndFuzzy, goodPoints);
		//light_factor += calculateRay(origPoint, RAY_STEPS, n0, -ray_step, ray_step, 1.0, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, -ray_step, n0, -ray_step, 0.5, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, ray_step, -ray_step, n0, 0.5, rndFuzzy, goodPoints);
		light_factor += calculateRay(origPoint, RAY_STEPS, -ray_step,  -ray_step, -ray_step, 0.5, rndFuzzy, goodPoints);
		
		//direct light
		light_factor += calculateRay(origPoint, RAY_STEPS*3, -ray_step/6.0,  -ray_step/5.0, ray_step/2.0, primary_light, rndFuzzy, goodPoints);
		
		return light_factor;
	}
	
	private double calculateRay(double[] origPoint, double steps, double stepx, double stepy, double stepz, double bright, double ray_depth, Vector goodPoints)
	{
		stepx *= ray_depth;
		stepy *= ray_depth;
		stepz *= ray_depth;

		double x = origPoint[0];
		double y = origPoint[1];
		double z = origPoint[2];

		for (int i = 1; i < steps; i++)
		{
			origPoint[0] += stepx*i;
			origPoint[1] += stepy*i;
			origPoint[2] += stepz*i;
			
			insideFractal( origPoint, null );
			if (origPoint[5] > 64)
			{
				//System.out.println("mag out!");
				break;
			}
			else if ( origPoint[3] == depth )
			{
				//brightness = ((double)i/steps)*0.3;
				bright = 0.0;
				
				if (goodPoints != null && i > 1)
				{
					//save points that are moderately close to the known surface
					double[] clone_data = new double[6];
					System.arraycopy(origPoint, 0, clone_data, 0, clone_data.length);
					goodPoints.addElement(clone_data);
				}
				break;
			}			
			else if ( steps <= RAY_STEPS && origPoint[3] < 4)
			{
				//ambient ray has left the general area of the solid, stop tracing.
				break;
			}
		}
		origPoint[0] = x;
		origPoint[1] = y;
		origPoint[2] = z;
		
		return bright;
	}
	

	private boolean plotShadowPixel(double[] origPoint, double colorVal, double first_magnitude, Vector goodPoints, int doLight)
	{
		if (first_magnitude > 1.0) first_magnitude = 1.0;
		
		//project the point into 3D space
		boolean isVisible = false;
		double rnd1 = Math.random();
		double rnd2 = Math.random();
		double[] projectedPoint = projectPitchYawDOF(origPoint, rnd1, rnd2);
		
		double realx = projectedPoint[0] + xcen;
		double realy = projectedPoint[1] + ycen;
		boolean isPointInViewport = realx > -zoom && realy > -zoom && realx < zoom && realy < zoom;
		
		if (isPointInViewport)
		{
			//-- Update the histogram --
			int tempx = (int)Math.floor ( (realx/zoom)*half_ximlen + half_ximlen );
			int tempy = (int)Math.floor ( (realy/zoom)*half_yimlen + half_yimlen );
			
			int colorIndex = (int)(Math.abs(colorVal/3)*255.0);
			if (colorIndex > 255) colorIndex = 255;
			
			double difference_from_camera = -projectedPoint[2] - occlusionPositions[tempx][tempy][3];
			if ( difference_from_camera < occlusion_depth * opacity * root_zoom )
			{
				//this point is on the surface
				double redplus = Pallet.fpalette[pal][colorIndex][0]*first_magnitude;
				double greenplus = Pallet.fpalette[pal][colorIndex][1]*first_magnitude;
				double blueplus = Pallet.fpalette[pal][colorIndex][2]*first_magnitude;
				double red = redplus;
				double green = greenplus;
				double blue = blueplus;
				
				double light_factor = 6.0;
				if (renderpass > ximlen)
				{
					double light_penetrate = 1.0;
					if (opacity > 1.0)
					{
						light_penetrate = difference_from_camera / (occlusion_depth*root_zoom);
						light_penetrate = light_penetrate*frost*rnd1;
						light_penetrate = Math.max(1.0, light_penetrate);
					}
					light_factor = calculateRays(origPoint, goodPoints, rnd2, light_penetrate);
				}
					
				red += redplus*light_factor;
				green += greenplus*light_factor;
				blue += blueplus*light_factor;
				
				red /= shadow_darkness;
				green /= shadow_darkness;
				blue /= shadow_darkness;
				
				//solid point
				occlusionPositions[tempx][tempy][0] = origPoint[0];
				occlusionPositions[tempx][tempy][1] = origPoint[1];
				occlusionPositions[tempx][tempy][2] = origPoint[2];
				occlusionPositions[tempx][tempy][3] = -projectedPoint[2];

				img_red[tempx][tempy] += red;
				img_green[tempx][tempy] += green;
				img_blue[tempx][tempy] += blue;
				img_alpha[tempx][tempy] += first_magnitude;
				
				visiblePixels++;
				isVisible = true;
			}
			else
			{
				if (rnd1 > 0.08) 
					isVisible = true;
				occludedPixels++;
				
				if (glow > 0)
				{
					//this point occluded, add as subsurface glow
					double red = Pallet.fpalette[pal][colorIndex][0]*first_magnitude;
					double green = Pallet.fpalette[pal][colorIndex][1]*first_magnitude;
					double blue = Pallet.fpalette[pal][colorIndex][2]*first_magnitude;
					
					img_red[tempx][tempy] += red*glow;
					img_green[tempx][tempy] += green*glow;
					img_blue[tempx][tempy] += blue*glow;
					img_alpha[tempx][tempy] += first_magnitude*glow;
				}
			}
		}
		else
		{
			rejectedPixels++;
		}
		return isVisible;
	}
	
	private void plotFogPixel(double[] origPoint, double factor, double r, double g, double b)
	{
		//project the point into 3D space
		double rnd1 = Math.random();
		double rnd2 = Math.random();
		double[] projectedPoint = projectPitchYawDOF(origPoint, rnd1, rnd2);

		double realx = projectedPoint[0] + xcen;
		double realy = projectedPoint[1] + ycen;
		if (realx > -zoom && realy > -zoom && realx < zoom && realy < zoom)
		{
			//-- Update the histogram --
			int tempx = (int)Math.floor ( (realx/zoom)*half_ximlen + half_ximlen );
			int tempy = (int)Math.floor ( (realy/zoom)*half_yimlen + half_yimlen );
			
			//is the fog visible, then draw it.
			double difference_from_camera = -projectedPoint[2] - occlusionPositions[tempx][tempy][3];
			if ( difference_from_camera < 0 )
			{	
				//double horizon_dist = projectedPoint[2] - fog_dist - rnd2;
				//if (horizon_dist > 0) return;
				
				double light_factor = primary_light;
				
				//used for volumetric fog shadows - should match angle of primary light
				//double rl = -ray_step/3.0 + rnd1*ray_step*0.05;
				//double r2 = -ray_step/2.5 + rnd2*ray_step*0.05;
				//double light_factor = 4.0 + calculateRay(origPoint, RAY_STEPS*5, rl, r2, ray_step, false, primary_light, 1 + rnd2*0.35, null);
				
				double red = (r*factor*light_factor)/primary_light;
				double green = (g*factor*light_factor)/primary_light;
				double blue = (b*factor*light_factor)/primary_light;
				
				img_red[tempx][tempy] += red;
				img_green[tempx][tempy] += green;
				img_blue[tempx][tempy] += blue;
				img_alpha[tempx][tempy] += factor;
			}
		}
	}
	
	private void getConstants()
	{
		try
		{
			power = Double.parseDouble(txtPower.getText());
			depth = Integer.parseInt(txtDepth.getText());
			brightness = Double.parseDouble(txtBrightness.getText());
			gradient = Double.parseDouble(txtGradient.getText());
			zoom = Double.parseDouble(txtZoom.getText());
			root_zoom = Math.pow(zoom, 0.5);
			ray_step = occlusion_depth*zoom*0.5;

			cameraPersp = Double.parseDouble(txtPerspective.getText());
			cameraZpos = Double.parseDouble(txtZPos.getText());
			cameraPitch = Double.parseDouble(txtPitch.getText());
			cameraYaw = Double.parseDouble(txtYaw.getText());
			cameraDOF = Double.parseDouble(txtDOF.getText());
			opacity = Double.parseDouble(txtOpacity.getText());
			focus = Double.parseDouble(txtFocus.getText());
			glow = Double.parseDouble(txtZRes.getText());
			fog_factor = Double.parseDouble(txtFog.getText());
			primary_light = Double.parseDouble(txtLight.getText());
			
			setCamera();
		}
		catch(Exception e)
		{
			System.out.println("Problem parsing formula: " + e.toString());
		}
	}
	
	private void setCamera()
	{
		// 3D camera precalc
		CameraMatrix[0][0] = Math.cos(-cameraYaw);
		CameraMatrix[1][0] = -Math.sin(-cameraYaw);
		CameraMatrix[2][0] = 0;
		CameraMatrix[0][1] = Math.cos(cameraPitch) * Math.sin(-cameraYaw);
		CameraMatrix[1][1] = Math.cos(cameraPitch) * Math.cos(-cameraYaw);
		CameraMatrix[2][1] = -Math.sin(cameraPitch);
		CameraMatrix[0][2] = Math.sin(cameraPitch) * Math.sin(-cameraYaw);
		CameraMatrix[1][2] = Math.sin(cameraPitch) * Math.cos(-cameraYaw);
		CameraMatrix[2][2] = Math.cos(cameraPitch);
		
		InverseCameraMatrix[0][0] = Math.cos(cameraYaw);
		InverseCameraMatrix[1][0] = -Math.sin(cameraYaw);
		InverseCameraMatrix[2][0] = 0;
		InverseCameraMatrix[0][1] = Math.cos(-cameraPitch) * Math.sin(cameraYaw);
		InverseCameraMatrix[1][1] = Math.cos(-cameraPitch) * Math.cos(cameraYaw);
		InverseCameraMatrix[2][1] = -Math.sin(-cameraPitch);
		InverseCameraMatrix[0][2] = Math.sin(-cameraPitch) * Math.sin(cameraYaw);
		InverseCameraMatrix[1][2] = Math.sin(-cameraPitch) * Math.cos(cameraYaw);
		InverseCameraMatrix[2][2] = Math.cos(-cameraPitch);
	}
	
	private double[] reversePitchYaw(double[] point)
	{
		double x, y, z;
		
		//reverse perspective
		z = point[2];
		double zr = 1 - cameraPersp * z;
		point[0] =  point[0]*zr;
		point[1] =  point[1]*zr;
		
		//reverse rotation
		x = InverseCameraMatrix[0][0]*point[0] + InverseCameraMatrix[1][0]*point[1];
		y = InverseCameraMatrix[0][1]*point[0] + InverseCameraMatrix[1][1]*point[1] + InverseCameraMatrix[2][1]*z;
		z = InverseCameraMatrix[0][2]*point[0] + InverseCameraMatrix[1][2]*point[1] + InverseCameraMatrix[2][2]*z;
		
		double[] alteredPoint = new double[6];
		alteredPoint[0] = x;
		alteredPoint[1] = y;
		alteredPoint[2] = z+cameraZpos; 
		return alteredPoint;
	}
	
	private double[] projectPitchYawDOF(double[] point, double rnd1, double rnd2)
	{
		double x, y, z, zr, dr, dsin, dcos, blur_factor;
		
		z = point[2] - cameraZpos;
		x = CameraMatrix[0][0]*point[0] + CameraMatrix[1][0]*point[1] + CameraMatrix[2][0]*z;;
		y = CameraMatrix[0][1]*point[0] + CameraMatrix[1][1]*point[1] + CameraMatrix[2][1]*z;
		z = CameraMatrix[0][2]*point[0] + CameraMatrix[1][2]*point[1] + CameraMatrix[2][2]*z;
		
		zr = 1 - cameraPersp * z;
	
		double r2 = rnd1*2*Math.PI;
		dsin = Math.sin(r2);
		dcos = Math.cos(r2);
		
		blur_factor = z+focus;
		
		//only blur far away objects, not close up
		if (blur_factor > 0) blur_factor = 0;
		
		dr = rnd2 * cameraDOF * blur_factor;
		
		double[] alteredPoint = new double[3];
		alteredPoint[0] = (x + dr*dcos) / zr;
		alteredPoint[1] = (y + dr*dsin) / zr;
		alteredPoint[2] = z + dr*dsin; 
		return alteredPoint;
	}
	
	private void setConstants()
	{
		try
		{
			txtPower.setText(""+ power);
			txtDepth.setText(""+ depth);
			txtBrightness.setText(""+ brightness);
			txtGradient.setText(""+ gradient);
			txtZoom.setText(""+ zoom);
			
			txtPerspective.setText("" + cameraPersp);
			txtZPos.setText("" + cameraZpos);
			txtPitch.setText("" + cameraPitch);
			txtYaw.setText("" + cameraYaw);
			txtDOF.setText("" + cameraDOF);
			txtOpacity.setText("" + opacity);
			txtFocus.setText("" + focus);
			txtZRes.setText("" + glow);
			txtFog.setText("" + fog_factor);
			txtLight.setText("" + primary_light);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
	
	
	public boolean mouseDown(Event evt, int x, int y)
	{
		xanchor = x;
		yanchor = y;

		if (evt.modifiers == Event.ALT_MASK)
		{
			formula  = (formula + 1)%2;
			if (formula == 0) System.out.println("sin mandelbulb");
			else System.out.println("cos mandelbulb");
			reset = 1;
		}
		else if (evt.modifiers == Event.SHIFT_MASK)
		{
			pal  = (pal + 1)%(Pallet.fpalette.length);
                       
			reset = 1;
		}
		else if (evt.modifiers == Event.CTRL_MASK)
		{
			inverse_azimuth = -inverse_azimuth;
			System.out.println("inverse azimuth is " + inverse_azimuth);
			reset = 1;
		}
		else if (evt.modifiers == Event.META_MASK)
		{
			panelMain.setVisible(!panelMain.isVisible());
		}

		return true;
	}

	public boolean mouseDrag(Event evt, int x, int y)
	{
		m_down = true;
		xcurr = x;
		ycurr = y;
		repaint();
		return true;
	}

	public boolean mouseUp(Event evt, int x, int y)
	{
		xcurr = x;
		ycurr = y;
		m_down = false;

		int dx = Math.abs(xcurr - xanchor);
		int dy = Math.abs(ycurr - yanchor);
		if (dy > dx)  dx = dy;
		
        //make sure zoom isn't too small
       	if (dx > 10)
       	{
			double newxcen = (xanchor + xcurr)/2.0f;
			newxcen = ((newxcen - ximlen/2)/ximlen)*zoom;
			xcen = xcen - newxcen*2;
	
			double newycen = (yanchor + ycurr)/2.0f;
			newycen = ((newycen - yimlen/2)/yimlen)*zoom;
			ycen = ycen - newycen*2;
			
			System.out.println("Xcen is " + xcen + " Ycen is " + ycen);
	
			zoom = (double)dx/ximlen*zoom;
			root_zoom = Math.pow(zoom, 0.5);
			ray_step = occlusion_depth*zoom*0.5;
			reset = 1;
		}

		setConstants();
		getConstants();
		return true;
	}

	
	public void keyPressed(KeyEvent e)
	{
	}
	
    @Override
	public void keyReleased(KeyEvent e)
	{
		char c = e.getKeyChar();
		int keyCode = (int)c;
		
		//22: ctrl-v
		//8: backspace
		if (keyCode == 22 || keyCode == 8 || c == ' ' || c == '-' || c == '|' || c == '.' || (c >= '0' && c <= '9') )
		{
		
			if (e.getSource() != txtGradient && e.getSource() != txtBrightness && e.getSource() != txtFocus && e.getSource() != txtDOF && e.getSource() != txtFog && e.getSource() != txtLight && e.getSource() != txtZRes && e.getSource() != txtOpacity) 
				reset = 1;
			else
				reset = 2;
	
			getConstants();
		}
	}
	
    @Override
	public void keyTyped(KeyEvent e)
	{
	}
	
    @Override
	public void actionPerformed(ActionEvent e)
	{
		m_down = true;
		try
		{
			if (e.getSource() == saveFractal)
			{
				String filename = saveFile("Save Fractal", null, "mandelbrot3d_" + ximlen + "x" + yimlen + ".fractal");
				writeFractalData(filename);
			}
			else if (e.getSource() == openFractal)
			{
				String filename = loadFile("Open Fractal", null, ".fractal");
				readFractalData(filename);
			}
			else
			{
				String filename = saveFile("Export PNG", null, ".png");
				writePNG(filename);
			}
		}
		catch(Exception ex)
		{
		}
		m_down = false;
	}
	
	
	/* File Operations */
	public String loadFile(String title, String defDir, String fileType) throws Exception
	{
		Frame parent = new Frame();
		FileDialog fd = new FileDialog(parent, title, FileDialog.LOAD);
		fd.setFile(fileType);
		fd.setDirectory(defDir);
		fd.setLocation(50, 50);
		fd.show();
		lastDir = fd.getDirectory();
		if (lastDir == null) throw new Exception();
		return lastDir + fd.getFile();
	}

	public String saveFile(String title, String defDir, String fileType) throws Exception
	{
		Frame parent = new Frame();
		FileDialog fd = new FileDialog(parent, title, FileDialog.SAVE);
		fd.setFile(fileType);
		fd.setDirectory(defDir);
		fd.setLocation(50, 50);
		fd.show();
		lastDir = fd.getDirectory();
		if (lastDir == null) throw new Exception();
		return lastDir + fd.getFile();
	} 
	
	public void writePNG(String filename)
	{
		try 
		{
			BufferedImage bi = new BufferedImage(ximlen, yimlen, BufferedImage.TYPE_INT_ARGB); 
			bi.setRGB(0, 0, ximlen, yimlen, pixels, 0, ximlen);
			File outputfile = new File(filename);
			ImageIO.write(bi, "png", outputfile);
			System.out.println("Export PNG success: " + outputfile.getAbsolutePath());
			showStatus("Export PNG success: " + outputfile.getAbsolutePath() );
		} 
		catch (Exception e) 
		{
			System.out.println("export: " + e.toString());
		}
	}
	
	public void writeFractalData(String filename)
	{
		try 
		{
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(img_alpha);
			oos.writeObject(img_red);
			oos.writeObject(img_green);
			oos.writeObject(img_blue);
			
			//write int pref array
			int[] intPrefs = { depth, ximlen, yimlen, pal, inverse_azimuth, formula };
			oos.writeObject(intPrefs);
			
			//write double pref array
			double[] doublePrefs = { power, gradient, brightness, zoom, xcen, ycen, cameraPersp, cameraZpos, cameraYaw, cameraPitch, cameraDOF, opacity, focus, glow, fog_factor, primary_light };
			oos.writeObject(doublePrefs);
			
			oos.flush();
			fos.close();
			System.out.println("write Fractal Data success: " + filename);
			showStatus("write Fractal Data success: " + filename);
		}
		catch (Throwable e) 
		{
			System.out.println("write: " + e.toString());
		} 
	}
	
	public void readFractalData(String filename)
	{
		try 
		{
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);

			img_alpha = (float[][])ois.readObject();
			img_red = (float[][])ois.readObject();
			img_green = (float[][])ois.readObject();
			img_blue = (float[][])ois.readObject();

			//read int pref array
			int[] intPrefs = (int[])ois.readObject();
			depth = intPrefs[0];
			pal = intPrefs[3];
			
			
			//read double pref array
			double[] doublePrefs = (double[])ois.readObject();
			power = doublePrefs[0];
			gradient = doublePrefs[1];
			brightness = doublePrefs[2];
			zoom = doublePrefs[3];
			xcen = doublePrefs[4];
			ycen = doublePrefs[5];
			cameraPersp = doublePrefs[6];
			cameraZpos = doublePrefs[7];
			cameraYaw = doublePrefs[8];
			cameraPitch = doublePrefs[9];
			cameraDOF = doublePrefs[10];
			opacity = doublePrefs[11];
			focus = doublePrefs[12];
			glow = doublePrefs[13];
			
			try
			{
				inverse_azimuth= intPrefs[4];
				fog_factor = doublePrefs[14];
				primary_light = doublePrefs[15];
				formula = intPrefs[5];
			}
			catch(Exception e)
			{
			}
			
			root_zoom = Math.pow(zoom, 0.5);
			ray_step = occlusion_depth*zoom*0.5;
			fis.close();
			
			
			if (yimlen != img_alpha[0].length)
			{
				initVars(true);
			}
			else
			{
				ximlen = intPrefs[1];
				yimlen = intPrefs[2];
				initVars(false);
			}
			System.out.println("open success! " + filename);
		}
		catch (Throwable e) 
		{
			System.out.println("read: " + e.toString());
		} 
	}
	
}


/*

Transform3DPointsTo2DPoints = function(points, axisRotations){
	var TransformedPointsArray = [];
	var sx = Math.sin(axisRotations.x);
	var cx = Math.cos(axisRotations.x);
	var sy = Math.sin(axisRotations.y);
	var cy = Math.cos(axisRotations.y);
	var sz = Math.sin(axisRotations.z);
	var cz = Math.cos(axisRotations.z);
	var x,y,z, xy,xz, yx,yz, zx,zy, scaleFactor;

	var i = points.length;
	while (i--){
		x = points[i].x;
		y = points[i].y;
		z = points[i].z;

		// rotation around x
		xy = cx*y - sx*z;
		xz = sx*y + cx*z;
		// rotation around y
		yz = cy*xz - sy*x;
		yx = sy*xz + cy*x;
		// rotation around z
		zx = cz*yx - sz*xy;
		zy = sz*yx + cz*xy;
		
		scaleFactor = focalLength/(focalLength + yz);
		x = zx*scaleFactor;
		y = zy*scaleFactor;
		z = yz;

		TransformedPointsArray[i] = make2DPoint(x, y, -z, scaleFactor);
	}
	return TransformedPointsArray;
};

*/

	

/*
keytool -genkey -keyalg rsa -alias yourkey

Follow the instructions and type in all needed information.

Now we make the certificate:

keytool -export -alias yourkey -file yourcert.crt


Now we have to sign the applet:

Just make a *.bat file including this:

javac yourapplet.java
jar cvf yourapplet.jar yourapplet.class
jarsigner yourapplet.jar yourkey


The batch-file compiles the applet, makes a jar-archive and signs the jar-file.

The HTML-code to display the applet:

<applet code="yourapplet.class" archive="yourapplet.jar" width="600" height="500">
</applet>
*/

