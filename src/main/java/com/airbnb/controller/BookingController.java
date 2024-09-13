package com.airbnb.controller;

import com.airbnb.dto.BookingDto;
import com.airbnb.entity.Booking;
import com.airbnb.entity.Property;
import com.airbnb.entity.PropertyUser;
import com.airbnb.repository.BookingRepository;
import com.airbnb.repository.PropertyRepository;
import com.airbnb.service.BucketService;
import com.airbnb.service.PDFService;

import com.airbnb.service.SendGridEmailSender;
import com.airbnb.service.TwilioSmsSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private BookingRepository bookingRepository;
    private PropertyRepository propertyRepository;
    private PDFService pdfService;
    private BucketService bucketService;
    private TwilioSmsSender twilioSmsSender;
    private SendGridEmailSender sendGridEmailSender;

    public BookingController(BookingRepository bookingRepository, PropertyRepository propertyRepository, PDFService pdfService, BucketService bucketService, TwilioSmsSender twilioSmsSender, SendGridEmailSender sendGridEmailSender) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.pdfService = pdfService;
        this.bucketService = bucketService;
        this.twilioSmsSender = twilioSmsSender;
        this.sendGridEmailSender = sendGridEmailSender;
    }

    @PostMapping("/createBooking/{propertyId}")
    public ResponseEntity<?> createBooking(
            @RequestBody Booking booking,
            @AuthenticationPrincipal PropertyUser user,
            @PathVariable long propertyId
            ) throws IOException {
        booking.setPropertyUser(user);
        Property property = propertyRepository.findById(propertyId).get();
        int propertyPrice = property.getNightlyPrice();
        int totalNights = booking.getTotalNights();
        int totalPrice = totalNights*propertyPrice;
        booking.setProperty(property);
        booking.setTotalPrice(totalPrice);
        Booking createdBooking = bookingRepository.save(booking);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setBookingId(createdBooking.getId());
        bookingDto.setGuestName(createdBooking.getGuestName());
        bookingDto.setPrice(propertyPrice);
        bookingDto.setTotalPrice(totalPrice);

        //create pdf with booking confirmation
        boolean b = pdfService.generatePDF("F://air_bnb_reservation//" + "booking-confirmation-id" + createdBooking.getId() + ".pdf", bookingDto);
        if(b){
            //upload pdf file into bucket
            MultipartFile file = convertToMultipartFile("F://air_bnb_reservation//" + "booking-confirmation-id" + createdBooking.getId() + ".pdf");
            String uploadedFileURL = bucketService.uploadFile(file, "myairbnb12274");
           // System.out.println(uploadedFileURL);
            twilioSmsSender.sendSms("+918960972274","this is the message from java backend developer +"+uploadedFileURL);
            //sendGridEmailSender.sendEmail("ashutoshgautamspn@gmail.com", "smstesting","email testing");
        }else {
            return new ResponseEntity<>("somthing went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);

    }

    public static MultipartFile convertToMultipartFile(String filePath) throws IOException {
        File file = new File(filePath);
        String fileName = file.getName();
        String contentType = "application/pdf";
        byte[] fileContent = readBytesFromFile(file);

        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return fileContent.length == 0;
            }

            @Override
            public long getSize() {
                return fileContent.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return fileContent;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(fileContent);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                    outputStream.write(fileContent);
                }
            }
        };
    }

    private static byte[] readBytesFromFile(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return StreamUtils.copyToByteArray(inputStream);
        }
    }


}
