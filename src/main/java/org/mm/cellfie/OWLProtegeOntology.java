package org.mm.cellfie;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import org.mm.core.OWLEntityResolver;
import org.mm.core.OWLOntologySourceHook;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class OWLProtegeOntology implements OWLOntologySourceHook {

   private final OWLEditorKit editorKit;

   public OWLProtegeOntology(@Nonnull OWLEditorKit editorKit) {
      this.editorKit = checkNotNull(editorKit);
   }

   @Override
   public OWLOntology getOWLOntology() {
      return editorKit.getOWLModelManager().getActiveOntology();
   }

   @Override
   public OWLEntityResolver getEntityResolver() {
      return new OWLProtegeEntityResolver(editorKit);
   }
}
