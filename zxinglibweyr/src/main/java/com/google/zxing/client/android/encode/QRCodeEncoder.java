/*
 * Copyright (C) 2008 ZXing authors
 * Modifications Copyright (C) 2013-2014 Weyr Associates LLC authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.encode;



import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;

import com.google.zxing.WriterException;

import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;

import com.google.zxing.common.BitMatrix;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import android.util.Log;

import java.util.EnumMap;
import java.util.Map;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

  private static final String TAG = QRCodeEncoder.class.getSimpleName();

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;

  private final Activity activity;
  private String contents;
  private String label;
  private String date;
  private String sheepName;
  private String displayContents;
  private String title;
  private BarcodeFormat format;
  private final int dimension;


  QRCodeEncoder(Activity activity, Intent intent, int dimension) throws WriterException {
    this.activity = activity;
    this.dimension = dimension;

    String action = intent.getAction();
    if (action.equals(Intents.Encode.ACTION)) {
      encodeContentsFromZXingIntent(intent);
      encodeLabelFromZXingIntent(intent);
      encodeDateFromZXingIntent(intent);
      encodeSheepNameFromZXingIntent(intent);
    } else if (action.equals(Intent.ACTION_SEND)) {
      Log.i(TAG, "Action_Send");

    }
  }

  String getContents() {
    return contents;
  }

  String getDisplayContents() {
    return displayContents;
  }

  String getTitle() {
    return title;
  }

  String getLabel() {
	  return label;

  }
  
  String getDate() {
	  return date;

  }


  private boolean encodeSheepNameFromZXingIntent(Intent intent) {
	  
      String data = intent.getStringExtra(Intents.Encode.SHEEPNAME);
      
      if (data != null && data.length() > 0) {
    	  sheepName = data;  
      }
      
      return sheepName != null && date.length() > 0;
  }
  
  private boolean encodeDateFromZXingIntent(Intent intent) {
	  
      String data = intent.getStringExtra(Intents.Encode.DATE);
      if (data != null && data.length() > 0) {
    	  date = data;
	  
      }
      return date != null && date.length() > 0;
  }
  
  private boolean encodeLabelFromZXingIntent(Intent intent) {
	  
      String data = intent.getStringExtra(Intents.Encode.DATA1);
      if (data != null && data.length() > 0) {
    	  label = data;
	  
      }
      return label != null && label.length() > 0;
  }
   
  // It would be nice if the string encoding lived in the core ZXing library,
  // but we use platform specific code like PhoneNumberUtils, so it can't.
  private boolean encodeContentsFromZXingIntent(Intent intent) {
     // Default to QR_CODE if no format given.
    String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
    format = null;
    if (formatString != null) {
      try {
        format = BarcodeFormat.valueOf(formatString);
      } catch (IllegalArgumentException iae) {
        // Ignore it then
      }
    }
    if (format == null || format == BarcodeFormat.QR_CODE) {
      String type = intent.getStringExtra(Intents.Encode.TYPE);
      if (type == null || type.length() == 0) {
        return false;
      }
      this.format = BarcodeFormat.QR_CODE;

    } else {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    }
    return contents != null && contents.length() > 0;
  }


//  Customized for LanbTracker & AnimalTrakker and specific label printers
  Bitmap encodeAsBitmap() throws WriterException {
//	  Log.e("QRCodeEncoder", "at bitmapencode");
    String contentsToEncode = contents ;
    if (contentsToEncode == null) {
      return null;
    }
    Map<EncodeHintType,Object> hints = null;
    String encoding = guessAppropriateEncoding(contentsToEncode);
    if (encoding != null) {
      hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
      hints.put(EncodeHintType.CHARACTER_SET, encoding);
    }
    MultiFormatWriter writer = new MultiFormatWriter();
    BitMatrix result;
    try {
//      result = writer.encode(contentsToEncode, format, dimension, dimension/4, hints);
//      result = writer.encode(contentsToEncode, format, 696, 271, hints); // brother QL-710
//      result = writer.encode(contentsToEncode, format, 318, 240, hints);

//  build a barcode to fit in the smaller than full size label
//  Phomemo M110 with 40mm x 30mm label, full size is 50mm wide
//      Log.i("QRCodeEncoder", contentsToEncode + " " + hints);

      result = writer.encode(contentsToEncode, format, 318, 240, hints);
    } catch (IllegalArgumentException iae) {
      // Unsupported format
      return null;
    }
       
    int width = result.getWidth();
    int height = result.getHeight();
 
//	  Log.e("kenm", "dimension="+ dimension);
//	  Log.e("kenm", "width="+ width + "height= "+ height);
  
    int[] pixels = new int[width * height];   

    for (int y = 0; y < height; y++) {
        int offset = y * width;
        for (int x = 0; x < width; x++) {
          pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
        }
      }   
    
    Bitmap bitmap = Bitmap.createBitmap(384, height, Bitmap.Config.ARGB_8888);

//  clear the whole bitmap before putting the barcode in
      bitmap.eraseColor(WHITE);

//  clear out the top and bottom of the barcode
    for (int y = height - 100; y < height; y++) {
        int offset = y * width;
        for (int x = 0; x < width; x++) {
          pixels[offset + x] =  WHITE;
        }
      } 

    for (int y = 0; y < 100; y++) {
        int offset = y * width;
        for (int x = 0; x < width; x++) {
          pixels[offset + x] =  WHITE;
        }
      } 

//  copy the barcode into the larger bitmap, offset so it fits into the smaller label
    bitmap.setPixels(pixels, 0, width, 66, 0, width, height);

//  Fixme don't hardcode these and fix all the move to's to be label specific
//  redefine the width to be the whole label size and create a canvas to draw on
    width = 384;
    Canvas canvas = new Canvas(bitmap);

//  draw the aux data onto the label canvas
    Paint paint = new Paint();
    paint.setColor(Color.BLACK);
    paint.setTypeface(Typeface.DEFAULT);

//  draw the date at the top of the label
    paint.setTextSize(20);
    Path p0 = new Path();
    p0.moveTo(width - 310,  40);
    p0.lineTo(width,  40);

    canvas.drawTextOnPath(date, p0, 0, 0, paint);

//  draw the sheepname which will be the Trich info in BullSoundness and other data in AnimalTrakker
//  for quotes around the name you will need to add that in the calling app's code
    if (sheepName != null) {
      paint.setTextSize(20);
      Path p1 = new Path();
      p1.moveTo(width - 310, 80);
      p1.lineTo(width, 80);

      canvas.drawTextOnPath(sheepName, p1, 0, 0, paint);
    }

//  draw the numeric EID under the barcode
    paint.setTextSize(18);
    Path p2 = new Path();
    p2.moveTo(width - 260, height - 75);
    p2.lineTo(width, height - 75);

    canvas.drawTextOnPath(contents, p2, 0, 0, paint);

//  draw the user supplied text
    paint.setTextSize(25);
    Path p3 = new Path();
    p3.moveTo(width - 310, height - 30);
    p3.lineTo(width, height - 30);

    canvas.drawTextOnPath(label, p3, 0, 0, paint);

// add other info to draw on the label as required here

//	  Log.e("QRCodeEncoder", "done bitmapencode");
    return bitmap;
  }

  private static String guessAppropriateEncoding(CharSequence contents) {
    // Very crude at the moment
    for (int i = 0; i < contents.length(); i++) {
      if (contents.charAt(i) > 0xFF) {
        return "UTF-8";
      }
    }
    return null;
  }

}
