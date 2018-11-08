package ryley.wordsprint2;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.github.mertakdut.BookSection;
import com.github.mertakdut.Reader;
import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BookParser {

  private Context mContext;
  private int currentSection = 0;

  BookParser(Context context)
  {
    mContext=context;
  }

  protected BookSection parseSection (String location) throws IOException, ReadingException, OutOfPagesException {

    File file = getFileFromAssets(location);

    Reader reader = new Reader();
    reader.setIsIncludingTextContent(true);
    reader.setFullContent(file.getPath());
    reader.setIsOmittingTitleTag(true);

    BookSection bookSection = new BookSection();

    try{
      bookSection = reader.readSection(currentSection);
      currentSection++;

    }
    catch (OutOfPagesException e){
      Log.d("Parser","Read Entire Book");
    }

    return bookSection;
  }

  public int getCurrentSection(){
    return currentSection;
  }

  public File getFileFromAssets(String fileName) {

    File file = new File(mContext.getCacheDir() + "/" + fileName);

    if (!file.exists()) try {

      InputStream is = mContext.getAssets().open(fileName);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();

      FileOutputStream fos = new FileOutputStream(file);
      fos.write(buffer);
      fos.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return file;
  }
}
