package org.mm.cellfie.action;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.mm.app.MMApplicationFactory;
import org.mm.app.MMApplicationModel;
import org.mm.core.TransformationRule;
import org.mm.parser.ASTExpression;
import org.mm.parser.MappingMasterParser;
import org.mm.parser.node.ExpressionNode;
import org.mm.core.settings.ReferenceSettings;
import org.mm.rendering.owlapi.OWLRendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadsheetLocation;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the saved workbook and mapping through Cellfie's actual ontology hook. */
public class ProtegeTransformationTest {
    @Test public void transformsFixtureThroughProtegeAdapter() throws Exception {
        String base = "https://example.org/cellfie#";
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(fixture("cellfie-smoke-test.ttl"));
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLModelManager model = mock(OWLModelManager.class);
        OWLEditorKit kit = mock(OWLEditorKit.class);
        OWLEntityFactory factory = mock(OWLEntityFactory.class);
        when(kit.getModelManager()).thenReturn(model);
        when(kit.getOWLModelManager()).thenReturn(model);
        when(model.getActiveOntology()).thenReturn(ontology);
        when(model.getOWLOntologyManager()).thenReturn(manager);
        // A label-based finder does not match explicit prefixed names.
        when(model.getOWLEntityFinder()).thenReturn(mock(OWLEntityFinder.class));
        when(model.getOWLEntityFactory()).thenReturn(factory);
        when(factory.createOWLEntity(any(), anyString(), nullable(IRI.class))).thenAnswer(call -> {
            Class<?> type = call.getArgument(0);
            String name = call.getArgument(1);
            IRI prefix = call.getArgument(2);
            IRI iri = IRI.create((prefix == null ? base : prefix.toString()) + name);
            OWLEntity entity = type == OWLClass.class ? df.getOWLClass(iri) : df.getOWLAnnotationProperty(iri);
            return new OWLEntityCreationSet<>(entity);
        });
        MMApplicationFactory applications = new MMApplicationFactory();
        applications.setWorkbookFileLocation(fixture("classes.xlsx").getPath());
        applications.setRuleFileLocation(fixture("rules.json").getPath());
        MMApplicationModel application = applications.createApplication(new OWLProtegeOntology(kit)).getApplicationModel();
        SpreadSheetDataSource data = application.getDataSourceModel().getDataSource();
        try (Workbook workbook = data.getWorkbook()) {
            TransformationRule rule = application.getTransformationRuleModel().getRule(0);
            Set<OWLAxiom> generated = new HashSet<>();
            for (int row = 2; row <= 3; row++) {
                data.setCurrentLocation(new SpreadsheetLocation("Classes", 1, row));
                ASTExpression ast = (ASTExpression) new MappingMasterParser(
                        new ByteArrayInputStream(rule.getRuleString().getBytes(StandardCharsets.UTF_8)),
                        new ReferenceSettings(), -1).expression();
                OWLRendering rendering = (OWLRendering) application.getDefaultRenderer()
                        .render(new ExpressionNode(ast).getMMExpressionNode()).get();
                generated.addAll(rendering.getOWLAxioms());
            }
            OWLClass banana = df.getOWLClass(IRI.create(base + "Banana"));
            OWLAxiom subclass = df.getOWLSubClassOfAxiom(banana, df.getOWLClass(IRI.create(base + "Fruit")));
            assertTrue(generated.contains(subclass));
            assertTrue(generated.contains(df.getOWLAnnotationAssertionAxiom(df.getRDFSLabel(), banana.getIRI(),
                    df.getOWLLiteral("Banane française 中文"))));
            assertFalse(ontology.containsAxiom(subclass));
            manager.addAxioms(ontology, generated);
            assertTrue(ontology.containsAxiom(subclass));
            manager.removeAxioms(ontology, generated);
            assertFalse(ontology.containsAxiom(subclass));
        }
    }

    private File fixture(String name) throws Exception {
        return new File(getClass().getResource("/smoke-test/" + name).toURI());
    }
}
