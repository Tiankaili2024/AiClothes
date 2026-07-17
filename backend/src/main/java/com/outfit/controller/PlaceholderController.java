package com.outfit.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@RestController
public class PlaceholderController {

    @GetMapping("/api/placeholder/outfit")
    public ResponseEntity<byte[]> placeholder() {
        try {
            int w = 512, h = 512;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();

            // Gradient background
            GradientPaint gp = new GradientPaint(0, 0, new Color(230, 240, 255),
                                                 w, h, new Color(200, 220, 250));
            g.setPaint(gp);
            g.fillRect(0, 0, w, h);

            // Fashion icon (simple clothing shape)
            g.setColor(new Color(100, 140, 200));
            g.setStroke(new BasicStroke(3));
            // Draw a simple T-shirt shape
            int[] xs = {w/2 - 60, w/2 - 30, w/2 - 30, w/2 + 30, w/2 + 30, w/2 + 60};
            int[] ys = {h/2 - 80, h/2 - 20, h/2 + 30, h/2 + 30, h/2 - 20, h/2 - 80};
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fillPolygon(xs, ys, 6);

            // Text
            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
            g.setColor(new Color(120, 140, 170));
            String txt = "AI\u7a7f\u642d\u751f\u6210\u4e2d...";
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(txt);
            g.drawString(txt, (w - tw) / 2, h/2 + 100);

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}