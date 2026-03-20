package ma.safar.morocco.invoice.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import ma.safar.morocco.invoice.dto.InvoiceDTO;
import ma.safar.morocco.invoice.entity.Invoice;
import ma.safar.morocco.invoice.enums.InvoiceStatus;
import ma.safar.morocco.invoice.repository.InvoiceRepository;
import ma.safar.morocco.itinerary.entity.Itineraire;
import ma.safar.morocco.itinerary.repository.ItineraireRepository;
import ma.safar.morocco.reservation.entity.OfferReservation;
import ma.safar.morocco.reservation.repository.OfferReservationRepository;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ItineraireRepository itineraryRepository;
    private final UtilisateurRepository userRepository;
    private final OfferReservationRepository offerReservationRepository;


    @Override
    @Transactional
    public InvoiceDTO generateInvoice(Long itineraryId, Long userId, String lang) {
        Itineraire itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));
        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OfferReservation> approvedReservations = offerReservationRepository.findByItineraryId(itineraryId).stream()
                .filter(res -> res.getStatus() == ma.safar.morocco.reservation.enums.ReservationStatus.APPROVED)
                .collect(Collectors.toList());

        if (approvedReservations.isEmpty()) {
            throw new RuntimeException("No approved reservations found for this itinerary. Cannot generate invoice.");
        }

        Double grandTotal = 0.0;
        for (OfferReservation res : approvedReservations) {
            if (res.getTotalPrice() != null) {
                grandTotal += res.getTotalPrice();
            }
        }

        Invoice invoice = Invoice.builder()
                .itinerary(itinerary)
                .user(user)
                .totalAmount(grandTotal)
                .generatedDate(LocalDateTime.now())
                .status(InvoiceStatus.UNPAID)
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        // Generate PDF
        String pdfPath = generatePdfSafely(saved, itinerary, approvedReservations, user, lang);
        saved.setPdfPath(pdfPath);

        saved = invoiceRepository.save(saved);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public InvoiceDTO generateInvoiceForReservation(Long reservationId, String lang) {
        OfferReservation reservation = offerReservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        Itineraire itinerary = reservation.getItinerary();
        Utilisateur user = reservation.getUser();

        if (reservation.getStatus() != ma.safar.morocco.reservation.enums.ReservationStatus.APPROVED && 
            reservation.getStatus() != ma.safar.morocco.reservation.enums.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Reservation must be APPROVED or CONFIRMED to generate invoice.");
        }

        Double grandTotal = reservation.getTotalPrice() != null ? reservation.getTotalPrice() : 0.0;

        Invoice invoice = Invoice.builder()
                .itinerary(itinerary)
                .user(user)
                .totalAmount(grandTotal)
                .generatedDate(LocalDateTime.now())
                .status(InvoiceStatus.UNPAID)
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        // Generate PDF
        String pdfPath = generatePdfSafely(saved, itinerary, List.of(reservation), user, lang);
        saved.setPdfPath(pdfPath);

        saved = invoiceRepository.save(saved);
        return mapToDTO(saved);
    }

    private String generatePdfSafely(Invoice invoice, Itineraire itinerary, List<OfferReservation> reservations,
            Utilisateur user, String lang) {
        try {
            String dirPath = "uploads/invoices/";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "Invoice_" + invoice.getId() + ".pdf";
            String fullPath = dirPath + fileName;

            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            // Size and margins
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // Fonts
            PdfFont fontHelvetica = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontHelveticaBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Brand Colors
            DeviceRgb brandColor = new DeviceRgb(249, 115, 22); // Orange used often in UI
            DeviceRgb borderColor = new DeviceRgb(220, 220, 220); // Light Gray
            DeviceRgb footerColor = new DeviceRgb(128, 128, 128); // Gray

            // HEADER: Logo (Left) and Title (Right)
            float[] headerWidths = { 1, 1 };
            Table headerTable = new Table(UnitValue.createPercentArray(headerWidths)).useAllAvailableWidth();

            // Logo Cell
            Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
            try {
                // Load from classpath to ensure it works when packaged
                ClassPathResource resource = new ClassPathResource("static/logo.png");
                try (InputStream is = resource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    ImageData imageData = ImageDataFactory.create(bytes);
                    Image logo = new Image(imageData);
                    logo.setHeight(50);
                    logoCell.add(logo);
                }
            } catch (Exception e) {
                // Fallback if no logo found
                logoCell.add(new Paragraph("SAFAR MOROCCO").setFont(fontHelveticaBold).setFontColor(brandColor)
                        .setFontSize(16));
            }
            headerTable.addCell(logoCell);

            // Title Cell
            Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            titleCell.add(new Paragraph("SAFAR MOROCCO").setFont(fontHelveticaBold).setFontSize(18));
            titleCell.add(new Paragraph("Travel & Experiences").setFont(fontHelvetica).setFontSize(11)
                    .setFontColor(footerColor));
            headerTable.addCell(titleCell);

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // Separator Line
            SolidLine line = new SolidLine(1f);
            line.setColor(brandColor);
            LineSeparator ls = new LineSeparator(line);
            document.add(ls);
            document.add(new Paragraph("\n"));

            // INVOICE DETAILS
            float[] infoWidths = { 1, 1 };
            Table infoTable = new Table(UnitValue.createPercentArray(infoWidths)).useAllAvailableWidth();

            Cell leftInfoCell = new Cell().setBorder(Border.NO_BORDER);
            leftInfoCell
                    .add(new Paragraph(getTranslatedText("INVOICE", lang)).setFont(fontHelveticaBold).setFontSize(22).setFontColor(brandColor));
            leftInfoCell
                    .add(new Paragraph(getTranslatedText("Invoice No", lang) + " INV-" + invoice.getId()).setFont(fontHelvetica).setFontSize(11));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            leftInfoCell.add(new Paragraph(getTranslatedText("Date", lang) + " " + invoice.getGeneratedDate().format(formatter))
                    .setFont(fontHelvetica).setFontSize(11));
            infoTable.addCell(leftInfoCell);

            Cell rightInfoCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            rightInfoCell.add(new Paragraph(getTranslatedText("Billed To", lang)).setFont(fontHelveticaBold).setFontSize(11));
            rightInfoCell.add(new Paragraph(user.getNom()).setFont(fontHelvetica).setFontSize(11));
            rightInfoCell.add(new Paragraph(user.getEmail()).setFont(fontHelvetica).setFontSize(11));
            rightInfoCell.add(new Paragraph(getTranslatedText("Itinerary", lang) + " " + itinerary.getNom()).setFont(fontHelvetica).setFontSize(11));
            infoTable.addCell(rightInfoCell);

            document.add(infoTable);
            document.add(new Paragraph("\n\n"));

            // ITEMS TABLE
            float[] columnWidths = { 3, 2, 3, 1, 2, 2 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            // Table Header
            String[] headers = { getTranslatedText("Name", lang), getTranslatedText("Type", lang), getTranslatedText("Dates", lang), getTranslatedText("Qty", lang), getTranslatedText("Unit Price", lang), getTranslatedText("Total", lang) };
            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setFont(fontHelveticaBold).setFontColor(ColorConstants.WHITE));
                cell.setBackgroundColor(brandColor);
                cell.setPadding(6);
                cell.setBorder(new SolidBorder(borderColor, 1));
                if (header.equals("Qty") || header.equals("Unit Price") || header.equals("Total")) {
                    cell.setTextAlignment(TextAlignment.RIGHT);
                } else {
                    cell.setTextAlignment(TextAlignment.LEFT);
                }
                table.addHeaderCell(cell);
            }

            // Table Body
            for (OfferReservation res : reservations) {
                ma.safar.morocco.offer.entity.Offer offer = res.getOffer();
                
                // Enhanced Name/Description with category details
                String nameWithDetails = offer.getName();
                if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.HOTEL) {
                    nameWithDetails += "\nRoom: " + (offer.getRoomType() != null ? offer.getRoomType() : "Standard") + 
                                       (offer.getStars() != null ? " (" + offer.getStars() + "* Stars)" : "");
                } else if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.RESTAURANT) {
                    nameWithDetails += "\nCuisine: " + (offer.getCuisineType() != null ? offer.getCuisineType() : "Various");
                } else if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.ACTIVITY) {
                    nameWithDetails += "\nDuration: " + (offer.getDuration() != null ? offer.getDuration() : "N/A") + 
                                       " | " + (offer.getActivityType() != null ? offer.getActivityType() : "");
                }

                table.addCell(createCell(nameWithDetails, fontHelvetica, borderColor, TextAlignment.LEFT));
                table.addCell(createCell(offer.getType().toString(), fontHelvetica, borderColor, TextAlignment.LEFT));

                String dates = "";
                if (res.getStartDate() != null && res.getEndDate() != null) {
                    dates = res.getStartDate().toString() + " to " + res.getEndDate().toString();
                } else if (res.getStartDate() != null) {
                    dates = res.getStartDate().toString();
                } else {
                    dates = "N/A";
                }
                
                table.addCell(createCell(dates, fontHelvetica, borderColor, TextAlignment.LEFT));
                table.addCell(createCell(String.valueOf(res.getQuantity()), fontHelvetica, borderColor, TextAlignment.RIGHT));

                Double unitPrice = offer.getPrice() != null ? offer.getPrice()
                        : (offer.getAveragePrice() != null ? offer.getAveragePrice()
                                : offer.getPricePerNight());

                String priceStr = unitPrice != null ? formatCurrency(unitPrice) : "0.00 MAD";
                table.addCell(createCell(priceStr, fontHelvetica, borderColor, TextAlignment.RIGHT));

                String totalStr = res.getTotalPrice() != null ? formatCurrency(res.getTotalPrice()) : "0.00 MAD";
                table.addCell(createCell(totalStr, fontHelvetica, borderColor, TextAlignment.RIGHT));
            }
            document.add(table);
            document.add(new Paragraph("\n"));

            // GRAND TOTAL
            Table totalTable = new Table(1).setHorizontalAlignment(HorizontalAlignment.RIGHT);
            Cell totalCell = new Cell().setBorder(Border.NO_BORDER).setBackgroundColor(new DeviceRgb(240, 240, 240));
            totalCell.setPadding(10);
            totalCell.add(new Paragraph(getTranslatedText("GRAND TOTAL", lang) + " " + formatCurrency(invoice.getTotalAmount()))
                    .setFont(fontHelveticaBold).setFontSize(14).setTextAlignment(TextAlignment.RIGHT));
            totalTable.addCell(totalCell);
            document.add(totalTable);

            // FOOTER (Positioned near the bottom)
            Paragraph footer = new Paragraph(getTranslatedText("Thank you", lang))
                    .setFont(fontHelvetica)
                    .setFontSize(9)
                    .setFontColor(footerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedPosition(40, 30, document.getPdfDocument().getDefaultPageSize().getWidth() - 80);
            document.add(footer);

            document.close();
            return fullPath;
        } catch (Exception e) {
            System.err.println("Failed to generate PDF: " + e.getMessage());
            e.printStackTrace();
            return null; // Don't block flow
        }
    }

    private Cell createCell(String content, PdfFont font, DeviceRgb borderColor, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content).setFont(font));
        cell.setPadding(5);
        cell.setBorder(new SolidBorder(borderColor, 1));
        cell.setTextAlignment(alignment);
        return cell;
    }

    private String formatCurrency(Double amount) {
        return String.format(Locale.US, "%.2f MAD", amount);
    }

    private String getTranslatedText(String key, String lang) {
        if ("en".equalsIgnoreCase(lang) || "gb".equalsIgnoreCase(lang)) {
            switch (key) {
                case "INVOICE": return "INVOICE";
                case "Invoice No": return "Invoice No:";
                case "Date": return "Date:";
                case "Billed To": return "Billed To:";
                case "Itinerary": return "Itinerary:";
                case "Name": return "Name";
                case "Type": return "Type";
                case "Dates": return "Dates";
                case "Qty": return "Qty";
                case "Unit Price": return "Unit Price";
                case "Total": return "Total";
                case "GRAND TOTAL": return "GRAND TOTAL:";
                case "Thank you": return "Thank you for choosing Safar Morocco!";
            }
        } else if ("es".equalsIgnoreCase(lang)) {
            switch (key) {
                case "INVOICE": return "FACTURA";
                case "Invoice No": return "Nº Factura:";
                case "Date": return "Fecha:";
                case "Billed To": return "Facturado a:";
                case "Itinerary": return "Itinerario:";
                case "Name": return "Nombre";
                case "Type": return "Tipo";
                case "Dates": return "Fechas";
                case "Qty": return "Cant.";
                case "Unit Price": return "Precio Unit.";
                case "Total": return "Total";
                case "GRAND TOTAL": return "TOTAL GENERAL:";
                case "Thank you": return "¡Gracias por elegir Safar Morocco!";
            }
        } else if ("ar".equalsIgnoreCase(lang) || "ma".equalsIgnoreCase(lang)) {
            // Note: iText7's standard Helvetica font does not support Arabic shaping natively. 
            // We fallback to French translation to avoid generating a blank PDF.
            switch (key) {
                case "INVOICE": return "FACTURE";
                case "Invoice No": return "N° Facture :";
                case "Date": return "Date :";
                case "Billed To": return "Facturé à :";
                case "Itinerary": return "Itinéraire :";
                case "Name": return "Nom";
                case "Type": return "Type";
                case "Dates": return "Dates";
                case "Qty": return "Qté";
                case "Unit Price": return "Prix Unit.";
                case "Total": return "Total";
                case "GRAND TOTAL": return "TOTAL GÉNÉRAL :";
                case "Thank you": return "Merci d'avoir choisi Safar Morocco !";
            }
        }
        // Default to French
        switch (key) {
            case "INVOICE": return "FACTURE";
            case "Invoice No": return "N° Facture :";
            case "Date": return "Date :";
            case "Billed To": return "Facturé à :";
            case "Itinerary": return "Itinéraire :";
            case "Name": return "Nom";
            case "Type": return "Type";
            case "Dates": return "Dates";
            case "Qty": return "Qté";
            case "Unit Price": return "Prix Unit.";
            case "Total": return "Total";
            case "GRAND TOTAL": return "TOTAL GÉNÉRAL :";
            case "Thank you": return "Merci d'avoir choisi Safar Morocco !";
        }
        return key;
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return mapToDTO(invoice);
    }

    @Override
    public List<InvoiceDTO> getInvoicesByUser(Long userId) {
        return invoiceRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDTO> getInvoicesByItinerary(Long itineraryId) {
        return invoiceRepository.findByItineraryId(itineraryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .userId(invoice.getUser().getId())
                .itineraryId(invoice.getItinerary().getId())
                .totalAmount(invoice.getTotalAmount())
                .generatedDate(invoice.getGeneratedDate())
                .status(invoice.getStatus())
                .pdfPath(invoice.getPdfPath())
                .build();
    }
}
