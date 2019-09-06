package org.mm.cellfie.transformationrule;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;
import com.google.gson.Gson;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class TransformationRuleWriter {

   public static void writeToDocument(@Nonnull File file, @Nonnull Collection<TransformationRule> rules)
         throws IOException {
      TransformationRuleSet ruleSet = TransformationRuleSet.create(rules);
      writeToDocument(file, ruleSet);
   }

   public static void writeToDocument(@Nonnull File file, @Nonnull TransformationRuleSet ruleSet)
         throws IOException {
      checkNotNull(file);
      checkNotNull(ruleSet);
      String json = new Gson().toJson(TransformationRuleSet.create(ruleSet));
      FileWriter writer = new FileWriter(file);
      writer.write(json);
      writer.close();
   }
}