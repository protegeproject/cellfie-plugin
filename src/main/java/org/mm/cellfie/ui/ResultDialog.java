package org.mm.cellfie.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.mm.cellfie.exception.CellfieException;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.ontology.OntologyPreferences;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * Represents the action listener for the 'Generate Axioms' command.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class ResultDialog {

   private static final int CANCEL_IMPORT = 0;
   private static final int ADD_TO_NEW_ONTOLOGY = 1;
   private static final int ADD_TO_CURRENT_ONTOLOGY = 2;

   private final static ImportOption[] options = {
         new ImportOption(CANCEL_IMPORT, "Cancel"),
         new ImportOption(ADD_TO_NEW_ONTOLOGY, "Add to a new ontology"),
         new ImportOption(ADD_TO_CURRENT_ONTOLOGY, "Add to current ontology") };

   public static void showDialog(CellfieWorkspace cellfieWorkspace, Collection<OWLAxiom> axioms)
         throws CellfieException {
      final OWLEditorKit editorKit = cellfieWorkspace.getEditorKit();
      final OWLModelManager modelManager = editorKit.getModelManager();
      
      try {
         OWLOntology currentOntology = cellfieWorkspace.getOntology();
         int answer = showConfirmDialog(cellfieWorkspace, createPreviewAxiomsPanel(editorKit, axioms), options, options[1]);
         switch (answer) {
            case ADD_TO_CURRENT_ONTOLOGY:
               modelManager.applyChanges(addAxioms(currentOntology, axioms));
               break;
            case ADD_TO_NEW_ONTOLOGY:
               final OWLOntologyID ontologyId = createOntologyID();
               final URI physicalUri = ontologyId.getDefaultDocumentIRI().get().toURI();
               OWLOntology newOntology = modelManager.createNewOntology(ontologyId, physicalUri);
               IRI ontologyIri = currentOntology.getOntologyID().getOntologyIRI().get();
               OWLImportsDeclaration importDeclaration = modelManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);
               modelManager.applyChange(addImport(newOntology, importDeclaration));
               modelManager.applyChanges(addAxioms(newOntology, axioms));
               break;
         }
      } catch (ClassCastException e) {
         // NO-OP: Fix should be to Protege API
      } catch (OWLOntologyCreationException e) {
         throw new CellfieException("Error while creating a new ontology: " + e.getMessage());
      }
   }

   public static int showConfirmDialog(Component parent, JComponent content, Object[] options, Object defaultOption) {
      JOptionPane optionPane = new JOptionPane(content,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            options,
            defaultOption);
      JDialog dlg = optionPane.createDialog(parent, "Generated Axioms");
      dlg.setModalityType(Dialog.ModalityType.MODELESS);
      dlg.setSize(642, 682);
      dlg.setLocationRelativeTo(parent);
      dlg.setResizable(true);
      dlg.setVisible(true);
      return getReturnValueAsInteger(optionPane);
  }

   private static int getReturnValueAsInteger(JOptionPane optionPane) {
      Object value = optionPane.getValue();
      if(value == null) {
         return JOptionPane.CLOSED_OPTION;
      }
      Object[] options = optionPane.getOptions();
      if (options == null) {
         if(value instanceof Integer) {
            return (Integer) value;
         }
         else {
            return JOptionPane.CLOSED_OPTION;
         }
      }
      for(int i = 0; i < options.length; i++) {
         Object valueAtIndex = options[i];
         if (value.equals(valueAtIndex)) {
            return i;
         }
      }
      return JOptionPane.CLOSED_OPTION;
  }

   private static OWLOntologyChange addImport(OWLOntology newOntology, OWLImportsDeclaration importDeclaration) {
      return new AddImport(newOntology, importDeclaration);
   }

   private static OWLOntologyID createOntologyID() {
      OntologyPreferences ontologyPreferences = OntologyPreferences.getInstance();
      IRI freshIRI = IRI.create(ontologyPreferences.generateNextURI());
      return new OWLOntologyID(com.google.common.base.Optional.of(freshIRI),
            com.google.common.base.Optional.absent());
   }

   private static List<OWLOntologyChange> addAxioms(OWLOntology ontology, Collection<OWLAxiom> axioms) {
      List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
      for (OWLAxiom ax : axioms) {
         changes.add(new AddAxiom(ontology, ax));
      }
      return changes;
   }

   private static JPanel createPreviewAxiomsPanel(OWLEditorKit editorKit, Collection<OWLAxiom> generatedAxioms) {
      PreviewAxiomsPanel previewPanel = new PreviewAxiomsPanel(editorKit);
      previewPanel.setContent(generatedAxioms);
      return previewPanel;
   }

   /**
    * A helper class for creating import axioms command buttons.
    */
   static class ImportOption implements Comparable<ImportOption> {
      private int option;
      private String title;

      public ImportOption(int option, String title) {
         this.option = option;
         this.title = title;
      }

      public int get() {
         return option;
      }

      @Override
      public String toString() {
         return title;
      }

      @Override
      public int compareTo(ImportOption o) {
         return option - o.option;
      }
   }
}
