package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.QRCode;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeValidationResult;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface QRCodeService {
    /**
     * Genera una imagen en formato Base64 de un código QR basado en el texto proporcionado.
     *
     * @param text El texto que se convertirá en un código QR.
     * @return Un CompletableFuture que contendrá una cadena de texto en formato Base64 que representa la imagen del código QR.
     * @throws WriterException Si ocurre un error al escribir el código QR.
     * @throws IOException     Si ocurre un error de entrada/salida.
     */
    CompletableFuture<String> generateQRCodeImage(String text) throws WriterException, IOException;

    QRCode agregarQRCode(QRCode QRCode);

    QRCodeValidationResult validateQRCode(String ValorQR);

    int countQRCodesForEvent(Long eventoId);

    QRCode getQRCodeById(Long id);

    Evento getEvento(Long eventoId);

    boolean isQRCodeUsed(String valorQR);
}
