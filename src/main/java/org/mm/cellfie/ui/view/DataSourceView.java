package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Sheet;
import org.mm.ui.ModelView;
import org.protege.editor.core.ui.tabbedpane.ViewTabbedPane;
import org.protege.editor.core.ui.util.ComponentFactory;

public class DataSourceView extends JPanel implements ModelView
{
   private static final long serialVersionUID = 1L;

   public DataSourceView(ApplicationView container)
   {
      String title = String.format("Workbook (%s)", container.getWorkbookFileLocation());

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      JPanel pnlContainer = new JPanel();
      pnlContainer.setLayout(new BorderLayout());
      pnlContainer.setBorder(ComponentFactory.createTitledBorder(title));
      add(pnlContainer, BorderLayout.CENTER);

      JPanel pnlWorkbook = new JPanel();
      pnlWorkbook.setLayout(new BorderLayout());
      pnlWorkbook.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      ViewTabbedPane tabSheetContainer = new ViewTabbedPane();
      pnlWorkbook.add(tabSheetContainer, BorderLayout.CENTER);
      pnlContainer.add(pnlWorkbook, BorderLayout.CENTER);

      for (Sheet sheet : container.getActiveWorkbook().getSheets()) {
         SheetPanel sheetPanel = new SheetPanel(sheet);
         tabSheetContainer.addTab(sheetPanel.getSheetName(), null, sheetPanel);
      }

      validate();
   }

   @Override
   public void update()
   {
      // NO-OP
   }
}
