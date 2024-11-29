package com.example.aos.sistema_aos_spring_boot.Servicios.impl;

import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.QRCode;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeValidationResult;
import com.example.aos.sistema_aos_spring_boot.Repositorios.EventoRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.QRCodeRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.RegistroRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class QRCodeServiceImpl implements QRCodeService {

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    private static final String FORMAT = "PNG";

    /**
     * Genera una imagen en formato Base64 de un código QR basado en el texto proporcionado.
     *
     * @param text El texto que se convertirá en un código QR.
     * @return Una cadena de texto en formato Base64 que representa la imagen del código QR.
     * @throws WriterException Si ocurre un error al escribir el código QR.
     * @throws IOException     Si ocurre un error de entrada/salida.
     */
    @Override
    public CompletableFuture<String> generateQRCodeImage(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, FORMAT, baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (WriterException | IOException e) {
                throw new RuntimeException("Error generando el código QR", e);
            }
        });
    }

    @Override
    @Transactional
    public QRCode agregarQRCode(QRCode qrCode) {
        return qrCodeRepository.save(qrCode);
    }

    @Override
    public QRCode getQRCodeById(Long id) {
        Optional<QRCode> optionalQRCode = qrCodeRepository.findById(id);
        return optionalQRCode.orElse(null);
    }

    @Override
    @Transactional
    public QRCodeValidationResult validateQRCode(String ValorQR) {
        Optional<QRCode> qrCodeOpt = qrCodeRepository.findByValorQR(ValorQR);
        if (qrCodeOpt.isPresent()) {
            QRCode qrCode = qrCodeOpt.get();
            if (!qrCode.isUsed()) {
                qrCode.setUsed(true);
                try {
                    qrCodeRepository.save(qrCode);
                    return new QRCodeValidationResult(true, "QR Code valid and used.");
                } catch (OptimisticLockException e) {
                    return new QRCodeValidationResult(false, "El código QR ha sido validado por otro usuario. Intente nuevamente.");
                }
            } else {
                return new QRCodeValidationResult(false, "QR Code already used.");
            }
        }
        return new QRCodeValidationResult(false, "QR Code invalid.");
    }



    @Override
    public int countQRCodesForEvent(Long eventoId) {
        return registroRepository.countByEvento_EventoId(eventoId);
    }

    @Override
    public Evento getEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }


    @Override
    public boolean isQRCodeUsed(String valorQR) {
        Optional<QRCode> qrCode = qrCodeRepository.findByValorQR(valorQR);
        return qrCode.isPresent() && qrCode.get().isUsed();
    }
}