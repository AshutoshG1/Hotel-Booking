package com.airbnb.service;

import com.airbnb.dto.BookingDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class PDFService {

    private static final String PDF_DIRECTORY = "/path/to/your/pdf/directory/";

    public boolean generatePDF(String filename, BookingDto bookingDto){
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));

            document.open();
            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            //Chunk chunk = new Chunk("Hello World", font);
            Paragraph paragraph = new Paragraph("Booking Confirmation", font);
            Paragraph guestName = new Paragraph("Guest Name: "+bookingDto.getGuestName(), font);
            Paragraph nightPrice = new Paragraph("Price Per Night: "+bookingDto.getPrice(), font);
            Paragraph totalPrice = new Paragraph("Total Price: "+bookingDto.getTotalPrice(), font);

            document.add(paragraph);
            document.add(guestName);
            document.add(nightPrice);
            document.add(totalPrice);
            document.close();

            return true;

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
