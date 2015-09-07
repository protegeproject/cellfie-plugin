package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mm.cellfie.ui.list.OWLAxiomList;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

public class PreviewAxiomsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private JButton cmdViewLog;
	
	public PreviewAxiomsPanel(OWLEditorKit editorKit, Set<OWLAxiom> axioms)
	{
		setLayout(new BorderLayout());
		
		OWLAxiomList previewList = new OWLAxiomList(editorKit);
		previewList.setAxioms(axioms);
		
		add(new JScrollPane(previewList), BorderLayout.CENTER);
		
		JLabel lblPreview = new JLabel();
		lblPreview.setText("Mapping Master generates " + axioms.size() + " axioms:");
		add(lblPreview, BorderLayout.NORTH);
		
		cmdViewLog = new JButton("View Log");
		add(cmdViewLog, BorderLayout.SOUTH);
	}
}
