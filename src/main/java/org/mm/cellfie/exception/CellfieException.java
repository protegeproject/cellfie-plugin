package org.mm.cellfie.exception;

/**
 * @author Josef Hardi <josef.hardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class CellfieException extends Exception {

   private static final long serialVersionUID = 1L;

   public CellfieException(String message) {
      super(message);
   }

   public CellfieException(String message, Throwable cause) {
      super(message, cause);
   }
}
