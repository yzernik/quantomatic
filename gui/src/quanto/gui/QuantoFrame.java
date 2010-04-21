package quanto.gui;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.*;

public class QuantoFrame extends JFrame {

	private static final long serialVersionUID = 3656684775223085393L;
	private QuantoCore core;
	private InteractiveView focusedView;
	private final ViewPort viewPort;
	boolean consoleVisible;
	private volatile static int frameCount = 0;

	public QuantoFrame() {
		super("Quantomatic");
		frameCount++;
		core = QuantoApp.getInstance().getCore();
		setBackground(Color.white);
		consoleVisible = true;
		focusedView = null;
		QuantoApp.MainMenu mb = QuantoApp.getInstance().getMainMenu();
		setJMenuBar(mb);


		// the view port will tell its views what menu to update when they are focused
		viewPort = new ViewPort(mb);

		addWindowFocusListener(new WindowFocusListener() {

			public void windowGainedFocus(WindowEvent e) {
				QuantoApp.getInstance().getViewManager().setFocusedViewPort(viewPort);
			}

			public void windowLostFocus(WindowEvent e) {
			}
		});

		getContentPane().setLayout(new BorderLayout());

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(new TheoryTree(viewPort));
		splitPane.setRightComponent(viewPort);
		splitPane.setDividerLocation(150);

		getContentPane().add(splitPane, BorderLayout.CENTER);

		//this.pack();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (frameCount == 1) {
				QuantoApp.getInstance().shutdown();
			}
			else {
				frameCount--;
				dispose();
			}
		}
		else {
			super.processWindowEvent(e);
		}
	}

	public QuantoGraph getCurrentGraph() {
		if (focusedView != null
			&& focusedView instanceof InteractiveGraphView) {
			return ((InteractiveGraphView) focusedView).getGraph();
		}
		else {
			return null;
		}
	}

	public void updateCurrentGraph() throws QuantoCore.ConsoleError {
		if (focusedView != null
			&& focusedView instanceof InteractiveGraphView) {
			((InteractiveGraphView) focusedView).updateGraph();
		}
	}

	public QuantoCore getCore() {
		return core;
	}

	public ViewPort getViewPort() {
		return viewPort;
	}
}
