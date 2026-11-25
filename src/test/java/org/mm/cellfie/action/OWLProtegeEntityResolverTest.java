package org.mm.cellfie.action;

import org.junit.Test;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OWLProtegeEntityResolverTest {
    @Test public void createsEntityUsingActiveOntologyPrefix() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology();
        TurtleDocumentFormat format = new TurtleDocumentFormat();
        format.setPrefix("ex:", "https://example.org/cellfie#");
        manager.setOntologyFormat(ontology, format);
        OWLModelManager model = mock(OWLModelManager.class);
        OWLEditorKit kit = mock(OWLEditorKit.class);
        OWLEntityFinder finder = mock(OWLEntityFinder.class);
        OWLEntityFactory factory = mock(OWLEntityFactory.class);
        when(kit.getModelManager()).thenReturn(model);
        when(model.getOWLEntityFinder()).thenReturn(finder);
        when(model.getOWLEntityFactory()).thenReturn(factory);
        when(model.getActiveOntology()).thenReturn(ontology);
        when(model.getOWLOntologyManager()).thenReturn(manager);
        OWLClass expected = manager.getOWLDataFactory().getOWLClass(IRI.create("https://example.org/cellfie#Child"));
        OWLEntityCreationSet<OWLClass> creation = new OWLEntityCreationSet<>(expected);
        when(factory.createOWLEntity(OWLClass.class, "Child", IRI.create("https://example.org/cellfie#")))
                .thenReturn(creation);
        assertEquals(expected, new OWLProtegeEntityResolver(kit).create("ex:Child", OWLClass.class));
    }

    @Test public void resolvesExistingEntityWithoutCreatingIt() throws Exception {
        OWLEditorKit kit = mock(OWLEditorKit.class);
        OWLModelManager model = mock(OWLModelManager.class);
        OWLEntityFinder finder = mock(OWLEntityFinder.class);
        OWLEntityFactory factory = mock(OWLEntityFactory.class);
        when(kit.getModelManager()).thenReturn(model);
        when(model.getOWLEntityFinder()).thenReturn(finder);
        when(model.getOWLEntityFactory()).thenReturn(factory);
        OWLClass expected = OWLManager.getOWLDataFactory().getOWLClass(IRI.create("https://example.org/Existing"));
        when(finder.getOWLEntity("Existing")).thenReturn(expected);
        assertEquals(expected, new OWLProtegeEntityResolver(kit).resolve("Existing", OWLClass.class));
        verifyNoInteractions(factory);
    }

    @Test public void resolvesPrefixedImportedEntityWhenRendererUsesLabels() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.create("https://example.org/main"));
        OWLOntology imported = manager.createOntology(IRI.create("https://example.org/imported"));
        manager.applyChange(new AddImport(ontology,
                manager.getOWLDataFactory().getOWLImportsDeclaration(imported.getOntologyID().getOntologyIRI().get())));
        OWLClass fruit = manager.getOWLDataFactory().getOWLClass(IRI.create("https://example.org/cellfie#Fruit"));
        manager.addAxiom(imported, manager.getOWLDataFactory().getOWLDeclarationAxiom(fruit));
        TurtleDocumentFormat format = new TurtleDocumentFormat();
        format.setPrefix("ex:", "https://example.org/cellfie#");
        manager.setOntologyFormat(ontology, format);
        OWLEditorKit kit = mock(OWLEditorKit.class);
        OWLModelManager model = mock(OWLModelManager.class);
        OWLEntityFinder finder = mock(OWLEntityFinder.class);
        OWLEntityFactory factory = mock(OWLEntityFactory.class);
        when(kit.getModelManager()).thenReturn(model);
        when(model.getOWLEntityFinder()).thenReturn(finder);
        when(model.getOWLEntityFactory()).thenReturn(factory);
        when(model.getActiveOntology()).thenReturn(ontology);
        when(model.getOWLOntologyManager()).thenReturn(manager);
        OWLProtegeEntityResolver resolver = new OWLProtegeEntityResolver(kit);
        assertEquals(fruit, resolver.resolve("ex:Fruit", OWLClass.class));
        assertEquals(fruit, resolver.create("ex:Fruit", OWLClass.class));
        verifyNoInteractions(factory);
    }
}
