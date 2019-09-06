package org.mm.cellfie.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.mm.cellfie.exception.CellfieException;
import org.mm.parser.ParseException;
import org.protege.editor.core.ui.util.JOptionPaneEx;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the action listener for the 'Generate Axioms' command.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class GenerateAxiomsAction implements ActionListener {

   private static final Logger logger = LoggerFactory.getLogger(GenerateAxiomsAction.class);

   private static final int CANCEL_IMPORT = 0;
   private static final int ADD_TO_NEW_ONTOLOGY = 1;
   private static final int ADD_TO_CURRENT_ONTOLOGY = 2;

   private final CellfieWorkspace cellfieWorkspace;

   public GenerateAxiomsAction(@Nonnull CellfieWorkspace cellfieWorkspace) {
      this.cellfieWorkspace = checkNotNull(cellfieWorkspace);
   }

   @Override
   public void actionPerformed(ActionEvent event) {
      try {
         Set<OWLAxiom> results = cellfieWorkspace.doTransformation();
         showAxiomPreviewDialog(results);
      } catch (ParseException e) {
         String message = e.getMessage();
         DialogUtils.showErrorDialog(cellfieWorkspace, message);
         logger.error(message, e);
      } catch (Exception e) {
         String message = e.getMessage();
         DialogUtils.showErrorDialog(cellfieWorkspace, message);
         logger.error(message, e);
      } 
   }

   public String getOntologyFileLocation() {
      OWLOntology ontology = cellfieWorkspace.getOntology();
      String iriString = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology).toString();
      return iriString.substring(iriString.indexOf(":") + 1, iriString.length());
   }

   private void showAxiomPreviewDialog(Set<OWLAxiom> axioms)
         throws CellfieException {
      final OWLModelManager modelManager = cellfieWorkspace.getEditorKit().getModelManager();
      final ImportOption[] options = {
            new ImportOption(CANCEL_IMPORT, "Cancel"),
            new ImportOption(ADD_TO_NEW_ONTOLOGY, "Add to a new ontology"),
            new ImportOption(ADD_TO_CURRENT_ONTOLOGY, "Add to current ontology") };
      try {
         OWLOntology currentOntology = cellfieWorkspace.getOntology();
         int answer = JOptionPaneEx.showConfirmDialog(cellfieWorkspace, "Generated Axioms",
               createPreviewAxiomsPanel(axioms), JOptionPane.PLAIN_MESSAGE,
               JOptionPane.DEFAULT_OPTION, null, options, options[1]);
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

   private OWLOntologyChange addImport(OWLOntology newOntology, OWLImportsDeclaration importDeclaration) {
      return new AddImport(newOntology, importDeclaration);
   }

   private OWLOntologyID createOntologyID() {
      OntologyPreferences ontologyPreferences = OntologyPreferences.getInstance();
      IRI freshIRI = IRI.create(ontologyPreferences.generateNextURI());
      return new OWLOntologyID(com.google.common.base.Optional.of(freshIRI),
            com.google.common.base.Optional.absent());
   }

   private List<OWLOntologyChange> addAxioms(OWLOntology ontology, Set<OWLAxiom> axioms) {
      List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
      for (OWLAxiom ax : axioms) {
         changes.add(new AddAxiom(ontology, ax));
      }
      return changes;
   }

   private JPanel createPreviewAxiomsPanel(Set<OWLAxiom> generatedAxioms) {
      PreviewAxiomsPanel previewPanel = new PreviewAxiomsPanel(cellfieWorkspace);
      previewPanel.setContent(generatedAxioms);
      return previewPanel;
   }

   /**
    * A helper class for creating import axioms command buttons.
    */
   class ImportOption implements Comparable<ImportOption> {
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
