package pdf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class PdfGeneratorGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private final JSplitPane splitPane = new JSplitPane();
	private final JScrollPane componentScrollPane = new JScrollPane();
	private final JList list = new JList();
	private final JScrollPane documentScrollPane = new JScrollPane();

	private final JPanel documentPanel = new JPanel();


	public PdfGeneratorGUI() {
		final int width = getGraphicsConfiguration().getDevice().getDisplayMode().getWidth();
		final int height = getGraphicsConfiguration().getDevice().getDisplayMode().getWidth();

		setSize(width, height);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		getContentPane().add(splitPane, BorderLayout.CENTER);

		splitPane.setLeftComponent(componentScrollPane);

		componentScrollPane.setMinimumSize(new Dimension(100, 600));
		componentScrollPane.setMaximumSize(new Dimension(150, 600));
		componentScrollPane.setPreferredSize(new Dimension(150, 600));
		componentScrollPane.getViewport().setLayout(new BorderLayout());

		final JPanel view = new JPanel();
		view.setLayout(new BorderLayout());
		view.add(list, BorderLayout.CENTER);
		componentScrollPane.setViewportView(view);

		splitPane.setRightComponent(documentScrollPane);

		final DocumentView documentView = new DocumentView();
		documentView.setBackground(Color.BLACK);
		documentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		documentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		documentScrollPane.setViewportView(documentView);

		documentPanel.setBackground(Color.WHITE);
		documentPanel.setBounds(0, 0, (int) PDRectangle.A4.getWidth(), (int) PDRectangle.A4.getHeight());
		documentPanel.setMinimumSize(new Dimension((int) PDRectangle.A4.getWidth(), (int) PDRectangle.A4.getHeight()));
		documentPanel.setMaximumSize(new Dimension((int) PDRectangle.A4.getWidth(), (int) PDRectangle.A4.getHeight()));
		documentPanel.setPreferredSize(new Dimension((int) PDRectangle.A4.getWidth(), (int) PDRectangle.A4.getHeight()));

		final JEditorPane editorPane = new JEditorPane();

		final JLabel label = new JLabel("Hallo");
		label.setOpaque(true);
		label.setBackground(Color.RED);
		documentPanel.add(label);

		documentView.add(documentPanel, BorderLayout.CENTER);
	}


	public static void main(final String[] args) {
		final PdfGeneratorGUI gui = new PdfGeneratorGUI();
		gui.setVisible(true);
	}
}
