package ryley.wordsprint2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReaderView extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reader_view);

    Intent intent = getIntent();
    String bookLoc = intent.getStringExtra("BOOK");
    TextView wordTrack = (TextView) findViewById(R.id.wordTrackView);

    List<String[]> parsedBook = new ArrayList<String[]>();

    BookParser bookparser = new BookParser(this);
    try {
      parsedBook = bookparser.parse("Doesn't matter right now");
    } catch (IOException e) {
      e.printStackTrace();
    }

    String text = "?";


    wordTrack.setText(text);

  }
}
