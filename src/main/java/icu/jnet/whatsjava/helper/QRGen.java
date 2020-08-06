package icu.jnet.whatsjava.helper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRGen {
	
	public static BufferedImage generateQRcode(String clientId, String serverId, byte[] publicKey) {
		String base64PubKey = Base64.getEncoder().encodeToString(publicKey);
		
		// Combine serverId + base64 public key + clientId and encode it to a QR code
		String qrCodeString = serverId + "," + base64PubKey + "," + clientId;
		
		return createAsBufferedImage(qrCodeString);
	}
	
	private static String createAsBase64(String input) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(encodeToBufferedImage(input), "jpg", baos);
			byte[] byteArray = baos.toByteArray();
			return Base64.getEncoder().encodeToString(byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static BufferedImage createAsBufferedImage(String input) {
		return encodeToBufferedImage(input);
	}
	
	private static BufferedImage encodeToBufferedImage(String input) {
		int size = 500;
		
		try {
			Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			
			// Now with zxing version 3.2.1 you could change border size (white border size to just 1)
			hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
 
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(input, BarcodeFormat.QR_CODE, size,
					size, hintMap);
			int qrWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(qrWidth, qrWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
 
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, qrWidth, qrWidth);
			graphics.setColor(Color.BLACK);
 
			for (int i = 0; i < qrWidth; i++) {
				for (int j = 0; j < qrWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			
			return image;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
}
