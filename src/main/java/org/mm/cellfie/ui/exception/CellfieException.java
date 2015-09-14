package org.mm.cellfie.ui.exception;

public class CellfieException extends Exception
{
   private static final long serialVersionUID = 1L;

   public CellfieException(String message)
   {
      super(message);
   }

   public CellfieException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
