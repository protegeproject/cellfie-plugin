package org.mm.cellfie.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class LogWriter {

   private final static String logSeparator = "===========================================================================================================";

   public static void save(File fout, StringBuilder stringBuilder, boolean append)
         throws FileNotFoundException {
      save(fout, stringBuilder.toString(), append);
   }

   public static void save(File fout, String logMessage, boolean append)
         throws FileNotFoundException {
      PrintWriter printer = new PrintWriter(new FileOutputStream(fout, append));
      printer.print(logMessage);
      if (append) {
         printer.println();
         printer.print(logSeparator);
         printer.println();
      }
      printer.close();
   }

   public static String getTimestamp() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
   }
}
