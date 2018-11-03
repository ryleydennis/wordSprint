package ryley.wordsprint2;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BookParser {

  private Context mContext;

  BookParser(Context context)
  {

    mContext=context;
  }
  protected List<String[]> parse (String location) throws IOException {

    List<String[]> unformattedText = new ArrayList<>();

    AssetManager am = mContext.getAssets();

    try {

      InputStream is = mContext.getAssets().open("PrideAndPrejudiceChapter7.html");
      BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
      String line;

      int loopTest1 = 0;


      while ((line = reader.readLine()) != null)
      {


        boolean isValidLine = false;

        if(line.indexOf("title") != -1) isValidLine = true;
        //if(line.indexOf("div") != -1) isValidLine = true;
        if(line.indexOf("span") != -1) isValidLine = true;

        if(isValidLine)
        {
          while(line.indexOf(">") != -1) {

            int startLoc = line.indexOf(">");
            int endLoc = line.lastIndexOf("<");

            if(endLoc > startLoc+1)
            {
              line = line.substring(startLoc+1, endLoc);

            }
            else{
              line = "";
            }
          }
          if(!(line.isEmpty())){
            unformattedText.add(line.split(" "));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return unformattedText;
  }

}
