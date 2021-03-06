package com.mast.wxpay.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Hashtable;


@RestController
@Log
@RequestMapping("/qrCode")
@Api(value="QrController",description="获取二维码")
public class QrController {

    /**
     * 显示支付二维码
     *
     * @param code     String
     * @param response HttpServletResponse
     * @throws Exception Exception
     */
    @ApiOperation("获取二维码")
    @GetMapping("/getQRCode")
    public void qrCode(@RequestParam("code") String code, HttpServletResponse response)  {
        try {
            String qrCode = new String(Base64.getDecoder().decode(code));
            response.setContentType("image/png");
            Hashtable hints = new Hashtable();
            //内容所使用编码
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            OutputStream stream = response.getOutputStream();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, 300, 300,hints);
            MatrixToImageWriter.writeToStream(bitMatrix, "png", stream);
        } catch (Exception e){
        e.printStackTrace();
        }

    }

}
