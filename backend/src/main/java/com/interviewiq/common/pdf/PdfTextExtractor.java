package com.interviewiq.common.pdf;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfTextExtractor {
    public String extract(byte[] content) throws IOException {
        try (PDDocument document = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(document).trim();
        }
    }
}

