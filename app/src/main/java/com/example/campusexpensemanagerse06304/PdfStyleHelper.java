package com.example.campusexpensemanagerse06304;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * Helper class for consistent PDF styling throughout the application
 */
public class PdfStyleHelper {

    // Font sizes
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADING_FONT_SIZE = 14;
    private static final float NORMAL_FONT_SIZE = 10;

    // Colors
    private static final BaseColor HEADER_BG_COLOR = new BaseColor(220, 220, 220);
    private static final BaseColor POSITIVE_COLOR = new BaseColor(0, 150, 0);
    private static final BaseColor NEGATIVE_COLOR = new BaseColor(200, 0, 0);
    private static final BaseColor ALTERNATE_ROW_COLOR = new BaseColor(245, 245, 250);

    // Spacing
    private static final float SPACING_BEFORE = 10f;
    private static final float SPACING_AFTER = 10f;

    // Font styles
    public static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, TITLE_FONT_SIZE, Font.BOLD);
    public static final Font HEADING_FONT = new Font(Font.FontFamily.HELVETICA, HEADING_FONT_SIZE, Font.BOLD);
    public static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE, Font.NORMAL);
    public static final Font SMALL_BOLD_FONT = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE, Font.BOLD);
    public static final Font POSITIVE_FONT = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE, Font.NORMAL, POSITIVE_COLOR);
    public static final Font NEGATIVE_FONT = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE, Font.NORMAL, NEGATIVE_COLOR);

    /**
     * Creates a centered title paragraph with consistent styling
     */
    public static Paragraph createTitle(String titleText) {
        Paragraph title = new Paragraph(titleText, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(SPACING_BEFORE);
        title.setSpacingAfter(SPACING_AFTER);
        return title;
    }

    /**
     * Creates a section heading paragraph with consistent styling
     */
    public static Paragraph createHeading(String headingText) {
        Paragraph heading = new Paragraph(headingText, HEADING_FONT);
        heading.setSpacingBefore(SPACING_BEFORE);
        heading.setSpacingAfter(SPACING_AFTER);
        return heading;
    }

    /**
     * Creates a standard table header cell with consistent styling
     */
    public static PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, SMALL_BOLD_FONT));
        cell.setBackgroundColor(HEADER_BG_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    /**
     * Creates a standard data cell with consistent styling
     */
    public static PdfPCell createDataCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, NORMAL_FONT));
        cell.setPadding(5);
        return cell;
    }

    /**
     * Creates a monetary value cell with consistent styling and right alignment
     */
    public static PdfPCell createCurrencyCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        return cell;
    }

    /**
     * Creates a monetary value cell with color coding for positive/negative values
     */
    public static PdfPCell createCurrencyCell(double amount, String format) {
        String formattedAmount = String.format(format, amount);
        Font font = amount >= 0 ? NORMAL_FONT : NEGATIVE_FONT;

        PdfPCell cell = new PdfPCell(new Paragraph(formattedAmount, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        return cell;
    }

    /**
     * Creates a table with alternating row colors for better readability
     */
    public static PdfPTable createTableWithAlternatingColors(PdfPTable table, int columnCount) {
        boolean alternate = false;

        // Skip the first row (header)
        for (int i = columnCount; i < table.getRows().size(); i++) {
            // Apply to all cells in this row
            for (int j = 0; j < columnCount; j++) {
                PdfPCell cell = table.getRow(i).getCells()[j];
                if (cell != null && alternate) {
                    cell.setBackgroundColor(ALTERNATE_ROW_COLOR);
                }
            }
            alternate = !alternate;
        }

        return table;
    }

    /**
     * Adds a horizontal line (separator) to the document
     */
    public static void addSeparator(Document document) throws DocumentException {
        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthBottom(1);
        cell.setBorderColorBottom(BaseColor.LIGHT_GRAY);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setFixedHeight(5);
        separator.addCell(cell);
        document.add(separator);
    }
}