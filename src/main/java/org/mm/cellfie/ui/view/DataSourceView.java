package org.mm.cellfie.ui.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.mm.ui.ModelView;
import org.protege.editor.core.ui.tabbedpane.ViewTabbedPane;
import org.protege.editor.core.ui.util.ComponentFactory;

public class DataSourceView extends JPanel implements ModelView
{
   private static final long serialVersionUID = 1L;

   private ViewTabbedPane tabSheetContainer;

   public DataSourceView(WorkspacePanel container)
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

      tabSheetContainer = new ViewTabbedPane();
      pnlWorkbook.add(tabSheetContainer, BorderLayout.CENTER);
      pnlContainer.add(pnlWorkbook, BorderLayout.CENTER);

      for (org.apache.poi.ss.usermodel.Sheet sheet : container.getActiveWorkbook().getSheets()) {
         SheetPanel sheetPanel = new SheetPanel(sheet);
         tabSheetContainer.addTab(sheet.getSheetName(), null, sheetPanel);
      }
      validate();
   }

   public Sheet getActiveSheet()
   {
      SheetPanel selectedSheetPanel = (SheetPanel) tabSheetContainer.getSelectedComponent();
      Sheet sheet = new Sheet(selectedSheetPanel.getSheetName());
      sheet.setSelectionRange(selectedSheetPanel.getSelectionRange());
      return sheet;
   }

   @Override
   public void update()
   {
      // NO-OP
   }
}
