package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mm.app.Environment;
import org.mm.cellfie.ui.action.MapExpressionsAction;
import org.mm.core.settings.ReferenceSettings;
import org.mm.core.settings.ValueEncodingSetting;
import org.mm.ui.MMView;

public class MappingControlView extends JPanel implements MMView
{
	private static final long serialVersionUID = 1L;

	private ApplicationView container;

	private JComboBox<ValueEncodingSetting> cbbValueEncoding;

	private JTextArea txtMessageArea;
	private JButton cmdRunMapping;

	public MappingControlView(ApplicationView container)
	{
		this.container = container;

		setLayout(new BorderLayout());

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));

		JPanel pnlTop = new JPanel(new BorderLayout());
		add(pnlTop, BorderLayout.NORTH);

		cmdRunMapping = new JButton("Run Mapping");
		cmdRunMapping.setEnabled(false);
		cmdRunMapping.setPreferredSize(new Dimension(125, 32));
		cmdRunMapping.addActionListener(new MapExpressionsAction(container));

		pnlTop.add(cmdRunMapping, BorderLayout.WEST);

		JPanel pnlReferenceSettings = new JPanel(new GridLayout(1, 2));
		pnlTop.add(pnlReferenceSettings, BorderLayout.EAST);

		JLabel lblValueEncoding = new JLabel("Default value encoding:");
		pnlReferenceSettings.add(lblValueEncoding);
		
		cbbValueEncoding = new JComboBox<>();
		cbbValueEncoding.setModel(new DefaultComboBoxModel<>(ValueEncodingSetting.values()));
		cbbValueEncoding.addActionListener(new ConfigurationActionListener());
		pnlReferenceSettings.add(cbbValueEncoding);

		updateValueEncoding();

		txtMessageArea = new JTextArea();
		txtMessageArea.setBorder(BorderFactory.createEtchedBorder());
		txtMessageArea.setBackground(Color.WHITE);
		txtMessageArea.setLineWrap(true);
		txtMessageArea.setEditable(false);
		JScrollPane scrMessageArea = new JScrollPane(txtMessageArea);

		add(scrMessageArea, BorderLayout.CENTER);

		messageAreaAppend("MappingMaster v" + Environment.MAPPINGMASTER_VERSION + "\n\n");
		messageAreaAppend("See https://github.com/protegeproject/mapping-master/wiki for documentation.\n");
		messageAreaAppend("Use the 'Mapping Browser' tab to define mappings using MappingMaster DSL expression.\n");
		messageAreaAppend("Click the 'Run Mapping' button to perform mappings.\n");
		
		validate();
	}

	private void updateValueEncoding()
	{
		getReferenceSettings().setValueEncodingSetting((ValueEncodingSetting) cbbValueEncoding.getSelectedItem());
	}

	@Override
	public void update()
	{
		cmdRunMapping.setEnabled(true);
	}

	public void messageAreaClear()
	{
		txtMessageArea.setText("");
	}

	public void messageAreaAppend(String text)
	{
		txtMessageArea.append(text);
	}

	private ReferenceSettings getReferenceSettings()
	{
		return container.getReferenceSettings();
	}

	private class ConfigurationActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			updateValueEncoding();
		}
	}
}
