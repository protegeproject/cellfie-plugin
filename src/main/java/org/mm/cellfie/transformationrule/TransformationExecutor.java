package org.mm.cellfie.transformationrule;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mm.cellfie.transformationrule.TransformationRule.ANY_WILDCARD;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.mm.renderer.Renderer;
import org.mm.renderer.RenderingContext;
import org.mm.renderer.Sheet;
import org.mm.renderer.Workbook;
import org.mm.renderer.internal.CellUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import com.google.common.collect.Sets;

/**
 * Used to run the data transformation from spreadsheet cells to a
 * collection of OWL axioms based the rules defined by the user.
 *
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationExecutor {

   private final Renderer<Set<OWLAxiom>> renderer;

   public TransformationExecutor(@Nonnull Renderer<Set<OWLAxiom>> renderer) {
      this.renderer = checkNotNull(renderer);
   }

   public Collection<OWLAxiom> execute(Workbook workbook, TransformationRuleList transformationRules) {
      Set<OWLAxiom> results = Sets.newHashSet();
      for (TransformationRule rule : transformationRules) {
         String ruleString = rule.getRuleExpression();
         results.addAll(renderer.render(ruleString, workbook,
               new RenderingContext(rule.getSheetName(),
                     getStartColumnNumber(rule.getStartColumn()),
                     getEndColumnNumber(rule.getEndColumn(), workbook.getSheet(rule.getSheetName())),
                     getStartRowNumber(rule.getStartRow()),
                     getEndRowNumber(rule.getEndRow(), workbook.getSheet(rule.getSheetName())))));
      }
      return results;
   }

   private int getStartColumnNumber(String startColumn) {
      return CellUtils.toColumnNumber(startColumn);
   }

   private int getEndColumnNumber(String endColumn, Sheet sheet) {
      return (ANY_WILDCARD.equals(endColumn)) ? sheet.getEndColumnNumber() : CellUtils.toColumnNumber(endColumn);
   }

   private int getStartRowNumber(String startRow) {
      return CellUtils.toRowNumber(startRow);
   }

   private int getEndRowNumber(String endRow, Sheet sheet) {
      return (ANY_WILDCARD.equals(endRow)) ? sheet.getEndRowNumber() : CellUtils.toRowNumber(endRow);
   }
}
