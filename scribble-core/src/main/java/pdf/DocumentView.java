package pdf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * Zoomable view for the DocumentPanel.
 *
 * @author Torsten Oltmanns
 *
 */
public class DocumentView extends JPanel {
	private static final long serialVersionUID = 1L;
	private double zoom = 1d;
	private final Dimension formatSize = new Dimension(595, 842);


	public DocumentView() {
		setLayout(null);

		addMouseWheelListener(e -> {
			final double delta = e.getWheelRotation() / 10d;
			double factor = zoom - delta;

			if (factor < 0.5d) {
				factor = 0.5d;
			} else if (factor > 2.5d) {
				factor = 2.5d;
			}

			zoom = factor;

			final int width = (int) (formatSize.width * zoom);
			final int height = (int) (formatSize.height * zoom);
			setSize(width, height);
			setPreferredSize(new Dimension(width, height));
			System.out.println(factor + " -> " + getSize());

			repaint();
		});
	}


	@Override
	protected void paintChildren(final Graphics g) {
		((Graphics2D) g).scale(zoom, zoom);
		super.paintChildren(g);
	}


	/**
	 * @return the zoom
	 */
	public double getZoom() {
		return zoom;
	}


	/**
	 * @param zoom the zoom to set
	 */
	public void setZoom(final double zoom) {
		this.zoom = zoom;
	}

}
